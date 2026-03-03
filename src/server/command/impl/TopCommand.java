package server.command.impl;

import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.command.response.view.TopEntryView;
import server.validation.Validator;
import server.command.CommandType;
import server.model.stats.Stats;
import server.session.ClientSession;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

public final class TopCommand implements Command {
    private static final String TOP_COMMAND_INVALID_USAGE_COMMAND = "Usage: top <number>";
    private static final String TOP_COMMAND_INVALID_NUMBER_GIVEN = "Invalid number provided ";
    private static final String TOP_COMMAND_NO_PLAYS = "No songs have been played yet";
    private static final String TOP_COMMAND_LOGIN_FIRST = "You must be logged in";
    private static final String TOP_COMMAND_SUCCESSFUL = "Top songs retrieved";
    private static final short NUMBER_INDEX = 0;

    private final Stats stats;

    public TopCommand(Stats stats) {
        this.stats = stats;
    }

    @Override
    public CommandResponse<Collection<TopEntryView>> execute(String[] input, ClientSession session) {
        if (!Validator.hasArgs(input, CommandType.TOP.getCommandArgs())) {
            return CommandResponse.error(StatusCode.INVALID_ARGUMENTS, TOP_COMMAND_INVALID_USAGE_COMMAND);
        }
        if (!session.isLoggedIn()) {
            return CommandResponse.error(StatusCode.UNAUTHORIZED, TOP_COMMAND_LOGIN_FIRST);
        }

        int number;
        try {
            number = Integer.parseInt(input[NUMBER_INDEX]);
        } catch (NumberFormatException e) {
            return CommandResponse.error(StatusCode.INVALID_ARGUMENTS,
                    TOP_COMMAND_INVALID_NUMBER_GIVEN + TOP_COMMAND_INVALID_USAGE_COMMAND);
        }
        if (number <= 0) {
            return CommandResponse.error(StatusCode.INVALID_ARGUMENTS,
                    TOP_COMMAND_INVALID_NUMBER_GIVEN + TOP_COMMAND_INVALID_USAGE_COMMAND);
        }
        Map<String, Long> plays = this.stats.getPlays();
        if (plays.isEmpty()) {
            return CommandResponse.error(StatusCode.NOT_FOUND, TOP_COMMAND_NO_PLAYS);
        }

        Collection<TopEntryView> result = plays.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(number)
                .map(e -> new TopEntryView(e.getKey(), e.getValue())).toList();
        return CommandResponse.success(TOP_COMMAND_SUCCESSFUL, result);
    }
}
