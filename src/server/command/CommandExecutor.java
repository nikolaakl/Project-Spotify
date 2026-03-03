package server.command;

import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.exception.checked.DataSaveException;
import server.util.Logger;
import server.validation.Validator;
import server.ClientInput;
import server.command.impl.Command;
import server.model.stats.Stats;
import server.model.user.Users;
import server.repository.SongRepository;
import server.repository.StatsRepository;
import server.repository.UsersRepository;
import server.session.ClientSession;

public class CommandExecutor {
    private static final String INVALID_COMMAND_USAGE_MESSAGE = "Invalid command";
    private static final String COMMAND_NOT_NULL_VALIDATOR_MESSAGE = "Command must not be null";
    private static final String SESSION_NOT_NULL_VALIDATOR_MESSAGE = "Session must not be null";
    private static final String SAVE_STATE_FAILED_MESSAGE = "Save state failed for %s";

    private final Users users;
    private final SongRepository songRepository;
    private final Stats stats;

    public CommandExecutor(Users users, Stats stats, SongRepository songRepository) {
        this.users = users;
        this.stats = stats;
        this.songRepository = songRepository;
    }

    public CommandResponse<?> executeCommand(String input, ClientSession session) {
        Validator.requireNotNullOrBlankString(input, COMMAND_NOT_NULL_VALIDATOR_MESSAGE);
        Validator.requireNotNullObject(session, SESSION_NOT_NULL_VALIDATOR_MESSAGE);

        ClientInput parsedInput = ClientInput.of(input);
        CommandType type = CommandType.getCommandTypeByString(parsedInput.commandName());

        Command command = CommandFactory.of(parsedInput.commandName(), this.users,
                this.songRepository, this.stats);

        if (command == null) {
            return CommandResponse.error(StatusCode.NOT_FOUND, INVALID_COMMAND_USAGE_MESSAGE);
        }
        CommandResponse<?> response = command.execute(parsedInput.commandArguments(), session);

        saveStateIfModified(type);
        return response;
    }

    private void saveStateIfModified(CommandType type) {
        try {
            switch (type) {
                case REGISTER -> UsersRepository.saveUsers(users);
                case PLAY -> StatsRepository.saveStats(stats);
            }
        } catch (DataSaveException e) {
            Logger.log(SAVE_STATE_FAILED_MESSAGE.formatted(type), e);
        }
    }

    public Users getUsers() {
        return this.users;
    }

    public Stats getStats() {
        return this.stats;
    }

    public SongRepository getSongRepository() {
        return this.songRepository;
    }
}