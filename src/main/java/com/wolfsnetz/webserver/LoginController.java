package com.wolfsnetz.webserver;

import com.wolfsnetz.webserver.auth.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController
{
    private final UserService users;

    public LoginController(UserService users)
    {
        this.users = users;
    }

    @GetMapping("/login")
    public String login()
    {
        return "login";
    }

    @GetMapping("/register")
    public String register()
    {
        return "register";
    }

    @PostMapping("/register")
    public String register(
        @RequestParam String username,
        @RequestParam String password,
        Model model
    ) {
        try {
            users.register(username, password);
            model.addAttribute("success", "Registrierung gespeichert. Ein Admin muss deinen Zugang noch freigeben.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
        }

        return "register";
    }
}
