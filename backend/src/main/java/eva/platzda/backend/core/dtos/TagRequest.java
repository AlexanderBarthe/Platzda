package eva.platzda.backend.core.dtos;

/**
 * Simple dto for tag creation and deletion
 */
public class TagRequest {

    private String tag;

    public TagRequest() {
    }

    public TagRequest(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
