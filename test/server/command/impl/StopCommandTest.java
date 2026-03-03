package server.command.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.session.ClientSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StopCommandTest {
    private static ClientSession session;
    private static StopCommand command;

    @BeforeEach
    void setUp() {
        session = mock(ClientSession.class);
        command = new StopCommand();
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
    void testExecuteLoggedInUserReturnNothingPlayingWhenNotStreaming() {
        when(session.isLoggedIn()).thenReturn(true);
        when(session.isStreaming()).thenReturn(false);

        CommandResponse<Void> result = command.execute(new String[0], session);

        assertEquals(StatusCode.NOT_FOUND, result.getStatus(),
                "Status code should be equal to NOT_FOUND when nothing is playing");
    }

    @Test
    void testExecuteLoggedInUserReturnStoppingWhenActualStreaming() {
        when(session.isLoggedIn()).thenReturn(true);
        when(session.isStreaming()).thenReturn(true);

        CommandResponse<Void> result = command.execute(new String[0], session);

        assertEquals(StatusCode.SUCCESS, result.getStatus(),
                "Status code should be equal to SUCCESS when successfully stopped");
    }
}
