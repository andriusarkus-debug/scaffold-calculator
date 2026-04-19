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
import java.util.ArrayList;
import java.util.List;

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
        addAccessTowerLists(model, calculations);
        return "history";
    }

    // Visi skaičiavimai (tik MANAGER ir ADMIN)
    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
    public String allHistory(Model model) {
        List<Calculation> calculations = calculationService.findAll();
        model.addAttribute("calculations", calculations);
        model.addAttribute("viewAll", true);
        addAccessTowerLists(model, calculations);
        return "history";
    }

    private void addAccessTowerLists(Model model, List<Calculation> calculations) {
        List<LoadingBayResult> lbList = new ArrayList<>();
        List<LadderTowerResult> ltList = new ArrayList<>();
        for (Calculation calc : calculations) {
            int lifts = calc.getLifts() != null ? calc.getLifts().size() : 0;
            lbList.add(accessTowerService.calculateLoadingBay(lifts));
            ltList.add(accessTowerService.calculateLadderTower(lifts));
        }
        model.addAttribute("lbList", lbList);
        model.addAttribute("ltList", ltList);
    }
}
