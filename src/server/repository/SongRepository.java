package server.repository;

import server.exception.checked.DataLoadException;
import server.validation.Validator;
import server.model.song.Song;
import server.model.song.Songs;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class SongRepository {
    private static final short EXTENSION_WAV_LENGTH = 4;
    private static final short PARTS_LIMIT = 2;
    private static final String ARTIST_TITLE_SEPARATOR = "-";
    private static final String DEFAULT_SONG_TITLE = "Unknown";
    private static final String GLOB_MATCHING_PATTERN = "*.wav";
    private static final short FIRST_PART_INDEX = 0;
    private static final short SECOND_PART_INDEX = 1;
    private static final String SONGS_FAIL_LOAD_MESSAGE = "Failed to load songs file %s";
    private static final String SONG_DIRECTORY_PATH_NOT_BLANK_VALIDATOR_MESSAGE =
            "Songs directory path must not be blank";

    private final Path songsDir;
    private final Songs songs;

    public SongRepository(String dirPath) {
        Validator.requireNotNullOrBlankString(dirPath, SONG_DIRECTORY_PATH_NOT_BLANK_VALIDATOR_MESSAGE);

        this.songsDir = Path.of(dirPath);
        this.songs = new Songs();
    }

    public void loadSongs() throws DataLoadException {
        try {
            this.songs.clear();
            Files.createDirectories(this.songsDir);

            try (DirectoryStream<Path> directoryStream =
                         Files.newDirectoryStream(this.songsDir, GLOB_MATCHING_PATTERN)) {
                for (Path currentPath : directoryStream) {
                    String filename = currentPath.getFileName().toString();
                    String songId = filename.substring(0, filename.length() - EXTENSION_WAV_LENGTH);

                    String artist = DEFAULT_SONG_TITLE;
                    String title = songId;

                    if (songId.contains(ARTIST_TITLE_SEPARATOR)) {
                        String[] parts = songId.split(ARTIST_TITLE_SEPARATOR, PARTS_LIMIT);
                        artist = parts[FIRST_PART_INDEX].strip();
                        title = parts[SECOND_PART_INDEX].strip();
                    }

                    this.songs.addSong(new Song(songId, title, artist));
                }
            }
        } catch (IOException e) {
            throw new DataLoadException(SONGS_FAIL_LOAD_MESSAGE.formatted(this.songsDir), e);
        }
    }

    public List<Song> search(String... requestedWords) {
        if (requestedWords == null || requestedWords.length == 0) {
            return List.of();
        }

        return this.songs.getSongs().stream()
                .filter(currSong -> containAllWords(currSong, requestedWords))
                .toList();
    }

    private boolean containAllWords(Song currentSong, String... requestedWords) {
        return Arrays.stream(requestedWords)
                .filter(word -> word != null && !word.isBlank())
                .map(String::toLowerCase)
                .allMatch(word -> currentSong.getArtist().toLowerCase().contains(word) ||
                        currentSong.getTitle().toLowerCase().contains(word));
    }

    public Song findById(String songId) {
        if (Validator.isNullObject(songId)) {
            return null;
        }

        return this.songs.getSongs().stream()
                .filter(song -> song.getId().equalsIgnoreCase(songId))
                .findFirst()
                .orElse(null);
    }

    public Path getSongsDir() {
        return this.songsDir;
    }
}