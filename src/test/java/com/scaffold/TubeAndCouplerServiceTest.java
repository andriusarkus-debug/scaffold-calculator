package com.scaffold;

import com.scaffold.model.LiftInput;
import com.scaffold.model.MaterialResult;
import com.scaffold.model.ScaffoldInput;
import com.scaffold.model.enums.BoardSize;
import com.scaffold.model.enums.HouseShape;
import com.scaffold.model.enums.LedgerScenario;
import com.scaffold.model.enums.RoofType;
import com.scaffold.model.enums.TubeSize;
import com.scaffold.service.AccessTowerService;
import com.scaffold.service.GableService;
import com.scaffold.service.TubeAndCouplerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TubeAndCouplerServiceTest {

    private TubeAndCouplerService service;

    @BeforeEach
    void setUp() {
        service = new TubeAndCouplerService(new GableService(), new AccessTowerService());
    }

    // =========================================================
    // HELPER — sukuria standartinį 2 liftų įvesties objektą
    // =========================================================
    private ScaffoldInput buildInput(HouseShape shape, double length, double width,
                                     double cutLength, double cutWidth) {
        ScaffoldInput input = new ScaffoldInput();
        input.setHouseShape(shape);
        input.setHouseLength(length);
        input.setHouseWidth(width);
        input.setLCutLength(cutLength);
        input.setLCutWidth(cutWidth);
        input.setRoofType(RoofType.HIP);
        input.setTubeSize(TubeSize.TWENTY_ONE_FOOT);

        LiftInput lift1 = new LiftInput();
        lift1.setHeight(1.5);
        lift1.setHasBoards(true);
        lift1.setBoardSize(BoardSize.THIRTEEN_FOOT);

        LiftInput lift2 = new LiftInput();
        lift2.setHeight(1.0);
        lift2.setHasBoards(false);

        input.setLifts(List.of(lift1, lift2));
        return input;
    }

    // =========================================================
    //  STAČIAKAMPIS — 10m × 7m, 2 liftai (1.5m + 1.0m)
    // =========================================================

    @Test
    @DisplayName("RECTANGULAR: perimetras ir bays")
    void rectangular_perimeterAndBays() {
        MaterialResult r = service.calculate(
                buildInput(HouseShape.RECTANGULAR, 10, 7, 0, 0));

        assertEquals(34.0, r.getPerimeter(), "Perimetras: 2*(10+7)=34");
        assertEquals(19, r.getBays(), "Bays: ceil(34/1.8)=19");
    }

    @Test
    @DisplayName("RECTANGULAR: returnCount = 4")
    void rectangular_returnCount() {
        MaterialResult r = service.calculate(
                buildInput(HouseShape.RECTANGULAR, 10, 7, 0, 0));

        assertEquals(4, r.getReturnCount(), "Stačiakampis turi 4 kampus");
    }

    @Test
    @DisplayName("RECTANGULAR: standartai")
    void rectangular_standards() {
        MaterialResult r = service.calculate(
                buildInput(HouseShape.RECTANGULAR, 10, 7, 0, 0));

        // standardPositions = (19+1)*2 + 4 = 44, tubesPerStandard = ceil(2.5/6.401) = 1
        assertEquals(44, r.getStandards(), "Standards: 44 pozicijos × 1 vamzdis");
        assertEquals(44, r.getBasePlates(), "Base plates = standard positions");
        assertEquals(44, r.getSoleBoards(), "Sole boards = standard positions");
        assertEquals(1, r.getTubesPerStandard(), "1 vamzdis pakankamas 2.5m aukščiui");
    }

    @Test
    @DisplayName("RECTANGULAR: transomiai")
    void rectangular_transoms() {
        MaterialResult r = service.calculate(
                buildInput(HouseShape.RECTANGULAR, 10, 7, 0, 0));

        // Siena 10m: run=13.048, spans=4, contrib=17
        // Siena 7m:  run=10.048, spans=3, contrib=13
        // transomPositions = 17+13+17+13 = 60
        // transomsSaved = 4 kampai × 2 = 8
        // transoms = 60*3 - 8 = 172
        assertEquals(172, r.getTransoms(), "Transomiai: 60 pozicijos × 3 lygiai − 8 sutaupytų");
    }

    @Test
    @DisplayName("RECTANGULAR: ledgeriai ir handrailai")
    void rectangular_ledgersAndHandrails() {
        MaterialResult r = service.calculate(
                buildInput(HouseShape.RECTANGULAR, 10, 7, 0, 0));

        // ledgersPerLevel = 4 + (6+4+6+4)*2 = 44
        // ledgers = 44*3 = 132, handrails = 44*2*2 = 176
        assertEquals(132, r.getLedgers(), "Ledgers: 44/lygis × 3 lygiai");
        assertEquals(176, r.getHandrails(), "Handrails: 44/lygis × 2 eilutės × 2 liftai");
    }

    @Test
    @DisplayName("RECTANGULAR: bracing")
    void rectangular_bracing() {
        MaterialResult r = service.calculate(
                buildInput(HouseShape.RECTANGULAR, 10, 7, 0, 0));

        // swayBracing: (1+1+1+1) sets × 2 liftai = 8  (formulė: swayBracingSets * lifts)
        // bracesPerLift (faceLengths): ceil(6/2)+ceil(4/2)+ceil(6/2)+ceil(4/2) = 3+2+3+2 = 10
        // ledgerBracing = 10 * 2 liftai = 20
        assertEquals(8,  r.getSwayBracing(),   "Sway bracing: 4 rinkiniai × 2 liftai");
        assertEquals(20, r.getLedgerBracing(), "Ledger bracing: 10/liftas × 2 liftai");
    }

    @Test
    @DisplayName("RECTANGULAR: lentos (boards)")
    void rectangular_boards() {
        MaterialResult r = service.calculate(
                buildInput(HouseShape.RECTANGULAR, 10, 7, 0, 0));

        // SCENARIO_ONE (default): even faces (0,2) = TOP → use scaffold run; odd faces (1,3) = BOTTOM → use wall length
        // Face 0 (TOP, run=13.048): 3×13ft + 1×5ft  = 18 + 6 = 24
        // Face 1 (BOTTOM, wall=7):  1×13ft + 1×13ft =  6 + 6 = 12  (rem=3.038 → 13ft secondary)
        // Face 2 (TOP, run=13.048): 3×13ft + 1×5ft  = 18 + 6 = 24
        // Face 3 (BOTTOM, wall=7):  1×13ft + 1×13ft =  6 + 6 = 12  (rem=3.038 → 13ft secondary)
        // Total = 72 | 13ft = 48, 5ft = 12
        assertEquals(72, r.getBoards(), "Iš viso bortų 1 platformai (SCENARIO_ONE)");
        assertEquals(48, r.getBoardSummary().get("13ft"), "48 × 13ft bortų");
        assertEquals(12, r.getBoardSummary().get("5ft"),  "12 × 5ft bortų (tik nuo TOP sienų)");
        assertNull(r.getBoardSummary().get("8ft"), "8ft nenaudojami šiam namui");
    }

    @Test
    @DisplayName("RECTANGULAR: jungtys")
    void rectangular_couplers() {
        MaterialResult r = service.calculate(
                buildInput(HouseShape.RECTANGULAR, 10, 7, 0, 0));

        // rightAngle (standartais grįsta formulė — long tubes su sleeve coupleriais):
        //   ledgerRACouplers   = 44 × (2+1) + 4 × 2 × (2+1) = 132 + 24 = 156
        //   handrailRACouplers = 44 × 2 × 2 + 4 × 4 × 2     = 176 + 32 = 208
        //   rightAngle = 156 + 208 + 20 = 384  (ledgerBracing=20 pagal faceLengths)
        // swivel     = 8*2 + 20 = 36  (sway=8 actual, ledgerBracing=20 actual pagal faceLengths)
        // sleeve: face 10m=3 tubes→2 joints, face 7m=2 tubes→1 joint
        //   ledger joins: (2+1+2+1) × 2*(2+1) = 6 × 6 = 36
        //   handrail joins: 6 × 2*2 = 24
        //   standard joins: tubesPerStandard=1 → 0
        //   sleeve = 36 + 24 + 0 = 60
        // putlog = (transoms + returnPlatformTransoms) * 2 = (172+16)*2 = 376
        assertEquals(384, r.getRightAngleCouplers(), "Right-angle couplers");
        assertEquals(36,  r.getSwivelCouplers(),      "Swivel couplers");
        assertEquals(60,  r.getSleeveCouplers(),       "Sleeve couplers");
        assertEquals(376, r.getPutlogCouplers(),       "Putlog couplers: (172+16)×2");
    }

    @Test
    @DisplayName("RECTANGULAR: returnų platformos")
    void rectangular_returnPlatforms() {
        MaterialResult r = service.calculate(
                buildInput(HouseShape.RECTANGULAR, 10, 7, 0, 0));

        // returnBays=1, returnPlatformTransoms = 4*1*2*2=16
        // returnPlatformLedgers = 4*1*3=12
        // returnPlatformBoards: lift1 (13ft bortai, 2 bays/board, 1 run/return)
        //   = 4 kampai × 1 run × 6 bortų = 24
        assertEquals(16, r.getReturnPlatformTransoms(), "Return platform transomiai");
        assertEquals(12, r.getReturnPlatformLedgers(),  "Return platform ledgeriai");
        assertEquals(24, r.getReturnPlatformBoards(),   "Return platform bortai");
    }

    @Test
    @DisplayName("RECTANGULAR: kiti elementai")
    void rectangular_misc() {
        MaterialResult r = service.calculate(
                buildInput(HouseShape.RECTANGULAR, 10, 7, 0, 0));

        assertEquals(19, r.getToeboards(), "Toeboards = bays");
        assertEquals(0,  r.getAdvanceGuardRailSets(), "Nėra advance guard rail (lift2.height=1.0 < 1.5)");
        assertEquals(2.5, r.getTotalHeight(), "Bendras aukštis: 1.5+1.0=2.5");
    }

    // =========================================================
    //  L FORMA — 10m × 8m, išpjova 4m × 3m, 2 liftai
    // =========================================================

    @Test
    @DisplayName("L_SHAPE: perimetras lygus bounding rectangle")
    void lShape_perimeter() {
        MaterialResult r = service.calculate(
                buildInput(HouseShape.L_SHAPE, 10, 8, 4, 3));

        // L forma: 10+5+4+3+6+8 = 36, lygiai 2*(10+8)=36
        assertEquals(36.0, r.getPerimeter(), "L formos perimetras = 2*(10+8)=36");
        assertEquals(20, r.getBays(), "Bays: ceil(36/1.8)=20");
    }

    @Test
    @DisplayName("L_SHAPE: returnCount = 5")
    void lShape_returnCount() {
        MaterialResult r = service.calculate(
                buildInput(HouseShape.L_SHAPE, 10, 8, 4, 3));

        // Kampas D yra įgaubtas (vidinis) — returnas ten neįmanomas, todėl 5 ne 6
        assertEquals(5, r.getReturnCount(), "L forma turi 5 return platformas (kampas D draudžiamas)");
    }

    @Test
    @DisplayName("L_SHAPE: standartai didesni nei stačiakampio")
    void lShape_standards() {
        MaterialResult rRect = service.calculate(
                buildInput(HouseShape.RECTANGULAR, 10, 8, 0, 0));
        MaterialResult rL = service.calculate(
                buildInput(HouseShape.L_SHAPE, 10, 8, 4, 3));

        // L forma: standardPositions = (20+1)*2 + returnCount(5) = 47
        assertEquals(47, rL.getStandards(), "Standards: 47 pozicijos × 1 vamzdis");
        assertTrue(rL.getStandards() > rRect.getStandards(),
                "L forma turi daugiau standartų (5 kampai vs 4)");
    }

    @Test
    @DisplayName("L_SHAPE: transomiai")
    void lShape_transoms() {
        MaterialResult r = service.calculate(
                buildInput(HouseShape.L_SHAPE, 10, 8, 4, 3));

        // 6 sienos: 17+13+9+9+13+13 = 74 pozicijos × 3 lygiai = 222
        // transomsSaved = 6 kampai × 2 = 12
        // transoms = 222 − 12 = 210
        assertEquals(210, r.getTransoms(), "L formos transomiai: 74 pozicijos × 3 lygiai − 12 sutaupytų");
    }

    @Test
    @DisplayName("L_SHAPE: ledgeriai ir handrailai")
    void lShape_ledgersAndHandrails() {
        MaterialResult r = service.calculate(
                buildInput(HouseShape.L_SHAPE, 10, 8, 4, 3));

        // ledgersPerLevel = returnCount(5) + (12+6+6+4+8+10) = 5 + 46 = 51
        // ledgers = 51*3 = 153, handrails = 51*2*2 = 204
        assertEquals(153, r.getLedgers(),   "L forma: 51/lygis × 3 lygiai");
        assertEquals(204, r.getHandrails(), "L forma: 51/lygis × 2 eilutės × 2 liftai");
    }

    @Test
    @DisplayName("L_SHAPE: bracing")
    void lShape_bracing() {
        MaterialResult r = service.calculate(
                buildInput(HouseShape.L_SHAPE, 10, 8, 4, 3));

        // L_SHAPE faces (L=10, W=8, CL=4, CW=3): [10, 5, 4, 3, 6, 8]
        // swayBracing sets per face: ceil(bays/6) = 1+1+1+1+1+1 = 6 → 6 * 2 liftai = 12
        // bracesPerLift: ceil(bays/2) = 3+2+2+1+2+3 = 13 → ledgerBracing = 13 * 2 = 26
        assertEquals(12, r.getSwayBracing(),   "L forma: 6 sienų sway bracing × 2 liftai");
        assertEquals(26, r.getLedgerBracing(), "L forma: 13/liftas × 2 liftai");
    }

    @Test
    @DisplayName("L_SHAPE: lentos (boards)")
    void lShape_boards() {
        MaterialResult r = service.calculate(
                buildInput(HouseShape.L_SHAPE, 10, 8, 4, 3));

        // SCENARIO_ONE (default): even faces = TOP (scaffold run), odd faces = BOTTOM (wall length)
        // Face 0 (TOP,  run=13.048): 3×13ft + 1×5ft = 24
        // Face 1 (BOTTOM, wall=5):  1×13ft + 1×5ft = 12
        // Face 2 (TOP,  run=5.524):  1×13ft + 1×6ft = 12
        // Face 3 (BOTTOM, wall=3):  0×13ft + 1×13ft =  6
        // Face 4 (TOP,  run=9.048):  2×13ft + 1×5ft = 18
        // Face 5 (BOTTOM, wall=8):  2×13ft           = 12
        assertEquals(84, r.getBoards(), "L forma: iš viso bortų 1 platformai (SCENARIO_ONE)");
    }

    @Test
    @DisplayName("L_SHAPE: returnų platformos atitinka 5 kampus")
    void lShape_returnPlatforms() {
        MaterialResult r = service.calculate(
                buildInput(HouseShape.L_SHAPE, 10, 8, 4, 3));

        // returnCount=5 (kampas D draudžiamas), returnBays=1
        // returnPlatformTransoms = 5*1*2*2 = 20
        // returnPlatformLedgers  = 5*1*3   = 15
        // returnPlatformBoards   = 5*1*6   = 30
        assertEquals(20, r.getReturnPlatformTransoms(), "5 kampai × return transomiai");
        assertEquals(15, r.getReturnPlatformLedgers(),  "5 kampai × return ledgeriai");
        assertEquals(30, r.getReturnPlatformBoards(),   "5 kampai × return bortai");
    }

    // =========================================================
    //  LYGINIMAS — tie patys matmenys, skirtinga forma
    // =========================================================

    @Test
    @DisplayName("L_SHAPE vs RECTANGULAR: perimetras vienodas, bet medžiagų daugiau")
    void lShape_moreMaterialsThanRectangular() {
        // Lyginame su stačiakampiu, kuris apgaubia L formą (10×8)
        MaterialResult rRect = service.calculate(
                buildInput(HouseShape.RECTANGULAR, 10, 8, 0, 0));
        MaterialResult rL = service.calculate(
                buildInput(HouseShape.L_SHAPE, 10, 8, 4, 3));

        assertEquals(rRect.getPerimeter(), rL.getPerimeter(),
                "Perimetras identiškas");
        assertTrue(rL.getStandards() > rRect.getStandards(),
                "L forma: daugiau standartų dėl papildomų kampų");
        assertTrue(rL.getReturnPlatformBoards() > rRect.getReturnPlatformBoards(),
                "L forma: daugiau return bortų dėl 5 kampų vs 4");
    }

    // =========================================================
    //  LEDGER VAMZDŽIŲ KOMBINACIJOS (face run = siena + 2 × 1.524m)
    // =========================================================

    @Test
    @DisplayName("10m siena: ledger run = 13.048m → 21ft + 16ft + 6ft")
    void ledgerTubes_tenMeterWall_correctCombination() {
        // A→B siena: 10m + 1.524m (A return) + 1.524m (B return) = 13.048m
        // Teisingas derinys: 21ft(6.401) + 16ft(4.877) + 6ft(1.829) = 13.107m
        MaterialResult r = service.calculate(
                buildInput(HouseShape.RECTANGULAR, 10, 7, 0, 0));

        // Ledger tube summary apima visas 4 sienas × 2 (inside+outside) × (lifts+1) lygiai
        // 10m siena naudoja: 21ft, 16ft, 6ft
        // 7m siena naudoja: 21ft, 13ft
        assertNotNull(r.getLedgerTubeSummary().get("21ft"), "21ft vamzdžiai reikalingi");
        assertNotNull(r.getLedgerTubeSummary().get("16ft"), "16ft vamzdžiai: 10m sienai");
        assertNotNull(r.getLedgerTubeSummary().get("6ft"),  "6ft vamzdžiai: likusiam 1.770m po 21ft+16ft");
        assertNotNull(r.getLedgerTubeSummary().get("13ft"), "13ft vamzdžiai: 7m sienai");
        assertNull(r.getLedgerTubeSummary().get("5ft"),
                "5ft NENAUDOJAMI — 6ft dengia 10m sienos likutį efektyviau");
    }

    @Test
    @DisplayName("7m siena: ledger run = 10.048m → 21ft + 13ft")
    void ledgerTubes_sevenMeterWall_correctCombination() {
        // 7m + 2×1.524 = 10.048m → 21ft(6.401) + 13ft(3.962) = 10.363m
        // 4m + 2×1.524 = 7.048m  → 16ft(4.877) + 8ft(2.438) = 7.315m
        //   (21ft skipped — overhang after 21ft would be 1.524-0.647=0.877m > 0.5m limit)
        MaterialResult r = service.calculate(
                buildInput(HouseShape.RECTANGULAR, 7, 4, 0, 0));

        assertNotNull(r.getLedgerTubeSummary().get("21ft"), "21ft vamzdžiai: 7m sienai");
        assertNotNull(r.getLedgerTubeSummary().get("13ft"), "13ft vamzdžiai: 7m sienai");
        assertNotNull(r.getLedgerTubeSummary().get("16ft"), "16ft vamzdžiai: 4m sienai");
        assertNotNull(r.getLedgerTubeSummary().get("8ft"),  "8ft vamzdžiai: 4m sienai likučiui");
        assertNull(r.getLedgerTubeSummary().get("5ft"),
                "5ft nenaudojami — overhang taisyklė blokuoja 21ft su 5ft likimu 4m sienai");
    }

    // =========================================================
    //  STANDARTŲ SUVESTINĖ (inside vs outside)
    // =========================================================

    @Test
    @DisplayName("2 liftai: inside=10ft, outside=13ft")
    void standards_twoLifts_insideTenOutsideThirteen() {
        MaterialResult r = service.calculate(
                buildInput(HouseShape.RECTANGULAR, 10, 7, 0, 0));

        // insidePositions = outsidePositions = (19+1) + 4/2 = 22
        // 2.5m total height: ceil(2.5/3.048)=1 → 22×10ft, ceil(2.5/3.962)=1 → 22×13ft
        assertNotNull(r.getStandardTubeSummary(), "Suvestinė neturi būti null");
        assertEquals(22, r.getStandardTubeSummary().get("10ft"),
                "Inside standartai: 22 × 10ft");
        assertEquals(22, r.getStandardTubeSummary().get("13ft"),
                "Outside standartai: 22 × 13ft");
        assertNull(r.getStandardTubeSummary().get("21ft"),
                "21ft nenaudojami 2 liftams");
    }

    @Test
    @DisplayName("4 liftai: visi standartai 21ft")
    void standards_fourLifts_allTwentyOne() {
        ScaffoldInput input = new ScaffoldInput();
        input.setHouseShape(HouseShape.RECTANGULAR);
        input.setHouseLength(10.0);
        input.setHouseWidth(7.0);
        input.setRoofType(RoofType.HIP);
        input.setTubeSize(TubeSize.TWENTY_ONE_FOOT);

        LiftInput lift1 = new LiftInput(); lift1.setHeight(1.5); lift1.setHasBoards(true);
        lift1.setBoardSize(BoardSize.THIRTEEN_FOOT);
        LiftInput lift2 = new LiftInput(); lift2.setHeight(1.0); lift2.setHasBoards(false);
        LiftInput lift3 = new LiftInput(); lift3.setHeight(1.5); lift3.setHasBoards(false);
        LiftInput lift4 = new LiftInput(); lift4.setHeight(1.0); lift4.setHasBoards(false);
        input.setLifts(List.of(lift1, lift2, lift3, lift4));

        MaterialResult r = service.calculate(input);

        // 4 liftai → visi 44 standartai = 21ft
        assertNotNull(r.getStandardTubeSummary(), "Suvestinė neturi būti null");
        assertEquals(44, r.getStandardTubeSummary().get("21ft"),
                "Visi standartai: 44 × 21ft");
        assertNull(r.getStandardTubeSummary().get("10ft"),
                "10ft nenaudojami 4 liftams");
        assertNull(r.getStandardTubeSummary().get("13ft"),
                "13ft nenaudojami 4 liftams");
    }

    @Test
    @DisplayName("2 liftai: inside + outside suma lygi viso standartų skaičiui")
    void standards_twoLifts_sumMatchesTotalStandards() {
        MaterialResult r = service.calculate(
                buildInput(HouseShape.RECTANGULAR, 10, 7, 0, 0));

        int inside = r.getStandardTubeSummary().get("10ft");
        int outside = r.getStandardTubeSummary().get("13ft");
        assertEquals(r.getStandards(), inside + outside,
                "inside + outside = bendras standartų skaičius");
    }

    // =========================================================
    //  ADVANCE GUARD RAIL
    // =========================================================

    @Test
    @DisplayName("Advance guard rail — kai sekančio lifto aukštis >= 1.5m")
    void advanceGuardRail_triggersWhenNextLiftTall() {
        ScaffoldInput input = buildInput(HouseShape.RECTANGULAR, 10, 7, 0, 0);

        LiftInput lift1 = new LiftInput();
        lift1.setHeight(1.5);
        lift1.setHasBoards(true);
        lift1.setBoardSize(BoardSize.THIRTEEN_FOOT);

        LiftInput lift2 = new LiftInput();
        lift2.setHeight(1.5); // >= 1.5 → aktyvuoja
        lift2.setHasBoards(false);

        input.setLifts(List.of(lift1, lift2));
        MaterialResult r = service.calculate(input);

        assertEquals(1, r.getAdvanceGuardRailSets(),
                "1 advance guard rail rinkinys kai lift2.height=1.5");
    }

    @Test
    @DisplayName("Advance guard rail — nesuaktyvuojamas kai sekančio lifto aukštis < 1.5m")
    void advanceGuardRail_doesNotTriggerWhenNextLiftShort() {
        MaterialResult r = service.calculate(
                buildInput(HouseShape.RECTANGULAR, 10, 7, 0, 0));

        assertEquals(0, r.getAdvanceGuardRailSets(),
                "0 advance guard rail rinkinių kai lift2.height=1.0");
    }

    // =========================================================
    //  LEDGER SCENARIJUS — bortai pagal TOP/BOTTOM priskyrimus
    // =========================================================

    @Test
    @DisplayName("L_SHAPE SCENARIO_ONE: TOP sienos naudoja scaffold run ilgį bortams")
    void lShapeScenarioOneBoards() {
        ScaffoldInput input = buildInput(HouseShape.L_SHAPE, 10, 8, 4, 3);
        input.setLedgerScenario(LedgerScenario.SCENARIO_ONE);
        MaterialResult r = service.calculate(input);

        // SCENARIO_ONE: lyginiai indeksai (0,2,4) = TOP → bortai nuo scaffold run
        //               nelyginiai (1,3,5) = BOTTOM → bortai nuo sienos ilgio
        // Face 0 (TOP,  run=13.048): 3×13ft + 1×5ft = 24
        // Face 1 (BOTTOM, wall=5):  1×13ft + 1×5ft = 12
        // Face 2 (TOP,  run=5.524):  1×13ft + 1×6ft = 12
        // Face 3 (BOTTOM, wall=3):  0×13ft + 1×13ft =  6
        // Face 4 (TOP,  run=9.048):  2×13ft + 1×5ft = 18
        // Face 5 (BOTTOM, wall=8):  2×13ft           = 12
        assertEquals(84, r.getBoards(), "SCENARIO_ONE: iš viso bortų");
        // 6ft bortai atsiranda tik kai TOP sienos yra trumpos (run=5.524 → 6ft papildymui)
        assertNotNull(r.getBoardSummary().get("6ft"), "SCENARIO_ONE turi 6ft bortų (face2 TOP run=5.524)");
        assertNull(r.getBoardSummary().get("8ft"), "SCENARIO_ONE neturi 8ft bortų");
    }

    @Test
    @DisplayName("L_SHAPE SCENARIO_TWO: TOP sienos naudoja scaffold run ilgį bortams")
    void lShapeScenarioTwoBoards() {
        ScaffoldInput input = buildInput(HouseShape.L_SHAPE, 10, 8, 4, 3);
        input.setLedgerScenario(LedgerScenario.SCENARIO_TWO);
        MaterialResult r = service.calculate(input);

        // SCENARIO_TWO: nelyginiai indeksai (1,3,5) = TOP → bortai nuo scaffold run
        //               lyginiai (0,2,4) = BOTTOM → bortai nuo sienos ilgio
        // Face 0 (BOTTOM, wall=10):  2×13ft + 1×8ft = 18
        // Face 1 (TOP,  run=8.048):   2×13ft + 1×5ft = 18
        // Face 2 (BOTTOM, wall=4):   1×13ft           =  6
        // Face 3 (TOP,  run=4.524):   1×13ft + 1×5ft = 12
        // Face 4 (BOTTOM, wall=6):   1×13ft + 1×8ft = 12
        // Face 5 (TOP,  run=11.048):  2×13ft + 1×13ft = 18
        assertEquals(84, r.getBoards(), "SCENARIO_TWO: iš viso bortų");
        // SCENARIO_TWO naudoja 8ft bortus (ilgesnės BOTTOM sienos 10m, 6m)
        assertNotNull(r.getBoardSummary().get("8ft"), "SCENARIO_TWO turi 8ft bortų (BOTTOM sienos)");
        assertNull(r.getBoardSummary().get("6ft"), "SCENARIO_TWO neturi 6ft bortų");
    }

    @Test
    @DisplayName("L_SHAPE: transomsSavedByTopLedgers = 12 (6 sienų kampai × 2)")
    void lShapeTransomsSaved() {
        MaterialResult r = service.calculate(
                buildInput(HouseShape.L_SHAPE, 10, 8, 4, 3));

        // Kiekvieno kampo viršutinis ledgeris veikia kaip transonas (inside + outside = 2 sutaupymai)
        // L forma turi 6 sienų kampų: 6 × 2 = 12
        assertEquals(12, r.getTransomsSavedByTopLedgers(),
                "L forma: 6 kampai × 2 = 12 sutaupytų transomų");
    }

    @Test
    @DisplayName("SCENARIO_ONE ir SCENARIO_TWO sutaupo vienodą transomų skaičių")
    void scenarioOneVsTwoHaveSameTransomSaving() {
        ScaffoldInput inputS1 = buildInput(HouseShape.L_SHAPE, 10, 8, 4, 3);
        inputS1.setLedgerScenario(LedgerScenario.SCENARIO_ONE);

        ScaffoldInput inputS2 = buildInput(HouseShape.L_SHAPE, 10, 8, 4, 3);
        inputS2.setLedgerScenario(LedgerScenario.SCENARIO_TWO);

        MaterialResult rS1 = service.calculate(inputS1);
        MaterialResult rS2 = service.calculate(inputS2);

        // Scenarijus keičia kurios sienos ledgeriai yra viršuje, bet kampų skaičius nesikeičia
        assertEquals(rS1.getTransomsSavedByTopLedgers(), rS2.getTransomsSavedByTopLedgers(),
                "Abu scenarijai sutaupo tą patį transomų skaičių");
        assertEquals(12, rS1.getTransomsSavedByTopLedgers(),
                "Abu scenarijai: 6 kampai × 2 = 12");
    }

    @Test
    @DisplayName("Vamzdžių kombinacija: 7.048m → 16ft+8ft (ne 21ft+5ft — overhang taisyklė)")
    void tubeComboMaxOverhang() {
        // 4m siena + 2 returnai = 4 + 2×1.524 = 7.048m scaffold run
        // 21ft(6.401) paliktu 0.647m → mažiausias dengiamasis 5ft(1.524) → overhang=0.877m > 0.5m → 21ft draudžiamas
        // 16ft(4.877) paliktu 2.171m → 8ft(2.438) dengia → overhang=0.267m ≤ 0.5m → ✓
        MaterialResult r = service.calculate(
                buildInput(HouseShape.RECTANGULAR, 4, 4, 0, 0));

        // Visos sienos = 4m, run = 7.048m → kiekviena siena turi naudoti 16ft+8ft
        assertNotNull(r.getLedgerTubeSummary().get("16ft"), "16ft vamzdžiai: 7.048m runui");
        assertNotNull(r.getLedgerTubeSummary().get("8ft"),  "8ft vamzdžiai: likutis po 16ft");
        assertNull(r.getLedgerTubeSummary().get("21ft"),
                "21ft draudžiami — overhang 0.877m viršytų 0.5m limitą");
        assertNull(r.getLedgerTubeSummary().get("5ft"),
                "5ft nenaudojami — 8ft dengia likutį");
    }
}
