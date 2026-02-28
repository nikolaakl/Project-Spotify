package server;

import server.command.response.CommandResponse;
import server.exception.checked.DataLoadException;
import server.exception.checked.DataSaveException;
import server.exception.checked.NetworkException;
import server.util.GsonSingleton;
import server.util.Logger;
import server.command.impl.StreamCommand;
import server.model.stats.Stats;
import server.model.user.Users;
import server.command.CommandExecutor;
import server.repository.SongRepository;
import server.repository.StatsRepository;
import server.repository.UsersRepository;
import server.session.ClientSession;
import server.validation.Validator;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpotifyServer {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 7777;
    private static final int BUFFER_SIZE = 8192;

    private static final Path SONGS_DB = Path.of("dataset" + File.separator + "songs");
    private static final String INPUT_ERROR = "Error when reading user input!";
    private static final String SHUTDOWN_SERVER_RESPONSE = "Shutting down the server...";
    private static final String RECEIVED_MESSAGE = "Received from ";
    private static final String DISCONNECTED_MESSAGE = "Client disconnected: ";
    private static final String UNKNOWN_ADDRESS_MESSAGE = "unknown-address";
    private static final String STREAMING_FAILED_MESSAGE = "Streaming failed for %s, client %s ";
    private static final String COMMAND_HANDLED_ERROR_MESSAGE = "Command was not handled correctly. %s";
    private static final String SOCKET_CHANNEL_CLOSING_ERROR_MESSAGE =
            "Socket channel closing was not successful. %s";
    private static final String RUN_STREAMING_ERROR_MESSAGE =
            "Run streaming was not successful due to network issue. %s";
    private static final String NETWORK_ERROR_MESSAGE =
            "Network error occurred. Server was not started correctly. %s";
    private static final String CLIENT_LABEL_MESSAGE = "%s : %s";
    private static final String GUEST_MESSAGE = "guest";
    private static final String EMPTY_STRING = "";
    private static final String SPLIT_REGEX_SEPARATOR = "\\s+";

    private boolean isRunning;
    private Selector selector;

    private final CommandExecutor cmdExecutor;
    private final StreamCommand streamCommand;
    private final ExecutorService workers =
            Executors.newVirtualThreadPerTaskExecutor();

    public SpotifyServer() {
        Users users;
        Stats stats;

        try {
            users = UsersRepository.loadUsers();
        } catch (DataLoadException e) {
            Logger.log(e.getMessage(), e);
            users = new Users(new HashSet<>());
        }

        try {
            stats = StatsRepository.loadStats();
        } catch (DataLoadException e) {
            Logger.log(e.getMessage(), e);
            stats = new Stats();
        }

        SongRepository songs = new SongRepository(SONGS_DB.toString());
        try {
            songs.loadSongs();
        } catch (DataLoadException e) {
            Logger.log(e.getMessage(), e);
        }

        this.cmdExecutor = new CommandExecutor(users, stats, songs);
        this.streamCommand = new StreamCommand(cmdExecutor.getSongRepository());
        this.isRunning = true;
    }

    public void stop() {
        isRunning = false;
        if (selector != null && selector.isOpen()) {
            selector.wakeup();
        }

        this.workers.shutdown();

        try {
            UsersRepository.saveUsers(this.cmdExecutor.getUsers());
            StatsRepository.saveStats(this.cmdExecutor.getStats());
        } catch (DataSaveException e) {
            Logger.log(e.getMessage(), e);
        }
    }

    public void startServer() throws NetworkException {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
             Selector selector = Selector.open()) {
            this.selector = selector;
            configureServerSocketChannel(serverSocketChannel, selector);

            this.isRunning = true;

            while (this.isRunning) {
                handleRequests(this.selector);
            }
        } catch (IOException e) {
            throw new NetworkException(NETWORK_ERROR_MESSAGE.formatted(e.getMessage()), e);
        }
    }

    private void handleRequests(Selector selector) throws IOException {
        int readyChannels = selector.select();
        if (readyChannels == 0) {
            return;
        }

        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();

            if (!key.isValid()) {
                continue;
            }

            if (key.isAcceptable()) {
                accept(selector, key);
            } else if (key.isReadable()) {
                SocketChannel sc = (SocketChannel) key.channel();
                key.interestOps(0);

                this.workers.submit(() -> handleKey(sc, key));
            }

            keyIterator.remove();
        }
    }

    private void handleKey(SocketChannel socketChannel, SelectionKey key) {
        try {
            ClientSession session = (ClientSession) key.attachment();
            ByteBuffer buffer = session.getBuffer();
            String clientInput = getClientInput(socketChannel, key, buffer);
            if (Validator.isNullObject(clientInput)) {
                return;
            }

            System.out.println(RECEIVED_MESSAGE + getClientLabel(socketChannel, session) + " -> " + clientInput);
            CommandResponse<?> response = this.cmdExecutor.executeCommand(clientInput, session);

            if (response.getMessage().equals(StreamCommand.STREAM_TAKEOVER)) {
                key.cancel();

                startStreamingThread(socketChannel, clientInput);
                return;
            }
            String jsonResponse = GsonSingleton.getInstance().toJson(response);
            sendResponseToClient(socketChannel, jsonResponse, buffer);
        } catch (IOException e) {
            Logger.log(COMMAND_HANDLED_ERROR_MESSAGE.formatted(e.getMessage()), e);
        } finally {
            if (key.isValid()) {
                key.interestOps(SelectionKey.OP_READ);
                key.selector().wakeup();
            }
        }
    }

    private String getClientLabel(SocketChannel socketChannel, ClientSession session) {
        String address;
        try {
            address = socketChannel.getRemoteAddress().toString();
        } catch (IOException e) {
            address = UNKNOWN_ADDRESS_MESSAGE;
        }

        String user = (session != null && session.isLoggedIn())
                ? session.getLoggedUser().getEmail() : GUEST_MESSAGE;

        return CLIENT_LABEL_MESSAGE.formatted(user, address);
    }

    private void startStreamingThread(SocketChannel socketChannel, String clientInput) {
        String[] parts = clientInput.split(SPLIT_REGEX_SEPARATOR);
        String songId = parts.length >= 2 ? parts[1].strip() : EMPTY_STRING;

        workers.submit(() -> runStreaming(socketChannel, songId));
    }

    private void runStreaming(SocketChannel socketChannel, String songId) {
        try {
            socketChannel.configureBlocking(true);
            this.streamCommand.stream(socketChannel, songId);
        } catch (IOException e) {
            Logger.log(RUN_STREAMING_ERROR_MESSAGE.formatted(e.getMessage()), e);
        } catch (UnsupportedAudioFileException e) {
            Logger.log(STREAMING_FAILED_MESSAGE.formatted(songId, socketChannel), e);
        } finally {
            try {
                socketChannel.close();
            } catch (IOException e) {
                Logger.log(SOCKET_CHANNEL_CLOSING_ERROR_MESSAGE.formatted(e.getMessage()), e);
            }
        }
    }

    private String getClientInput(SocketChannel clientChanel, SelectionKey key, ByteBuffer buffer) throws IOException {
        buffer.clear();
        int readBytes = clientChanel.read(buffer);

        if (readBytes < 0) {
            handleLogout(clientChanel, key);
            return null;
        }

        buffer.flip();
        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);

        return new String(byteArray, StandardCharsets.UTF_8).strip();
    }

    private void handleLogout(SocketChannel socketChannel, SelectionKey key) {
        try {
            System.out.println(DISCONNECTED_MESSAGE + socketChannel.getRemoteAddress());
            key.cancel();
            socketChannel.close();
        } catch (IOException e) {
            Logger.log(SOCKET_CHANNEL_CLOSING_ERROR_MESSAGE.formatted(e.getMessage()), e);
        }
    }

    private void sendResponseToClient(SocketChannel socketChannel, String serverResponse, ByteBuffer buffer)
            throws IOException {
        if (serverResponse == null) {
            serverResponse = INPUT_ERROR;
        }

        buffer.clear();
        buffer.put((serverResponse + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
        buffer.flip();

        socketChannel.write(buffer);
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();

        clientChannel.configureBlocking(false);

        ClientSession session = new ClientSession(BUFFER_SIZE);
        clientChannel.register(selector, SelectionKey.OP_READ, session);
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void start() {
        Thread serverThread = new Thread(() -> {
            System.out.println("Starting server on " + SERVER_HOST + ":" + SERVER_PORT + "...");
            try {
                startServer();
            } catch (NetworkException e) {
                Logger.log(e.getMessage(), e);
            }
        });

        serverThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println(SHUTDOWN_SERVER_RESPONSE);
            stop();
        }));
    }
}