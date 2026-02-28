package server.exception.checked;

import server.exception.SpotifyException;

public class DataSaveException extends SpotifyException {
    public DataSaveException(String message) {
        super(message);
    }

    public DataSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
