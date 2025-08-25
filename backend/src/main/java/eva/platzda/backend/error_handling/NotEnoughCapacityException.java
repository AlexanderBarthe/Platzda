package eva.platzda.backend.error_handling;


public class NotEnoughCapacityException extends RuntimeException {

    public  NotEnoughCapacityException() {
        super("Zur gewuenschten Zeit ist nicht genug Platz verfuegbar");

    }

    public NotEnoughCapacityException(String message) {
        super(message);
    }
}
