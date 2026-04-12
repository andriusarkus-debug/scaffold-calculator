package com.scaffold.controller;

import com.scaffold.service.CalculationService;
import com.scaffold.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {

    private final CalculationService calculationService;
    private final UserService userService;

    // Vartotojo savi skaičiavimai
    @GetMapping
    public String myHistory(Model model, Principal principal) {
        Long userId = userService.getByUsername(principal.getName()).getId();
        model.addAttribute("calculations", calculationService.findByUser(userId));
        model.addAttribute("viewAll", false);
        return "history";
    }

    // Visi skaičiavimai (tik MANAGER ir ADMIN)
    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
    public String allHistory(Model model) {
        model.addAttribute("calculations", calculationService.findAll());
        model.addAttribute("viewAll", true);
        return "history";
    }
}
