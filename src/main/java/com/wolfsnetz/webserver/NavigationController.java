package com.wolfsnetz.webserver;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NavigationController
{
    @GetMapping({"/games", "/games/"})
    public String games()
    {
        return "redirect:/games/index.html";
    }

    @GetMapping({"/games/minecraft", "/games/minecraft/"})
    public String gamesMinecraft()
    {
        return "redirect:/games/minecraft/index.html";
    }

    @GetMapping({"/games/minecraft/server", "/games/minecraft/server/"})
    public String gamesMinecraftServer()
    {
        return "redirect:/games/minecraft/server/index.html";
    }
}
