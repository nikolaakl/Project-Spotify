package server.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidatorTest {

    @Test
    void testRequireNotNullOrBlankStringNullThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                Validator.requireNotNullOrBlankString(null, "msg"),
                "IllegalArgumentException should be thrown when string is null");
    }

    @Test
    void testRequireNotNullOrBlankStringBlankThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                Validator.requireNotNullOrBlankString("   ", "msg"),
                        "IllegalArgumentException should be thrown when string is blank");
    }

    @Test
    void testRequireNotNullObjectNullThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                Validator.requireNotNullObject(null, "msg"),
                "IllegalArgumentException should be thrown when string is null");
    }

    @Test
    void testHasArgsExactMatchReturnTrue() {
        assertTrue(Validator.hasArgs(new String[]{"a", "b"}, 2),
                "Args should be equal to 2");
    }

    @Test
    void testHasArgsWrongLengthReturnFalse() {
        assertFalse(Validator.hasArgs(new String[]{"a"}, 2),
                "Args should not be equal to 2");
    }

    @Test
    void testHasArgsExpectedNegativeReturnTrueWhenArgsNotEmpty() {
        assertTrue(Validator.hasArgs(new String[]{"a"}, -1),
                "Negative expected return true when args not empty");
    }

    @Test
    void testHasArgsExpectedNegativeReturnFalseWhenArgsEmpty() {
        assertFalse(Validator.hasArgs(new String[]{}, -1),
                "Negative expected return false when args empty");
    }
}
