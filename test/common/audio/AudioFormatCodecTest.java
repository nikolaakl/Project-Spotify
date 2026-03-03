package common.audio;

import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AudioFormatCodecTest {

    private static AudioFormat sampleFormat() {
        return new AudioFormat(
                new AudioFormat.Encoding("Something"),
                44100.0f,
                16,
                2,
                4,
                44100.0f,
                false
        );
    }

    @Test
    void testSerializeNonEmptyByteArray() throws IOException {
        AudioFormat format = sampleFormat();

        byte[] bytes = AudioFormatCodec.serialize(format);

        assertNotNull(bytes, "Byte array should not be null");
        assertTrue(bytes.length > 0, "Serialized payload should not be empty");
    }

    @Test
    void testDeserializePayloadTooShortThrowIOException() {
        byte[] corrupted = new byte[]{1, 2, 3};

        assertThrows(IOException.class, () -> AudioFormatCodec.deserialize(corrupted),
                "IOException should be thrown when payload is too short");
    }

    @Test
    void testDeserializePayloadSuccessfully() throws IOException {
        AudioFormat format = sampleFormat();
        byte[] bytes = AudioFormatCodec.serialize(format);

        AudioFormat decodedFormat = AudioFormatCodec.deserialize(bytes);

        assertNotNull(decodedFormat, "Decoded audio format must not be null");
        assertEquals("Something", decodedFormat.getEncoding().toString(),
                "Encoding should be equal to Something");
    }

    @Test
    void testDeserializePayloadNullThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> AudioFormatCodec.deserialize(null),
                "NullPointerException should be thrown when payload is null");
    }
}
