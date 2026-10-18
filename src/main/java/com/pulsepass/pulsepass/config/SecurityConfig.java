package com.pulsepass.pulsepass.config;

import com.pulsepass.pulsepass.security.CustomUserDetailsService;
import com.pulsepass.pulsepass.security.JwtAuthenticationFilter;
import com.pulsepass.pulsepass.security.JwtProperties;
import com.pulsepass.pulsepass.security.JwtService;
import com.pulsepass.pulsepass.security.RestAccessDeniedHandler;
import com.pulsepass.pulsepass.security.RestAuthenticationEntryPoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Declara filtro, autenticacion y reglas de rutas. Sin negocio.
 *
 * La autenticacion por usuario/correo + contrasena la resuelve Spring automaticamente: al existir UN
 * UserDetailsService (CustomUserDetailsService) y UN PasswordEncoder, AuthenticationConfiguration arma un
 * DaoAuthenticationProvider. Asi no dependemos de constructores/setters que cambian entre versiones.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtService jwtService,
                                                   CustomUserDetailsService userDetailsService,
                                                   RestAuthenticationEntryPoint authEntryPoint,
                                                   RestAccessDeniedHandler deniedHandler) throws Exception {

        // Se crea aqui (sin @Component/@Bean) para que NO se registre tambien como filtro de servlet.
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, userDetailsService, authEntryPoint);

        http
            .csrf(AbstractHttpConfigurer::disable)            // API Bearer, sin cookies de autenticacion
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authEntryPoint)
                .accessDeniedHandler(deniedHandler))
            .authorizeHttpRequests(auth -> auth
                // --- publicas ---
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/users").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/venues/**", "/api/artists/**").permitAll()
                // eventos: publicas, pero EventController oculta los no publicados (404) a quien no sea STAFF/ADMIN
                .requestMatchers(HttpMethod.GET, "/api/events/published", "/api/events/by-artist", "/api/events/*").permitAll()
                // --- ADMIN ---
                .requestMatchers(HttpMethod.POST, "/api/events").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/events/*/publish").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/events/*/artists/*").hasRole("ADMIN")
                // --- STAFF / ADMIN ---
                .requestMatchers(HttpMethod.PATCH, "/api/tickets/*/use").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/events/*/tickets/paid").hasAnyRole("STAFF", "ADMIN")
                // --- todo lo demas: autenticado (la propiedad se valida en los Services) ---
                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
