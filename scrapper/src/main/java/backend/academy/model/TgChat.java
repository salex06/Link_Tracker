package backend.academy.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public class TgChat {
    private final Long id;

    @Setter
    private List<Link> links;

    public void addLink(Link link) {
        links.add(link);
    }
}
