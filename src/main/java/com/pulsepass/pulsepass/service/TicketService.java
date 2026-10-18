package com.pulsepass.pulsepass.service;

import com.pulsepass.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.pulsepass.dto.response.TicketResponse;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface TicketService {

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
