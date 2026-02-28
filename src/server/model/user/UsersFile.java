package server.model.user;

import java.util.Set;

public class UsersFile {
    private final Set<User> usersFile;

    public UsersFile(Set<User> usersFile) {
        this.usersFile = usersFile;
    }

    public Set<User> getUsersFile() {
        return this.usersFile;
    }
}
