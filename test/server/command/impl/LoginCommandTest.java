package server.command.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.model.user.User;
import server.model.user.Users;
import server.security.PasswordHasher;
import server.session.ClientSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoginCommandTest {
    private static Users users;
    private static ClientSession session;
    private static LoginCommand command;

    @BeforeEach
    void setUp() {
        users = mock(Users.class);
        session = mock(ClientSession.class);
        command = new LoginCommand(users);
    }

    @Test
    void testExecuteNoArgumentsReturnUsage() {
        CommandResponse<User> result = command.execute(new String[0], session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when no arguments");
    }

    @Test
    void testExecuteNullArgumentsReturnUsage() {
        CommandResponse<User> result = command.execute(null, session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when NULL arguments");
    }

    @Test
    void testExecuteLoggedInReturnAlreadyLogged() {
        when(session.isLoggedIn()).thenReturn(true);
        User logged = mock(User.class);
        when(logged.getEmail()).thenReturn("n@a.a");
        when(session.getLoggedUser()).thenReturn(logged);

        CommandResponse<User> result = command.execute(new String[]{"n@a.a", "pass"}, session);

        assertEquals(StatusCode.ALREADY_EXISTS, result.getStatus(),
                "Status code should be equal to ALREADY_EXIST when already logged in");
    }

    @Test
    void testExecuteUserNotFoundReturnNotFound() {
        when(session.isLoggedIn()).thenReturn(false);
        when(users.findUser("missing@a.a")).thenReturn(null);

        CommandResponse<User> result = command.execute(new String[]{"missing@a.a", "pass"}, session);

        assertEquals(StatusCode.NOT_FOUND, result.getStatus(),
                "Status code should be equal to NOT_FOUND when user not found");

        verify(users).findUser("missing@a.a");
    }

    @Test
    void testExecutePasswordIncorrectReturnIncorrectPassword() {
        when(session.isLoggedIn()).thenReturn(false);

        User found = mock(User.class);
        when(found.getPasswordHash()).thenReturn("hash");
        when(users.findUser("n@a.a")).thenReturn(found);

        try (MockedStatic<PasswordHasher> mocked = mockStatic(PasswordHasher.class)) {
            mocked.when(() -> PasswordHasher.verify("wrong", "hash")).thenReturn(false);

            CommandResponse<User> result = command.execute(new String[]{"n@a.a", "wrong"}, session);

            assertEquals(StatusCode.UNAUTHORIZED, result.getStatus(),
                    "Status code should be equal to UNAUTHORIZED when password is incorrect");
        }
    }

    @Test
    void testExecuteSuccessfulSetLoggedUserAndReturnOk() {
        when(session.isLoggedIn()).thenReturn(false);

        User found = mock(User.class);
        when(found.getPasswordHash()).thenReturn("hash");
        when(users.findUser("n@a.a")).thenReturn(found);

        try (MockedStatic<PasswordHasher> mocked = mockStatic(PasswordHasher.class)) {
            mocked.when(() -> PasswordHasher.verify("pass", "hash")).thenReturn(true);

            CommandResponse<User> result = command.execute(new String[]{"n@a.a", "pass"}, session);

            assertEquals(StatusCode.SUCCESS, result.getStatus(),
                    "Status code should be equal to SUCCESS when successfully logged in");
            verify(session).setLoggedUser(found);
        }
    }
}
