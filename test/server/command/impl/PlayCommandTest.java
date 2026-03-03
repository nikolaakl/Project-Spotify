package server.command.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.model.song.Song;
import server.model.stats.Stats;
import server.repository.SongRepository;
import server.session.ClientSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PlayCommandTest {
    private SongRepository songRepository;
    private Stats stats;
    private ClientSession session;
    private PlayCommand command;

    @BeforeEach
    void setUp() {
        songRepository = mock(SongRepository.class);
        stats = mock(Stats.class);
        session = mock(ClientSession.class);
        command = new PlayCommand(songRepository, stats);
    }

    @Test
    void testExecuteInvalidArgumentsCountReturnUsage() {
        CommandResponse<String> result = command.execute(new String[]{}, session);

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
    void testExecuteNotLoggedInUserReturnLoginFirst() {
        when(session.isLoggedIn()).thenReturn(false);

        CommandResponse<String> result = command.execute(new String[]{"song"}, session);

        assertEquals(StatusCode.UNAUTHORIZED, result.getStatus(),
                "Status code should be equal to UNAUTHORIZED when not logged in");
    }

    @Test
    void testExecuteSongIdBlankReturnUsage() {
        when(session.isLoggedIn()).thenReturn(true);

        CommandResponse<String> result = command.execute(new String[]{"   "}, session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when songId is blank");;
    }

    @Test
    void testExecuteSongNotFoundReturnNotFound() {
        when(session.isLoggedIn()).thenReturn(true);
        when(songRepository.findById("missing")).thenReturn(null);

        CommandResponse<String> result = command.execute(new String[]{"missing"}, session);

        assertEquals(StatusCode.NOT_FOUND, result.getStatus(),
                "Status code should be equal to NOT_FOUND when song not found");

        verify(songRepository).findById("missing");
    }

    @Test
    void testExecuteSuccessfulIncrementsStatsAndReturnsOk() {
        when(session.isLoggedIn()).thenReturn(true);

        Song song = new Song("Justin-Baby", "Baby", "Justin");
        when(songRepository.findById("Justin-Baby")).thenReturn(song);

        CommandResponse<String> result = command.execute(new String[]{"Justin-Baby"}, session);

        assertEquals(StatusCode.SUCCESS, result.getStatus(),
                "Status code should be equal to SUCCESS when successfully increments stats");

        verify(songRepository).findById("Justin-Baby");
        verify(stats).incrementPlays("Justin-Baby");
    }
}
