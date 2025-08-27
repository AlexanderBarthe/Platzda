package eva.platzda.backend.core.dtos;

/**
 * Simple dto for tag creation and deletion with special strings (containing '/' and similar)
 */
public class StringRequest {

    private String string;

    public StringRequest() {
    }

    public StringRequest(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }
}
