package server.command.impl;

import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.validation.Validator;
import server.command.CommandType;
import server.model.song.Song;
import server.model.stats.Stats;
import server.repository.SongRepository;
import server.session.ClientSession;

public final class PlayCommand implements Command {
    private static final String PLAY_COMMAND_INVALID_COMMAND_USAGE = "Usage: play <song>";
    private static final String PLAY_COMMAND_LOGIN_FIRST = "You must be logged in";
    private static final String PLAY_COMMAND_SONG_NOT_FOUND = "Song %s not found";
    public static final String PLAY_COMMAND_SONG_PLAYED_SUCCESSFULLY = "Streaming started";

    private final SongRepository songRepository;
    private final Stats stats;

    public PlayCommand(SongRepository songRepository, Stats stats) {
        this.songRepository = songRepository;
        this.stats = stats;
    }

    @Override
    public CommandResponse<String> execute(String[] input, ClientSession session) {
        if (!Validator.hasArgs(input, CommandType.PLAY.getCommandArgs())) {
            return CommandResponse.error(StatusCode.INVALID_ARGUMENTS, PLAY_COMMAND_INVALID_COMMAND_USAGE);
        }

        if (!session.isLoggedIn()) {
            return CommandResponse.error(StatusCode.UNAUTHORIZED, PLAY_COMMAND_LOGIN_FIRST);
        }

        String songId = String.join("", input).strip();

        if (Validator.isEmptyString(songId)) {
            return CommandResponse.error(StatusCode.INVALID_ARGUMENTS, PLAY_COMMAND_INVALID_COMMAND_USAGE);
        }

        Song song = songRepository.findById(songId);
        if (Validator.isNullObject(song)) {
            return CommandResponse.error(StatusCode.NOT_FOUND, PLAY_COMMAND_SONG_NOT_FOUND.formatted(songId));
        }

        this.stats.incrementPlays(song.getId());
        session.setStreaming(true);

        return CommandResponse.success(PLAY_COMMAND_SONG_PLAYED_SUCCESSFULLY, song.getId());
    }
}
