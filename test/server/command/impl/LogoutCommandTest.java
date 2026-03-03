package server.command.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.session.ClientSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LogoutCommandTest {
    private static ClientSession session;
    private static LogoutCommand command;

    @BeforeEach
    void setUp() {
        session = mock(ClientSession.class);
        command = new LogoutCommand();
    }

    @Test
    void testExecuteHasArgumentsReturnUsage() {
        CommandResponse<Void> result = command.execute(new String[1], session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when has arguments");
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

        CommandResponse<Void> result = command.execute(new String[0], session);

        assertEquals(StatusCode.UNAUTHORIZED, result.getStatus(),
                "Status code should be equal to UNAUTHORIZED when not logged in");
    }

    @Test
    void testExecuteSuccessfulSetLoggedUserToNullAndReturnOk() {
        when(session.isLoggedIn()).thenReturn(true);

        CommandResponse<Void> result = command.execute(new String[0], session);

        assertEquals(StatusCode.SUCCESS, result.getStatus(),
                "Status code should be equal to SUCCESS when successfully logged out");

        verify(session).setLoggedUser(null);
    }
}
