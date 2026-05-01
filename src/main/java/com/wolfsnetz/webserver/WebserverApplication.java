package com.wolfsnetz.webserver;

import com.wolfsnetz.webserver.minecraft.MinecraftProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(MinecraftProperties.class)
@SpringBootApplication
public class WebserverApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(WebserverApplication.class, args);
    }
}
