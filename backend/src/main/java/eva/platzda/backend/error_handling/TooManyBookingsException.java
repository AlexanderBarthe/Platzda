package eva.platzda.backend.error_handling;

public class TooManyBookingsException extends RuntimeException {
    public TooManyBookingsException(String message) {
        super(message);
    }
}
