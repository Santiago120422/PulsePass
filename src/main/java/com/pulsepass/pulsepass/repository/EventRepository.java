package com.pulsepass.pulsepass.repository;

import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.domain.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByEventCode(String eventCode);

    // FR-EVT-005
    List<Event> findByStatusOrderByEventDateAsc(EventStatus status);

    // FR-VEN-004
    List<Event> findByVenue_Code(String venueCode);

    // FR-SRC-001
    @Query("SELECT DISTINCT e FROM Event e JOIN e.artists a WHERE a.stageName = :stageName")
    List<Event> findByArtistStageName(@Param("stageName") String stageName);

    // FR-SRC-002
    @Query("SELECT DISTINCT e FROM Event e JOIN e.artists a " +
           "WHERE e.venue.city = :city AND a.stageName = :stageName")
    List<Event> findByCityAndArtist(@Param("city") String city,
                                     @Param("stageName") String stageName);

    // FR-SRC-003
    @Query("SELECT DISTINCT e FROM Event e JOIN e.artists a " +
           "WHERE e.status = 'PUBLISHED' AND e.eventDate > :fromDate " +
           "AND e.venue.city = :city " +
           "AND LOWER(a.stageName) LIKE LOWER(CONCAT('%', :artistText, '%')) " +
           "ORDER BY e.eventDate ASC")
    List<Event> findRecommendedEvents(@Param("fromDate") LocalDateTime fromDate,
                                       @Param("city") String city,
                                       @Param("artistText") String artistText);
}