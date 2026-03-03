package server.command.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.command.response.CommandResponse;
import server.command.response.StatusCode;
import server.exception.checked.DataLoadException;
import server.exception.checked.DataSaveException;
import server.model.song.Song;
import server.model.user.User;
import server.repository.PlaylistsRepository;
import server.repository.SongRepository;
import server.session.ClientSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AddSongToCommandTest {
    private static PlaylistsRepository playlistsRepository;
    private static SongRepository songRepository;
    private static ClientSession session;
    private static AddSongToCommand command;

    @BeforeEach
    void setUp() {
        playlistsRepository = mock(PlaylistsRepository.class);
        songRepository = mock(SongRepository.class);
        session = mock(ClientSession.class);
        command = new AddSongToCommand(songRepository, playlistsRepository);
    }

    @Test
    void testExecuteNoArgumentsReturnUsage() {
        CommandResponse<Void> result = command.execute(new String[0], session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when no arguments");
    }

    @Test
    void testExecuteNullArgumentsReturnUsage() {
        CommandResponse<Void> result = command.execute(null, session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when NULL arguments");
    }

    @Test
    void testExecuteNotLoggedInUserReturnLoginFirst() {
        when(session.isLoggedIn()).thenReturn(false);

        CommandResponse<Void> result = command.execute(new String[]{"playlist", "--", "song"}, session);

        assertEquals(StatusCode.UNAUTHORIZED, result.getStatus(),
                "Status code should be equal to UNAUTHORIZED when not logged in");
    }

    @Test
    void testExecuteBlankPlaylistOrSongReturnUsage() {
        when(session.isLoggedIn()).thenReturn(true);

        CommandResponse<Void> result = command.execute(new String[]{"   ", "--", "  "}, session);

        assertEquals(StatusCode.INVALID_ARGUMENTS, result.getStatus(),
                "Status code should be equal to INVALID_ARGUMENTS when blank playlist or song");
    }

    @Test
    void testExecuteSongNotInSongsRepositoryReturnSongDoesNotExist() {
        when(session.isLoggedIn()).thenReturn(true);
        when(songRepository.findById("Justin-Baby")).thenReturn(null);

        CommandResponse<Void> result = command.execute(new String[]{"hiphop", "--", "Justin-Baby"}, session);

        assertEquals(StatusCode.NOT_FOUND, result.getStatus(),
                "Status code should be equal to NOT_FOUND when song does not exist");

        verify(songRepository).findById("Justin-Baby");
    }

    @Test
    void testExecutePlaylistNotFoundReturnPlaylistNotFound() throws DataSaveException, DataLoadException {
        when(session.isLoggedIn()).thenReturn(true);
        when(session.getLoggedUser()).thenReturn(new User("a@b.c", "hash"));

        when(songRepository.findById("Justin-Baby")).thenReturn(new Song("Justin-Baby", "Baby", "Justin"));
        when(playlistsRepository.addSongToPlaylist("a@b.c", "pop", "Justin-Baby")).thenReturn(null);

        CommandResponse<Void> result = command.execute(new String[]{"pop", "--", "Justin-Baby"}, session);

        assertEquals(StatusCode.NOT_FOUND, result.getStatus(),
                "Status code should be equal to NOT_FOUND when playlist not found");

        verify(playlistsRepository).addSongToPlaylist("a@b.c", "pop", "Justin-Baby");
    }

    @Test
    void testExecuteSongAlreadyInPlaylistReturnAlreadyExists() throws DataSaveException, DataLoadException {
        when(session.isLoggedIn()).thenReturn(true);
        when(session.getLoggedUser()).thenReturn(new User("a@b.c", "hash"));

        when(songRepository.findById("Justin-Baby")).thenReturn(new Song("Justin-Baby", "Baby", "Justin"));
        when(playlistsRepository.addSongToPlaylist("a@b.c", "My rock", "Justin-Baby")).thenReturn(false);

        CommandResponse<Void> result = command.execute(new String[]{"My rock", "--", "Justin-Baby"}, session);

        assertEquals(StatusCode.ALREADY_EXISTS, result.getStatus(),
                "Status code should be equal to ALREADY_EXISTS when song already exist in the playlist");
    }

    @Test
    void testExecuteMultiWordsSuccessfulAddSongReturnOk() throws DataSaveException, DataLoadException {
        when(session.isLoggedIn()).thenReturn(true);
        when(session.getLoggedUser()).thenReturn(new User("a@b.c", "hash"));

        when(songRepository.findById("Justin-Baby")).thenReturn(new Song("Justin-Baby", "Baby", "Justin"));
        when(playlistsRepository.addSongToPlaylist("a@b.c", "pop", "Justin-Baby")).thenReturn(true);

        CommandResponse<Void> result = command.execute(new String[]{"pop", "--", "Justin ", "- ", "Baby"}, session);

        assertEquals(StatusCode.SUCCESS, result.getStatus(),
                "Status code should be equal to SUCCESS when song added successfully in the playlist");
    }

    @Test
    void testExecuteLoadThrowDataLoadExceptionReturnUserFriendlyError() throws DataSaveException, DataLoadException {
        when(session.isLoggedIn()).thenReturn(true);
        when(session.getLoggedUser()).thenReturn(new User("a@b.c", "hash"));

        when(songRepository.findById("Justin-Baby")).thenReturn(new Song("Justin-Baby", "Baby", "Justin"));
        when(playlistsRepository.addSongToPlaylist("a@b.c", "rock", "Justin-Baby"))
                .thenThrow(new DataLoadException("boom"));

        CommandResponse<Void> result = command.execute(new String[]{"rock", "--", "Justin-Baby"}, session);

        assertEquals(StatusCode.SERVER_ERROR, result.getStatus(),
                "Status code should be equal to SERVER_ERROR when load throw DataLoadException");
    }
}
