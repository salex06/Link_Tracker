package backend.academy.service.orm;

import backend.academy.model.mapper.chat.ChatMapper;
import backend.academy.model.orm.OrmChat;
import backend.academy.model.orm.OrmChatLink;
import backend.academy.model.orm.OrmChatLinkFilters;
import backend.academy.model.orm.OrmChatLinkIdEmbedded;
import backend.academy.model.orm.OrmChatLinkTags;
import backend.academy.model.orm.OrmChatLinkTagsIdEmbedded;
import backend.academy.model.orm.OrmLink;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import backend.academy.repository.orm.OrmChatLinkFiltersRepository;
import backend.academy.repository.orm.OrmChatLinkRepository;
import backend.academy.repository.orm.OrmChatLinkTagsRepository;
import backend.academy.repository.orm.OrmChatRepository;
import backend.academy.repository.orm.OrmLinkRepository;
import backend.academy.service.ChatService;
import backend.academy.service.LinkService;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service("ormChatService")
@ConditionalOnProperty(prefix = "service", name = "access-type", havingValue = "ORM")
public class OrmChatService implements ChatService {
    private final OrmChatRepository chatRepository;
    private final OrmChatLinkRepository chatLinkRepository;
    private final OrmChatLinkTagsRepository tagsRepository;
    private final OrmChatLinkFiltersRepository filtersRepository;
    private final OrmLinkRepository linkRepository;
    private final ChatMapper mapper;
    private final LinkService linkService;

    public OrmChatService(
            OrmChatRepository chatRepository,
            OrmChatLinkRepository chatLinkRepository,
            OrmChatLinkTagsRepository tagsRepository,
            OrmChatLinkFiltersRepository filtersRepository,
            OrmLinkRepository linkRepository,
            ChatMapper mapper,
            @Qualifier("ormLinkService") LinkService linkService) {
        this.chatRepository = chatRepository;
        this.chatLinkRepository = chatLinkRepository;
        this.tagsRepository = tagsRepository;
        this.filtersRepository = filtersRepository;
        this.linkRepository = linkRepository;
        this.mapper = mapper;
        this.linkService = linkService;
    }

    @Override
    public TgChat saveChat(Long chatId) {
        if (containsChat(chatId)) {
            return null;
        }
        OrmChat chat = chatRepository.save(new OrmChat(null, chatId, null));
        return mapper.toPlainTgChat(chat, new HashSet<>());
    }

    @Override
    public Optional<TgChat> getPlainTgChatByChatId(Long chatId) {
        Optional<TgChat> plainChat = Optional.empty();
        Optional<OrmChat> ormChat = chatRepository.findByChatId(chatId);
        if (ormChat.isPresent()) {
            plainChat =
                    Optional.of(mapper.toPlainTgChat(ormChat.orElseThrow(), linkService.getAllLinksByChatId(chatId)));
        }
        return plainChat;
    }

    @Override
    public boolean containsChat(Long chatId) {
        return chatRepository.existsByChatId(chatId);
    }

    @Override
    public void deleteChatByChatId(Long chatId) {
        chatRepository.deleteByChatId(chatId);
    }

    @Override
    public void updateTags(Link link, TgChat chat, List<String> tags) {
        Optional<OrmLink> ormLink = linkRepository.findByLinkValue(link.getUrl());
        Optional<OrmChat> ormChat = chatRepository.findByChatId(chat.getChatId());
        if (ormChat.isPresent() && ormLink.isPresent()) {
            tagsRepository.deleteByChatPrimaryIdAndLinkId(ormChat.orElseThrow().getId(), link.getId());
            for (String tag : new HashSet<>(tags)) {
                tagsRepository.save(new OrmChatLinkTags(ormChat.orElseThrow(), ormLink.orElseThrow(), tag));
            }
            link.setTags(tags);
            chat.getLinks().stream()
                    .filter(i -> Objects.equals(i.getUrl(), link.getUrl()))
                    .forEach(i -> i.setTags(link.getTags()));
        }
    }

    @Override
    public void updateFilters(Link link, TgChat chat, List<String> filters) {
        Optional<OrmLink> ormLink = linkRepository.findByLinkValue(link.getUrl());
        Optional<OrmChat> ormChat = chatRepository.findByChatId(chat.getChatId());
        if (ormChat.isPresent() && ormLink.isPresent()) {
            filtersRepository.deleteByChatPrimaryIdAndLinkId(
                    ormChat.orElseThrow().getId(), link.getId());
            for (String filter : new HashSet<>(filters)) {
                filtersRepository.save(new OrmChatLinkFilters(ormChat.orElseThrow(), ormLink.orElseThrow(), filter));
            }
            link.setFilters(filters);
            chat.getLinks().stream()
                    .filter(i -> Objects.equals(i.getUrl(), link.getUrl()))
                    .forEach(i -> i.setFilters(link.getFilters()));
        }
    }

