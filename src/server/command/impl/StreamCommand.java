package server.command.impl;

import common.audio.AudioFormatCodec;
import common.net.StreamIO;
import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.validation.Validator;
import server.command.CommandType;
import server.model.song.Song;
import server.repository.SongRepository;
import server.session.ClientSession;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class StreamCommand implements Command {
    public static final String STREAM_TAKEOVER = "Ready to stream";
    private static final String STREAM_COMMAND_INVALID_COMMAND_USAGE = "Usage: stream <song>";
    private static final String STREAM_COMMAND_SONG_NOT_FOUND = "Song %s not found";
    private static final String STREAM_COMMAND_SONG_FILE_MISSING = "Song file %s is missing";
    private static final String WAV_FILE_EXTENSION = ".wav";
    private static final String OK_COMMAND = "OK";
    private static final String STOP_COMMAND = "stop";
    private static final String SEND_COMMAND = "send";
    private static final int CHUNK = 4096;

    private final SongRepository songRepository;

    public StreamCommand(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    @Override
    public CommandResponse<String> execute(String[] input, ClientSession session) {
        if (!Validator.hasArgs(input, CommandType.STREAM.getCommandArgs())) {
            return CommandResponse.error(StatusCode.INVALID_ARGUMENTS, STREAM_COMMAND_INVALID_COMMAND_USAGE);
        }

        String songId = String.join("", input).strip();

        if (Validator.isEmptyString(songId)) {
            return CommandResponse.error(StatusCode.INVALID_ARGUMENTS, STREAM_COMMAND_INVALID_COMMAND_USAGE);
        }

        Song song = this.songRepository.findById(songId);
        if (Validator.isNullObject(song)) {
            return CommandResponse.error(StatusCode.NOT_FOUND,
                    STREAM_COMMAND_SONG_NOT_FOUND.formatted(songId));
        }

        return CommandResponse.success(STREAM_TAKEOVER, song.getId());
    }

    public void stream(SocketChannel socketChannel, String songId) throws IOException, UnsupportedAudioFileException {
        Song song = this.songRepository.findById(songId);
        if (Validator.isNullObject(song)) {
            StreamIO.writeString(socketChannel, STREAM_COMMAND_SONG_NOT_FOUND.formatted(songId));
            return;
        }
        Path wavFile = this.songRepository.getSongsDir().resolve(song.getId() + WAV_FILE_EXTENSION);
        if (!Files.exists(wavFile)) {
            StreamIO.writeString(socketChannel, STREAM_COMMAND_SONG_FILE_MISSING.formatted(wavFile));
            return;
        }

        try (AudioInputStream inputStream = AudioSystem.getAudioInputStream(wavFile.toFile())) {
            StreamIO.writeString(socketChannel, OK_COMMAND);
            StreamIO.writeBytes(socketChannel, AudioFormatCodec.serialize(inputStream.getFormat()));
            streamChunks(socketChannel, inputStream);
        }
    }

    private void streamChunks(SocketChannel socketChannel, AudioInputStream inputStream) throws IOException {
        byte[] buffer = new byte[CHUNK];

        while (true) {
            String command = normalize(StreamIO.readString(socketChannel));
            if (command == null || command.equals(STOP_COMMAND)) {
                return;
            }
            if (!command.equals(SEND_COMMAND)) {
                continue;
            }

            int readBytes = inputStream.read(buffer);
            if (readBytes == -1) {
                StreamIO.writeBytes(socketChannel, new byte[0]);
                return;
            }

            StreamIO.writeBytes(socketChannel, Arrays.copyOf(buffer, readBytes));
        }
    }

    private String normalize(String command) {
        return (command == null) ? null : command.strip().toLowerCase();
    }
}
