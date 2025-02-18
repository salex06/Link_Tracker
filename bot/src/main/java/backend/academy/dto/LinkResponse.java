package backend.academy.dto;

public record LinkResponse(Long id, String url) {
    public LinkResponse() {
        this(null, null);
    }
}
