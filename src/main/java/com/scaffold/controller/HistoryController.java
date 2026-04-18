package com.scaffold.controller;

import com.scaffold.entity.Calculation;
import com.scaffold.model.LadderTowerResult;
import com.scaffold.model.LoadingBayResult;
import com.scaffold.service.AccessTowerService;
import com.scaffold.service.CalculationService;
import com.scaffold.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {

    private final CalculationService calculationService;
    private final UserService userService;
    private final AccessTowerService accessTowerService;

    // Vartotojo savi skaičiavimai
    @GetMapping
    public String myHistory(Model model, Principal principal) {
        Long userId = userService.getByUsername(principal.getName()).getId();
        List<Calculation> calculations = calculationService.findByUser(userId);
        model.addAttribute("calculations", calculations);
        model.addAttribute("viewAll", false);
        addAccessTowerMaps(model, calculations);
        return "history";
    }

    // Visi skaičiavimai (tik MANAGER ir ADMIN)
    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
    public String allHistory(Model model) {
        List<Calculation> calculations = calculationService.findAll();
        model.addAttribute("calculations", calculations);
        model.addAttribute("viewAll", true);
        addAccessTowerMaps(model, calculations);
        return "history";
    }

    private void addAccessTowerMaps(Model model, List<Calculation> calculations) {
        Map<Long, LoadingBayResult> loadingBays = calculations.stream()
                .collect(Collectors.toMap(
                        Calculation::getId,
                        c -> accessTowerService.calculateLoadingBay(c.getLifts().size())
                ));
        Map<Long, LadderTowerResult> ladderTowers = calculations.stream()
                .collect(Collectors.toMap(
                        Calculation::getId,
                        c -> accessTowerService.calculateLadderTower(c.getLifts().size())
                ));
        model.addAttribute("loadingBays", loadingBays);
        model.addAttribute("ladderTowers", ladderTowers);
    }
}
