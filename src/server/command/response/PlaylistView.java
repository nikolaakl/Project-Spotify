package server.command.response;

import java.util.Collection;

public record PlaylistView(String name, Collection<String> songs) {
}