package eva.platzda.backend.error_handling;


public class NotEnoughCapacityException extends RuntimeException {

    public  NotEnoughCapacityException() {
        super("Not enough capacity at that point in time.");

    }

    public NotEnoughCapacityException(String message) {
        super(message);
    }
}
