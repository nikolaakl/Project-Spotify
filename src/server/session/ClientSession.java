package server.session;

import server.model.user.User;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientSession {
    private final ByteBuffer buffer;
    private User loggedUser;

    private final AtomicBoolean streaming = new AtomicBoolean(false);

    public ClientSession(int bufferSize) {
        this.buffer = ByteBuffer.allocate(bufferSize);
    }

    public boolean isStreaming() {
        return this.streaming.get();
    }

    public void setStreaming(boolean value) {
        this.streaming.set(value);
    }

    public ByteBuffer getBuffer() {
        return this.buffer;
    }

    public User getLoggedUser() {
        return this.loggedUser;
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;
    }

    public boolean isLoggedIn() {
        return this.loggedUser != null;
    }
}
