package com.wolfsnetz.webserver.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AuthBootstrap implements CommandLineRunner
{
    private final UserService users;

    public AuthBootstrap(UserService users)
    {
        this.users = users;
    }

    @Override
    public void run(String... args)
    {
        users.initialize();
    }
}
