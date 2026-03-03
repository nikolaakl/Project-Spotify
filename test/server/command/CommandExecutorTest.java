package server.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import server.command.impl.Command;
import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.model.stats.Stats;
import server.model.user.Users;
import server.repository.SongRepository;
import server.repository.StatsRepository;
import server.repository.UsersRepository;
import server.session.ClientSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class CommandExecutorTest {
    private static Users users;
    private static Stats stats;
    private static SongRepository songRepository;
    private static ClientSession session;

    private CommandExecutor executor;

    @BeforeEach
    void setUp() {
        users = mock(Users.class);
        stats = mock(Stats.class);
        songRepository = mock(SongRepository.class);
        session = mock(ClientSession.class);

        executor = new CommandExecutor(users, stats, songRepository);
    }

    @Test
    void testExecuteCommandInputBlankReturnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> executor.executeCommand("   ", session),
                "IllegalArgumentException should be thrown when command input is blank");
    }

    @Test
    void testExecuteCommandNullSessionReturnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> executor.executeCommand("login a@b.c pass", null),
                "IllegalArgumentException should be thrown when command input is null");
    }

    @Test
    void testExecuteCommandCommandFactoryReturnNullReturnInvalidCommand() {
        try (MockedStatic<CommandFactory> factoryMock = mockStatic(CommandFactory.class)) {
            factoryMock.when(() -> CommandFactory.of(eq("unknown"), any(), any(), any()))
                    .thenReturn(null);

            CommandResponse<?> result = executor.executeCommand("unknown arg1 arg2", session);

            assertEquals(StatusCode.NOT_FOUND, result.getStatus(),
                    "Status code should be equal to NOT_FOUND when command not found");
        }
    }

    @Test
    void testExecuteCommandCommandExecutesReturnResult() {
        Command cmd = mock(Command.class);

        try (MockedStatic<CommandFactory> factoryMock = mockStatic(CommandFactory.class)) {
            factoryMock.when(() -> CommandFactory.of(eq("login"), any(), any(), any()))
                    .thenReturn(cmd);

            CommandResponse<String> res =
                    CommandResponse.success("OK", null);

            when(cmd.execute(any(String[].class), eq(session)))
                    .thenReturn((CommandResponse) res);

            CommandResponse<?> result =
                    executor.executeCommand("login", session);

            assertTrue(result.isSuccess());
            assertEquals("OK", result.getMessage());
        }
    }

    @Test
    void testExecuteCommandRegisterPersistUsers() {
        Command cmd = mock(Command.class);

        try (MockedStatic<CommandFactory> factoryMock = mockStatic(CommandFactory.class);
             MockedStatic<UsersRepository> usersRepoMock = mockStatic(UsersRepository.class)) {

            factoryMock.when(() -> CommandFactory.of(eq("register"), any(), any(), any()))
                    .thenReturn(cmd);

            CommandResponse<String> response =
                    CommandResponse.success("REGISTERED", null);

            when(cmd.execute(any(), eq(session))).thenReturn((CommandResponse) response);

            CommandResponse<?> result = executor.executeCommand("register a@b.c pass", session);

            assertTrue(result.isSuccess());
            assertEquals("REGISTERED", result.getMessage());

            usersRepoMock.verify(() -> UsersRepository.saveUsers(users), times(1));
        }
    }

    @Test
    void testExecuteCommandPlayPersistStats() {
        Command cmd = mock(Command.class);

        try (MockedStatic<CommandFactory> factoryMock = mockStatic(CommandFactory.class);
             MockedStatic<StatsRepository> statsRepoMock = mockStatic(StatsRepository.class)) {

            factoryMock.when(() -> CommandFactory.of(eq("play"), any(), any(), any()))
                    .thenReturn(cmd);

            CommandResponse<String> response =
                    CommandResponse.success("PLAYING", null);

            when(cmd.execute(any(), eq(session))).thenReturn((CommandResponse) response);

            CommandResponse<?> result = executor.executeCommand("play Justin-Baby", session);

            assertTrue(result.isSuccess());
            assertEquals("PLAYING", result.getMessage());

            statsRepoMock.verify(() -> StatsRepository.saveStats(stats), times(1));
        }
    }
}