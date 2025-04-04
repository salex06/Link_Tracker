package backend.academy.dto;

import java.util.List;

public record ListLinksResponse(List<backend.academy.model.plain.Link> links, Integer size) {}
