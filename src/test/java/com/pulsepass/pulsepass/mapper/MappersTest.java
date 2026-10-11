package com.pulsepass.pulsepass.mapper;

import com.pulsepass.pulsepass.domain.*;
import com.pulsepass.pulsepass.domain.enums.*;
import com.pulsepass.pulsepass.dto.response.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifica el código que MapStruct genera (sin contexto Spring). */
class MappersTest {

    private final LocalDateTime date = LocalDateTime.of(2026, 12, 1, 20, 0);
    private final Venue venue = Venue.builder().id(1L).code("VEN-SMR-01").name("Marina Convention Center")
            .city("Santa Marta").address("Calle 1").capacity(3).active(true).build();
    private final Artist artist = Artist.builder().id(5L).stageName("Solar Beat").country("CO")
            .genre("Electronic").active(true).build();
    private final Event event = Event.builder().id(10L).eventCode("CMF-2026").name("Caribbean Music Fest 2026")
            .category(EventCategory.MUSIC).status(EventStatus.DRAFT).eventDate(date).minimumAge(18)
            .venue(venue).build();

    @Test
    void eventMapper_flattensVenueAndMapsArtists() {
        event.getArtists().add(artist);
        EventMapper mapper = new EventMapperImpl(new ArtistMapperImpl());

        EventResponse response = mapper.toResponse(event);

        assertThat(response.eventCode()).isEqualTo("CMF-2026");
        assertThat(response.status()).isEqualTo(EventStatus.DRAFT);
        assertThat(response.venueCode()).isEqualTo("VEN-SMR-01");
        assertThat(response.venueName()).isEqualTo("Marina Convention Center");
        assertThat(response.artists()).extracting(ArtistResponse::stageName).containsExactly("Solar Beat");

        EventSummaryResponse summary = mapper.toSummary(event);
        assertThat(summary.venueCode()).isEqualTo("VEN-SMR-01");
        assertThat(summary.eventDate()).isEqualTo(date);
    }

    @Test
    void venueAndArtistMappers_copyAllFields() {
        assertThat(new VenueMapperImpl().toResponse(venue).capacity()).isEqualTo(3);
        assertThat(new ArtistMapperImpl().toResponse(artist).stageName()).isEqualTo("Solar Beat");
    }

    @Test
    void userMapper_flattensProfile() {
        User user = User.builder().id(1L).username("andrea").email("andrea@email.com").active(true).build();
        user.setProfile(UserProfile.builder().firstName("Andrea").lastName("Perez").phone("300")
                .city("Santa Marta").birthDate(LocalDate.of(2001, 1, 1)).user(user).build());

        UserResponse response = new UserMapperImpl().toResponse(user);

        assertThat(response.email()).isEqualTo("andrea@email.com");
        assertThat(response.firstName()).isEqualTo("Andrea");
        assertThat(response.birthDate()).isEqualTo(LocalDate.of(2001, 1, 1));
    }

    @Test
    void ticketMapper_exposesUserEmailAndEventData() {
        User user = User.builder().id(1L).username("andrea").email("andrea@email.com").active(true).build();
        Ticket ticket = Ticket.builder().id(30L).ticketCode("TKT-1").type(TicketType.VIP)
                .price(new BigDecimal("200000.00")).status(TicketStatus.PAID).purchaseDate(date)
                .user(user).event(event).build();

        TicketResponse response = new TicketMapperImpl().toResponse(ticket);

        assertThat(response.userEmail()).isEqualTo("andrea@email.com");
        assertThat(response.eventCode()).isEqualTo("CMF-2026");
        assertThat(response.eventName()).isEqualTo("Caribbean Music Fest 2026");
        assertThat(response.price()).isEqualByComparingTo("200000.00");
    }
}
