package com.scaffold.controller;

import com.scaffold.dto.RegisterRequest;
import com.scaffold.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // Rodo prisijungimo puslapį.
    // Spring Security pats apdoroja POST /login — mums nereikia POST metodo čia.
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String registered,
                            Model model) {
        if (error != null) {
            // "disabled" — paskyra egzistuoja, bet administratorius dar nepatvirtino
            if ("disabled".equals(error)) {
                model.addAttribute("error", "Your account is awaiting administrator approval.");
            } else {
                model.addAttribute("error", "Invalid username or password.");
            }
        }
        if (logout != null)     model.addAttribute("message", "Logged out successfully.");
        if (registered != null) model.addAttribute("message", "Account created. Awaiting administrator approval — you will gain access once approved.");
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterRequest registerRequest, Model model) {
        try {
            userService.register(registerRequest);
            return "redirect:/login?registered";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registerRequest", registerRequest);
            return "register";
        }
    }
}
