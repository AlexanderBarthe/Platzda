package eva.platzda.backend.error_handling;

public class BadRequestBodyException extends RuntimeException {
    public BadRequestBodyException(String message) {
        super(message);
    }
}
