package server.command.impl;

import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.exception.checked.DataLoadException;
import server.exception.checked.DataSaveException;
import server.model.song.Song;
import server.util.Logger;
import server.validation.Validator;
import server.command.CommandType;
import server.repository.PlaylistsRepository;
import server.repository.SongRepository;
import server.session.ClientSession;

public final class AddSongToCommand implements Command {
    private static final String ADD_SONG_TO_COMMAND_INVALID_COMMAND_USAGE =
            "Usage: add-song-to <name_of_the_playlist> -- <songId>";
    private static final String ADD_SONG_TO_COMMAND_LOGIN_FIRST = "You must be logged in";
    private static final String ADD_SONG_T0_COMMAND_PLAYLIST_NOT_FOUND = "Playlist %s not found";
    private static final String ADD_SONG_T0_COMMAND_SONG_ALREADY_EXIST = "Song %s already exist in the playlist %s";
    private static final String ADD_SONG_TO_COMMAND_SONG_DOES_NOT_EXIST =
            "Song %s does not exist in the database songs";
    private static final String ADD_SONG_TO_COMMAND_SONG_ADDED_SUCCESSFULLY =
            "Song %s has been successfully added to playlist %s";
    private static final short PLAYLIST_NAME_INDEX = 0;
    private static final short SONG_NAME_INDEX = 1;
    private static final short NEEDED_SPLIT_LIMIT = 2;
    private static final String ERROR_MESSAGE = "Unable to add song %s to playlist %s by user %s";
    private static final String ADD_SONG_TO_COMMAND_ERROR_MESSAGE = "Unable to add song %s to playlist %s. " +
            "Try again later or contact administrator by providing the logs in %s";
    private static final String REGEX_WHITE_SPACES = "\\s*";
    private static final String REGEX_PLAYLIST_SONG_SPLIT_SEPARATOR = "\\s+--\\s+";
    private static final String SPACE_DELIMITER = " ";
    private static final String DASH_SYMBOL = "-";
    private static final String EMPTY_REPLACEMENT = "";

    private final SongRepository songRepository;
    private final PlaylistsRepository playlistsRepository;

    public AddSongToCommand(SongRepository songRepository, PlaylistsRepository playlistsRepository) {
        this.songRepository = songRepository;
        this.playlistsRepository = playlistsRepository;
    }

    public AddSongToCommand(SongRepository songRepository) {
        this(songRepository, new PlaylistsRepository());
    }

    @Override
    public CommandResponse<Void> execute(String[] input, ClientSession session) {
        if (!Validator.hasArgs(input, CommandType.ADD_SONG_TO.getCommandArgs())) {
            return CommandResponse.error(StatusCode.INVALID_ARGUMENTS, ADD_SONG_TO_COMMAND_INVALID_COMMAND_USAGE);
        }
        if (!session.isLoggedIn()) {
            return CommandResponse.error(StatusCode.UNAUTHORIZED, ADD_SONG_TO_COMMAND_LOGIN_FIRST);
        }

        String[] parsed = parsePlaylistAndSong(input);
        if (parsed == null) {
            return CommandResponse.error(StatusCode.INVALID_ARGUMENTS, ADD_SONG_TO_COMMAND_INVALID_COMMAND_USAGE);
        }

        String playlistName = parsed[PLAYLIST_NAME_INDEX];
        String songId = parsed[SONG_NAME_INDEX];
        Song song = this.songRepository.findById(songId);
        if (Validator.isNullObject(song)) {
            return CommandResponse.error(StatusCode.NOT_FOUND,
                    ADD_SONG_TO_COMMAND_SONG_DOES_NOT_EXIST.formatted(songId));
        }

        String email = session.getLoggedUser().getEmail();
        try {
            return addSong(email, playlistName, song);
        } catch (DataLoadException | DataSaveException e) {
            Logger.log(ERROR_MESSAGE.formatted(songId, playlistName, email), e);
            return CommandResponse.error(StatusCode.SERVER_ERROR,
                    ADD_SONG_TO_COMMAND_ERROR_MESSAGE.formatted(songId, playlistName, Logger.getLogPath()));
        }
    }

    private CommandResponse<Void> addSong(String email, String playlistName, Song song)
            throws DataLoadException, DataSaveException {
        Boolean addedSong = this.playlistsRepository.addSongToPlaylist(email, playlistName, song.getId());
        if (Validator.isNullObject(addedSong)) {
            return CommandResponse.error(StatusCode.NOT_FOUND,
                    ADD_SONG_T0_COMMAND_PLAYLIST_NOT_FOUND.formatted(playlistName));
        }

        if (!addedSong) {
            return CommandResponse.error(StatusCode.ALREADY_EXISTS,
                    ADD_SONG_T0_COMMAND_SONG_ALREADY_EXIST.formatted(song.getId(), playlistName));
        }

        return CommandResponse.success(
                ADD_SONG_TO_COMMAND_SONG_ADDED_SUCCESSFULLY.formatted(song.getId(), playlistName), null);
    }

    private String[] parsePlaylistAndSong(String[] input) {
        String fullLine = String.join(SPACE_DELIMITER, input).strip();
        String[] parts = fullLine.split(REGEX_PLAYLIST_SONG_SPLIT_SEPARATOR, NEEDED_SPLIT_LIMIT);
        if (parts.length < NEEDED_SPLIT_LIMIT) {
            return null;
        }

        String playlistName = parts[PLAYLIST_NAME_INDEX].strip();
        String songRaw = parts[SONG_NAME_INDEX].strip();

        if (Validator.isEmptyString(playlistName) || Validator.isEmptyString(songRaw)
                || !songRaw.contains(DASH_SYMBOL)) {
            return null;
        }

        String songId = songRaw.replaceAll(REGEX_WHITE_SPACES, EMPTY_REPLACEMENT);

        return new String[]{playlistName, songId};
    }
}
