package com.wolfsnetz.webserver.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(properties = {
    "app.display-name=Test Webserver",
    "app.bootstrap-admin.username=admin",
    "app.bootstrap-admin.password=admin123"
})
class UserServiceTest
{
    private static final Path DATABASE = Path.of("build", "test-data", "user-service.sqlite");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) throws Exception
    {
        Files.deleteIfExists(DATABASE);
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + DATABASE);
    }

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DatabaseUserDetailsService userDetailsService;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void bootstrapAdminIsCreatedOnlyOnce()
    {
        assertThat(userRepository.findByUsername("admin"))
            .get()
            .satisfies(admin -> {
                assertThat(admin.role()).isEqualTo(UserRole.ADMIN);
                assertThat(admin.approved()).isTrue();
            });

        userService.initialize();

        Integer adminCount = jdbc.queryForObject(
            "select count(*) from users where role = 'ADMIN'",
            Integer.class
        );

        assertThat(adminCount).isEqualTo(1);
    }

    @Test
    void registeredUsersArePendingUntilApproved()
    {
        userService.register("pending-user", "password123");

        AppUser pendingUser = userRepository.findByUsername("pending-user").orElseThrow();

        assertThat(pendingUser.role()).isEqualTo(UserRole.USER);
        assertThat(pendingUser.approved()).isFalse();

        UserDetails userDetails = userDetailsService.loadUserByUsername("pending-user");

        assertThat(userDetails.isEnabled()).isFalse();
        assertThat(userService.pendingUsers())
            .extracting(AppUser::username)
            .contains("pending-user");
    }

    @Test
    void approvingUserEnablesLogin()
    {
        userService.register("approved-user", "password123");

        AppUser user = userRepository.findByUsername("approved-user").orElseThrow();
        userService.approveUser(user.id());

        AppUser approvedUser = userRepository.findByUsername("approved-user").orElseThrow();
        UserDetails userDetails = userDetailsService.loadUserByUsername("approved-user");

        assertThat(approvedUser.approved()).isTrue();
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void duplicateUsernamesAreRejected()
    {
        userService.register("duplicate-user", "password123");

        assertThatThrownBy(() -> userService.register("duplicate-user", "password123"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("bereits vergeben");
    }
}
