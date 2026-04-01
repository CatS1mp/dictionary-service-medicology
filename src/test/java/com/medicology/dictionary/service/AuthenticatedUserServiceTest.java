package com.medicology.dictionary.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticatedUserServiceTest {

    private final AuthenticatedUserService authenticatedUserService = new AuthenticatedUserService();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserIdUsesDeterministicUuidForStringPrincipal() {
        String principal = "doctor@medicology.vn";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );

        UUID userId = authenticatedUserService.getCurrentUserId();

        assertThat(userId).isEqualTo(UUID.nameUUIDFromBytes(principal.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void getCurrentUserIdRejectsMissingAuthentication() {
        assertThatThrownBy(authenticatedUserService::getCurrentUserId)
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401 UNAUTHORIZED");
    }
}
