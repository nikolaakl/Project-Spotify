package server.exception.checked;

import server.exception.SpotifyException;

public class DataLoadException extends SpotifyException {
    public DataLoadException(String message) {
        super(message);
    }

    public DataLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
