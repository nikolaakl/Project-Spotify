package server;

import server.validation.Validator;

import java.util.Arrays;

public record ClientInput(String commandName, String[] commandArguments) {
    private static final String REGEX_SPLIT_SEPARATOR = "\\s+";
    private static final String EMPTY_INPUT_MESSAGE = "Command must not be empty";

    public static ClientInput of(String input) {
        Validator.requireNotNullOrBlankString(input, EMPTY_INPUT_MESSAGE);

        String[] tokens = input.strip().split(REGEX_SPLIT_SEPARATOR);

        String command = tokens[0];
        String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

        return new ClientInput(command, args);
    }
}