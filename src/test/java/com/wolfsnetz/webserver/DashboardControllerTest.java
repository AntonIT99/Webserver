package com.wolfsnetz.webserver;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "app.display-name=Test Webserver",
    "app.bootstrap-admin.username=admin",
    "app.bootstrap-admin.password=admin123"
})
@AutoConfigureMockMvc
class DashboardControllerTest
{
    private static final Path DATABASE = Path.of("build", "test-data", "dashboard.sqlite");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) throws Exception
    {
        Files.deleteIfExists(DATABASE);
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + DATABASE);
    }

    @Autowired
    private MockMvc mvc;

    @Test
    void dashboardRoutesAdminsToAdminDashboard() throws Exception
    {
        mvc.perform(get("/dashboard").with(user("admin").roles("ADMIN")))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/dashboard"));
    }

    @Test
    void dashboardRoutesUsersToUserDashboard() throws Exception
    {
        mvc.perform(get("/dashboard").with(user("user").roles("USER")))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/user/dashboard"));
    }

    @Test
    void adminDashboardRequiresAdminRole() throws Exception
    {
        mvc.perform(get("/admin/dashboard").with(user("user").roles("USER")))
            .andExpect(status().isForbidden());
    }

    @Test
    void userDashboardRequiresUserRole() throws Exception
    {
        mvc.perform(get("/user/dashboard").with(user("admin").roles("ADMIN")))
            .andExpect(status().isForbidden());
    }
}
