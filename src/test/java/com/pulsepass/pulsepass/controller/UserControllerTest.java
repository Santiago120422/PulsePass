package com.pulsepass.pulsepass.controller;

import com.pulsepass.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.pulsepass.dto.response.UserResponse;
import com.pulsepass.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    private static final String VALID_BODY = """
            {
              "username": "andrea",
              "email": "andrea@pulsepass.com",
              "firstName": "Andrea",
              "lastName": "Gómez",
              "phone": "3001234567",
              "city": "Santa Marta",
              "birthDate": "1995-05-10"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private UserResponse andrea() {
        return new UserResponse(1L, "andrea", "andrea@pulsepass.com", true,
                "Andrea", "Gómez", "3001234567", "Santa Marta", LocalDate.of(1995, 5, 10));
    }

    // TEST-CTRL-USR-001 / AC-CTRL007
    @Test
    void register_valid_returns201() throws Exception {
        when(userService.register(any(RegisterUserRequest.class))).thenReturn(andrea());

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.email").value("andrea@pulsepass.com"))
                .andExpect(jsonPath("$.active").value(true));

        verify(userService).register(any(RegisterUserRequest.class));
    }

    // TEST-CTRL-USR-002
    @Test
    void register_invalidEmail_returns400AndDoesNotCallService() throws Exception {
        String body = VALID_BODY.replace("andrea@pulsepass.com", "no-es-un-email");

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.email").value("Email must be valid"));

        verify(userService, never()).register(any());
    }

    @Test
    void register_missingFields_returns400WithAllInvalidFields() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.username").exists())
                .andExpect(jsonPath("$.details.email").exists())
                .andExpect(jsonPath("$.details.firstName").exists())
                .andExpect(jsonPath("$.details.lastName").exists())
                .andExpect(jsonPath("$.details.birthDate").exists());

        verify(userService, never()).register(any());
    }

    @Test
    void register_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content("{ no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(userService, never()).register(any());
    }

    // TEST-CTRL-USR-003 / AC-CTRL008
    @Test
    void register_duplicate_returns409() throws Exception {
        when(userService.register(any(RegisterUserRequest.class)))
                .thenThrow(new DuplicateResourceException("Username already exists."));

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Username already exists."));
    }

    // TEST-CTRL-USR-004
    @Test
    void findByEmail_existing_returns200() throws Exception {
        when(userService.findByEmail("andrea@pulsepass.com")).thenReturn(andrea());

        mockMvc.perform(get("/api/users/by-email").param("email", "andrea@pulsepass.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("andrea@pulsepass.com"));

        verify(userService).findByEmail("andrea@pulsepass.com");
    }

    @Test
    void findByEmail_missing_returns404() throws Exception {
        when(userService.findByEmail("x@x.com")).thenThrow(ResourceNotFoundException.of("User", "x@x.com"));

        mockMvc.perform(get("/api/users/by-email").param("email", "x@x.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: x@x.com"));
    }

    // TEST-CTRL-USR-005
    @Test
    void findByUsername_existing_returns200() throws Exception {
        when(userService.findByUsername("andrea")).thenReturn(andrea());

        mockMvc.perform(get("/api/users/by-username").param("username", "andrea"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("andrea"));

        verify(userService).findByUsername("andrea");
    }

    @Test
    void findByUsername_missing_returns404() throws Exception {
        when(userService.findByUsername("ghost")).thenThrow(ResourceNotFoundException.of("User", "ghost"));

        mockMvc.perform(get("/api/users/by-username").param("username", "ghost"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findByEmail_missingParam_returns400() throws Exception {
        mockMvc.perform(get("/api/users/by-email"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
