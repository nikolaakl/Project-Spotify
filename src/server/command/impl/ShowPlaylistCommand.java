package server.command.impl;

import server.command.response.CommandResponse;
import server.command.response.PlaylistView;
import server.command.response.StatusCode;
import server.exception.checked.DataLoadException;
import server.util.Logger;
import server.validation.Validator;
import server.command.CommandType;
import server.model.playlist.Playlist;
import server.repository.PlaylistsRepository;
import server.session.ClientSession;

import java.util.Collection;
import java.util.List;

public final class ShowPlaylistCommand implements Command {
    private static final String SHOW_PLAYLIST_COMMAND_INVALID_COMMAND_USAGE =
            "Usage: show-playlist <name_of_the_playlist>";
    private static final String SHOW_PLAYLIST_COMMAND_LOGIN_FIRST = "You must be logged in";
    private static final String SHOW_PLAYLIST_COMMAND_PLAYLIST_NOT_FOUND = "Playlist %s not found";
    private static final String ERROR_MESSAGE = "Unable to show playlist %s by user %s";
    private static final String SHOW_PLAYLIST_COMMAND_SUCCESSFUL = "Playlist loaded";
    private static final String SHOW_PLAYLIST_COMMAND_ERROR_MESSAGE = "Unable to show playlist %s. " +
            "Try again later or contact administrator by providing the logs in %s";

    private final PlaylistsRepository playlistsRepository;

    public ShowPlaylistCommand(PlaylistsRepository playlistsRepository) {
        this.playlistsRepository = playlistsRepository;
    }

    public ShowPlaylistCommand() {
        this(new PlaylistsRepository());
    }

    @Override
    public CommandResponse<PlaylistView> execute(String[] input, ClientSession session) {
        if (!Validator.hasArgs(input, CommandType.SHOW_PLAYLIST.getCommandArgs())) {
            return CommandResponse.error(StatusCode.INVALID_ARGUMENTS, SHOW_PLAYLIST_COMMAND_INVALID_COMMAND_USAGE);
        }
        if (!session.isLoggedIn()) {
            return CommandResponse.error(StatusCode.UNAUTHORIZED, SHOW_PLAYLIST_COMMAND_LOGIN_FIRST);
        }

        String playlistName = String.join(" ", input).strip();
        if (Validator.isEmptyString(playlistName)) {
            return CommandResponse.error(StatusCode.INVALID_ARGUMENTS, SHOW_PLAYLIST_COMMAND_INVALID_COMMAND_USAGE);
        }

        String email = session.getLoggedUser().getEmail();
        try {
            Playlist playlist = this.playlistsRepository.loadPlaylist(email, playlistName);
            if (Validator.isNullObject(playlist)) {
                return CommandResponse.error(StatusCode.NOT_FOUND,
                        SHOW_PLAYLIST_COMMAND_PLAYLIST_NOT_FOUND.formatted(playlistName));
            }
            Collection<String> songs = playlist.getSongs();
            PlaylistView view = new PlaylistView(playlist.getPlaylistName(), (songs == null) ? List.of() : songs);

            return CommandResponse.success(SHOW_PLAYLIST_COMMAND_SUCCESSFUL, view);
        } catch (DataLoadException e) {
            Logger.log(ERROR_MESSAGE.formatted(playlistName, email), e);
            return CommandResponse.error(StatusCode.SERVER_ERROR,
                    SHOW_PLAYLIST_COMMAND_ERROR_MESSAGE.formatted(playlistName, Logger.getLogPath()));
        }
    }
}
