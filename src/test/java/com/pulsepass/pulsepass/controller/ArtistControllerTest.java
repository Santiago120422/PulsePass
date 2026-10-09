package com.pulsepass.pulsepass.controller;

import com.pulsepass.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.service.ArtistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArtistController.class)
class ArtistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArtistService artistService;

    private ArtistResponse solarBeat() {
        return new ArtistResponse(1L, "Solar Beat", "Colombia", "Electronic", true);
    }

    // TEST-CTRL-ART-001
    @Test
    void findById_existing_returns200() throws Exception {
        when(artistService.findById(1L)).thenReturn(solarBeat());

        mockMvc.perform(get("/api/artists/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.stageName").value("Solar Beat"));

        verify(artistService).findById(1L);
    }

    // TEST-CTRL-ART-002
    @Test
    void findById_missing_returns404() throws Exception {
        when(artistService.findById(99L)).thenThrow(ResourceNotFoundException.of("Artist", 99L));

        mockMvc.perform(get("/api/artists/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Artist not found: 99"));
    }

    // ID no numérico → 400 (no llega al Service)
    @Test
    void findById_nonNumericId_returns400() throws Exception {
        mockMvc.perform(get("/api/artists/{id}", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        org.mockito.Mockito.verifyNoInteractions(artistService);
    }

    // TEST-CTRL-ART-003
    @Test
    void findByStageName_existing_returns200() throws Exception {
        when(artistService.findByStageName("Solar Beat")).thenReturn(solarBeat());

        mockMvc.perform(get("/api/artists/by-stage-name").param("stageName", "Solar Beat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stageName").value("Solar Beat"));

        verify(artistService).findByStageName("Solar Beat");
    }

    @Test
    void findByStageName_missing_returns404() throws Exception {
        when(artistService.findByStageName("Ghost"))
                .thenThrow(ResourceNotFoundException.of("Artist", "Ghost"));

        mockMvc.perform(get("/api/artists/by-stage-name").param("stageName", "Ghost"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Artist not found: Ghost"));
    }

    // TEST-CTRL-ART-004
    @Test
    void findActiveArtists_returns200() throws Exception {
        when(artistService.findActiveArtists()).thenReturn(List.of(solarBeat()));

        mockMvc.perform(get("/api/artists/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].stageName").value("Solar Beat"));

        verify(artistService).findActiveArtists();
    }
}
