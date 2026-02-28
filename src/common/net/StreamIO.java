package common.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class StreamIO {
    private static final short BUFFER_CAPACITY = 4;
    private StreamIO() {
    }

    public static void writeBytes(SocketChannel socketChannel, byte[] data) throws IOException {
        ByteBuffer header = ByteBuffer.allocate(BUFFER_CAPACITY);
        header.putInt(data.length);
        header.flip();

        while (header.hasRemaining()) {
            socketChannel.write(header);
        }

        ByteBuffer body = ByteBuffer.wrap(data);
        while (body.hasRemaining()) {
            socketChannel.write(body);
        }
    }

    public static byte[] readBytes(SocketChannel socketChannel) throws IOException {
        ByteBuffer header = ByteBuffer.allocate(BUFFER_CAPACITY);
        int readBytes;
        while (header.hasRemaining()) {
            readBytes = socketChannel.read(header);
            if (readBytes == -1) {
                return null;
            }
        }
        header.flip();

        int len = header.getInt();
        if (len == 0) {
            return new byte[0];
        }

        ByteBuffer body = ByteBuffer.allocate(len);
        while (body.hasRemaining()) {
            readBytes = socketChannel.read(body);
            if (readBytes == -1) {
                return null;
            }
        }
        return body.array();
    }

    public static void writeString(SocketChannel ch, String s) throws IOException {
        writeBytes(ch, s.getBytes(StandardCharsets.UTF_8));
    }

    public static String readString(SocketChannel ch) throws IOException {
        byte[] bytes = readBytes(ch);
        if (bytes == null) {
            return null;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
