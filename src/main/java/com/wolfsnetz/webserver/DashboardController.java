package com.wolfsnetz.webserver;

import com.wolfsnetz.webserver.auth.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardController
{
    private final UserService users;

    public DashboardController(UserService users)
    {
        this.users = users;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication)
    {
        boolean admin = authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        return admin ? "redirect:/admin/dashboard" : "redirect:/user/dashboard";
    }

    @GetMapping("/user/dashboard")
    @PreAuthorize("hasRole('USER')")
    public String userDashboard()
    {
        return "user-dashboard";
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model)
    {
        model.addAttribute("pendingUsers", users.pendingUsers());
        return "admin-dashboard";
    }

    @PostMapping("/admin/users/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public String approveUser(@RequestParam long userId)
    {
        users.approveUser(userId);
        return "redirect:/admin/dashboard";
    }
}
