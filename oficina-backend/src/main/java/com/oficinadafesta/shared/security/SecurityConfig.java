package com.oficinadafesta.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final DeviceAuthFilter deviceAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, DeviceAuthFilter deviceAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.deviceAuthFilter = deviceAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .authorizeHttpRequests(auth -> auth
                        // públicas
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()

                        // TEMP: liberar criação (bootstrap)
                        .requestMatchers(HttpMethod.POST, "/admin/usuarios/criar").permitAll()

                        // swagger
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // catraca: não usa JWT (usa X-Device-Key no filtro)
                        .requestMatchers("/catraca/**").permitAll()

                        // todo o resto precisa JWT
                        .anyRequest().authenticated()
                )

                // DeviceAuthFilter precisa rodar antes do JwtAuthFilter
                .addFilterBefore(deviceAuthFilter, JwtAuthFilter.class)

                // JWT antes do UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}