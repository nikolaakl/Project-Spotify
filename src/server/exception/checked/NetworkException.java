package server.exception.checked;

import server.exception.SpotifyException;

public class NetworkException extends SpotifyException {
    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
