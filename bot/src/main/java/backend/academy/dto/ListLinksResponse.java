package backend.academy.dto;

import backend.academy.model.Link;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** DTO для передачи набора отслеживаемых ссылок */
public class ListLinksResponse {
    /** Набор ссылок на ресурсы */
    List<Link> links;
    /** Количество ссылок в DTO */
    Integer size;

    public ListLinksResponse() {
        links = new ArrayList<>();
        size = 0;
    }

    public ListLinksResponse(List<Link> links, Integer size) {
        this.links = links;
        this.size = size;
    }

    public List<Link> getLinks() {
        return links;
    }

    public Integer getSize() {
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListLinksResponse that)) return false;
        return Objects.equals(links, that.links) && Objects.equals(size, that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(links, size);
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
