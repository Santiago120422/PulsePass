package com.pulsepass.pulsepass.controller;

import com.pulsepass.pulsepass.config.SecurityConfig;
import com.pulsepass.pulsepass.security.CustomUserDetailsService;
import com.pulsepass.pulsepass.security.JwtService;
import com.pulsepass.pulsepass.security.RestAccessDeniedHandler;
import com.pulsepass.pulsepass.security.RestAuthenticationEntryPoint;

import com.pulsepass.pulsepass.security.RestAuthenticationEntryPoint;
import com.pulsepass.pulsepass.security.RestAccessDeniedHandler;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Base de los @WebMvcTest: carga la SecurityConfig real (filtros incluidos). Las identidades se simulan
 * con @WithMockUser. MockMvc se construye aqui con springSecurity() para no depender de autoconfiguracion.
 */
@Import({
    SecurityConfig.class,
    RestAuthenticationEntryPoint.class,
    RestAccessDeniedHandler.class
})@TestPropertySource(properties = {
        "pulsepass.security.jwt.issuer=test-issuer",
        "pulsepass.security.jwt.secret-base64=x2XCGrSYLyn/ytOPS1jxM5uNb+7nKc9/Dy6sphYoYBI="
})
abstract class ControllerSecurityTestBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    protected JwtService jwtService;

    @MockitoBean
    protected CustomUserDetailsService userDetailsService;

    protected MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }
}
