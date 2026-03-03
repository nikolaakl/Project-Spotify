package server.command.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.exception.checked.DataSaveException;
import server.model.user.User;
import server.repository.PlaylistsRepository;
import server.session.ClientSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreatePlaylistCommandTest {
    private static PlaylistsRepository playlistsRepository;
    private static ClientSession session;
    private static CreatePlaylistCommand command;

    @BeforeEach
    void setUp() {
        playlistsRepository = mock(PlaylistsRepository.class);
        session = mock(ClientSession.class);
        command = new CreatePlaylistCommand(playlistsRepository);
    }

    @Test
    void testExecuteNoArgumentsReturnUsage() {
        CommandResponse<Void> result = command.execute(new String[0], session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when no arguments");
    }

    @Test
    void testExecuteNullArgumentsReturnUsage() {
        CommandResponse<Void> result = command.execute(null, session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when NULL arguments");
    }

    @Test
    void testExecuteNotLoggedInUserReturnLoginFirst() {
        when(session.isLoggedIn()).thenReturn(false);

        CommandResponse<Void> result = command.execute(new String[]{"song"}, session);

        assertEquals(StatusCode.UNAUTHORIZED, result.getStatus(),
                "Status code should be equal to UNAUTHORIZED when not logged in");
    }

    @Test
    void testExecuteBlankPlaylistNameReturnUsage() {
        when(session.isLoggedIn()).thenReturn(true);

        CommandResponse<Void> result = command.execute(new String[]{"   "}, session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when blank playlist");
    }

    @Test
    void testExecutePlaylistAlreadyExistsReturnAlreadyExistsMessage() throws DataSaveException {
        when(session.isLoggedIn()).thenReturn(true);
        when(session.getLoggedUser()).thenReturn(new User("a@b.c", "hash"));

        when(playlistsRepository.createPlaylistIfAbsent("a@b.c", "rock")).thenReturn(false);

        CommandResponse<Void> result = command.execute(new String[]{"rock"}, session);

        assertEquals(StatusCode.ALREADY_EXISTS, result.getStatus(),
                "Status code should be equal to ALREADY_EXIST when playlist already exists");
    }

    @Test
    void testExecuteSuccessfulCreatesPlaylistAndReturnOk() throws DataSaveException {
        when(session.isLoggedIn()).thenReturn(true);
        when(session.getLoggedUser()).thenReturn(new User("a@b.c", "hash"));

        when(playlistsRepository.createPlaylistIfAbsent("a@b.c", "mix")).thenReturn(true);

        CommandResponse<Void> result = command.execute(new String[]{"mix"}, session);

        assertEquals(StatusCode.SUCCESS, result.getStatus(),
                "Status code should be equal to SUCCESS when playlist created successfully");
    }

    @Test
    void testExecuteSaveFailsReturnUserErrorMessage() throws DataSaveException {
        when(session.isLoggedIn()).thenReturn(true);
        when(session.getLoggedUser()).thenReturn(new User("a@b.c", "hash"));

        when(playlistsRepository.createPlaylistIfAbsent("a@b.c", "chill"))
                .thenThrow(new DataSaveException("boom"));

        CommandResponse<Void> result = command.execute(new String[]{"chill"}, session);

        assertEquals(StatusCode.SERVER_ERROR, result.getStatus(),
                "Status code should be equal to SERVER_ERROR when load throw DataSaveException");
    }
}
