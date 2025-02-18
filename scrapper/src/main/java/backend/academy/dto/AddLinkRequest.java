package backend.academy.dto;

public record AddLinkRequest(String url) {
    public AddLinkRequest() {
        this("");
    }
}
