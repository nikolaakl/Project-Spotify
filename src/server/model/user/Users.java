package server.model.user;

import server.validation.Validator;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Users {
    private final ConcurrentHashMap<String, User> usersByEmail;

    public Users(Set<User> users) {
        this.usersByEmail = new ConcurrentHashMap<>();
        if (users != null) {
            users.forEach(user -> this.usersByEmail.put(user.getEmail(), user));
        }
    }

    public void addUser(User user) {
        this.usersByEmail.putIfAbsent(user.getEmail(), user);
    }

    public User findUser(String email) {
        return (Validator.isNullObject(email)) ? null : this.usersByEmail.get(email);
    }

    public Set<User> getUsers() {
        return Set.copyOf(this.usersByEmail.values());
    }
}