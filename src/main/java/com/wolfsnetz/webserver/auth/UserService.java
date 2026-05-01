package com.wolfsnetz.webserver.auth;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService
{
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String bootstrapAdminUsername;
    private final String bootstrapAdminPassword;

    public UserService(
        UserRepository users,
        PasswordEncoder passwordEncoder,
        @Value("${app.bootstrap-admin.username}") String bootstrapAdminUsername,
        @Value("${app.bootstrap-admin.password:}") String bootstrapAdminPassword
    ) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapAdminUsername = bootstrapAdminUsername;
        this.bootstrapAdminPassword = bootstrapAdminPassword;
    }

    @Transactional
    public void initialize()
    {
        users.initializeSchema();

        if (!users.adminExists()) {
            if (bootstrapAdminPassword == null || bootstrapAdminPassword.isBlank()) {
                throw new IllegalStateException("BOOTSTRAP_ADMIN_PASSWORD must be set before the first production start");
            }

            users.createUser(
                bootstrapAdminUsername,
                passwordEncoder.encode(bootstrapAdminPassword),
                UserRole.ADMIN,
                true
            );
        }
    }

    @Transactional
    public void register(String username, String password)
    {
        String normalizedUsername = username == null ? "" : username.trim();

        if (normalizedUsername.length() < 3) {
            throw new IllegalArgumentException("Der Benutzername muss mindestens 3 Zeichen haben.");
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Das Passwort muss mindestens 8 Zeichen haben.");
        }

        if (users.findByUsername(normalizedUsername).isPresent()) {
            throw new IllegalArgumentException("Dieser Benutzername ist bereits vergeben.");
        }

        users.createUser(
            normalizedUsername,
            passwordEncoder.encode(password),
            UserRole.USER,
            false
        );
    }

    public List<AppUser> pendingUsers()
    {
        return users.findPendingUsers();
    }

    @Transactional
    public void approveUser(long userId)
    {
        users.approveUser(userId);
    }
}
