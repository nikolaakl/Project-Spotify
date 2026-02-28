package server.util;

import server.validation.Validator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class FilesCreator {
    private static final String NULL_PATH_MESSAGE = "Path is null";
    private FilesCreator() {
    }

    public static void ensureFile(Path path, String initialContent) throws IOException {
        if (Validator.isNullObject(path)) {
            throw new IllegalArgumentException(NULL_PATH_MESSAGE);
        }

        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        if (!Files.exists(path)) {
            Files.createFile(path);
        }

        if (Files.size(path) == 0 && initialContent != null && !initialContent.isEmpty()) {
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING)) {
                writer.write(initialContent);
            }
        }
    }
}