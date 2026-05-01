package com.wolfsnetz.webserver.minecraft;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minecraft")
public record MinecraftProperties(
    String host,
    int port,
    int timeoutMs
) {}
