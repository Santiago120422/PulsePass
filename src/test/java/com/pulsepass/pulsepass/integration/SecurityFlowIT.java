package com.pulsepass.pulsepass.integration;

import com.pulsepass.pulsepass.AbstractIntegrationTest;
import com.pulsepass.pulsepass.domain.UserRole;
import com.pulsepass.pulsepass.domain.UserRoleId;
import com.pulsepass.pulsepass.domain.enums.Role;
import com.pulsepass.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.pulsepass.repository.UserRepository;
import com.pulsepass.pulsepass.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Flujo completo contra PostgreSQL real: registro -> login -> JWT -> roles -> propiedad -> usuario inactivo. */
class SecurityFlowIT extends AbstractIntegrationTest {

    private static final String PASSWORD = "UnaFraseSeguraLarga2026!";

    @Autowired WebApplicationContext context;
    @Autowired UserRepository userRepository;
    @Autowired UserCredentialRepository credentialRepository;
    @Autowired UserRoleRepository roleRepository;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private String registerBody(String username, String email, String password) {
        return """
                { "username": "%s", "email": "%s", "firstName": "Test", "lastName": "User",
                  "phone": "3001234567", "city": "Santa Marta", "birthDate": "1995-05-10", "password": "%s" }
                """.formatted(username, email, password);
    }

    private void register(String username, String email) throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(username, email, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("password"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("$2"))));
    }

    private String login(String identifier, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"%s\",\"password\":\"%s\"}".formatted(identifier, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andReturn();
        Matcher m = Pattern.compile("\"accessToken\":\"([^\"]+)\"").matcher(result.getResponse().getContentAsString());
        assertTrue(m.find());
        return m.group(1);
    }

    @Test
    void registrationStoresBcryptHashAndRoleUser_atomically() throws Exception {
        register("andrea", "andrea@pulsepass.com");

        Long id = userRepository.findByUsername("andrea").orElseThrow().getId();
        var credential = credentialRepository.findById(id).orElseThrow();
        assertTrue(credential.getPasswordHash().startsWith("$2"));
        assertNotEquals(PASSWORD, credential.getPasswordHash());
        assertEquals(java.util.List.of(Role.USER), roleRepository.findRolesByUserId(id));
    }

    @Test
    void registrationRejectsShortPassword_with400() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("x1", "x1@pulsepass.com", "corta")))
                .andExpect(status().isBadRequest());
        assertTrue(userRepository.findByUsername("x1").isEmpty());
    }

    @Test
    void loginFailures_are401AndGeneric() throws Exception {
        register("andrea", "andrea@pulsepass.com");

        String wrongPassword = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"andrea\",\"password\":\"incorrecta-123456\"}"))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        String unknownUser = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"fantasma\",\"password\":\"incorrecta-123456\"}"))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();

        assertTrue(wrongPassword.contains("Credenciales inválidas"));
        assertTrue(unknownUser.contains("Credenciales inválidas"));   // no revela si el usuario existe
    }

    @Test
    void loginWorksWithUsernameOrEmail_andMeReturnsPrincipal() throws Exception {
        register("andrea", "andrea@pulsepass.com");

        String token = login("andrea", PASSWORD);
        login("Andrea@PulsePass.com", PASSWORD);

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("email"))));
    }

    @Test
    void tamperedToken_is401() throws Exception {
        register("andrea", "andrea@pulsepass.com");
        String token = login("andrea", PASSWORD);
        int i = token.indexOf('.') + 5;
        String tampered = token.substring(0, i) + (token.charAt(i) == 'A' ? 'B' : 'A') + token.substring(i + 1);

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void userCannotReadOthersData_butCanReadOwn() throws Exception {
        register("andrea", "andrea@pulsepass.com");
        register("carlos", "carlos@pulsepass.com");
        String token = login("andrea", PASSWORD);
        String auth = "Bearer " + token;

        mockMvc.perform(get("/api/users/by-email").param("email", "carlos@pulsepass.com").header("Authorization", auth))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
        mockMvc.perform(get("/api/users/by-username").param("username", "carlos").header("Authorization", auth))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/tickets/by-user").param("email", "carlos@pulsepass.com").header("Authorization", auth))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/tickets").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userEmail\":\"carlos@pulsepass.com\",\"eventCode\":\"CMF-2026\",\"type\":\"VIP\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/users/by-email").param("email", "andrea@pulsepass.com").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("andrea"));
    }

    @Test
    void foreignOrUnknownTicket_is403ForUser() throws Exception {
        register("andrea", "andrea@pulsepass.com");
        String auth = "Bearer " + login("andrea", PASSWORD);

        mockMvc.perform(get("/api/tickets/NO-EXISTE").header("Authorization", auth)).andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/tickets/NO-EXISTE/cancel").header("Authorization", auth)).andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/tickets/NO-EXISTE/use").header("Authorization", auth)).andExpect(status().isForbidden());
    }

    @Test
    void userCannotPublish_adminReachesBusinessLayer() throws Exception {
        register("andrea", "andrea@pulsepass.com");
        register("rootadmin", "root@pulsepass.com");
        Long adminId = userRepository.findByUsername("rootadmin").orElseThrow().getId();
        roleRepository.deleteById(new UserRoleId(adminId, Role.USER));
        roleRepository.save(new UserRole(new UserRoleId(adminId, Role.ADMIN)));

        String userAuth = "Bearer " + login("andrea", PASSWORD);
        String adminAuth = "Bearer " + login("rootadmin", PASSWORD);

        mockMvc.perform(patch("/api/events/NO-EXISTE/publish").header("Authorization", userAuth))
                .andExpect(status().isForbidden());
        // ADMIN supera la seguridad; el evento no existe => el 404 viene de la capa de negocio
        mockMvc.perform(patch("/api/events/NO-EXISTE/publish").header("Authorization", adminAuth))
                .andExpect(status().isNotFound());
    }

    @Test
    void deactivatedUser_loses_access_withPreviouslyIssuedToken() throws Exception {
        register("andrea", "andrea@pulsepass.com");
        String token = login("andrea", PASSWORD);
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token)).andExpect(status().isOk());

        var user = userRepository.findByUsername("andrea").orElseThrow();
        user.setActive(false);
        userRepository.saveAndFlush(user);

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"andrea\",\"password\":\"%s\"}".formatted(PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicRoutesWorkWithoutToken() throws Exception {
        mockMvc.perform(get("/api/events/published")).andExpect(status().isOk());
        mockMvc.perform(get("/api/venues/active")).andExpect(status().isOk());
        mockMvc.perform(get("/api/artists/active")).andExpect(status().isOk());
        mockMvc.perform(get("/api/events/NO-EXISTE")).andExpect(status().isNotFound());
    }
}
