package server.repository;

import com.google.gson.Gson;
import server.exception.checked.DataLoadException;
import server.exception.checked.DataSaveException;
import server.util.FilesCreator;
import server.validation.Validator;
import server.model.stats.Stats;
import server.util.GsonSingleton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StatsRepository {
    private static final String DIRECTORY = "dataset";
    private static final String DB_FILE = "stats.json";
    private static final Path STATS_DB = Path.of(DIRECTORY + File.separator + DB_FILE);
    private static final String STATS_FAIL_READ_MESSAGE = "Failed to load stats database ";
    private static final String STATS_FAIL_WRITE_MESSAGE = "Failed to save stats database ";
    private static final String STATS_NOT_NULL_VALIDATOR_MESSAGE = "Stats must not be null";

    private static final String INITIAL_OBJECT = "{\"plays\": {}}";
    private static final Gson GSON = GsonSingleton.getInstance();

    private static final Object STATS_LOCK = new Object();

    public static Stats loadStats() throws DataLoadException {
        return loadStats(STATS_DB);
    }

    public static void saveStats(Stats stats) throws DataSaveException {
        saveStats(STATS_DB, stats);
    }

    static Stats loadStats(Path file) throws DataLoadException {
        synchronized (STATS_LOCK) {
            try {
                FilesCreator.ensureFile(file, INITIAL_OBJECT);

                try (BufferedReader reader = Files.newBufferedReader(file)) {
                    Stats stats = GSON.fromJson(reader, Stats.class);
                    return (stats != null) ? stats : new Stats();
                }
            } catch (IOException e) {
                throw new DataLoadException(STATS_FAIL_READ_MESSAGE + file, e);
            }
        }
    }

    static void saveStats(Path file, Stats stats) throws DataSaveException {
        Validator.requireNotNullObject(stats, STATS_NOT_NULL_VALIDATOR_MESSAGE);

        synchronized (STATS_LOCK) {
            try {
                FilesCreator.ensureFile(file, INITIAL_OBJECT);

                String jsonStats = GSON.toJson(stats);
                try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                    writer.write(jsonStats);
                }
            } catch (IOException e) {
                throw new DataSaveException(STATS_FAIL_WRITE_MESSAGE + file, e);
            }
        }
    }
}