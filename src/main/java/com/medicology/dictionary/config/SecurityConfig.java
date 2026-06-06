package com.medicology.dictionary.config;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.Customizer;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor

public class SecurityConfig {
        private final JwtAuthenticationFilter jwtAuthFilter; // Filter bạn đã viết

        @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:8082}")
        private String corsAllowedOrigins;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http.csrf(csrf -> csrf.disable())
                        .cors(Customizer.withDefaults())
                        .authorizeHttpRequests(auth -> auth
                                // 1. Mở cửa hoàn toàn cho Swagger và các file cấu hình của nó
                                .requestMatchers(
                                        "/",
                                        "/v3/api-docs/**",
                                        "/v3/api-docs",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/api-docs/**" // Thêm dòng này vì lỗi của bạn đang báo
                                // ở đây
                                ).permitAll()

                                // 2. Mở cửa cho API Auth
                                .requestMatchers("/api/v1/auth/**").permitAll()

                                // 3. Public read access for dictionary content
                                .requestMatchers(HttpMethod.GET, "/api/dictionary/articles", "/api/dictionary/articles/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/dictionary/assets/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/dictionary/tags", "/api/dictionary/tags/**").permitAll()

                                // 4. Các request khác mới cần login
                                .anyRequest().authenticated())
                        .sessionManagement(session -> session
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        // 5. CHÈN FILTER: Chạy JwtAuthFilter TRƯỚC UsernamePasswordAuthenticationFilter
                        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public OpenAPI medicologyOpenAPI() {
                Server productionServer = new Server()
                                .url("https://dictionary-service-medicology-2eca7b389d41.herokuapp.com")
                                .description("Server chính thức trên Heroku");

                Server localServer = new Server()
                        .url("http://localhost:8082")
                        .description("Server chạy ở Local");
                return new OpenAPI()
                        .servers(List.of(productionServer, localServer))
                        // 1. Định nghĩa cách thức bảo mật là JWT
                        .components(new Components()
                                .addSecuritySchemes("bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")))
                        // 2. Áp dụng bảo mật này cho tất cả API trong tài liệu
                        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
        }
        // Các cấu hình SecurityFilterChain khác của bạn...
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                
                // 1. Cho phép các nguồn gửi yêu cầu
                configuration.setAllowedOrigins(Arrays.stream(corsAllowedOrigins.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .toList());
                
                // 2. Cho phép các phương thức HTTPS
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS","PATCH"));
                
                // 3. Cho phép tất cả các Header (Authorization, Content-Type, v.v.)
                configuration.setAllowedHeaders(Arrays.asList("*"));
                
                // 4. Cho phép gửi kèm Cookie/Auth Header
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
