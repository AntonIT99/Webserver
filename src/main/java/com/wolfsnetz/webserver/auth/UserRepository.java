package com.wolfsnetz.webserver.auth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository
{
    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc)
    {
        this.jdbc = jdbc;
    }

    public void initializeSchema()
    {
        jdbc.execute("""
            create table if not exists users (
                id integer primary key autoincrement,
                username text not null unique,
                password_hash text not null,
                role text not null,
                approved integer not null default 0,
                created_at text not null
            )
            """);
    }

    public Optional<AppUser> findByUsername(String username)
    {
        try {
            return Optional.ofNullable(jdbc.queryForObject(
                "select * from users where lower(username) = lower(?)",
                this::mapUser,
                username
            ));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean adminExists()
    {
        Integer count = jdbc.queryForObject("select count(*) from users where role = 'ADMIN'", Integer.class);
        return count != null && count > 0;
    }

    public List<AppUser> findPendingUsers()
    {
        return jdbc.query(
            "select * from users where role = 'USER' and approved = 0 order by created_at asc",
            this::mapUser
        );
    }

    public void createUser(String username, String passwordHash, UserRole role, boolean approved)
    {
        jdbc.update(
            "insert into users (username, password_hash, role, approved, created_at) values (?, ?, ?, ?, ?)",
            username,
            passwordHash,
            role.name(),
            approved ? 1 : 0,
            Instant.now().toString()
        );
    }

    public void approveUser(long userId)
    {
        jdbc.update("update users set approved = 1 where id = ? and role = 'USER'", userId);
    }

    private AppUser mapUser(ResultSet rs, int rowNum) throws SQLException
    {
        return new AppUser(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            UserRole.valueOf(rs.getString("role")),
            rs.getInt("approved") == 1,
            Instant.parse(rs.getString("created_at"))
        );
    }
}
