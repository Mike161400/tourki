package com.autoservice.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AppUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(AppUserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write(
                            "{\"error\":\"Unauthorized\",\"message\":\"Token missing, invalid or expired. Please login again.\"}");
                }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/tours/*/auto-assign-guide").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/tours/*/book-seat").hasAnyRole("TRAVELER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/tours/*/complete").hasAnyRole("ADMIN", "GUIDE")
                        .requestMatchers(HttpMethod.POST, "/api/tours/*/reviews").hasAnyRole("TRAVELER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/tours/*/availability").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/reports/guides/workload").hasAnyRole("ADMIN", "GUIDE")

                        .requestMatchers(HttpMethod.GET, "/api/bookings/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/bookings/*/cancel").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/bookings/**").hasAnyRole("ADMIN", "GUIDE")

                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/reviews/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/destinations/**").authenticated()
                        .requestMatchers("/api/destinations/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/guides/**").authenticated()
                        .requestMatchers("/api/guides/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/tours/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/tours/**").hasAnyRole("ADMIN", "GUIDE")
                        .requestMatchers(HttpMethod.PUT, "/api/tours/**").hasAnyRole("ADMIN", "GUIDE")
                        .requestMatchers(HttpMethod.DELETE, "/api/tours/**").hasRole("ADMIN")

                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
