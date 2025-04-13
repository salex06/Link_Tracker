package backend.academy.filters;

import backend.academy.dto.LinkUpdateInfo;
import backend.academy.model.plain.Link;
import java.util.List;

public interface LinkFilter {
    List<Long> filterChatIds(LinkUpdateInfo updateInfo, Link link);
}
