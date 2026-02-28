package server.model.song;

import java.util.Objects;

public class Song {
    private final String id;
    private final String title;
    private final String artist;

    public Song(String id, String title, String artist) {
        this.id = id;
        this.title = title;
        this.artist = artist;
    }

    public String getArtist() {
        return this.artist;
    }

    public String getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Song song = (Song) o;
        return Objects.equals(this.title, song.title) && Objects.equals(this.artist, song.artist);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.title, this.artist);
    }
}
