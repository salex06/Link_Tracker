package backend.academy.filters.impl;

import backend.academy.dto.LinkUpdateInfo;
import backend.academy.filters.LinkFilter;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import backend.academy.service.ChatService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LinkFilterByAuthor implements LinkFilter {
    public static final String FILTER_FIELD_NAME = "user";
    private final ChatService chatService;

    @Override
    public List<Long> filterChatIds(LinkUpdateInfo updateInfo, Link link) {
        List<Long> result = new ArrayList<>();

        Set<Long> currentChatIds = link.getTgChatIds();
        for (Long tgChatId : currentChatIds) {
            TgChat chat = chatService.getPlainTgChatByChatId(tgChatId).orElseThrow();
            List<String> filters = chatService.getFilters(link.getId(), chat.getChatId());

            if (!containsForbiddenAuthor(filters, updateInfo)) {
                result.add(tgChatId);
            }
        }
        return result;
    }

    private boolean containsForbiddenAuthor(List<String> filters, LinkUpdateInfo updateInfo) {
        for (String filter : filters) {
            String[] splittedFilter = filter.split(":", 2);
            String filterKey = splittedFilter[0];
            String filterValue = splittedFilter[1];

            if (Objects.equals(filterKey, FILTER_FIELD_NAME) && Objects.equals(updateInfo.authorName(), filterValue)) {
                return true;
            }
        }
        return false;
    }
}
