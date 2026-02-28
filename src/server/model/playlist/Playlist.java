package server.model.playlist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class Playlist {
    private final String ownerEmail;
    private final String playlistName;
    private final Collection<String> songIds;

    public Playlist(String ownerEmail, String playlistName) {
        this.ownerEmail = ownerEmail;
        this.playlistName = playlistName;
        this.songIds = new ArrayList<>();
    }

    public void addSong(String song) {
        if (song == null) {
            return;
        }

        this.songIds.add(song);
    }

    public String getPlaylistName() {
        return this.playlistName;
    }

    public Collection<String> getSongs() {
        return this.songIds;
    }

    public String getOwnerEmail() {
        return this.ownerEmail;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Playlist playlist = (Playlist) o;
        return Objects.equals(this.playlistName, playlist.playlistName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.playlistName);
    }
}