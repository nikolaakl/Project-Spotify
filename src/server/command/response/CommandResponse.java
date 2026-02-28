package server.command.response;

public class CommandResponse<T> {
    private final StatusCode status;
    private final String message;
    private final T payload;

    public CommandResponse(StatusCode status, String message) {
        this(status, message, null);
    }

    public CommandResponse(StatusCode status, String message, T payload) {
        this.status = status;
        this.message = message;
        this.payload = payload;
    }

    public boolean isSuccess() {
        return status == StatusCode.SUCCESS;
    }

    public static <T> CommandResponse<T> success(String message, T payload) {
        return new CommandResponse<>(StatusCode.SUCCESS, message, payload);
    }

    public static <T> CommandResponse<T> error(StatusCode status, String message) {
        if (status == StatusCode.SUCCESS) {
            throw new IllegalArgumentException("Error response must not have SUCCESS status");
        }
        return new CommandResponse<>(status, message, null);
    }

    public StatusCode getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getPayload() {
        return payload;
    }
}