package server.repository;

import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import server.exception.checked.DataLoadException;
import server.exception.checked.DataSaveException;
import server.model.user.User;
import server.model.user.Users;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UsersRepositoryTest {
    private static Path file;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        file = tempDir.resolve("users.json");
    }

    @Test
    void testLoadUsersFileMissingCreatesAndReturnEmptyUsers() throws DataLoadException {
        assertFalse(Files.exists(file), "File should not exist");

        Users users = UsersRepository.loadUsers(file);

        assertNotNull(users, "Users should not be null");
        assertNotNull(users.getUsers(), "Users set should not be null");
        assertTrue(users.getUsers().isEmpty(), "Users set should be empty");
        assertTrue(Files.exists(file), "File should exist now");
    }

    @Test
    void testSaveUsersNullThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> UsersRepository.saveUsers(file, null),
                "IllegalArgumentException should be thrown when users is null");
    }

    @Test
    void testLoadUsersSuccessfully() throws DataSaveException, DataLoadException {
        Users users = new Users(Set.of(
                new User("a@b.c", "hash1"),
                new User("c@d.e", "hash2")
        ));

        UsersRepository.saveUsers(file, users);
        Users loaded = UsersRepository.loadUsers(file);

        assertEquals(2, loaded.getUsers().size(),
                "Users size should be equal to 2");
        assertTrue(loaded.getUsers().containsAll(users.getUsers()),
                "Loaded users should contain all saved users");
    }

    @Test
    void testLoadUsersCorruptedJsonThrowJsonSyntaxException() throws IOException {
        Files.writeString(file, "{not-valid-json");

        assertThrows(JsonSyntaxException.class, () -> UsersRepository.loadUsers(file),
                "JsonSyntaxException should be thrown when corrupted json");
    }

    @Test
    void testSaveUsersPathIsDirectoryThrowDataSaveException() throws IOException {
        Path dir = tempDir.resolve("users.json");
        Files.createDirectories(dir);

        Users users = new Users(Set.of());
        assertThrows(DataSaveException.class,
                () -> UsersRepository.saveUsers(dir, users),
                "DataSaveException should be thrown when path is directory");
    }
}
