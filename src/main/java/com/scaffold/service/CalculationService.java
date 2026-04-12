package com.scaffold.service;

import com.scaffold.entity.Calculation;
import com.scaffold.entity.CalculationLift;
import com.scaffold.entity.User;
import com.scaffold.model.LiftInput;
import com.scaffold.model.MaterialResult;
import com.scaffold.model.ScaffoldInput;
import com.scaffold.repository.CalculationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculationService {

    private final CalculationRepository calculationRepository;

    // Išsaugo skaičiavimo įvesties duomenis ir rezultatą duomenų bazėje
    public Calculation save(ScaffoldInput input, MaterialResult result, User user) {

        Calculation calculation = Calculation.builder()
                .user(user)
                // --- Input ---
                .projectName(input.getProjectName())
                .houseShape(input.getHouseShape())
                .houseLength(input.getHouseLength())
                .houseWidth(input.getHouseWidth())
                .lCutLength(input.getLCutLength())
                .lCutWidth(input.getLCutWidth())
                .roofType(input.getRoofType())
                .roofPitch(input.getRoofPitch())
                .gableEnds(input.getGableEnds())
                .tubeSize(input.getTubeSize())
                .ledgerScenario(input.getLedgerScenario())
                // --- Calculations ---
                .perimeter(result.getPerimeter())
                .bays(result.getBays())
                .totalHeight(result.getTotalHeight())
                .tubesPerStandard(result.getTubesPerStandard())
                // --- Main components ---
                .standards(result.getStandards())
                .ledgers(result.getLedgers())
                .handrails(result.getHandrails())
                .advanceGuardRailSets(result.getAdvanceGuardRailSets())
                .transoms(result.getTransoms())
                .boards(result.getBoards())
                .basePlates(result.getBasePlates())
                .soleBoards(result.getSoleBoards())
                // --- Fittings ---
                .rightAngleCouplers(result.getRightAngleCouplers())
                .swivelCouplers(result.getSwivelCouplers())
                .sleeveCouplers(result.getSleeveCouplers())
                .putlogCouplers(result.getPutlogCouplers())
                // --- Other elements ---
                .swayBracing(result.getSwayBracing())
                .ledgerBracing(result.getLedgerBracing())
                .toeboards(result.getToeboards())
                // --- Gables ---
                .gableStandards(result.getGableStandards())
                .gableCouplers(result.getGableCouplers())
                // --- Returns ---
                .returnCount(result.getReturnCount())
                .returnPlatformBoards(result.getReturnPlatformBoards())
                .returnPlatformTransoms(result.getReturnPlatformTransoms())
                .returnPlatformLedgers(result.getReturnPlatformLedgers())
                // --- Tube counts ---
                .tubeCount5ft(result.getTubeCount5ft())
                .tubeCount6ft(result.getTubeCount6ft())
                .tubeCount8ft(result.getTubeCount8ft())
                .tubeCountStandardSize(result.getTubeCountStandardSize())
                .standardTubeSize(result.getStandardTubeSize())
                // --- Sway bracing ---
                .swayBraceTubeCount(result.getSwayBraceTubeCount())
                .swayBraceTubeSize(result.getSwayBraceTubeSize())
                // --- Ledger scenario ---
                .transomsSavedByTopLedgers(result.getTransomsSavedByTopLedgers())
                // --- Map summaries ---
                .boardSummary(result.getBoardSummary())
                .ledgerTubeSummary(result.getLedgerTubeSummary())
                .handrailTubeSummary(result.getHandrailTubeSummary())
                .ledgerBraceTubeSummary(result.getLedgerBraceTubeSummary())
                .standardTubeSummary(result.getStandardTubeSummary())
                .build();

        // Sukuriame liftų sąrašą ir susiejame su skaičiavimu
        List<CalculationLift> liftEntities = new ArrayList<>();
        List<LiftInput> inputLifts = input.getLifts();
        for (int i = 0; i < inputLifts.size(); i++) {
            LiftInput liftInput = inputLifts.get(i);
            liftEntities.add(CalculationLift.builder()
                    .calculation(calculation)
                    .liftNumber(i + 1)
                    .height(liftInput.getHeight())
                    .hasBoards(liftInput.isHasBoards())
                    .boardSize(liftInput.getBoardSize())
                    .build());
        }
        calculation.setLifts(liftEntities);

        return calculationRepository.save(calculation);
    }

    // Grąžina prisijungusio vartotojo skaičiavimų istoriją
    public List<Calculation> findByUser(Long userId) {
        return calculationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Grąžina visų vartotojų skaičiavimų istoriją (ROLE_MANAGER+)
    public List<Calculation> findAll() {
        return calculationRepository.findAllByOrderByCreatedAtDesc();
    }
}
