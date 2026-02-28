package server.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public final class Logger {
    private static final String DIRECTORY = "logs";
    private static final String LOGS_FILE = "app.log";
    private static final Path LOG_PATH = Path.of(DIRECTORY, LOGS_FILE);
    private static final String INITIAL_CONTENT_MESSAGE = "";
    private static final String LOG_FAILED_MESSAGE = "Writing in the log file was not executed successfully";
    private static final String OPEN_BRACE = " [";
    private static final String CLOSE_BRACE = "] ";
    private static final String DASH_SEPARATOR = " - ";
    private static final String LOGS_MESSAGE_SEPARATOR = "------------------------------------------------";

    private Logger() {
    }

    public static Path getLogPath() {
        return LOG_PATH;
    }

    public static void log(String message, Throwable t) {
        try {
            FilesCreator.ensureFile(LOG_PATH, INITIAL_CONTENT_MESSAGE);
            String logEntry = buildLogEntry(message, t);

            try (BufferedWriter writer = Files.newBufferedWriter(LOG_PATH, StandardOpenOption.APPEND)) {
                writer.write(logEntry);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println(LOG_FAILED_MESSAGE);
        }
    }

    private static String buildLogEntry(String message, Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append(LocalDateTime.now()).append(DASH_SEPARATOR)
                .append(OPEN_BRACE).append(Thread.currentThread().getName()).append(CLOSE_BRACE)
                .append(message);

        if (t != null) {
            sb.append(System.lineSeparator()).append(stackTraceToString(t));
        }

        sb.append(System.lineSeparator()).append(LOGS_MESSAGE_SEPARATOR);
        return sb.toString();
    }

    private static String stackTraceToString(Throwable t) {
        StringWriter writer = new StringWriter();
        t.printStackTrace(new PrintWriter(writer));

        return writer.toString();
    }
}
