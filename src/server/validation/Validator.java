package server.validation;

public final class Validator {
    private Validator() {
    }

    public static void requireNotNullOrBlankString(String s, String message) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void requireNotNullObject(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static boolean hasArgs(String[] args, int expected) {
        if (expected < 0) {
            return args != null && args.length > 0;
        }
        return args != null && args.length == expected;
    }

    public static boolean isNullObject(Object object) {
        return object == null;
    }

    public static boolean isEmptyString(String s) {
        return s == null || s.isBlank();
    }
}
