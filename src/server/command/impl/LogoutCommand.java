package server.command.impl;

import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.validation.Validator;
import server.command.CommandType;
import server.session.ClientSession;

public final class LogoutCommand implements Command {
    private static final String LOGOUT_COMMAND_INVALID_COMMAND_USAGE = "Usage: logout";
    private static final String LOGOUT_COMMAND_NOT_LOGGED_IN = "You must login before logging out";
    private static final String LOGOUT_COMMAND_SUCCESSFUL_OPERATION = "You have successfully logged out";

    @Override
    public CommandResponse<Void> execute(String[] input, ClientSession session) {
        if (!Validator.hasArgs(input, CommandType.LOGOUT.getCommandArgs())) {
            return CommandResponse.error(StatusCode.INVALID_ARGUMENTS, LOGOUT_COMMAND_INVALID_COMMAND_USAGE);
        }

        if (!session.isLoggedIn()) {
            return CommandResponse.error(StatusCode.UNAUTHORIZED, LOGOUT_COMMAND_NOT_LOGGED_IN);
        }

        session.setLoggedUser(null);

        return CommandResponse.success(LOGOUT_COMMAND_SUCCESSFUL_OPERATION, null);
    }
}
