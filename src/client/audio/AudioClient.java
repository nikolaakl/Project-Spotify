package client.audio;

import common.audio.AudioFormatCodec;
import common.net.StreamIO;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioClient implements Runnable {
    private static final String OK_MESSAGE = "OK";
    private static final String SEND_COMMAND_MESSAGE = "send";
    private static final String STOP_COMMAND_MESSAGE = "stop";
    private static final String STREAM_COMMAND_PREFIX_MESSAGE = "stream ";
    private static final String INTERRUPTED_MESSAGE =
            "<Streaming interrupted>" + System.lineSeparator() + "Enter message: ";

    private final String host;
    private final int port;
    private final String songId;

    private final AtomicBoolean running = new AtomicBoolean(true);

    private SocketChannel socketChannel;
    private SourceDataLine line;

    public AudioClient(String host, int port, String songId) {
        this.host = host;
        this.port = port;
        this.songId = songId;
    }

    @Override
    public void run() {
        boolean endedNormally = false;

        try (SocketChannel channel = SocketChannel.open(new InetSocketAddress(this.host, this.port))) {
            this.socketChannel = channel;

            if (!handshakeStream()) {
                return;
            }

            AudioFormat format = readAudioFormat();
            if (format == null || !openLine(format)) {
                return;
            }

            endedNormally = streamLoop();
        } catch (IOException e) {
            System.out.print(System.lineSeparator() + INTERRUPTED_MESSAGE);
        } finally {
            closeLine(endedNormally);
        }
    }

    private boolean streamLoop() throws IOException {
        while (this.running.get()) {
            StreamIO.writeString(this.socketChannel, SEND_COMMAND_MESSAGE);

            byte[] chunk = StreamIO.readBytes(this.socketChannel);
            if (chunk == null) {
                return false;
            }
            if (chunk.length == 0) {
                return true;
            }

            this.line.write(chunk, 0, chunk.length);
        }
        return false;
    }

    public void stop() {
        this.running.set(false);

        try {
            if (this.socketChannel != null && this.socketChannel.isOpen()) {
                StreamIO.writeString(this.socketChannel, STOP_COMMAND_MESSAGE);
            }
        } catch (IOException e) {
            System.out.println("Failed to notify server for stop");
        }
    }

    private boolean handshakeStream() throws IOException {
        byte[] command = (STREAM_COMMAND_PREFIX_MESSAGE + this.songId + System.lineSeparator())
                .getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(command);
        while (buffer.hasRemaining()) {
            this.socketChannel.write(buffer);
        }

        String status = StreamIO.readString(this.socketChannel);

        return OK_MESSAGE.equals(status);
    }

    private AudioFormat readAudioFormat() throws IOException {
        byte[] formatBytes = StreamIO.readBytes(this.socketChannel);

        return (formatBytes == null) ? null : AudioFormatCodec.deserialize(formatBytes);
    }

    private boolean openLine(AudioFormat format) {
        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            this.line = (SourceDataLine) AudioSystem.getLine(info);
            this.line.open(format);
            this.line.start();

            return true;
        } catch (LineUnavailableException e) {
            return false;
        }
    }

    private void closeLine(boolean endedNormally) {
        if (this.line == null) {
            return;
        }

        if (endedNormally) {
            this.line.drain();
        } else {
            this.line.flush();
        }

        this.line.stop();
        this.line.close();
        this.line = null;
    }
}
