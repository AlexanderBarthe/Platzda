package eva.platzda.backend.error_handling;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<String> handleException(BadRequestBodyException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleException(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleBadJson(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;

            //Fetch field name
            String field = "";
            if (ife.getPath() != null && !ife.getPath().isEmpty() && ife.getPath().get(0).getFieldName() != null) {
                field = ife.getPath().get(0).getFieldName();
            }

            String targetType = ife.getTargetType() != null ? ife.getTargetType().getSimpleName() : "the expected type";

            String message;
            if (!field.isEmpty()) {
                message = String.format("Field '%s' must be of type %s.", field, targetType);
            } else {
                message = String.format("The specified value must be of type %s.", targetType);
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Malformed JSON request.");
    }

    @ExceptionHandler
    public ResponseEntity<String> handleException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }




}
