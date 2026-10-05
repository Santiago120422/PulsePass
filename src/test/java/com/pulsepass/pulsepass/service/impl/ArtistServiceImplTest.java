package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.domain.Artist;
import com.pulsepass.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.mapper.ArtistMapper;
import com.pulsepass.pulsepass.repository.ArtistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.pulsepass.pulsepass.service.impl.TestFixtures.artist;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtistServiceImplTest {

    @Mock private ArtistRepository artistRepository;
    @Mock private ArtistMapper artistMapper;
    @InjectMocks private ArtistServiceImpl service;

    private static ArtistResponse response(Long id, String name) {
        return new ArtistResponse(id, name, "CO", "Electronic", true);
    }

    @Test
    void findById_existingArtist_returnsDto() {
        Artist artist = artist(1L, "Solar Beat");
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(artistMapper.toResponse(artist)).thenReturn(response(1L, "Solar Beat"));

        assertThat(service.findById(1L).stageName()).isEqualTo("Solar Beat");
    }

    @Test
    void findById_missingArtist_throwsResourceNotFound() {
        when(artistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Artist not found: 99");
    }

    @Test
    void findByStageName_ignoresCaseThroughRepository() {
        Artist artist = artist(1L, "Solar Beat");
        when(artistRepository.findByStageNameIgnoreCase("solar beat")).thenReturn(Optional.of(artist));
        when(artistMapper.toResponse(artist)).thenReturn(response(1L, "Solar Beat"));

        assertThat(service.findByStageName("solar beat").id()).isEqualTo(1L);
    }

    @Test
    void findByStageName_missingArtist_throwsResourceNotFound() {
        when(artistRepository.findByStageNameIgnoreCase("Ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByStageName("Ghost"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findActiveArtists_returnsOnlyWhatRepositoryReturnsMapped() {
        Artist artist = artist(1L, "Solar Beat");
        when(artistRepository.findByActiveTrueOrderByStageNameAsc()).thenReturn(List.of(artist));
        when(artistMapper.toResponse(any(Artist.class))).thenReturn(response(1L, "Solar Beat"));

        assertThat(service.findActiveArtists()).hasSize(1);
    }
}
