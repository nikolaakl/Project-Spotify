package server.command.impl;

import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.validation.Validator;
import server.command.CommandType;
import server.session.ClientSession;

public final class StopCommand implements Command {
    private static final String STOP_COMMAND_INVALID_COMMAND_USAGE = "Usage: stop";
    private static final String STOP_COMMAND_LOGIN_FIRST = "You must be logged in";
    private static final String STOP_COMMAND_NOTHING = "Nothing is playing";
    private static final String STOP_COMMAND_STOPPING = "Stopping...";

    @Override
    public CommandResponse<Void> execute(String[] input, ClientSession session) {
        if (!Validator.hasArgs(input, CommandType.STOP.getCommandArgs())) {
            return CommandResponse.error(StatusCode.INVALID_ARGUMENTS, STOP_COMMAND_INVALID_COMMAND_USAGE);
        }

        if (!session.isLoggedIn()) {
            return CommandResponse.error(StatusCode.UNAUTHORIZED, STOP_COMMAND_LOGIN_FIRST);
        }

        if (!session.isStreaming()) {
            return CommandResponse.error(StatusCode.NOT_FOUND, STOP_COMMAND_NOTHING);
        }

        session.setStreaming(false);
        return CommandResponse.success(STOP_COMMAND_STOPPING, null);
    }
}
