package server.repository;

import com.google.gson.Gson;
import server.exception.checked.DataLoadException;
import server.exception.checked.DataSaveException;
import server.util.FilesCreator;
import server.validation.Validator;
import server.model.playlist.Playlist;
import server.util.GsonSingleton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

public class PlaylistsRepository {
    private static final String SAFE_REGEX = "[^a-zA-Z0-9._-]";
    private static final String REPLACEMENT_SYMBOL = "_";
    private static final String JSON_EXTENSION = ".json";
    private static final String DATASET_DIR = "dataset";
    private static final String PLAYLISTS_DIR = "playlists";
    private static final Gson GSON = GsonSingleton.getInstance();
    private static final String PLAYLIST_FAIL_LOAD_MESSAGE = "Failed to load playlist file %s";
    private static final String PLAYLIST_FAIL_SAVE_MESSAGE = "Failed to save playlist file %s";
    private static final String EMAIL_NOT_BLANK_VALIDATOR_MESSAGE = "Email must not be blank";
    private static final String SONG_ID_NOT_BLANK_VALIDATOR_MESSAGE = "Playlist name must not be blank";
    private static final String PLAYLIST_NAME_NOT_BLANK_VALIDATOR_MESSAGE = "Song id must not be blank";
    private static final String INITIAL_CONTENT = "";

    private static final ConcurrentHashMap<Path, Object> LOCKS = new ConcurrentHashMap<>();

    private final Path baseDir;

    public PlaylistsRepository() {
        this(Path.of(DATASET_DIR, PLAYLISTS_DIR));
    }

    PlaylistsRepository(Path baseDir) {
        this.baseDir = baseDir;
    }

    private static Object lockForPath(Path file) {
        return LOCKS.computeIfAbsent(file.toAbsolutePath().normalize(), path -> new Object());
    }

    private Path userDir(String email) {
        String safeEmailName = email.replaceAll(SAFE_REGEX, REPLACEMENT_SYMBOL).strip();
        return baseDir.resolve(safeEmailName);
    }

    private Path playlistFile(String email, String playlistName) {
        String safePlaylistName = playlistName.replaceAll(SAFE_REGEX, REPLACEMENT_SYMBOL).strip();
        return userDir(email).resolve(safePlaylistName + JSON_EXTENSION);
    }

    public Playlist loadPlaylist(String email, String playlistName) throws DataLoadException {
        Validator.requireNotNullOrBlankString(email, EMAIL_NOT_BLANK_VALIDATOR_MESSAGE);
        Validator.requireNotNullOrBlankString(playlistName, PLAYLIST_NAME_NOT_BLANK_VALIDATOR_MESSAGE);

        Path file = playlistFile(email, playlistName);
        Object lock = lockForPath(file);

        synchronized (lock) {
            try {
                if (!Files.exists(file) || Files.size(file) == 0) {
                    return null;
                }

                try (BufferedReader reader = Files.newBufferedReader(file)) {
                    return GSON.fromJson(reader, Playlist.class);
                }
            } catch (IOException e) {
                throw new DataLoadException(PLAYLIST_FAIL_LOAD_MESSAGE.formatted(file), e);
            }
        }
    }

    public boolean createPlaylistIfAbsent(String email, String playlistName) throws DataSaveException {
        Validator.requireNotNullOrBlankString(email, EMAIL_NOT_BLANK_VALIDATOR_MESSAGE);
        Validator.requireNotNullOrBlankString(playlistName, PLAYLIST_NAME_NOT_BLANK_VALIDATOR_MESSAGE);

        Path file = playlistFile(email, playlistName);
        Object lock = lockForPath(file);

        synchronized (lock) {
            try {
                if (Files.exists(file) && Files.size(file) > 0) {
                    return false;
                }

                FilesCreator.ensureFile(file, INITIAL_CONTENT);
                try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                    writer.write(GSON.toJson(new Playlist(email, playlistName)));
                }
                return true;
            } catch (IOException e) {
                throw new DataSaveException(PLAYLIST_FAIL_SAVE_MESSAGE.formatted(file), e);
            }
        }
    }

    public Boolean addSongToPlaylist(String email, String playlistName, String songId)
            throws DataLoadException, DataSaveException {
        Validator.requireNotNullOrBlankString(email, EMAIL_NOT_BLANK_VALIDATOR_MESSAGE);
        Validator.requireNotNullOrBlankString(playlistName, PLAYLIST_NAME_NOT_BLANK_VALIDATOR_MESSAGE);
        Validator.requireNotNullOrBlankString(songId, SONG_ID_NOT_BLANK_VALIDATOR_MESSAGE);

        Path file = playlistFile(email, playlistName);
        Object lock = lockForPath(file);

        synchronized (lock) {
            Playlist playlist = loadPlaylist(email, playlistName);
            if (playlist == null) {
                return null;
            }
            if (playlist.getSongs().contains(songId)) {
                return false;
            }

            playlist.addSong(songId);
            try {
                try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                    writer.write(GSON.toJson(playlist));
                }
            } catch (IOException e) {
                throw new DataSaveException(PLAYLIST_FAIL_SAVE_MESSAGE.formatted(file), e);
            }
            return true;
        }
    }
}
