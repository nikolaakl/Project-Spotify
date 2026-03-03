package server.command.impl;

import common.net.StreamIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.model.song.Song;
import server.repository.SongRepository;
import server.session.ClientSession;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class StreamCommandTest {
    private static SongRepository songRepository;
    private static ClientSession session;
    private static StreamCommand command;
    private static SocketChannel socketChannel;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        songRepository = mock(SongRepository.class);
        session = mock(ClientSession.class);
        command = new StreamCommand(songRepository);
        socketChannel = mock(SocketChannel.class);
    }

    @Test
    void testExecuteNoArgumentsReturnUsage() {
        CommandResponse<String> result = command.execute(new String[0], session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when no arguments");
    }

    @Test
    void testExecuteNullArgumentsReturnUsage() {
        CommandResponse<String> result = command.execute(null, session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when NULL arguments");
    }

    @Test
    void testExecuteSongIdBlankReturnUsage() {
        CommandResponse<String> result = command.execute(new String[]{"   "}, session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when songId is blank");
    }

    @Test
    void testExecuteSongNotFoundReturnNotFoundMessage() {
        when(songRepository.findById("missing")).thenReturn(null);

        CommandResponse<String> result = command.execute(new String[]{"missing"}, session);

        assertEquals(StatusCode.NOT_FOUND, result.getStatus(),
                "Status code should be equal to NOT_FOUND when song not found");
    }

    @Test
    void testExecuteSongExistsReturnStreamTakeoverMessage() {
        when(songRepository.findById("Justin-Baby"))
                .thenReturn(new Song("Justin-Baby", "Baby", "Justin"));

        CommandResponse<String> result = command.execute(new String[]{"Justin-Baby"}, session);

        assertEquals(StatusCode.SUCCESS, result.getStatus(),
                "Status code should be equal to SUCCESS when song exists");
    }

    @Test
    void testStreamSongNotFoundWritesNotFoundMessage() throws UnsupportedAudioFileException, IOException {
        when(songRepository.findById("Missing-Song")).thenReturn(null);

        try (MockedStatic<StreamIO> streamIOMock = mockStatic(StreamIO.class)) {
            command.stream(socketChannel, "Missing-Song");

            streamIOMock.verify(() -> StreamIO.writeString(socketChannel, "Song Missing-Song not found"));
        }
    }

    @Test
    void testStreamWavFileMissingWritesMissingFileMessage() throws UnsupportedAudioFileException, IOException {
        Song song = new Song("Justin-Baby", "Justin", "Baby");
        when(songRepository.findById("Justin-Baby")).thenReturn(song);
        when(songRepository.getSongsDir()).thenReturn(tempDir);

        Path expectedFile = tempDir.resolve("Justin-Baby.wav");

        try (MockedStatic<StreamIO> streamIOMock = mockStatic(StreamIO.class)) {
            command.stream(socketChannel, "Justin-Baby");
            streamIOMock.verify(() -> StreamIO.writeString(socketChannel, "Song file " + expectedFile + " is missing"));
        }
    }
}