package server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ClientInputTest {

    @Test
    void testOfNullThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ClientInput.of(null),
                "IllegalArgumentException should be thrown when input is null");
    }

    @Test
    void testOfBlankThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ClientInput.of("   "),
                "IllegalArgumentException should be thrown when input is blank");
    }

    @Test
    void testOfOnlyCommandEmptyArgs() {
        ClientInput input = ClientInput.of("login");

        assertEquals("login", input.commandName(), "Command name should be equal to login");
        assertNotNull(input.commandArguments(), "Command arguments should not be null");
        assertEquals(0, input.commandArguments().length, "Command arguments should be empty");
    }

    @Test
    void testOfSplitCommandAndArgsSuccessfully() {
        ClientInput input = ClientInput.of("register a@b.c pass");

        assertEquals("register", input.commandName(), "Command name should be equal to register");
        assertArrayEquals(new String[]{"a@b.c", "pass"}, input.commandArguments(),
                "Command arguments should be equal to expected string array");
    }

    @Test
    void testOfSplitAnyWhitespacesSuccessfully() {
        ClientInput input = ClientInput.of("search\tDjani\nsam\t\t\tsam");

        assertEquals("search", input.commandName(), "Command name should be equal to search");
        assertArrayEquals(new String[]{"Djani", "sam", "sam"}, input.commandArguments(),
                "Command arguments should be equal to expected array");
    }

    @Test
    void testOfPreserveDashesAndSymbols() {
        ClientInput input = ClientInput.of("add-song-to my-playlist -- djani - sam sam");

        assertEquals("add-song-to", input.commandName(),
                "Command name should be equal to add-song-to");
        assertArrayEquals(new String[]{"my-playlist", "--", "djani", "-", "sam", "sam"}, input.commandArguments(),
                "Command arguments should be equal to expected array");
    }
}
