package com.wolfsnetz.webserver;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(properties = {
    "app.display-name=Test Webserver",
    "app.bootstrap-admin.username=admin",
    "app.bootstrap-admin.password=admin123"
})
class WebserverApplicationTests
{
    private static final Path DATABASE = Path.of("build", "test-data", "context.sqlite");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) throws Exception
    {
        Files.deleteIfExists(DATABASE);
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + DATABASE);
    }

    @Test
    void contextLoads()
    {
    }

}
