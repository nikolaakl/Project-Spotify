package server.command.response.view;

import java.util.Collection;

public record PlaylistView(String name, Collection<String> songs) {
}