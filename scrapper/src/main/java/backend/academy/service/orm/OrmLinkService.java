package backend.academy.service.orm;

import backend.academy.model.mapper.link.LinkMapper;
import backend.academy.model.orm.OrmChat;
import backend.academy.model.orm.OrmChatLink;
import backend.academy.model.orm.OrmChatLinkFilters;
import backend.academy.model.orm.OrmChatLinkIdEmbedded;
import backend.academy.model.orm.OrmChatLinkTags;
import backend.academy.model.orm.OrmLink;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import backend.academy.repository.orm.OrmChatLinkFiltersRepository;
import backend.academy.repository.orm.OrmChatLinkRepository;
import backend.academy.repository.orm.OrmChatLinkTagsRepository;
import backend.academy.repository.orm.OrmChatRepository;
import backend.academy.repository.orm.OrmLinkRepository;
import backend.academy.service.LinkService;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для реализации бизнес логики взаимодействия с ссылками. Взаимодействует с базой данных на основа JPA
 * (Hibernate)
 */
@Service("ormLinkService")
@ConditionalOnProperty(prefix = "service", name = "access-type", havingValue = "ORM")
public class OrmLinkService implements LinkService {
    private final OrmLinkRepository linkRepository;
    private final OrmChatRepository chatRepository;
    private final OrmChatLinkRepository chatLinkRepository;
    private final OrmChatLinkTagsRepository chatLinkTagsRepository;
    private final OrmChatLinkFiltersRepository chatLinkFiltersRepository;
    private final LinkMapper mapper;

    public OrmLinkService(
            OrmLinkRepository linkRepository,
            OrmChatRepository chatRepository,
            OrmChatLinkRepository chatLinkRepository,
            OrmChatLinkTagsRepository chatLinkTagsRepository,
            OrmChatLinkFiltersRepository chatLinkFiltersRepository,
            LinkMapper mapper) {
        this.linkRepository = linkRepository;
        this.chatRepository = chatRepository;
        this.chatLinkRepository = chatLinkRepository;
        this.chatLinkTagsRepository = chatLinkTagsRepository;
        this.chatLinkFiltersRepository = chatLinkFiltersRepository;
        this.mapper = mapper;
    }

    @Override
    public Page<Link> getAllLinks(Pageable pageable, Duration duration) {
        Instant now = Instant.now();
        Instant timeFilter = now.minus(duration);

        List<Link> plainLinks = new ArrayList<>();
        Page<OrmLink> links = linkRepository.findAll(timeFilter, pageable);

        for (OrmLink link : links) {
            Set<Long> primaryChatIds = chatLinkRepository.findAllChatIdByLinkId(link.getId());
            Set<Long> chatIds = chatRepository.findAllById(primaryChatIds).stream()
                    .map(OrmChat::getChatId)
                    .collect(Collectors.toSet());
            plainLinks.add(mapper.toPlainLink(link, null, null, chatIds));
        }

        return new PageImpl<>(plainLinks, pageable, links.getTotalElements());
    }

    @Override
    public Optional<Link> getLink(Long chatId, String linkValue) {
        Optional<OrmChat> chat = chatRepository.findByChatId(chatId);
        if (chat.isEmpty()) {
            return Optional.empty();
        }
        Optional<OrmLink> result = chatLinkRepository.findByChatPrimaryIdAndLinkValue(
                chat.orElseThrow().getId(), linkValue);
        if (result.isEmpty()) {
            return Optional.empty();
        }

        Set<Long> primaryChatIds =
                chatLinkRepository.findAllChatIdByLinkId(result.orElseThrow().getId());
        Set<Long> tgChatIds = chatRepository.findAllById(primaryChatIds).stream()
                .map(OrmChat::getChatId)
                .collect(Collectors.toSet());

        List<String> tags = chatLinkTagsRepository.findTagValuesByChatPrimaryIdAndLinkId(
                chat.orElseThrow().getId(), result.orElseThrow().getId());

        List<String> filters = chatLinkFiltersRepository.findFilterValuesByChatIdAndLinkId(
                chat.orElseThrow().getId(), result.orElseThrow().getId());

        return Optional.of(mapper.toPlainLink(result.orElseThrow(), tags, filters, tgChatIds));
    }

