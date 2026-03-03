package server.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import server.exception.checked.DataLoadException;
import server.model.song.Song;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SongRepositoryTest {
    @TempDir
    private Path tempDir;

    private static SongRepository songRepository;

    @BeforeEach
    void setUp() {
        songRepository = new SongRepository(tempDir.toString());
    }

    private void createWav(String fileName) throws IOException {
        Files.write(tempDir.resolve(fileName), new byte[]{0, 1, 2});
    }

    @Test
    void testLoadSongsDirectoryEmptyNoSongs() throws DataLoadException {
        songRepository.loadSongs();

        assertNull(songRepository.findById("anything"), "No songs should be loaded");
    }

    @Test
    void testLoadSongsOnlyWavFilesAndParsedSuccessfullyArtistAndTitle() throws DataLoadException, IOException {
        createWav("Justin-Baby.wav");
        createWav("NoDash.wav");
        Files.write(tempDir.resolve("ignore.txt"), "x".getBytes());

        songRepository.loadSongs();

        Song firstSong = songRepository.findById("Justin-Baby");
        assertNotNull(firstSong, "First song should not be null");
        assertEquals("Justin", firstSong.getArtist(), "First song artist should be equal to Justin");
        assertEquals("Baby", firstSong.getTitle(), "First song title should be equal to Baby");

        Song secondSong = songRepository.findById("NoDash");
        assertNotNull(secondSong, "Second song should not be null");
        assertEquals("Unknown", secondSong.getArtist(), "Artist should be default when no dash");
        assertEquals("NoDash", secondSong.getTitle(), "Second song title should be equal to NoDash");

        Song emptySong = songRepository.findById("ignore");
        assertNull(emptySong, "EmptySong should be equal to null when extension is not .wav");
    }

    @Test
    void testFindByIdIsCaseInsensitive() throws DataLoadException, IOException {
        createWav("Justin-Baby.wav");
        songRepository.loadSongs();

        assertNotNull(songRepository.findById("justin-baby"),
                "Song found by justin-baby should not be null");
        assertNotNull(songRepository.findById("JUSTIN-BABY"),
                "Song found by JUSTIN-BABY should not be null");
    }

    @Test
    void testSearchNullReturnEmptyList() {
        assertTrue(songRepository.search((String[]) null).isEmpty(),
                "Empty list should be returned when null");
    }

    @Test
    void testSearchEmptyReturnEmptyList() {
        assertTrue(songRepository.search().isEmpty(),
                "Empty list should be returned when empty");
    }

    @Test
    void testSearchIgnoreNullAndBlankWords() throws DataLoadException, IOException {
        createWav("Justin-Baby.wav");
        songRepository.loadSongs();

        List<Song> result = songRepository.search("   ", null, "Justin");
        assertEquals(1, result.size(), "Result size should be equal to 1");
        assertEquals("Justin-Baby", result.get(0).getId(),
                "Result first song should be equal to Justin-Baby");
    }

    @Test
    void tesSearchRequireAllWordsToMatchEitherArtistOrTitle() throws Exception {
        createWav("Justin-Baby.wav");
        createWav("Djani-SamSam.wav");
        songRepository.loadSongs();

        List<Song> result = songRepository.search("justin", "sam");
        assertTrue(result.isEmpty(),
                "Result should be empty when not all words matched either artist or title");
    }
}
