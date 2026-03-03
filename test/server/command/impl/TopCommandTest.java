package server.command.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.command.response.view.TopEntryView;
import server.model.stats.Stats;
import server.session.ClientSession;

import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TopCommandTest {
    private static Stats stats;
    private static ClientSession session;
    private static TopCommand command;

    @BeforeEach
    void setUp() {
        stats = mock(Stats.class);
        session = mock(ClientSession.class);
        command = new TopCommand(stats);
    }

    @Test
    void testExecuteNoArgumentsReturnUsage() {
        CommandResponse<Collection<TopEntryView>> result = command.execute(new String[0], session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when no arguments");
    }

    @Test
    void testExecuteNullArgumentsReturnUsage() {
        CommandResponse<Collection<TopEntryView>> result = command.execute(null, session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when NULL arguments");
    }

    @Test
    void testExecuteNotLoggedInUserReturnLoginFirst() {
        when(session.isLoggedIn()).thenReturn(false);

        CommandResponse<Collection<TopEntryView>> result = command.execute(new String[]{"song"}, session);

        assertEquals(StatusCode.UNAUTHORIZED, result.getStatus(),
                "Status code should be equal to UNAUTHORIZED when not logged in");
    }

    @Test
    void testExecuteNumberNotIntReturnInvalidNumberPlusUsage() {
        when(session.isLoggedIn()).thenReturn(true);

        CommandResponse<Collection<TopEntryView>> result = command.execute(new String[]{"abc"}, session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when number not int");
    }

    @Test
    void testExecuteNumberNonPositiveReturnInvalidNumberPlusUsage() {
        when(session.isLoggedIn()).thenReturn(true);

        CommandResponse<Collection<TopEntryView>> result = command.execute(new String[]{"0"}, session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when number not positive");
    }

    @Test
    void testExecuteNoPlaysReturnNoPlays() {
        when(session.isLoggedIn()).thenReturn(true);
        when(stats.getPlays()).thenReturn(Map.of());

        CommandResponse<Collection<TopEntryView>> result = command.execute(new String[]{"5"}, session);

        assertEquals(StatusCode.NOT_FOUND, result.getStatus(),
                "Status code should be equal to NOT_FOUND when no plays");
    }

    @Test
    void testExecuteHasPlaysReturnSortedTopN() {
        when(session.isLoggedIn()).thenReturn(true);

        Map<String, Long> plays = Map.of(
                "Justin-Baby", 9L,
                "Djani-SamSam", 3L,
                "Other-Song", 5L
        );
        when(stats.getPlays()).thenReturn(plays);

        CommandResponse<Collection<TopEntryView>> result = command.execute(new String[]{"2"}, session);

        assertEquals(StatusCode.SUCCESS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when there are plays");
    }
}
