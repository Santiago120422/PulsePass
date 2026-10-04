package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.domain.Venue;
import com.pulsepass.pulsepass.dto.response.VenueResponse;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.mapper.VenueMapper;
import com.pulsepass.pulsepass.repository.VenueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.pulsepass.pulsepass.service.impl.TestFixtures.venue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VenueServiceImplTest {

    @Mock private VenueRepository venueRepository;
    @Mock private VenueMapper venueMapper;
    @InjectMocks private VenueServiceImpl service;

    private static VenueResponse response(String code) {
        return new VenueResponse(1L, code, "Marina Convention Center", "Santa Marta", "Calle 1", 3, true);
    }

    @Test
    void findByCode_existingVenue_returnsDto() {
        Venue venue = venue("VEN-SMR-01", 3, true);
        when(venueRepository.findByCode("VEN-SMR-01")).thenReturn(Optional.of(venue));
        when(venueMapper.toResponse(venue)).thenReturn(response("VEN-SMR-01"));

        VenueResponse result = service.findByCode("VEN-SMR-01");

        assertThat(result.code()).isEqualTo("VEN-SMR-01");
    }

    @Test
    void findByCode_missingVenue_throwsResourceNotFound() {
        when(venueRepository.findByCode("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByCode("NOPE"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Venue not found: NOPE");
    }

    @Test
    void findActiveVenues_usesActiveQueryAndMapsResults() {
        Venue venue = venue("VEN-SMR-01", 3, true);
        when(venueRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(venue));
        when(venueMapper.toResponse(any(Venue.class))).thenReturn(response("VEN-SMR-01"));

        List<VenueResponse> result = service.findActiveVenues();

        assertThat(result).hasSize(1);
        verify(venueRepository).findByActiveTrueOrderByNameAsc();
    }
}
