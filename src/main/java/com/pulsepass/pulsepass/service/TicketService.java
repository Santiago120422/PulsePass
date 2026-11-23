package com.pulsepass.pulsepass.service;

import com.pulsepass.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.pulsepass.dto.response.TicketResponse;
import com.pulsepass.pulsepass.domain.enums.TicketType;

import org.springframework.security.access.prepost.PreAuthorize;
import java.math.BigDecimal;
import java.util.Map;

import java.util.List;

public interface TicketService {

    Map<TicketType, BigDecimal> prices();

    @PreAuthorize("hasRole('USER') and @authz.isSelfEmail(#p0.userEmail(), authentication)")
    TicketResponse purchase(PurchaseTicketRequest request);

    @PreAuthorize("@authz.canAccessTicket(#p0, authentication)")
    TicketResponse findByCode(String ticketCode);

    @PreAuthorize("@authz.canAccessEmail(#p0, authentication)")
    List<TicketResponse> findByUserEmail(String email);

    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    List<TicketResponse> findPaidTicketsByEvent(String eventCode);

    @PreAuthorize("@authz.canCancelTicket(#p0, authentication)")
    TicketResponse cancel(String ticketCode);

    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    TicketResponse markAsUsed(String ticketCode);
}
