package server.command.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.model.user.User;
import server.model.user.Users;
import server.session.ClientSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RegisterCommandTest {
    private static Users users;
    private static ClientSession session;
    private static RegisterCommand command;

    @BeforeEach
    void setUp() {
        users = mock(Users.class);
        session = mock(ClientSession.class);
        command = new RegisterCommand(users);
    }

    @Test
    void testExecuteArgsCountIsInvalidReturnUsage() {
        CommandResponse<Void> result = command.execute(new String[]{"onlyEmail"}, session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when invalid count arguments");
    }

    @Test
    void testExecuteUserIsLoggedInReturnAlreadyLoggedInMessage() {
        User loggedUser = new User("a@a.a", "hash");
        when(session.isLoggedIn()).thenReturn(true);
        when(session.getLoggedUser()).thenReturn(loggedUser);

        CommandResponse<Void> result = command.execute(new String[]{"b@b.b", "123"}, session);

        assertEquals(StatusCode.ALREADY_EXISTS, result.getStatus(),
                "Status code should be equal to ALREADY_EXISTS when user is logged in");
    }

    @Test
    void testExecuteUserAlreadyExistsReturnAlreadyRegisteredMessage() {
        when(session.isLoggedIn()).thenReturn(false);
        when(users.findUser("test@test.com")).thenReturn(new User("test@test.com", "someHash"));

        CommandResponse<Void> result = command.execute(new String[]{"test@test.com", "pass"}, session);

        assertEquals(StatusCode.ALREADY_EXISTS, result.getStatus(),
                "Status code should be equal to ALREADY_EXISTS when user already exists");

        verify(users, never()).addUser(any());
    }

    @Test
    void testExecuteEmailIsInvalidReturnInvalidEmailMessage() {
        when(session.isLoggedIn()).thenReturn(false);
        when(users.findUser("invalid-email")).thenReturn(null);

        CommandResponse<Void> result = command.execute(new String[]{"invalid-email", "pass"}, session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when email is invalid");

        verify(users, never()).addUser(any());
    }

    @Test
    void testExecuteRegisterIsSuccessfulAddsUserAndReturnSuccessMessage() {
        when(session.isLoggedIn()).thenReturn(false);
        when(users.findUser("new@user.com")).thenReturn(null);

        CommandResponse<Void> result = command.execute(new String[]{"new@user.com", "pass123"}, session);

        assertEquals(StatusCode.SUCCESS, result.getStatus(),
                "Status code should be equal to SUCCESS when successfully registered");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(users).addUser(captor.capture());

        User added = captor.getValue();
        assertEquals("new@user.com", added.getEmail(),
                "Added user email should be equal to new@user.com");

        assertNotEquals("pass123", added.getPasswordHash(),
                "Added user password hash should not be equal to the given password");
        assertNotNull(added.getPasswordHash(), "Added user password hash should not be null");
        assertFalse(added.getPasswordHash().isBlank(), "Added user password hash should not be blank");
    }

    @Test
    void testExecuteEmailIsValidButHasUppercaseDomainReturnInvalid() {
        when(session.isLoggedIn()).thenReturn(false);
        when(users.findUser("A@A.A")).thenReturn(null);

        CommandResponse<Void> result = command.execute(new String[]{"A@A.A", "pass"}, session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when uppercase email domain");
    }
}
