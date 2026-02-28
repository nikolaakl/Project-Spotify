package common.audio;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class AudioFormatCodec {
    private AudioFormatCodec() {
    }

    public static byte[] serialize(AudioFormat format) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {

            dataOutputStream.writeUTF(format.getEncoding().toString());
            dataOutputStream.writeFloat(format.getSampleRate());
            dataOutputStream.writeInt(format.getSampleSizeInBits());
            dataOutputStream.writeInt(format.getChannels());
            dataOutputStream.writeInt(format.getFrameSize());
            dataOutputStream.writeFloat(format.getFrameRate());
            dataOutputStream.writeBoolean(format.isBigEndian());

            dataOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        }
    }

    public static AudioFormat deserialize(byte[] payload) throws IOException {
        try (DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(payload))) {

            String encodingStr = dataInputStream.readUTF();
            float sampleRate = dataInputStream.readFloat();
            int sampleSizeInBits = dataInputStream.readInt();
            int channels = dataInputStream.readInt();
            int frameSize = dataInputStream.readInt();
            float frameRate = dataInputStream.readFloat();
            boolean bigEndian = dataInputStream.readBoolean();

            return new AudioFormat(new AudioFormat.Encoding(encodingStr),
                    sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);
        }
    }
}