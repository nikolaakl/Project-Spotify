package client;

import client.audio.AudioClient;
import server.command.impl.PlayCommand;
import server.command.response.CommandResponse;
import server.util.GsonSingleton;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpotifyClient {
    private static final String CONNECTED_SUCCESSFULLY_MESSAGE = "Connected to the server";
    private static final String NETWORK_PROBLEM_MESSAGE =
            "There is a problem with the network communication. Try again later";
    private static final String ENTERING_MESSAGE = "[%s] Enter message: ";
    private static final String DISCONNECTING_MESSAGE = "Disconnected from server";
    private static final String SERVER_REPLY_MESSAGE = "The server replied: ";
    private static final String DISCONNECT_MESSAGE = "disconnect";
    private static final String LOGIN_MESSAGE = "login ";
    private static final String SUCCESSFULLY_LOGGED_MESSAGE = "successfully logged";
    private static final String LOGGED_OUT_MESSAGE = "logged out";
    private static final String STREAM_MESSAGE = "stream ";
    private static final String STOP_MESSAGE = "stop";
    private static final String GUEST_MESSAGE = "guest";
    private static final String INVALID_COMMAND_MESSAGE = "Invalid command. Use play <songId>";
    private static final String REGEX_SPLIT_SEPARATOR = "\\s+";
    private static final short FIRST_INDEX = 1;

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 8192;
    private static AudioClient audioClient;
    private String userPrompt = GUEST_MESSAGE;
    private final ExecutorService audioExecutor =
            Executors.newVirtualThreadPerTaskExecutor();

    public void start() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            connect(socketChannel);
            System.out.println(CONNECTED_SUCCESSFULLY_MESSAGE);

            runClientLoop(scanner, socketChannel, buffer);
        } catch (IOException e) {
            System.out.println(NETWORK_PROBLEM_MESSAGE);
        } finally {
            stopStreaming();
        }
    }

    private void runClientLoop(Scanner scanner, SocketChannel socketChannel, ByteBuffer buffer) throws IOException {
        while (true) {
            String enteringMessage = ENTERING_MESSAGE.formatted(this.userPrompt);
            System.out.print(enteringMessage);
            String message = scanner.nextLine().strip();

            if (message.equals(DISCONNECT_MESSAGE)) {
                break;
            }
            if (message.isBlank()) {
                continue;
            }
            if (message.startsWith(STREAM_MESSAGE)) {
                System.out.println(INVALID_COMMAND_MESSAGE);
                continue;
            }
            send(socketChannel, buffer, message);

            String jsonReply = receive(socketChannel, buffer);
            if (jsonReply == null || jsonReply.isEmpty()) {
                System.out.println(DISCONNECTING_MESSAGE);
                break;
            }
            CommandResponse<?> response = GsonSingleton.getInstance().fromJson(jsonReply, CommandResponse.class);
            System.out.println(SERVER_REPLY_MESSAGE);
            System.out.println("Status: " + response.getStatus());
            System.out.println("Message: " + response.getMessage());

            if (response.getPayload() != null) {
                System.out.println("Payload: " + response.getPayload());
            }
            System.out.println();
            updateUserPrompt(message, response.getMessage());

            if (response.getMessage().equals(PlayCommand.PLAY_COMMAND_SONG_PLAYED_SUCCESSFULLY)) {
                String songId = (String) response.getPayload();
                startStreaming(songId);
            }
            if (message.equals(STOP_MESSAGE)) {
                stopStreaming();
            }
        }
    }

    private void updateUserPrompt(String message, String reply) {
        if (message.startsWith(LOGIN_MESSAGE) && reply.toLowerCase().contains(SUCCESSFULLY_LOGGED_MESSAGE)) {
            String[] parts = message.split(REGEX_SPLIT_SEPARATOR);
            this.userPrompt = parts[FIRST_INDEX].strip();
        }

        if (message.equals(DISCONNECT_MESSAGE) || reply.toLowerCase().contains(LOGGED_OUT_MESSAGE)) {
            this.userPrompt = GUEST_MESSAGE;
        }
    }

    private void connect(SocketChannel socketChannel) throws IOException {
        socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
    }

    private void startStreaming(String songId) {
        stopStreaming();

        audioClient = new AudioClient(SERVER_HOST, SERVER_PORT, songId);
        this.audioExecutor.submit(audioClient);
    }

    private void stopStreaming() {
        if (audioClient != null) {
            audioClient.stop();
            audioClient = null;
        }
    }

    private void send(SocketChannel socketChannel, ByteBuffer buffer, String message) throws IOException {
        buffer.clear();
        buffer.put(message.getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
    }

    private String receive(SocketChannel socketChannel, ByteBuffer buffer) throws IOException {
        buffer.clear();
        int read = socketChannel.read(buffer);
        if (read < 0) {
            return null;
        }
        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        return new String(data, StandardCharsets.UTF_8).strip();
    }
}