package server.command.impl;

import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.validation.Validator;
import server.command.CommandType;
import server.model.song.Song;
import server.repository.SongRepository;
import server.session.ClientSession;

import java.util.Collection;
import java.util.List;

public final class SearchCommand implements Command {
    private static final String SEARCH_COMMAND_INVALID_COMMAND_USAGE = "Usage: search <words>";
    private static final String SEARCH_COMMAND_LOGIN_FIRST = "You must be logged in";
    private static final String SEARCH_COMMAND_NO_SONGS_FOUND = "No songs found";
    private static final String SEARCH_COMMAND_SONGS_FOUND = "Songs found";

    private final SongRepository songRepository;

    public SearchCommand(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    @Override
    public CommandResponse<Collection<String>> execute(String[] input, ClientSession session) {
        if (!Validator.hasArgs(input, CommandType.SEARCH.getCommandArgs())) {
            return CommandResponse.error(StatusCode.INVALID_ARGUMENTS, SEARCH_COMMAND_INVALID_COMMAND_USAGE);
        }

        if (!session.isLoggedIn()) {
            return CommandResponse.error(StatusCode.UNAUTHORIZED, SEARCH_COMMAND_LOGIN_FIRST);
        }

        List<Song> matchedSongs = this.songRepository.search(input);
        if (matchedSongs.isEmpty()) {
            return CommandResponse.error(StatusCode.NOT_FOUND, SEARCH_COMMAND_NO_SONGS_FOUND);
        }

        List<String> songIds = matchedSongs.stream()
                .map(Song::getId)
                .toList();

        return CommandResponse.success(SEARCH_COMMAND_SONGS_FOUND, songIds);
    }
}
