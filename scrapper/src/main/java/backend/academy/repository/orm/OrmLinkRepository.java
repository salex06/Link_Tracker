package backend.academy.repository.orm;

import backend.academy.model.orm.OrmLink;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrmLinkRepository extends JpaRepository<OrmLink, Long> {
    @Query(
            value =
                    "SELECT l FROM OrmLink l WHERE l.lastUpdate < :cutoff AND l.id IN (SELECT cl.id.linkId FROM OrmChatLink cl)",
            countQuery =
                    "SELECT count(l) FROM OrmLink l WHERE l.lastUpdate < :cutoff AND l.id IN (SELECT cl.id.linkId FROM OrmChatLink cl)")
    Page<OrmLink> findAll(@Param("cutoff") Instant cutoff, Pageable pageable);

    Optional<OrmLink> findByLinkValue(String linkValue);

    @Modifying
    @Transactional
    @Query(
            "UPDATE OrmLink link SET link.linkValue = :linkValue, link.lastUpdate = :lastUpdate, type = :type WHERE link.id = :id")
    void updateLink(
            @Param("id") Long id,
            @Param("linkValue") String linkValue,
            @Param("lastUpdate") Instant lastUpdate,
            @Param("type") String type);
}
