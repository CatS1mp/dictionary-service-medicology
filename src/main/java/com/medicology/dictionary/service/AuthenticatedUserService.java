package com.medicology.dictionary.service;

import com.medicology.dictionary.wrapper.UserPrincipal;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthenticatedUserService {

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(UNAUTHORIZED, "Authentication is required");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            if (userPrincipal.getId() == null) {
                throw new ResponseStatusException(UNAUTHORIZED, "Token missing user id");
            }
            return userPrincipal.getId();
        }

        throw new ResponseStatusException(UNAUTHORIZED, "Invalid authentication principal");
    }
}