    @Override
    public void saveTheChatLink(TgChat chat, Link link) {
        Optional<OrmChat> ormChat = chatRepository.findByChatId(chat.getChatId());
        Optional<OrmLink> ormLink = linkRepository.findByLinkValue(link.getUrl());
        if (ormChat.isPresent()
                && ormLink.isPresent()
                && chat.getLinks().stream()
                        .filter(i -> Objects.equals(i.getUrl(), link.getUrl()))
                        .findAny()
                        .isEmpty()) {
            chatLinkRepository.save(new OrmChatLink(ormChat.orElseThrow(), ormLink.orElseThrow()));
        }
    }

    @Override
    public void removeTheChatLink(TgChat chat, Link link) {
        Optional<OrmChat> ormChat = chatRepository.findByChatId(chat.getChatId());
        Optional<OrmLink> ormLink = linkRepository.findByLinkValue(link.getUrl());
        if (ormChat.isPresent() && ormLink.isPresent()) {
            chatLinkRepository.deleteById(new OrmChatLinkIdEmbedded(
                    ormChat.orElseThrow().getId(), ormLink.orElseThrow().getId()));
            tagsRepository.deleteByChatPrimaryIdAndLinkId(
                    ormChat.orElseThrow().getId(), ormLink.orElseThrow().getId());
            filtersRepository.deleteByChatPrimaryIdAndLinkId(
                    ormChat.orElseThrow().getId(), ormLink.orElseThrow().getId());
        }
    }

    @Override
    public List<String> getTags(Long linkId, Long chatId) {
        Optional<OrmChat> ormChat = chatRepository.findByChatId(chatId);
        if (ormChat.isEmpty()) {
            return List.of();
        }
        return tagsRepository.findTagValuesByChatPrimaryIdAndLinkId(
                ormChat.orElseThrow().getId(), linkId);
    }

    @Override
    public List<String> getFilters(Long linkId, Long chatId) {
        Optional<OrmChat> ormChat = chatRepository.findByChatId(chatId);
        if (ormChat.isEmpty()) {
            return List.of();
        }
        return filtersRepository.findFilterValuesByChatIdAndLinkId(
                ormChat.orElseThrow().getId(), linkId);
    }

    @Override
    public void addTagsToAllLinksByChatId(TgChat tgChat, List<String> tags) {
        OrmChat ormChat = chatRepository.findByChatId(tgChat.getChatId()).orElseThrow();
        List<OrmLink> links = chatLinkRepository.findAllByChatPrimaryId(ormChat.getId());
        for (String tag : tags) {
            for (OrmLink link : links) {
                if (!tagsRepository.existsById(new OrmChatLinkTagsIdEmbedded(ormChat.getId(), link.getId(), tag)))
                    tagsRepository.save(new OrmChatLinkTags(ormChat, link, tag));
            }
        }
    }

    @Override
    public void removeTagsToAllLinksByChatId(TgChat tgChat, List<String> tags) {
        OrmChat ormChat = chatRepository.findByChatId(tgChat.getChatId()).orElseThrow();
        List<OrmLink> links = chatLinkRepository.findAllByChatPrimaryId(ormChat.getId());
        for (String tag : tags) {
            for (OrmLink link : links) {
                if (tagsRepository.existsById(new OrmChatLinkTagsIdEmbedded(ormChat.getId(), link.getId(), tag)))
                    tagsRepository.delete(new OrmChatLinkTags(ormChat, link, tag));
            }
        }
    }

    @Override
    public boolean updateTimeConfig(TgChat tgChat, String timeConfig) {
        LocalTime config;
        if (Objects.equals(timeConfig, "immediately")) {
            config = null;
        } else if (timeConfig.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            String[] splittedConfig = timeConfig.split(":", 2);
            int hours = Integer.parseInt(splittedConfig[0]);
            int minutes = Integer.parseInt(splittedConfig[1]);
            config = LocalTime.of(hours, minutes);
        } else {
            return false;
        }
        chatRepository.updateTimeConfig(tgChat.getChatId(), config);
        return true;
    }
}
