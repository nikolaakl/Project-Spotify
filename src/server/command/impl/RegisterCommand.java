package server.command.impl;

import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.validation.Validator;
import server.command.CommandType;
import server.model.user.User;
import server.model.user.Users;
import server.security.PasswordHasher;
import server.session.ClientSession;

public final class RegisterCommand implements Command {
    private static final String REGISTER_COMMAND_INVALID_COMMAND_USAGE =
            "Usage: register <email> <password>";
    private static final String REGISTER_COMMAND_USER_LOGGED_IN =
            "User with email %s is already logged in! You cannot register a new user until they log out";
    private static final String REGISTER_COMMAND_USER_ALREADY_REGISTERED =
            "User with email %s already exists in DB";
    private static final String REGISTER_COMMAND_INVALID_EMAIL =
            "Invalid email address provided";
    private static final String REGISTER_COMMAND_SUCCESSFUL_REGISTER_DB =
            "User with email %s has been successfully registered";

    private static final String EMAIL_REGEX = "[A-Za-z0-9\\p{Punct}]+@[a-z0-9]+\\.[a-z]*";
    private static final int EMAIL_INDEX = 0;
    private static final int PASSWORD_INDEX = 1;

    private final Users users;

    public RegisterCommand(Users users) {
        this.users = users;
    }

    @Override
    public CommandResponse<Void> execute(String[] input, ClientSession session) {
        if (!Validator.hasArgs(input, CommandType.REGISTER.getCommandArgs())) {
            return CommandResponse.error(StatusCode.INVALID_ARGUMENTS, REGISTER_COMMAND_INVALID_COMMAND_USAGE);
        }

        if (session.isLoggedIn()) {
            return CommandResponse.error(StatusCode.UNAUTHORIZED,
                    REGISTER_COMMAND_USER_LOGGED_IN.formatted(session.getLoggedUser().getEmail()));
        }

        String email = input[EMAIL_INDEX];
        String password = input[PASSWORD_INDEX];
        User foundUser = this.users.findUser(email);

        if (foundUser != null) {
            return CommandResponse.error(StatusCode.ALREADY_EXISTS,
                    REGISTER_COMMAND_USER_ALREADY_REGISTERED.formatted(email));
        }

        if (!email.matches(EMAIL_REGEX)) {
            return CommandResponse.error(StatusCode.INVALID_ARGUMENTS, REGISTER_COMMAND_INVALID_EMAIL);
        }

        String hash = PasswordHasher.hash(password);
        this.users.addUser(new User(email, hash));

        return CommandResponse.success(REGISTER_COMMAND_SUCCESSFUL_REGISTER_DB.formatted(email), null);
    }
}
