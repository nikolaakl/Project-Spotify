package server.command.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.command.response.view.PlaylistView;
import server.exception.checked.DataLoadException;
import server.model.playlist.Playlist;
import server.model.user.User;
import server.repository.PlaylistsRepository;
import server.session.ClientSession;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ShowPlaylistCommandTest {
    private static ClientSession session;
    private static ShowPlaylistCommand command;
    private static PlaylistsRepository playlistsRepository;

    @BeforeEach
    void setUp() {
        session = mock(ClientSession.class);
        playlistsRepository = mock(PlaylistsRepository.class);
        command = new ShowPlaylistCommand(playlistsRepository);
    }

    @Test
    void testExecuteNoArgumentsReturnUsage() {
        CommandResponse<PlaylistView> result = command.execute(new String[0], session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when no arguments");
    }

    @Test
    void testExecuteNullArgumentsReturnUsage() {
        CommandResponse<PlaylistView> result = command.execute(null, session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when NULL arguments");
    }

    @Test
    void testExecuteNotLoggedInUserReturnLoginFirst() {
        when(session.isLoggedIn()).thenReturn(false);

        CommandResponse<PlaylistView> result = command.execute(new String[]{"song"}, session);

        assertEquals(StatusCode.UNAUTHORIZED, result.getStatus(),
                "Status code should be equal to UNAUTHORIZED when not logged in");
    }

    @Test
    void testExecutePlaylistNotFoundReturnNotFound() throws DataLoadException {
        when(session.isLoggedIn()).thenReturn(true);
        when(session.getLoggedUser()).thenReturn(new User("a@b.c", "hash"));

        when(playlistsRepository.loadPlaylist("a@b.c", "mix")).thenReturn(null);

        CommandResponse<PlaylistView> result = command.execute(new String[]{"mix"}, session);

        assertEquals(StatusCode.NOT_FOUND, result.getStatus(),
                "Status code should be equal to NOT_FOUND when playlist not found");
    }

    @Test
    void testExecutePlaylistEmptyReturnEmpty() throws DataLoadException {
        when(session.isLoggedIn()).thenReturn(true);
        when(session.getLoggedUser()).thenReturn(new User("a@b.c", "hash"));

        Playlist playlist = mock(Playlist.class);
        when(playlist.getPlaylistName()).thenReturn("mix");
        when(playlist.getSongs()).thenReturn(List.of());

        when(playlistsRepository.loadPlaylist("a@b.c", "mix")).thenReturn(playlist);

        CommandResponse<PlaylistView> result = command.execute(new String[]{"mix"}, session);

        assertEquals(StatusCode.SUCCESS, result.getStatus(),
                "Status code should be equal to SUCCESS when empty playlist");
        assertTrue(result.getPayload().songs().isEmpty(), "Songs should be empty");
    }

    @Test
    void testExecutePlaylistHasSongsReturnSongsEachOnNewLine() throws DataLoadException {
        when(session.isLoggedIn()).thenReturn(true);
        when(session.getLoggedUser()).thenReturn(new User("a@b.c", "hash"));

        Playlist playlist = mock(Playlist.class);
        when(playlist.getPlaylistName()).thenReturn("mix");
        when(playlist.getSongs()).thenReturn(List.of("Justin-Baby", "Djani-SamSam"));

        when(playlistsRepository.loadPlaylist("a@b.c", "mix")).thenReturn(playlist);

        CommandResponse<PlaylistView> result = command.execute(new String[]{"mix"}, session);

        assertEquals(StatusCode.SUCCESS, result.getStatus(),
                "Status code should be equal to SUCCESS when playlist with songs found");
    }

    @Test
    void testExecuteRepositoryThrowDataLoadExceptionReturnFriendlyError() throws DataLoadException {
        when(session.isLoggedIn()).thenReturn(true);
        when(session.getLoggedUser()).thenReturn(new User("a@b.c", "hash"));

        when(playlistsRepository.loadPlaylist("a@b.c", "mix"))
                .thenThrow(new DataLoadException("boom"));

        CommandResponse<PlaylistView> result = command.execute(new String[]{"mix"}, session);

        assertEquals(StatusCode.SERVER_ERROR, result.getStatus(),
                "Status code should be equal to SERVER_ERROR when DataLoadException has thrown");
    }
}
