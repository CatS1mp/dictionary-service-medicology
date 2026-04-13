package com.medicology.dictionary.service;

import com.medicology.dictionary.wrapper.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

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
    void getCurrentUserIdReturnsIdFromUserPrincipal() {
        UUID id = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(id, "user@test.com", false);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );

        assertThat(authenticatedUserService.getCurrentUserId()).isEqualTo(id);
    }

    @Test
    void getCurrentUserIdRejectsMissingAuthentication() {
        assertThatThrownBy(authenticatedUserService::getCurrentUserId)
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401 UNAUTHORIZED");
    }

    @Test
    void getCurrentUserIdRejectsNullIdOnPrincipal() {
        UserPrincipal principal = new UserPrincipal(null, "user@test.com", false);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );

        assertThatThrownBy(authenticatedUserService::getCurrentUserId)
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401 UNAUTHORIZED");
    }
}
