package com.scaffold.service;

import com.scaffold.model.enums.BoardSize;
import com.scaffold.model.enums.HouseShape;
import com.scaffold.model.enums.LedgerScenario;
import com.scaffold.model.LiftInput;
import com.scaffold.model.MaterialResult;
import com.scaffold.model.enums.RoofType;
import com.scaffold.model.ScaffoldConstants;
import com.scaffold.model.ScaffoldInput;
import com.scaffold.model.enums.TubeSize;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Pagrindinė skaičiavimo paslauga — apskaičiuoja visas medžiagas Tube & Coupler pastolių tipui
@Service
@RequiredArgsConstructor
    public class TubeAndCouplerService {

    private static final double BAY_SPACING = ScaffoldConstants.BAY_SPACING;
    private static final double MAX_OVERHANG_M = ScaffoldConstants.MAX_OVERHANG_M;
    private static final double RETURN_WIDTH_M = ScaffoldConstants.RETURN_WIDTH_M;
    private static final int BOARDS_PER_ROW = ScaffoldConstants.BOARDS_PER_ROW;

    private final GableService gableService;
    private final AccessTowerService accessTowerService;

    // Grąžina sienos ilgius (be returnų) kiekvienai namo pusei.
    // RECTANGULAR: 4 sienos [L, W, L, W]
    // L_SHAPE: 6 sienos pagal laikrodžio rodyklę nuo apačios:
    //   [L, W-CW, CL, CW, L-CL, W]
    //   kur CL = lCutLength (horizontali išpjova), CW = lCutWidth (vertikali išpjova)
    private List<Double> getFaceLengths(ScaffoldInput input) {
        List<Double> faces = new ArrayList<>();
        double L = input.getHouseLength();
        double W = input.getHouseWidth();
        if (input.getHouseShape() == HouseShape.L_SHAPE) {
            double CL = input.getLCutLength();
            double CW = input.getLCutWidth();
            faces.add(L);          // Apačia (pilnas ilgis)
            faces.add(W - CW);     // Dešinė apačia (žemiau išpjovos)
            faces.add(CL);         // Išpjovos horizontali dalis (einant kairėn)
            faces.add(CW);         // Išpjovos vertikali dalis (einant aukštyn)
            faces.add(L - CL);     // Viršus (be išpjovos dalies)
            faces.add(W);          // Kairė (pilnas plotis)
        } else {
            faces.add(L);          // Priekis
            faces.add(W);          // Dešinė
            faces.add(L);          // Galas
            faces.add(W);          // Kairė
        }
        return faces;
    }

    // Grąžina tikruosius pastolių bėgio ilgius (siena + returnai) kiekvienai pusei.
    // L formos namo kampas D yra įgaubtas (vidinis) — returnas ten eina į pastatą, todėl neleidžiamas.
    // Todėl C→D (index 2) gauna tik vieną returną (C gale), D→E (index 3) — tik vieną (E gale).
    // Visi kiti kampai gauna po du returnus kaip įprasta.
    private List<Double> getScaffoldRunLengths(ScaffoldInput input) {
        List<Double> wallLengths = getFaceLengths(input);
        List<Double> runs = new ArrayList<>();
        if (input.getHouseShape() == HouseShape.L_SHAPE) {
            for (int i = 0; i < wallLengths.size(); i++) {
                double wall = wallLengths.get(i);
                if (i == 2 || i == 3) {
                    // C→D ir D→E: kampas D neturi returno — tik vienas returnas
                    runs.add(wall + RETURN_WIDTH_M);
                } else {
                    runs.add(wall + 2 * RETURN_WIDTH_M);
                }
            }
        } else {
            for (double wall : wallLengths) {
                runs.add(wall + 2 * RETURN_WIDTH_M);
            }
        }
        return runs;
    }

    public MaterialResult calculate(ScaffoldInput input) {

        // --- 1. Perimetras pagal namo formą ---
        // Stačiakampio ir L formos namo perimetras matematiškai yra vienodas:
        // 2 × (ilgis + plotis) — L išpjova prideda dvi sienas, bet atima tiek pat
        double perimeter = 2 * (input.getHouseLength() + input.getHouseWidth()); // Perimetras metrais

        int bays = (int) Math.ceil(perimeter / BAY_SPACING);                    // Sekcijų skaičius — kas 1.8m viena sekcija
        int lifts = input.getLifts().size();                                     // Liftų (aukštų) skaičius

        double totalHeight = input.getLifts().stream()
                .mapToDouble(LiftInput::getHeight)
                .sum();                                                           // Bendras aukštis = visų liftų aukščių suma

        // --- 2. Returnų skaičius ir sienos pagal namo formą ---
        // Stačiakampis: 4 kampai, L forma: 5 kampai (kampas D yra įgaubtas — returnas draudžiamas)
        int returnCount = (input.getHouseShape() == HouseShape.L_SHAPE) ? 5 : 4;
        List<Double> faceLengths = getFaceLengths(input);
        List<Double> scaffoldRuns = getScaffoldRunLengths(input);

        // --- 3. Pagrindiniai pastolių elementai ---
        double tubeLengthM = input.getTubeSize() != null
                ? input.getTubeSize().getLengthM()
                : 6.401;                                                          // Numatytasis 21ft jei nenurodytas
        // Stovų pozicijos: (bays+1)×2 plius 1 papildomas stovas kiekviename returno kampe
        int standardPositions = (bays + 1) * 2 + returnCount;
        int tubesPerStandard = (int) Math.ceil(totalHeight / tubeLengthM);       // Kiek vamzdžių reikia vienam stovui pagal aukštį
        int standards = standardPositions * tubesPerStandard;                    // Iš viso stovų vamzdžių gabalų

        // --- Standartų suvestinė pagal dydį (inside vs outside) ---
        // 2 liftai: inside = 10ft, outside = 13ft (vienas vamzdis pakankamas žemam pastolių)
        // 3+ liftai: visi standartai = 21ft
        // insidePositions = outsidePositions = (bays+1) + returnCount/2
        int insidePositions = (bays + 1) + returnCount / 2;
        int outsidePositions = (bays + 1) + returnCount / 2;
        Map<String, Integer> standardTubeSummary = new LinkedHashMap<>();
        if (lifts <= 2) {
            int insideTubes = insidePositions * (int) Math.ceil(totalHeight / TubeSize.TEN_FOOT.getLengthM());
            int outsideTubes = outsidePositions * (int) Math.ceil(totalHeight / TubeSize.THIRTEEN_FOOT.getLengthM());
            standardTubeSummary.put("10ft", insideTubes);
            standardTubeSummary.put("13ft", outsideTubes);
        } else {
            standardTubeSummary.put(formatTubeSize(tubeLengthM), standards);
        }

        // --- Transomų skaičius priklauso nuo borto dydžio (fiksuotos taisyklės) ---
        // 5ft, 6ft → 3 transomų | 8ft, 10ft → 4 transomų | 13ft → 5 transomų
        LiftInput firstBoardedLift = input.getLifts().stream()
                .filter(LiftInput::isHasBoards)
                .findFirst()
                .orElse(null);
        BoardSize primaryBoardSize = (firstBoardedLift != null && firstBoardedLift.getBoardSize() != null)
                ? firstBoardedLift.getBoardSize()
                : BoardSize.THIRTEEN_FOOT;                                       // Numatytasis 13ft
        double boardFleetLength = primaryBoardSize.getLengthM();
        int transomCountPerFleet = primaryBoardSize.getTransomCount();

        // --- 4. Transom positions — iterate over all faces ---
        // Each face (with returns) = one continuous run
        // Transoms per face = fleets × (transomCount - 1) + 1
        int transomPositions = 0;
        for (double faceLength : scaffoldRuns) {
            int fleets = (int) Math.ceil(faceLength / boardFleetLength);
            transomPositions += fleets * (transomCountPerFleet - 1) + 1;
        }
        // Viršutinis ledgeris ties kiekvienu kampu veikia kaip transonas (inside + outside = 2 per kampą)
        // Kampų skaičius = sienų skaičius (kiekviena siena baigiasi kampu)
        int corners = faceLengths.size();
        int transomsSavedByTopLedgers = corners * 2;
        int transoms = transomPositions * (lifts + 1) - transomsSavedByTopLedgers;

        // --- 5. Lentos (skaičiuojamos pagal kiekvieną sieną atskirai) ---
        // Bortai skaičiuojami tik VIENAI darbo platformai —
        // progressing scaffolde tie patys bortai keliami aukštyn su kiekvienu liftu
        Map<String, Integer> boardSummary = new LinkedHashMap<>();               // Bortų kiekis pagal dydį

        LiftInput boardedLift = input.getLifts().stream()
                .filter(LiftInput::isHasBoards)
                .findFirst()
                .orElse(null);                                                   // Imame pirmą liftą su lentomis

        Map<String, String> faceBoardBreakdown = new LinkedHashMap<>();
        if (boardedLift != null) {
            double primaryLength = boardedLift.getBoardSize() != null
                    ? boardedLift.getBoardSize().getLengthM()
                    : 3.962;
            String primaryName = formatTubeSize(primaryLength);
            LedgerScenario scenario = input.getLedgerScenario() != null
                    ? input.getLedgerScenario() : LedgerScenario.SCENARIO_ONE;
            String[] boardFaceNames = buildFaceNames(input, faceLengths);
            for (int i = 0; i < faceLengths.size(); i++) {
                // TOP face: boards cover full scaffold run (wall + returns)
                // BOTTOM face: boards cover wall length only
                double boardLength = isTopFace(i, scenario) ? scaffoldRuns.get(i) : faceLengths.get(i);
                addFaceBoards(boardSummary, boardLength, primaryLength, primaryName, 1);
                String topBot = isTopFace(i, scenario) ? "TOP" : "BOTTOM";
                String key = boardFaceNames[i] + " – " + topBot;
                faceBoardBreakdown.put(key, boardComboString(boardLength, primaryLength, primaryName));
            }
        }

        int boards = boardSummary.values().stream().mapToInt(Integer::intValue).sum();

        // --- 6. Pagrindo elementai ---
        int basePlates = standardPositions;                                       // Po vieną kiekvienam stovui
        int soleBoards = standardPositions;

        // --- 7. Ledger ir handrail vamzdžiai — iteruojame per visas sienas ---
        Map<String, Integer> ledgerTubeSummary = new LinkedHashMap<>();
        Map<String, Integer> handrailTubeSummary = new LinkedHashMap<>();
        for (double faceLength : scaffoldRuns) {
            Map<String, Integer> faceRunTubes = tubesForRun(faceLength);
            mergeTubes(ledgerTubeSummary, faceRunTubes, 2 * (lifts + 1)); // inside + outside × visų lygių
            mergeTubes(handrailTubeSummary, faceRunTubes, 2 * lifts);     // top + mid rail × liftų
        }

        // --- 7b. Per-face ledger vamzdžių kombinacijos (informaciniam rodymui) ---
        LedgerScenario scenarioForFaces = input.getLedgerScenario() != null
                ? input.getLedgerScenario() : LedgerScenario.SCENARIO_ONE;
        String[] faceNames = buildFaceNames(input, faceLengths);
        Map<String, String> faceLedgerTubeBreakdown = new LinkedHashMap<>();
        for (int i = 0; i < scaffoldRuns.size(); i++) {
            double run = scaffoldRuns.get(i);
            Map<String, Integer> combo = tubesForRun(run);
            String tubeCombo = combo.entrySet().stream()
                    .map(e -> e.getValue() > 1 ? e.getKey() + "×" + e.getValue() : e.getKey())
                    .collect(Collectors.joining(" + "));
            String topBot = isTopFace(i, scenarioForFaces) ? "TOP" : "BOTTOM";
            String key = faceNames[i] + " – " + topBot;
            String value = tubeCombo + "  [run: " + String.format("%.2f", run) + "m]";
            faceLedgerTubeBreakdown.put(key, value);
        }

        // --- 8. Ledgerių ir handrailų SKAIČIUS ---
        // Kiekvienai sienai: ceil(sienos ilgis / 1.8) × 2 (inside+outside) + returnCount kampai
        int ledgersPerLevel = returnCount;
        for (double wallLength : faceLengths) {
            ledgersPerLevel += (int) Math.ceil(wallLength / BAY_SPACING) * 2;
        }
        int ledgers = ledgersPerLevel * (lifts + 1);                             // × visų lygių skaičius

        // Handrailai — tas pats bay skaičius, bet × 2 (top + mid rail) × liftai (ne lifts+1)
        int handrails = ledgersPerLevel * 2 * lifts;

        // --- 9. Sway bracing — 1 brace every 6 bays per face per lift ---
        int swayBracingSets = 0;
        for (double wallLength : faceLengths) {
            int faceBays = (int) Math.ceil(wallLength / BAY_SPACING);
            swayBracingSets += (int) Math.ceil(faceBays / 6.0);
        }
        int swayBracing = swayBracingSets * lifts;

        // --- 10. Ledger bracing — 1 brace every 2 bays per face per lift ---
        int bracesPerLift = 0;
        for (double wallLength : faceLengths) {
            int faceBays = (int) Math.ceil(wallLength / BAY_SPACING);
            bracesPerLift += (int) Math.ceil(faceBays / 2.0);
        }
        int ledgerBracing = bracesPerLift * lifts;

        // Ledger brace vamzdžių suvestinė — dydis priklauso nuo lifto aukščio
        // Taisyklė: mažiausias vamzdis kurio ilgis > lifto aukštis + 0.25m (pvz. 1.5m → 6ft)
        Map<String, Integer> ledgerBraceTubeSummary = new LinkedHashMap<>();
        for (LiftInput lift : input.getLifts()) {
            double minLength = lift.getHeight() + 0.25;
            String tubeLabel = "21ft";
            for (TubeSize size : TubeSize.values()) {
                if (size.getLengthM() >= minLength) {
                    tubeLabel = formatTubeSize(size.getLengthM());
                    break;
                }
            }
            ledgerBraceTubeSummary.merge(tubeLabel, bracesPerLift, Integer::sum);
        }

        // --- 11. Jungtys ---
        // Kiekvienas ledger brace: 1× swivel coupler + 1× right-angle coupler
        // Kiekvienas sway brace: 2× swivel couplers (viršus + apačia)
        int swivelCouplers = swayBracing * 2 + ledgerBracing;
        // Right-angle couplers: tik ledgeriai ir handrailai (transomams naudojami putlog couplers atskirai)
        int rightAngleCouplers = (ledgers + handrails) * 2 + ledgerBracing;

        // Sleeve couplers: standartų jungtys + ledger/handrail jungtys
        // Kiekvienas run su N vamzdžių reikalauja N-1 sleeve couplerių
        int joinsPerStandard = Math.max(0, tubesPerStandard - 1);
        int standardSleeveCouplers = standardPositions * joinsPerStandard;
        int ledgerSleeveCouplers = 0;
        int handrailSleeveCouplers = 0;
        for (double faceLength : scaffoldRuns) {
            Map<String, Integer> faceRunTubes = tubesForRun(faceLength);
            int joinsPerRun = Math.max(0, faceRunTubes.size() - 1);
            ledgerSleeveCouplers += 2 * (lifts + 1) * joinsPerRun;
            handrailSleeveCouplers += 2 * lifts * joinsPerRun;
        }
        int sleeveCouplers = standardSleeveCouplers + ledgerSleeveCouplers + handrailSleeveCouplers;

        // --- 12. Advance guard rail ---
        // Viršutinė rankena paliekama kai kitas liftas >= 1.5m (darbuotojų apsauga)
        int advanceGuardRailSets = 0;
        for (int i = 0; i < lifts - 1; i++) {
            if (input.getLifts().get(i + 1).getHeight() >= 1.5) {
                advanceGuardRailSets++;
            }
        }

        // --- 13. Toeboards ---
        int toeboards = bays;

        // --- 14. Returnų walking platformos (5ft = 1.524m kiekviename kampe) ---
        int returnBays = (int) Math.ceil(RETURN_WIDTH_M / BAY_SPACING);

        int returnPlatformTransoms = returnCount * returnBays * 2 * lifts;

        // Putlog couplers — kiekvienas transom reikalauja 2 putlog couplerių (po vieną kiekviename gale)
        int putlogCouplers = (transoms + returnPlatformTransoms) * 2;
        int returnPlatformLedgers = returnCount * returnBays * (lifts + 1);

        int returnPlatformBoards = input.getLifts().stream()
                .filter(LiftInput::isHasBoards)
                .mapToInt(lift -> {
                    double boardLengthM = lift.getBoardSize() != null
                            ? lift.getBoardSize().getLengthM()
                            : 3.962;
                    int baysPerBoard = Math.max(1, (int) (boardLengthM / BAY_SPACING));
                    int runsPerReturn = (int) Math.ceil((double) returnBays / baysPerBoard);
                    return returnCount * runsPerReturn * BOARDS_PER_ROW;
                })
                .sum();

        // --- 15. Vamzdžių užsakymo suvestinė ---
        String standardTubeSize = formatTubeSize(tubeLengthM);
        int tubeCount5ft = transoms + returnPlatformTransoms;
        int tubeCount6ft = ledgerBracing;
        int tubeCount8ft = swayBracing;
        int tubeCountStandardSize = standards;

        // --- 16. Frontono elementai (tik jei stogas yra GABLE tipo) ---
        int gableStandards = 0; // Numatytoji reikšmė — 0 jei ne GABLE tipas
        int gableCouplers = 0;  // Numatytoji reikšmė — 0 jei ne GABLE tipas

        if (input.getRoofType() == RoofType.GABLE) {                             // Tikriname ar stogas yra GABLE tipo
            gableStandards = gableService.calculateGableStandards(input);        // Apskaičiuojame frontono stovus
            gableCouplers = gableService.calculateGableCouplers(input);          // Apskaičiuojame frontono jungtis
        }

        // --- 17. Grąžiname surinktą rezultatą ---
        return MaterialResult.builder()
                .perimeter(perimeter)
                .bays(bays)
                .totalHeight(totalHeight)
                .tubesPerStandard(tubesPerStandard)
                .standards(standards)
                .ledgers(ledgers)
                .handrails(handrails)
                .advanceGuardRailSets(advanceGuardRailSets)
                .transoms(transoms)
                .tubeCount5ft(tubeCount5ft)
                .tubeCount6ft(tubeCount6ft)
                .tubeCount8ft(tubeCount8ft)
                .tubeCountStandardSize(tubeCountStandardSize)
                .standardTubeSize(standardTubeSize)
                .boards(boards)
                .boardSummary(boardSummary)
                .ledgerTubeSummary(ledgerTubeSummary)
                .basePlates(basePlates)
                .soleBoards(soleBoards)
                .rightAngleCouplers(rightAngleCouplers)
                .swayBracing(swayBracing)
                .ledgerBracing(ledgerBracing)
                .swivelCouplers(swivelCouplers)
                .sleeveCouplers(sleeveCouplers)
                .putlogCouplers(putlogCouplers)
                .toeboards(toeboards)
                .returnCount(returnCount)
                .returnPlatformBoards(returnPlatformBoards)
                .returnPlatformTransoms(returnPlatformTransoms)
                .returnPlatformLedgers(returnPlatformLedgers)
                .gableStandards(gableStandards)
                .gableCouplers(gableCouplers)
                .handrailTubeSummary(handrailTubeSummary)
                .ledgerBraceTubeSummary(ledgerBraceTubeSummary)
                .swayBraceTubeCount(swayBracing)
                .swayBraceTubeSize("8ft")
                .standardTubeSummary(standardTubeSummary)
                .ledgerScenario(input.getLedgerScenario() != null ? input.getLedgerScenario() : LedgerScenario.SCENARIO_ONE)
                .transomsSavedByTopLedgers(transomsSavedByTopLedgers)
                .faceLedgerTubeBreakdown(faceLedgerTubeBreakdown)
                .faceBoardBreakdown(faceBoardBreakdown)
                .loadingBay(accessTowerService.calculateLoadingBay(input.getLifts().size()))
                .ladderTower(accessTowerService.calculateLadderTower(input.getLifts().size()))
                .build();
    }

    // Apskaičiuoja vamzdžių kombinaciją vienam ledger/handrail runui.
    // Algoritmas: nusileidžiame nuo 21ft žemyn — kiekvieną dydį naudojame vieną kartą jei telpa.
    // Prieš naudodami vamzdį tikriname: jei likutis reikštų per didelį pakibimą (>0.5m), praleidžiame šį dydį.
    // Kai dydis netelpa, naudojame mažiausią dengiamą vamzdį ir stabdome.
    // Pvz: 13.048m → 21ft+16ft+6ft=13.107m  |  7.048m → 16ft+8ft=7.315m (ne 21ft+5ft=7.925m)
    private Map<String, Integer> tubesForRun(double runLengthM) {
        Map<String, Integer> result = new LinkedHashMap<>();
        double remaining = runLengthM;
        TubeSize[] sizes = TubeSize.values();                                    // FIVE_FOOT → TWENTY_ONE_FOOT

        for (int i = sizes.length - 1; i >= 0 && remaining > 0.05; i--) {
            double len = sizes[i].getLengthM();
            if (len <= remaining) {
                double newRemaining = remaining - len;
                // Jei dar liktų reikšmingas atstumas — tikriname ar pakibimas būtų priimtinas
                if (newRemaining > 0.05 && !anotherTubeFits(newRemaining, i - 1, sizes)) {
                    // Kitas žingsnis būtų dengiamasis vamzdis — tikriname pakibimą
                    double coverLen = smallestCoverLength(newRemaining, sizes);
                    if (coverLen - newRemaining > MAX_OVERHANG_M) {
                        continue;                                                 // Per didelis pakibimas — praleidžiame šį dydį
                    }
                }
                result.put(formatTubeSize(len), 1);
                remaining = newRemaining;
            } else {
                // Šis dydis netelpa — naudojame mažiausią dengiamą vamzdį ir stabdome
                for (TubeSize cover : sizes) {                                   // Nuo mažiausio
                    if (cover.getLengthM() >= remaining) {
                        result.merge(formatTubeSize(cover.getLengthM()), 1, Integer::sum);
                        remaining = 0;
                        break;
                    }
                }
                break;
            }
        }

        return result;
    }

    // Tikrina ar bent vienas mažesnis vamzdis telpa į likusį atstumą
    private boolean anotherTubeFits(double remaining, int maxIndex, TubeSize[] sizes) {
        for (int i = maxIndex; i >= 0; i--) {
            if (sizes[i].getLengthM() <= remaining) return true;
        }
        return false;
    }

    // Nustato ar siena (pagal indeksą) yra TOP ledgeris pagal pasirinktą scenarijų.
    // SCENARIO_ONE: lyginiai indeksai (0,2,4...) = TOP, nelyginiai = BOTTOM
    // SCENARIO_TWO: nelyginiai indeksai (1,3,5...) = TOP, lyginiai = BOTTOM
    private boolean isTopFace(int faceIndex, LedgerScenario scenario) {
        boolean evenIsTop = (scenario == LedgerScenario.SCENARIO_ONE);
        return (faceIndex % 2 == 0) == evenIsTop;
    }

    // Grąžina mažiausio dengiamojo vamzdžio ilgį (>= remaining)
    private double smallestCoverLength(double remaining, TubeSize[] sizes) {
        for (TubeSize size : sizes) {
            if (size.getLengthM() >= remaining) return size.getLengthM();
        }
        return sizes[sizes.length - 1].getLengthM();
    }

    // Sudeda vamzdžių kiekius į suvestinę, padaugintus iš kiekio
    private void mergeTubes(Map<String, Integer> target, Map<String, Integer> perRun, int multiplier) {
        for (Map.Entry<String, Integer> entry : perRun.entrySet()) {
            target.merge(entry.getKey(), entry.getValue() * multiplier, Integer::sum);
        }
    }

    // Skaičiuoja bortus vienai sienos pusei pagal sienos ilgį
    // Pagrindinis bortas dedamas kiek įmanoma daugiau kartų, likutį dengia mažesnis bortas
    private void addFaceBoards(Map<String, Integer> summary, double faceLength,
                                double primaryLength, String primaryName, int faceCount) {
        int primaryCount = (int) Math.floor(faceLength / primaryLength);         // Kiek pilnų pagrindinių bortų tilpsta
        double remainder = faceLength - primaryCount * primaryLength;            // Likusis atstumas

        summary.merge(primaryName, faceCount * primaryCount * BOARDS_PER_ROW, Integer::sum); // Pagrindiniai bortai

        if (remainder > 0.1) {                                                   // Jei liko reikšmingas atstumas
            BoardSize secondary = findSmallestBoardCovering(remainder);          // Randame trumpiausią bortą kuris dengia likutį
            if (secondary != null) {
                String secondaryName = formatTubeSize(secondary.getLengthM());
                summary.merge(secondaryName, faceCount * BOARDS_PER_ROW, Integer::sum); // Antriniai bortai
            }
        }
    }

    // Randa mažiausią bortų dydį kuris dengia nurodytą atstumą
    private BoardSize findSmallestBoardCovering(double lengthM) {
        for (BoardSize size : BoardSize.values()) {                              // Iteruojame nuo mažiausio dydžio
            if (size.getLengthM() >= lengthM) {
                return size;                                                     // Grąžiname pirmą tinkamą dydį
            }
        }
        return null;                                                             // Nė vienas netinka (neturėtų nutikti)
    }

    // Builds face name labels for display
    private String[] buildFaceNames(ScaffoldInput input, List<Double> faceLengths) {
        String[] names = new String[faceLengths.size()];
        if (input.getHouseShape() == HouseShape.L_SHAPE) {
            double L = input.getHouseLength(), W = input.getHouseWidth();
            double CL = input.getLCutLength(), CW = input.getLCutWidth();
            names[0] = String.format("Wall 1 – Bottom (%.1fm)", L);
            names[1] = String.format("Wall 2 – Right lower (%.1fm)", W - CW);
            names[2] = String.format("Wall 3 – Cut horizontal (%.1fm)", CL);
            names[3] = String.format("Wall 4 – Cut vertical (%.1fm)", CW);
            names[4] = String.format("Wall 5 – Top (%.1fm)", L - CL);
            names[5] = String.format("Wall 6 – Left (%.1fm)", W);
        } else {
            double L = input.getHouseLength(), W = input.getHouseWidth();
            names[0] = String.format("Wall 1 – Bottom/Front (%.1fm)", L);
            names[1] = String.format("Wall 2 – Right (%.1fm)", W);
            names[2] = String.format("Wall 3 – Top/Back (%.1fm)", L);
            names[3] = String.format("Wall 4 – Left (%.1fm)", W);
        }
        return names;
    }

    // Returns board combination string for one face using fleet terminology
    // 1 fleet = 6 boards across the scaffold width (BOARDS_PER_ROW)
    private String boardComboString(double faceLength, double primaryLength, String primaryName) {
        int fleets = (int) Math.floor(faceLength / primaryLength);
        double remainder = faceLength - fleets * primaryLength;
        StringBuilder sb = new StringBuilder();
        if (remainder > 0.1) {
            BoardSize secondary = findSmallestBoardCovering(remainder);
            if (secondary != null && Math.abs(secondary.getLengthM() - primaryLength) < 0.001) {
                // Cover fleet is the same size — merge into one count
                fleets++;
                sb.append(fleets).append(fleets == 1 ? " fleet " : " fleets ").append(primaryName);
            } else {
                sb.append(fleets).append(fleets == 1 ? " fleet " : " fleets ").append(primaryName);
                if (secondary != null) {
                    sb.append(" + 1 fleet ").append(formatTubeSize(secondary.getLengthM()));
                }
            }
        } else {
            sb.append(fleets).append(fleets == 1 ? " fleet " : " fleets ").append(primaryName);
        }
        sb.append("  (").append(String.format("%.2f", faceLength)).append("m)");
        return sb.toString();
    }

    // Konvertuoja metro ilgį į žmonėms suprantamą pavadinimą (pvz. 3.962 → "13ft")
    private String formatTubeSize(double lengthM) {
        if (lengthM == 1.524) return "5ft";
        if (lengthM == 1.829) return "6ft";
        if (lengthM == 2.438) return "8ft";
        if (lengthM == 3.048) return "10ft";
        if (lengthM == 3.962) return "13ft";
        if (lengthM == 4.877) return "16ft";
        if (lengthM == 6.401) return "21ft";
        return lengthM + "m";                                                    // Grąžiname metrais jei neatitinka standartinių
    }
}
