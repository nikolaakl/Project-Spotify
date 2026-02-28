package server.command.impl;

import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.validation.Validator;
import server.command.CommandType;
import server.model.user.User;
import server.model.user.Users;
import server.security.PasswordHasher;
import server.session.ClientSession;

public final class LoginCommand implements Command {
    private static final String LOGIN_COMMAND_INVALID_COMMAND_USAGE = "Usage: login <email> <password>";
    private static final String LOGIN_COMMAND_USER_ALREADY_LOGGED_IN = "User with email %s is already logged in";
    private static final String LOGIN_COMMAND_USER_NOT_FOUND_DB = "User with email %s does not exist in DB";
    private static final String LOGIN_COMMAND_INCORRECT_PASSWORD = "Incorrect password";
    private static final String LOGIN_COMMAND_SUCCESSFUL_LOGIN_DB =
            "User with email %s has been successfully logged into the system";

    private static final int EMAIL_INDEX = 0;
    private static final int PASSWORD_INDEX = 1;

    private final Users users;

    public LoginCommand(Users users) {
        this.users = users;
    }

    @Override
    public CommandResponse<User> execute(String[] input, ClientSession session) {
        if (!Validator.hasArgs(input, CommandType.LOGIN.getCommandArgs())) {
            return CommandResponse.error(StatusCode.INVALID_ARGUMENTS, LOGIN_COMMAND_INVALID_COMMAND_USAGE);
        }

        if (session.isLoggedIn()) {
            return CommandResponse.error(StatusCode.ALREADY_EXISTS,
                    LOGIN_COMMAND_USER_ALREADY_LOGGED_IN.formatted(session.getLoggedUser().getEmail()));
        }

        String email = input[EMAIL_INDEX];
        String password = input[PASSWORD_INDEX];
        User foundUser = this.users.findUser(email);

        if (Validator.isNullObject(foundUser)) {
            return CommandResponse.error(StatusCode.NOT_FOUND,
                    LOGIN_COMMAND_USER_NOT_FOUND_DB.formatted(email));
        }

        if (!PasswordHasher.verify(password, foundUser.getPasswordHash())) {
            return CommandResponse.error(StatusCode.UNAUTHORIZED, LOGIN_COMMAND_INCORRECT_PASSWORD);
        }

        session.setLoggedUser(foundUser);

        return CommandResponse.success(
                LOGIN_COMMAND_SUCCESSFUL_LOGIN_DB.formatted(email), foundUser);
    }
}
