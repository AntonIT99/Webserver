package com.wolfsnetz.webserver.minecraft;

public record MinecraftStatus(
    boolean online,
    String version,
    int onlinePlayers,
    int maxPlayers,
    String motd,
    String error
) {}
