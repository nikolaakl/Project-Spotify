package server.security;

import server.validation.Validator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordHasher {
    private static final String ALGORITHM = "SHA-256";
    private static final String FORMAT = "%02x";
    private static final String ALGORITHM_ERROR_MESSAGE = "SHA-256 algorithm not available";
    private static final String STRING_TO_HASH_NOT_BLANK_VALIDATOR_MESSAGE = "String to hash must not be blank";
    private static final String PASSWORD_NOT_BLANK_VALIDATOR_MESSAGE = "Password must not be blank";
    private static final String STORED_HASH_NOT_BLANK_VALIDATOR_MESSAGE = "Stored hash must not be blank";

    private PasswordHasher() {
    }

    public static String hash(String input) {
        Validator.requireNotNullOrBlankString(input, STRING_TO_HASH_NOT_BLANK_VALIDATOR_MESSAGE);

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM);
            byte[] hashBytes = messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format(FORMAT, b));
            }
            return hex.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(ALGORITHM_ERROR_MESSAGE, e);
        }
    }

    public static boolean verify(String rawPassword, String storedHash) {
        Validator.requireNotNullOrBlankString(rawPassword, PASSWORD_NOT_BLANK_VALIDATOR_MESSAGE);
        Validator.requireNotNullOrBlankString(storedHash, STORED_HASH_NOT_BLANK_VALIDATOR_MESSAGE);

        return hash(rawPassword).equals(storedHash);
    }
}
