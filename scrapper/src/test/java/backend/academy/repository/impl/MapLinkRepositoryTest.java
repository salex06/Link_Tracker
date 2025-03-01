package backend.academy.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.model.Link;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MapLinkRepositoryTest {
    private MapLinkRepository mapLinkRepository;

    @BeforeEach
    public void setup() {
        mapLinkRepository = new MapLinkRepository();
    }

    @Test
    public void getById_WhenLinkNotFound_ThenReturnEmpty() {
        Long linkId = 1L;

        Optional<Link> result = mapLinkRepository.getById(linkId);

        assertThat(result).isEmpty();
    }

    @Test
    public void getById_WhenLinkIsInDataBase_ThenReturnLink() {
        Link link = new Link(1L, "test");
        mapLinkRepository.save(link);

        Optional<Link> result = mapLinkRepository.getById(1L);

        assertThat(result).isNotEmpty();
        assertThat(result.get().getUrl()).isEqualTo(link.getUrl());
        assertThat(result.get().getId()).isEqualTo(link.getId());
    }

    @Test
    public void getAllLinks_WhenDataBaseIsEmpty_ThenReturnEmptyList() {
        List<Link> actual = mapLinkRepository.getAllLinks();

        assertThat(actual).isEmpty();
    }

    @Test
    public void getAllLinks_WhenDataBaseIsNotEmpty_ThenReturnLinksList() {
        Link link1 = new Link(1L, "test1");
        Link link2 = new Link(2L, "test2");
        mapLinkRepository.save(link1);
        mapLinkRepository.save(link2);

        List<Link> actual = mapLinkRepository.getAllLinks();

        assertThat(actual).isNotEmpty();
        assertThat(actual).contains(link1, link2);
    }

    @Test
    public void save_WhenLinkNotFoundInDataBase_ThenReturnNullAndSaveNewLink() {
        Link link1 = new Link(1L, "test1");

        Link previousLink = mapLinkRepository.save(link1);

        assertNull(previousLink);
        assertThat(mapLinkRepository.getById(link1.getId()).get()).isEqualTo(link1);
    }

    @Test
    public void save_WhenLinkHasAlreadyBeenAdded_ThenReturnPreviousLinkAndSaveNewLink() {
        Link expectedPreviousLink = new Link(1L, "test1", List.of("test"), List.of("test"), Set.of(1L));
        mapLinkRepository.save(expectedPreviousLink);
        Link link1 = new Link(1L, "test1");

        Link actual = mapLinkRepository.save(link1);

        assertEquals(expectedPreviousLink, actual);
        assertThat(mapLinkRepository.getById(link1.getId()).get()).isEqualTo(link1);
    }
}
