package server.model.song;

import java.util.HashSet;
import java.util.Set;

public class Songs {
    private final Set<Song> songs;

    public Songs() {
        this.songs = new HashSet<>();
    }

    public void addSong(Song song) {
        this.songs.add(song);
    }

    public void clear() {
        this.songs.clear();
    }

    public Set<Song> getSongs() {
        return Set.copyOf(this.songs);
    }
}
