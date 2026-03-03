package server.repository;

import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import server.exception.checked.DataLoadException;
import server.exception.checked.DataSaveException;
import server.model.stats.Stats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StatsRepositoryTest {
    private static Path file;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        file = tempDir.resolve("stats.json");
    }

    @Test
    void testLoadStatsFileMissingCreatesAndReturnEmptyStats() throws DataLoadException {
        assertFalse(Files.exists(file), "File should not exist");

        Stats stats = StatsRepository.loadStats(file);

        assertNotNull(stats, "Stats should not be null");
        assertNotNull(stats.getPlays(), "Stats plays should not be null");
        assertTrue(Files.exists(file), "File should exist now");
    }

    @Test
    void testSaveStatsNullThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> StatsRepository.saveStats(file, null),
                "IllegalArgumentException should be thrown when stats is null");
    }

    @Test
    void testLoadStatsSuccessfully() throws DataSaveException, DataLoadException {
        Stats stats = new Stats();
        stats.incrementPlays("Justin-Baby");
        stats.incrementPlays("Justin-Baby");
        stats.incrementPlays("Djani-SamSam");

        StatsRepository.saveStats(file, stats);

        Stats loaded = StatsRepository.loadStats(file);

        assertEquals(2L, loaded.getPlays().get("Justin-Baby"),
                "Plays should be equal to 2L");
        assertEquals(1L, loaded.getPlays().get("Djani-SamSam"),
                "Plays should be equal to 1L");
    }

    @Test
    void testLoadStatsCorruptedJsonThrowJsonSyntaxException() throws IOException {
        Files.writeString(file, "{not-valid-json");

        assertThrows(JsonSyntaxException.class, () -> StatsRepository.loadStats(file),
                "JsonSyntaxException should be thrown when corrupted json");
    }

    @Test
    void testSaveStatsPathIsDirectoryThrowDataSaveException() throws IOException {
        Path dir = tempDir.resolve("stats.json");
        Files.createDirectories(dir);

        Stats stats = new Stats();
        assertThrows(DataSaveException.class, () -> StatsRepository.saveStats(dir, stats),
                "DataSaveException should be thrown when path is directory");
    }
}
