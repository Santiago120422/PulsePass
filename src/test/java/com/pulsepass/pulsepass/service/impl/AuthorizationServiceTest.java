package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.domain.Ticket;
import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.repository.TicketRepository;
import com.pulsepass.pulsepass.repository.UserRepository;
import com.pulsepass.pulsepass.security.SecurityPrincipal;
import com.pulsepass.pulsepass.service.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthorizationServiceTest {

    private TicketRepository ticketRepository;
    private UserRepository userRepository;
    private AuthorizationService authz;

    private final User andrea = User.builder().id(1L).username("andrea").email("andrea@pulsepass.com").active(true).build();
    private final User carlos = User.builder().id(2L).username("carlos").email("carlos@pulsepass.com").active(true).build();

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        userRepository = mock(UserRepository.class);
        authz = new AuthorizationService(ticketRepository, userRepository);
        when(userRepository.findById(1L)).thenReturn(Optional.of(andrea));
        when(ticketRepository.findByTicketCode("TKT-A")).thenReturn(Optional.of(Ticket.builder().ticketCode("TKT-A").user(andrea).build()));
        when(ticketRepository.findByTicketCode("TKT-C")).thenReturn(Optional.of(Ticket.builder().ticketCode("TKT-C").user(carlos).build()));
    }

    private Authentication auth(long id, String username, String... roles) {
        var authorities = java.util.Arrays.stream(roles).map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
        var principal = new SecurityPrincipal(id, username, "x", true, authorities);
        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

    @Test
    void owner_canAccessAndCancelOwnTicket() {
        Authentication a = auth(1L, "andrea", "USER");
        assertTrue(authz.canAccessTicket("TKT-A", a));
        assertTrue(authz.canCancelTicket("TKT-A", a));
    }

    @Test
    void user_cannotAccessOrCancelForeignTicket() {
        Authentication a = auth(1L, "andrea", "USER");
        assertFalse(authz.canAccessTicket("TKT-C", a));
        assertFalse(authz.canCancelTicket("TKT-C", a));
    }

    @Test
    void unknownTicket_isDeniedForUser() {
        assertFalse(authz.canAccessTicket("NOPE", auth(1L, "andrea", "USER")));
    }

    @Test
    void staff_canViewAnyTicket_butCannotCancelForeign() {
        Authentication staff = auth(9L, "laura", "STAFF");
        assertTrue(authz.canAccessTicket("TKT-C", staff));
        assertFalse(authz.canCancelTicket("TKT-C", staff));
    }

    @Test
    void admin_canCancelAnyTicket() {
        assertTrue(authz.canCancelTicket("TKT-C", auth(8L, "admin", "ADMIN")));
    }

    @Test
    void isSelfEmail_comparesAgainstPrincipal_notAgainstRequest() {
        Authentication a = auth(1L, "andrea", "USER");
        assertTrue(authz.isSelfEmail("Andrea@Pulsepass.com", a));
        assertFalse(authz.isSelfEmail("carlos@pulsepass.com", a));
        assertFalse(authz.isSelfEmail(null, a));
    }

    @Test
    void emailAndUsernameLookups_ownOrAdminOnly() {
        Authentication user = auth(1L, "andrea", "USER");
        assertTrue(authz.canAccessEmail("andrea@pulsepass.com", user));
        assertFalse(authz.canAccessEmail("carlos@pulsepass.com", user));
        assertTrue(authz.canAccessUsername("andrea", user));
        assertFalse(authz.canAccessUsername("carlos", user));

        Authentication admin = auth(8L, "admin", "ADMIN");
        assertTrue(authz.canAccessEmail("carlos@pulsepass.com", admin));
        assertTrue(authz.canAccessUsername("carlos", admin));
    }

    @Test
    void nonApplicationPrincipal_isDenied() {
        Authentication foreign = new UsernamePasswordAuthenticationToken("alguien", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertFalse(authz.canAccessTicket("TKT-A", foreign));
        assertFalse(authz.isSelfEmail("andrea@pulsepass.com", foreign));
    }
}
