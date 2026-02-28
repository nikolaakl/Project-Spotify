package server.command;

import java.util.Map;

public enum CommandType {
    LOGIN("login", 2),
    REGISTER("register", 2),
    LOGOUT("logout", 0),
    SEARCH("search", -1),
    TOP("top", 1),
    CREATE_PLAYLIST("create-playlist", -1),
    ADD_SONG_TO("add-song-to", -1),
    SHOW_PLAYLIST("show-playlist", -1),
    PLAY("play", -1),
    STOP("stop", 0),
    STREAM("stream", -1);

    private final String command;
    private final int commandArgs;

    private static final Map<String, CommandType> TYPES = Map.ofEntries(
            Map.entry("login", LOGIN),
            Map.entry("register", REGISTER),
            Map.entry("logout", LOGOUT),
            Map.entry("search", SEARCH),
            Map.entry("top", TOP),
            Map.entry("create-playlist", CREATE_PLAYLIST),
            Map.entry("add-song-to", ADD_SONG_TO),
            Map.entry("show-playlist", SHOW_PLAYLIST),
            Map.entry("play", PLAY),
            Map.entry("stop", STOP),
            Map.entry("stream", STREAM)
    );

    CommandType(String command, int args) {
        this.command = command;
        this.commandArgs = args;
    }

    public String getCommand() {
        return this.command;
    }

    public int getCommandArgs() {
        return this.commandArgs;
    }

    public static CommandType getCommandTypeByString(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }

        return TYPES.get(type.toLowerCase());
    }
}
