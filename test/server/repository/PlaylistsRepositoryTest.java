package server.repository;

import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import server.exception.checked.DataLoadException;
import server.exception.checked.DataSaveException;
import server.model.playlist.Playlist;
import server.util.GsonSingleton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlaylistsRepositoryTest {
    @TempDir
    private Path tempDir;

    private static Path baseDir;
    private static PlaylistsRepository playlistsRepository;
    private static Path file;

    private static final String EMAIL = "a@b.c";
    private static final String PLAYLIST = "My Chill Playlist";
    private static final String SONG = "Justin-Baby";

    private static final String SAFE_REGEX = "[^a-zA-Z0-9._-]";
    private static final String UNDER_DASH_REPLACEMENT = "_";

    @BeforeEach
    void setUp() {
        baseDir = tempDir.resolve("playlists");
        playlistsRepository = new PlaylistsRepository(baseDir);
        file = playlistFile();
    }

    private Path playlistFile() {
        String safeEmail = PlaylistsRepositoryTest.EMAIL.replaceAll(SAFE_REGEX, UNDER_DASH_REPLACEMENT).strip();
        String safePlaylist = PlaylistsRepositoryTest.PLAYLIST.replaceAll(SAFE_REGEX, UNDER_DASH_REPLACEMENT).strip();
        return baseDir.resolve(safeEmail).resolve(safePlaylist + ".json");
    }

    @Test
    void testLoadPlaylistMissingReturnNull() throws DataLoadException {
        Playlist playlist = playlistsRepository.loadPlaylist(EMAIL, PLAYLIST);
        assertNull(playlist, "Playlist should be null when playlist file is missing");
    }

    @Test
    void testLoadPlaylistFileEmptyReturnNull() throws IOException, DataLoadException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, "");

        Playlist loadedPlaylist = playlistsRepository.loadPlaylist(EMAIL, PLAYLIST);
        assertNull(loadedPlaylist, "Loaded playlist should be equal to null");
    }

    @Test
    void testLoadPlaylistJsonCorruptedThrowJsonSyntaxException() throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, "{not-valid-json");

        assertThrows(JsonSyntaxException.class, () -> playlistsRepository.loadPlaylist(EMAIL, PLAYLIST),
                "JsonSyntaxException should be thrown when json corrupted");
    }

    @Test
    void testLoadPlaylistValidJsonReturnPlaylist() throws IOException, DataLoadException {
        Files.createDirectories(file.getParent());

        Playlist playlist = new Playlist(EMAIL, PLAYLIST);
        playlist.addSong(SONG);

        Files.writeString(file, GsonSingleton.getInstance().toJson(playlist));
        Playlist loadedPlaylist = playlistsRepository.loadPlaylist(EMAIL, PLAYLIST);

        assertNotNull(loadedPlaylist, "Loaded should not be null");
        assertEquals(EMAIL, loadedPlaylist.getOwnerEmail(), "Owner email should be equal to EMAIL");
        assertEquals(PLAYLIST, loadedPlaylist.getPlaylistName(), "Playlist should be equal to PLAYLIST");
        assertTrue(loadedPlaylist.getSongs().contains(SONG), "Songs should contain SONG");
    }

    @Test
    void testCreatePlaylistIfAbsentMissingCreateSuccessfullyAndReturnTrue() throws DataSaveException, DataLoadException {
        boolean createdPlaylist = playlistsRepository.createPlaylistIfAbsent(EMAIL, PLAYLIST);
        assertTrue(createdPlaylist, "CreatedPlaylist should be true when playlist is successfully created");

        Playlist loadedPlaylist = playlistsRepository.loadPlaylist(EMAIL, PLAYLIST);
        assertNotNull(loadedPlaylist, "LoadedPlaylist should not be null");
        assertEquals(EMAIL, loadedPlaylist.getOwnerEmail(), "Owner email should be equal to EMAIL");
        assertEquals(PLAYLIST, loadedPlaylist.getPlaylistName(), "Playlist should be equal to PLAYLIST");
    }

    @Test
    void testCreatePlaylistIfAbsentAlreadyExistsAndNotEmptyReturnFalse() throws IOException, DataSaveException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, "{\"email\":\"a@b.c\",\"name\":\"My Chill Playlist\",\"songs\":[]}");

        boolean createdPlaylist = playlistsRepository.createPlaylistIfAbsent(EMAIL, PLAYLIST);
        assertFalse(createdPlaylist, "CreatedPlaylist should be equal to false when playlist already exists");
    }

    @Test
    void testCreatePlaylistIfAbsentPathIsDirectoryThrowDataSaveException() throws IOException {
        Files.createDirectories(file);

        assertThrows(DataSaveException.class, () -> playlistsRepository.createPlaylistIfAbsent(EMAIL, PLAYLIST),
                "DataSaveException should be thrown when path is directory");
    }

    @Test
    void testAddSongToPlaylistPlaylistMissingReturnNull() throws DataSaveException, DataLoadException {
        Boolean result = playlistsRepository.addSongToPlaylist(EMAIL, PLAYLIST, SONG);
        assertNull(result, "Result should be equal to null when playlist is missing");
    }

    @Test
    void testAddSongToPlaylistSongAlreadyExistsReturnFalse() throws IOException, DataLoadException, DataSaveException {
        Files.createDirectories(file.getParent());

        Playlist playlist = new Playlist(EMAIL, PLAYLIST);
        playlist.addSong(SONG);

        Files.writeString(file, GsonSingleton.getInstance().toJson(playlist));

        Boolean result = playlistsRepository.addSongToPlaylist(EMAIL, PLAYLIST, SONG);
        assertEquals(Boolean.FALSE, result, "Result should be FALSE when song already exists");
    }

    @Test
    void testAddSongToPlaylistNewSongAddSuccessfullyAndReturnTrue() throws IOException, DataLoadException, DataSaveException {
        Files.createDirectories(file.getParent());

        Playlist playlist = new Playlist(EMAIL, PLAYLIST);
        Files.writeString(file, GsonSingleton.getInstance().toJson(playlist));

        Boolean result = playlistsRepository.addSongToPlaylist(EMAIL, PLAYLIST, SONG);
        assertEquals(Boolean.TRUE, result, "Result should be TRUE when song is added successfully");

        Playlist loadedPlaylist = playlistsRepository.loadPlaylist(EMAIL, PLAYLIST);
        assertNotNull(loadedPlaylist);
        assertNotNull(loadedPlaylist.getSongs());
        assertTrue(loadedPlaylist.getSongs().contains(SONG));
    }
}
