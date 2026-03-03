package server.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void testHashInputIsValidReturnSha256HexWithLength64() {
        String hash = PasswordHasher.hash("password123");

        assertNotNull(hash, "Hash should not be null");
        assertEquals(64, hash.length(), "SHA-256 hex hash should be 64 characters");
        assertTrue(hash.matches("[0-9a-f]{64}"), "Hash should be lowercase hex");
    }

    @Test
    void testVerifyPasswordMatchesStoredHashReturnTrue() {
        String stored = PasswordHasher.hash("secret");

        assertTrue(PasswordHasher.verify("secret", stored), "Stored hash should be equal to raw password");
    }

    @Test
    void testVerifyPasswordDoesNotMatchStoredHashReturnFalse() {
        String stored = PasswordHasher.hash("secret");

        assertFalse(PasswordHasher.verify("wrong", stored), "Stored hash should not be equal to raw password");
    }

    @Test
    void testHashBlankThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash("   "),
                "IllegalArgumentException should be thrown when input is blank");
    }

    @Test
    void testVerifyRawPasswordBlankThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.verify("   ", "abc"),
                "IllegalArgumentException should be thrown when raw password is blank");
    }

    @Test
    void testVerifyStoredHashBlankThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.verify("abc", "   "),
                "IllegalArgumentException should be thrown when stored hash is blank");
    }
}
