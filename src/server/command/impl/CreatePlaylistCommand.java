package server.command.impl;

import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.exception.checked.DataSaveException;
import server.util.Logger;
import server.validation.Validator;
import server.command.CommandType;
import server.repository.PlaylistsRepository;
import server.session.ClientSession;

public final class CreatePlaylistCommand implements Command {
    private static final String CREATE_PLAYLIST_COMMAND_INVALID_COMMAND_USAGE =
            "Usage: create-playlist <name_of_the_playlist>";
    private static final String CREATE_PLAYLIST_COMMAND_LOGIN_FIRST = "You must be logged in";
    private static final String CREATE_PLAYLIST_COMMAND_PLAYLIST_ALREADY_EXISTS = "Playlist %s already exists";
    private static final String CREATE_PLAYLIST_COMMAND_PLAYLIST_CREATED_SUCCESSFULLY =
            "Playlist %s has been successfully created";
    private static final short PLAYLIST_NAME_INDEX = 0;
    private static final String ERROR_MESSAGE = "Unable to create playlist %s by user %s";
    private static final String CREATE_PLAYLIST_COMMAND_ERROR_MESSAGE = "Unable to create playlist %s " +
            "Try again later or contact administrator by providing the logs in %s";
    private final PlaylistsRepository playlistsRepository;

    public CreatePlaylistCommand(PlaylistsRepository playlistsRepository) {
        this.playlistsRepository = playlistsRepository;
    }

    public CreatePlaylistCommand() {
        this(new PlaylistsRepository());
    }

    @Override
    public CommandResponse<Void> execute(String[] input, ClientSession session) {
        if (!Validator.hasArgs(input, CommandType.CREATE_PLAYLIST.getCommandArgs())) {
            return CommandResponse.error(StatusCode.INVALID_ARGUMENTS, CREATE_PLAYLIST_COMMAND_INVALID_COMMAND_USAGE);
        }
        if (!session.isLoggedIn()) {
            return CommandResponse.error(StatusCode.UNAUTHORIZED, CREATE_PLAYLIST_COMMAND_LOGIN_FIRST);
        }

        String playlistName = String.join(" ", input).strip();
        if (Validator.isEmptyString(playlistName)) {
            return CommandResponse.error(StatusCode.INVALID_ARGUMENTS, CREATE_PLAYLIST_COMMAND_INVALID_COMMAND_USAGE);
        }

        String email = session.getLoggedUser().getEmail();
        try {
            boolean createdPlaylist = this.playlistsRepository.createPlaylistIfAbsent(email, playlistName);
            if (!createdPlaylist) {
                return CommandResponse.error(StatusCode.ALREADY_EXISTS,
                        CREATE_PLAYLIST_COMMAND_PLAYLIST_ALREADY_EXISTS.formatted(playlistName));
            }

            return CommandResponse.success(
                    CREATE_PLAYLIST_COMMAND_PLAYLIST_CREATED_SUCCESSFULLY.formatted(playlistName), null);
        } catch (DataSaveException e) {
            Logger.log(ERROR_MESSAGE.formatted(playlistName, email), e);
            return CommandResponse.error(
                    StatusCode.SERVER_ERROR,
                    CREATE_PLAYLIST_COMMAND_ERROR_MESSAGE.formatted(playlistName, Logger.getLogPath()));
        }
    }
}
