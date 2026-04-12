package com.scaffold.controller;

import com.scaffold.entity.Role;
import com.scaffold.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        model.addAttribute("roles", Role.values());
        return "admin/users";
    }

    // Thymeleaf formose galimas tik GET ir POST — naudojame POST vietoj PUT
    @PostMapping("/users/{id}/role")
    public String updateRole(@PathVariable Long id, @RequestParam String role) {
        adminService.updateRole(id, Role.valueOf(role));
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/active")
    public String updateActive(@PathVariable Long id, @RequestParam boolean active) {
        adminService.updateActive(id, active);
        return "redirect:/admin/users";
    }
}
