package backend.academy.model.mapper.link.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.model.jdbc.JdbcLink;
import backend.academy.model.mapper.link.LinkMapper;
import backend.academy.model.orm.OrmLink;
import backend.academy.model.plain.Link;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class StandardLinkMapperTest {
    private final LinkMapper standardLinkMapper = new StandardLinkMapper();

    @Test
    public void toPlainLinkFromJdbcLinkWorksCorrectly() {
        Long expectedId = 1L;
        String expectedUrl = "url";
        List<String> expectedTags = List.of("tag1", "tag2");
        List<String> expectedFilters = List.of("filter1", "filter2");
        Set<Long> expectedChatIds = Set.of(1L, 2L);
        JdbcLink jdbcLink = new JdbcLink(expectedId, expectedUrl);

        Link actualLink = standardLinkMapper.toPlainLink(jdbcLink, expectedTags, expectedFilters, expectedChatIds);

        assertThat(actualLink).isNotNull();
        assertEquals(expectedId, actualLink.getId());
        assertEquals(expectedTags, actualLink.getTags());
        assertEquals(expectedFilters, actualLink.getFilters());
        assertEquals(expectedChatIds, actualLink.getTgChatIds());
    }

    @Test
    public void toPlainLinkFromOrmLinkWorksCorrectly() {
        Long expectedId = 1L;
        String expectedUrl = "url";
        List<String> expectedTags = List.of("tag1", "tag2");
        List<String> expectedFilters = List.of("filter1", "filter2");
        Set<Long> expectedChatIds = Set.of(1L, 2L);
        OrmLink jdbcLink = new OrmLink(expectedId, expectedUrl, LocalDateTime.now());

        Link actualLink = standardLinkMapper.toPlainLink(jdbcLink, expectedTags, expectedFilters, expectedChatIds);

        assertThat(actualLink).isNotNull();
        assertEquals(expectedId, actualLink.getId());
        assertEquals(expectedUrl, actualLink.getUrl());
        assertEquals(expectedTags, actualLink.getTags());
        assertEquals(expectedFilters, actualLink.getFilters());
        assertEquals(expectedChatIds, actualLink.getTgChatIds());
    }

    @Test
    public void toJdbcLinkWorksCorrectly() {
        Long expectedId = 1L;
        String expectedUrl = "url";
        List<String> expectedTags = List.of("tag1", "tag2");
        List<String> expectedFilters = List.of("filter1", "filter2");
        Set<Long> expectedChatIds = Set.of(1L, 2L);
        Link link = new Link(expectedId, expectedUrl, expectedTags, expectedFilters, expectedChatIds);

        JdbcLink actualLink = standardLinkMapper.toJdbcLink(link);

        assertThat(actualLink).isNotNull();
        assertEquals(expectedId, actualLink.getId());
        assertEquals(expectedUrl, actualLink.getUrl());
    }

    @Test
    public void toOrmLinkWorksCorrectly() {
        Long expectedId = 1L;
        String expectedUrl = "url";
        List<String> expectedTags = List.of("tag1", "tag2");
        List<String> expectedFilters = List.of("filter1", "filter2");
        Set<Long> expectedChatIds = Set.of(1L, 2L);
        Link link = new Link(expectedId, expectedUrl, expectedTags, expectedFilters, expectedChatIds);

        OrmLink actualLink = standardLinkMapper.toOrmLink(link);

        assertThat(actualLink).isNotNull();
        assertEquals(expectedId, actualLink.getId());
        assertEquals(expectedUrl, actualLink.getLinkValue());
    }
}