    @Override
    @Transactional
    public Link saveLink(Link link, TgChat chat) {
        if (!chatRepository.existsById(chat.internalId())) {
            return null;
        }
        OrmLink savedLink = linkRepository
                .findByLinkValue(link.getUrl())
                .orElseGet(() -> linkRepository.save(mapper.toOrmLink(link)));
        OrmChat savedChat = chatRepository.findByChatId(chat.chatId()).orElseGet(() -> null);
        if (savedChat == null) {
            return null;
        }

        if (!chatLinkRepository.existsById(new OrmChatLinkIdEmbedded(savedChat.getId(), savedLink.getId()))) {
            chatLinkRepository.save(new OrmChatLink(savedChat, savedLink));
        }

        chatLinkTagsRepository.deleteByChatPrimaryIdAndLinkId(savedChat.getId(), savedLink.getId());
        for (String tag : new HashSet<>(link.getTags())) {
            chatLinkTagsRepository.save(new OrmChatLinkTags(savedChat, savedLink, tag));
        }

        chatLinkFiltersRepository.deleteByChatPrimaryIdAndLinkId(savedChat.getId(), savedLink.getId());
        for (String filter : new HashSet<>(link.getFilters())) {
            chatLinkFiltersRepository.save(new OrmChatLinkFilters(savedChat, savedLink, filter));
        }

        link.setId(savedLink.getId());

        link.setTags(
                chatLinkTagsRepository.findTagValuesByChatPrimaryIdAndLinkId(savedChat.getId(), savedLink.getId()));
        link.setFilters(
                chatLinkFiltersRepository.findFilterValuesByChatIdAndLinkId(savedChat.getId(), savedLink.getId()));

        return link;
    }

    @Override
    public Set<Link> getAllLinksByChatId(Long chatId) {
        Set<Link> plainLinks = new HashSet<>();
        Optional<OrmChat> chat = chatRepository.findByChatId(chatId);
        if (chat.isEmpty()) {
            return Set.of();
        }

        List<OrmLink> ormLinks =
                chatLinkRepository.findAllByChatPrimaryId(chat.orElseThrow().getId());

        for (OrmLink link : ormLinks) {
            List<String> tags = chatLinkTagsRepository.findTagValuesByChatPrimaryIdAndLinkId(
                    chat.orElseThrow().getId(), link.getId());
            List<String> filters = chatLinkFiltersRepository.findFilterValuesByChatIdAndLinkId(
                    chat.orElseThrow().getId(), link.getId());
            Set<Long> primaryChatIds = chatLinkRepository.findAllChatIdByLinkId(link.getId());
            Set<Long> tgChatIds = chatRepository.findAllById(primaryChatIds).stream()
                    .map(OrmChat::getChatId)
                    .collect(Collectors.toSet());

            plainLinks.add(mapper.toPlainLink(link, tags, filters, tgChatIds));
        }
        return plainLinks;
    }

    @Override
    public Set<Long> getChatIdsListeningToLink(String url) {
        OrmLink link = linkRepository.findByLinkValue(url).orElseGet(() -> null);
        if (link == null) {
            return Set.of();
        }
        Set<Long> primaryChatIds = chatLinkRepository.findAllChatIdByLinkId(link.getId());
        return chatRepository.findAllById(primaryChatIds).stream()
                .map(OrmChat::getChatId)
                .collect(Collectors.toSet());
    }

    @Override
    public void updateLastUpdateTime(Link link, Instant updateTime) {
        link.setLastUpdateTime(updateTime);
        Optional<OrmLink> ormLink = linkRepository.findByLinkValue(link.getUrl());
        if (ormLink.isEmpty()) {
            return;
        }
        linkRepository.updateLink(ormLink.orElseThrow().getId(), link.getUrl(), link.getLastUpdateTime());
    }

    @Override
    public List<Link> getAllLinksByChatIdAndTag(Long chatId, String tag) {
        Optional<OrmChat> ormChat = chatRepository.findByChatId(chatId);
        if (ormChat.isEmpty()) {
            return List.of();
        }
        Long internalChatId = ormChat.orElseThrow().getId();

        List<Link> plainLinks = new ArrayList<>();

        List<Long> linkIds = chatLinkTagsRepository.findLinkIdsByChatIdAndTagValue(internalChatId, tag);
        List<OrmLink> ormLinks = linkRepository.findAllById(linkIds);
        for (OrmLink link : ormLinks) {
            List<String> tags =
                    chatLinkTagsRepository.findTagValuesByChatPrimaryIdAndLinkId(internalChatId, link.getId());
            List<String> filters =
                    chatLinkFiltersRepository.findFilterValuesByChatIdAndLinkId(internalChatId, link.getId());
            Set<Long> chatIds = chatLinkRepository.findAllChatIdByLinkId(link.getId());

            plainLinks.add(mapper.toPlainLink(link, tags, filters, chatIds));
        }

        return plainLinks;
    }
}
