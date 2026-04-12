package com.scaffold.controller;

import com.scaffold.model.LiftInput;
import com.scaffold.model.MaterialResult;
import com.scaffold.model.ScaffoldInput;
import com.scaffold.model.enums.*;
import com.scaffold.service.CalculationService;
import com.scaffold.service.TubeAndCouplerService;
import com.scaffold.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CalculatorController {

    private final TubeAndCouplerService tubeAndCouplerService;
    private final CalculationService calculationService;
    private final UserService userService;

    // Rodo tuščią skaičiuoklės formą
    @GetMapping("/calculator")
    public String showForm(Model model) {
        ScaffoldInput input = new ScaffoldInput();
        // Iš anksto sukuriame 4 liftų objektus — Thymeleaf reikalingas fiksuotas sąrašo dydis
        List<LiftInput> lifts = new ArrayList<>();
        for (int i = 0; i < 4; i++) lifts.add(new LiftInput());
        input.setLifts(lifts);
        addEnumsToModel(model);
        model.addAttribute("input", input);
        return "calculator";
    }

    // Apdoroja formą, atlieka skaičiavimą, išsaugo į DB
    @PostMapping("/calculator")
    public String calculate(@ModelAttribute ScaffoldInput input,
                            @RequestParam int liftCount,
                            Model model,
                            Principal principal) {
        try {
            // Pasiliekame tik tiek liftų, kiek vartotojas pasirinko
            input.setLifts(input.getLifts().subList(0, liftCount));

            // Lentų dydis visada 13ft (standartas namams) — vartotojui nereikia rinktis
            input.getLifts().forEach(lift -> {
                if (lift.isHasBoards()) lift.setBoardSize(BoardSize.THIRTEEN_FOOT);
            });

            // Standartų vamzdžio dydis: 2 liftai → 10ft/13ft (skaičiuojama automatiška),
            // 3+ liftai → 21ft. Vartotojas nesirenka — auto.
            input.setTubeSize(TubeSize.TWENTY_ONE_FOOT);

            MaterialResult result = tubeAndCouplerService.calculate(input);
            calculationService.save(input, result, userService.getByUsername(principal.getName()));
            model.addAttribute("result", result);
            model.addAttribute("input", input);
            return "result";
        } catch (Exception e) {
            model.addAttribute("error", "Skaičiavimo klaida: " + e.getMessage());
            addEnumsToModel(model);
            // Atstatome 4 liftų sąrašą jei klaida
            if (input.getLifts() == null || input.getLifts().size() < 4) {
                List<LiftInput> lifts = new ArrayList<>(input.getLifts() != null ? input.getLifts() : List.of());
                while (lifts.size() < 4) lifts.add(new LiftInput());
                input.setLifts(lifts);
            }
            model.addAttribute("input", input);
            return "calculator";
        }
    }

    private void addEnumsToModel(Model model) {
        model.addAttribute("houseShapes", HouseShape.values());
        model.addAttribute("roofTypes", RoofType.values());
        model.addAttribute("ledgerScenarios", LedgerScenario.values());
    }
}
