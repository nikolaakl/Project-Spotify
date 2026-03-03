package server.command.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.model.song.Song;
import server.repository.SongRepository;
import server.session.ClientSession;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SearchCommandTest {
    private static SongRepository songRepository;
    private static ClientSession session;
    private static SearchCommand command;

    @BeforeEach
    void setUp() {
        songRepository = mock(SongRepository.class);
        session = mock(ClientSession.class);
        command = new SearchCommand(songRepository);
    }

    @Test
    void testExecuteNoArgumentsReturnUsage() {
        CommandResponse<Collection<String>> result = command.execute(new String[0], session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when no arguments");
    }

    @Test
    void testExecuteNullArgumentsReturnUsage() {
        CommandResponse<Collection<String>> result = command.execute(null, session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when NULL arguments");
    }

    @Test
    void testExecuteNotLoggedInUserReturnLoginFirst() {
        when(session.isLoggedIn()).thenReturn(false);

        CommandResponse<Collection<String>> result = command.execute(new String[]{"song"}, session);

        assertEquals(StatusCode.UNAUTHORIZED, result.getStatus(),
                "Status code should be equal to UNAUTHORIZED when not logged in");
    }

    @Test
    void testExecuteLoggedInAndNoMatchesReturnNoSongsFound() {
        when(session.isLoggedIn()).thenReturn(true);
        when(songRepository.search(any())).thenReturn(List.of());

        CommandResponse<Collection<String>> result = command.execute(new String[]{"song"}, session);

        assertEquals(StatusCode.NOT_FOUND, result.getStatus(),
                "Status code should be equal to UNAUTHORIZED when there are no matched songs");

        verify(songRepository, times(1)).search("song");
    }

    @Test
    void testExecuteLoggedInAndMatchesExistReturnSongIdsSeparatedByNewLine() {
        when(session.isLoggedIn()).thenReturn(true);

        Song song1 = mock(Song.class);
        Song song2 = mock(Song.class);
        when(song1.getId()).thenReturn("Justin-Baby");
        when(song2.getId()).thenReturn("Djani-TheBaby");

        when(songRepository.search(any())).thenReturn(List.of(song1, song2));

        CommandResponse<Collection<String>> result = command.execute(new String[]{"baby"}, session);

        assertEquals(StatusCode.SUCCESS, result.getStatus(),
                "Status code should be equal to SUCCESS when song found");

        verify(songRepository, times(1)).search("baby");
    }
}
