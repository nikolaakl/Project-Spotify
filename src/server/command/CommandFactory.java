package server.command;

import server.command.impl.AddSongToCommand;
import server.command.impl.Command;
import server.command.impl.CreatePlaylistCommand;
import server.command.impl.LogoutCommand;
import server.command.impl.LoginCommand;
import server.command.impl.PlayCommand;
import server.command.impl.RegisterCommand;
import server.command.impl.SearchCommand;
import server.command.impl.ShowPlaylistCommand;
import server.command.impl.StopCommand;
import server.command.impl.StreamCommand;
import server.command.impl.TopCommand;
import server.model.stats.Stats;
import server.model.user.Users;
import server.repository.SongRepository;

public final class CommandFactory {
    private CommandFactory() {
    }

    public static Command of(String inputCommand, Users users, SongRepository songCatalog, Stats stats) {
        CommandType commandType = CommandType.getCommandTypeByString(inputCommand);

        if (commandType == null) {
            return null;
        }

        return switch (commandType) {
            case LOGIN-> new LoginCommand(users);
            case REGISTER -> new RegisterCommand(users);
            case LOGOUT -> new LogoutCommand();
            case SEARCH -> new SearchCommand(songCatalog);
            case TOP -> new TopCommand(stats);
            case CREATE_PLAYLIST -> new CreatePlaylistCommand();
            case ADD_SONG_TO -> new AddSongToCommand(songCatalog);
            case SHOW_PLAYLIST -> new ShowPlaylistCommand();
            case PLAY -> new PlayCommand(songCatalog, stats);
            case STOP -> new StopCommand();
            case STREAM -> new StreamCommand(songCatalog);
        };
    }
}