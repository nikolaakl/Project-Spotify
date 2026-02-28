package server.repository;

import com.google.gson.Gson;
import server.exception.checked.DataLoadException;
import server.exception.checked.DataSaveException;
import server.model.user.User;
import server.model.user.UsersFile;
import server.util.FilesCreator;
import server.validation.Validator;
import server.model.user.Users;
import server.util.GsonSingleton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class UsersRepository {
    private static final String DIRECTORY = "dataset";
    private static final String DB_FILE = "users.json";
    private static final Path USERS_DB = Path.of(DIRECTORY + File.separator + DB_FILE);
    private static final String USERS_FAIL_READ_MESSAGE = "Failed to load users database ";
    private static final String USERS_FAIL_WRITE_MESSAGE = "Failed to save users database ";
    private static final String USERS_NOT_NULL_VALIDATOR_MESSAGE = "Users must not be null";

    private static final String INITIAL_OBJECT = "{\"users\": []}";

    private static final Gson GSON = GsonSingleton.getInstance();

    private static final Object USERS_LOCK = new Object();

    public static Users loadUsers() throws DataLoadException {
        return loadUsers(USERS_DB);
    }

    public static void saveUsers(Users users) throws DataSaveException {
        saveUsers(USERS_DB, users);
    }

    static Users loadUsers(Path file) throws DataLoadException {
        synchronized (USERS_LOCK) {
            try {
                FilesCreator.ensureFile(file, INITIAL_OBJECT);

                try (BufferedReader reader = Files.newBufferedReader(file)) {
                    UsersFile usersFile = GSON.fromJson(reader, UsersFile.class);
                    Set<User> usersSet = (usersFile == null || usersFile.getUsersFile() == null) ?
                            Set.of() : usersFile.getUsersFile();

                    return new Users(usersSet);
                }
            } catch (IOException e) {
                throw new DataLoadException(USERS_FAIL_READ_MESSAGE + file, e);
            }
        }
    }

    static void saveUsers(Path file, Users users) throws DataSaveException {
        Validator.requireNotNullObject(users, USERS_NOT_NULL_VALIDATOR_MESSAGE);

        synchronized (USERS_LOCK) {
            try {
                FilesCreator.ensureFile(file, INITIAL_OBJECT);

                UsersFile usersFile = new UsersFile(users.getUsers());
                String jsonUsers = GSON.toJson(usersFile);

                try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                    writer.write(jsonUsers);
                }
            } catch (IOException e) {
                throw new DataSaveException(USERS_FAIL_WRITE_MESSAGE + file, e);
            }
        }
    }
}
