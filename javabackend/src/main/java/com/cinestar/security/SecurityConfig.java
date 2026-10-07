package com.cinestar.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Cong khai
                        .requestMatchers("/api/v1/auth/**", "/error",
                                // Anh tĩnh (poster phim, banner rap, khuyen mai) do backend serve
                                "/images/**",
                                // Swagger / OpenAPI
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                                // VNPAY goi/redirect ve khong mang JWT - bao mat bang chu ky HMAC
                                "/api/v1/payments/vnpay/return", "/api/v1/payments/vnpay/ipn").permitAll()
                        // Doc noi dung phim/rap/suat chieu/ve la public
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/movies/**", "/api/v1/cinemas/**", "/api/v1/showtimes/**",
                                "/api/v1/combos/**", "/api/v1/promotions/**").permitAll()
                        // Toan bo API quan tri can quyen ADMIN
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // Con lai (booking, payment...) can dang nhap
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Dev: cho phep moi origin (React 5173, Expo web 19006, 127.0.0.1...).
        // JWT di qua header Authorization nen khong phu thuoc cookie;
        // khi deploy that se siets lai danh sach origin cu the.
        config.setAllowedOriginPatterns(java.util.List.of("*"));
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(java.util.List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
