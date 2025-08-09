package eva.platzda.backend.dtos;

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
