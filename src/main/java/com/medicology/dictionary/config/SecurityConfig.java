package com.medicology.dictionary.config;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor

public class SecurityConfig {
        private final JwtAuthenticationFilter jwtAuthFilter; // Filter bạn đã viết

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http.csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                // 1. Mở cửa hoàn toàn cho Swagger và các file cấu hình của nó
                                                .requestMatchers(
                                                                "/v3/api-docs/**",
                                                                "/v3/api-docs",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/api-docs/**" // Thêm dòng này vì lỗi của bạn đang báo
                                                // ở đây
                                                ).permitAll()

                                                // 2. Mở cửa cho API Auth của Medicology (kiểm tra kỹ có /medicology ở
                                                // đầu
                                                // không)
                                                .requestMatchers("/api/v1/auth/**").permitAll()

                                                // 3. Các request khác mới cần login
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
                                .url("https://dictionary-service-medicology-production.up.railway.app")
                                .description("Server chính thức trên Railway");

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

}
