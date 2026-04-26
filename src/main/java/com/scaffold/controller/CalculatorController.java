package com.scaffold.controller;

import com.scaffold.entity.User;
import com.scaffold.exception.DomainException;
import com.scaffold.model.LiftInput;
import com.scaffold.model.MaterialResult;
import com.scaffold.model.ScaffoldInput;
import com.scaffold.model.enums.HouseShape;
import com.scaffold.model.enums.LedgerScenario;
import com.scaffold.model.enums.RoofType;
import com.scaffold.service.CalculationService;
import com.scaffold.service.TubeAndCouplerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CalculatorController {

    private static final Logger log = LoggerFactory.getLogger(CalculatorController.class);
    private static final int FORM_LIFT_SLOTS = 4; // Thymeleaf formai reikalingas fiksuotas sąrašo dydis

    private final TubeAndCouplerService tubeAndCouplerService;
    private final CalculationService calculationService;

    // Rodo tuščią skaičiuoklės formą
    @GetMapping("/calculator")
    public String showForm(Model model) {
        ScaffoldInput input = new ScaffoldInput();
        input.setLifts(emptyLiftSlots());
        populateFormModel(model, input);
        return "calculator";
    }

    // Apdoroja formą, atlieka skaičiavimą, išsaugo į DB
    @PostMapping("/calculator")
    public String calculate(@ModelAttribute ScaffoldInput input,
                            @RequestParam int liftCount,
                            @AuthenticationPrincipal User currentUser,
                            Model model) {
        try {
            input.normalizeForCalculation(liftCount);

            MaterialResult result = tubeAndCouplerService.calculate(input);
            com.scaffold.entity.Calculation saved = calculationService.save(input, result, currentUser);

            model.addAttribute("result", result);
            model.addAttribute("input", input);
            model.addAttribute("calculationId", saved.getId()); // PDF eksporto nuorodai
            return "result";

        } catch (DomainException e) {
            // Žinomos domeno klaidos — vartotojui rodome konkrečią žinutę
            return showFormWithError(model, input, e.getMessage());

        } catch (RuntimeException e) {
            // Netikėta klaida — loginame pilnai, vartotojui rodome bendrinę žinutę
            log.error("Netikėta skaičiavimo klaida", e);
            return showFormWithError(model, input, "Įvyko skaičiavimo klaida. Patikrinkite įvestis ir bandykite dar kartą.");
        }
    }

    // --- Pagalbiniai metodai ---

    /** Po klaidos grąžiname formą su originaliom įvestim ir klaidos pranešimu. */
    private String showFormWithError(Model model, ScaffoldInput input, String message) {
        restoreLiftSlots(input);
        model.addAttribute("error", message);
        populateFormModel(model, input);
        return "calculator";
    }

    /** Thymeleaf formai reikalingi 4 liftų slotai — po klaidos atstatome trūkstamus. */
    private void restoreLiftSlots(ScaffoldInput input) {
        List<LiftInput> lifts = input.getLifts() != null
                ? new ArrayList<>(input.getLifts())
                : new ArrayList<>();
        while (lifts.size() < FORM_LIFT_SLOTS) lifts.add(new LiftInput());
        input.setLifts(lifts);
    }

    private List<LiftInput> emptyLiftSlots() {
        List<LiftInput> lifts = new ArrayList<>(FORM_LIFT_SLOTS);
        for (int i = 0; i < FORM_LIFT_SLOTS; i++) lifts.add(new LiftInput());
        return lifts;
    }

    private void populateFormModel(Model model, ScaffoldInput input) {
        model.addAttribute("input", input);
        model.addAttribute("houseShapes", HouseShape.values());
        model.addAttribute("roofTypes", RoofType.values());
        model.addAttribute("ledgerScenarios", LedgerScenario.values());
    }
}
