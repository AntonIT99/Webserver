package com.wolfsnetz.webserver.auth;

import java.time.Instant;

public record AppUser(
    long id,
    String username,
    String passwordHash,
    UserRole role,
    boolean approved,
    Instant createdAt
) {
}
