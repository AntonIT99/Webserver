package com.wolfsnetz.webserver.auth;

import java.util.List;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService
{
    private final UserRepository users;

    public DatabaseUserDetailsService(UserRepository users)
    {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        AppUser user = users.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new User(
            user.username(),
            user.passwordHash(),
            user.approved(),
            true,
            true,
            true,
            List.of(() -> "ROLE_" + user.role().name())
        );
    }
}
