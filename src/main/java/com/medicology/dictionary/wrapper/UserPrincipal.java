package com.medicology.dictionary.wrapper;

import java.util.UUID;

import lombok.Getter;

@Getter
public class UserPrincipal {
    private final UUID id;
    private final String email;
    // Không cần password, không cần isAdmin nếu không dùng tới

    public UserPrincipal(UUID id, String email) {
        this.id = id;
        this.email = email;
    }
}
