package com.scaffold;

import com.scaffold.model.*;
import com.scaffold.model.enums.BoardSize;
import com.scaffold.model.enums.HouseShape;
import com.scaffold.model.enums.LedgerScenario;
import com.scaffold.model.enums.RoofType;
import com.scaffold.model.enums.TubeSize;
import com.scaffold.service.GableService;
import com.scaffold.service.TubeAndCouplerService;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CalculatorTest {

    private final TubeAndCouplerService service = new TubeAndCouplerService(new GableService());

    // ================================================================
    //  HELPER — spausdina rezultatus (naudojamas abiejuose testuose)
    // ================================================================
    private void printResult(MaterialResult r) {
        System.out.println("Perimetras:        " + r.getPerimeter() + " m");
        System.out.println("Bays:              " + r.getBays());
        System.out.println("Bendras aukštis:   " + r.getTotalHeight() + " m");
        System.out.println();
        System.out.println("--- STANDARTAI ---");
        System.out.println("Standartai:        " + r.getStandards() + " vnt (" + r.getStandardTubeSize() + ")");
        System.out.println("Base plates:       " + r.getBasePlates());
        System.out.println("Sole boards:       " + r.getSoleBoards());
        System.out.println();
        System.out.println("--- LEDGERS AND HANDRAILS ---");
        System.out.println("Ledger vamzdžiai:  " + r.getLedgerTubeSummary());
        System.out.println("Handrail vamzdžiai:" + r.getHandrailTubeSummary());
        System.out.println();
        System.out.println("--- LEDGER SCENARIJUS ---");
        System.out.println("Scenarijus:        " + r.getLedgerScenario());
        System.out.println("Sutaupyti transomiai (kampų viršutiniai ledgeriai): " + r.getTransomsSavedByTopLedgers() + " vnt");
        System.out.println();
        System.out.println("--- TRANSOMS ---");
        System.out.println("Transomiai:        " + r.getTransoms() + " vnt (5ft)");
        System.out.println();
        System.out.println("--- BOARDS ---");
        System.out.println("Bortai iš viso:    " + r.getBoards() + " vnt");
        System.out.println("Bortų suvestinė:   " + r.getBoardSummary());
        System.out.println();
        System.out.println("--- FITTINGS ---");
        System.out.println("Right-angle:       " + r.getRightAngleCouplers());
        System.out.println("Swivel:            " + r.getSwivelCouplers());
        System.out.println("Sleeve:            " + r.getSleeveCouplers());
        System.out.println("Putlog:            " + r.getPutlogCouplers());
        System.out.println();
        System.out.println("--- BRACING ---");
        System.out.println("Sway bracing:      " + r.getSwayBracing() + " vnt × " + r.getSwayBraceTubeSize());
        System.out.println("Ledger bracing:    " + r.getLedgerBracing());
        System.out.println("Ledger brace tubes:" + r.getLedgerBraceTubeSummary());
        System.out.println();
        System.out.println("--- KITA ---");
        System.out.println("Toeboards:         " + r.getToeboards());
        System.out.println("Advance guard rail:" + r.getAdvanceGuardRailSets() + " rinkiniai");
        System.out.println("Return platformos: " + r.getReturnCount() + " kampai");
        System.out.println("Return bortai:     " + r.getReturnPlatformBoards());
        System.out.println("===================================");
    }

    @Test
    void twoLiftsTest() {
        ScaffoldInput input = new ScaffoldInput();
        input.setProjectName("Two lifts test");
        input.setHouseShape(HouseShape.RECTANGULAR);
        input.setHouseLength(10.0);
        input.setHouseWidth(7.0);
        input.setRoofType(RoofType.HIP);
        input.setTubeSize(TubeSize.THIRTEEN_FOOT);

        LiftInput lift1 = new LiftInput();
        lift1.setHeight(1.5);
        lift1.setHasBoards(true);
        lift1.setBoardSize(BoardSize.THIRTEEN_FOOT);

        LiftInput lift2 = new LiftInput();
        lift2.setHeight(1.0);
        lift2.setHasBoards(false);

        input.setLifts(List.of(lift1, lift2));

        System.out.println("======= TWO LIFTS TEST (10m × 7m) =======");
        printResult(service.calculate(input));
    }

    @Test
    void lShapeTest() {
        // L-shape: 10m × 8m namas su 4m × 3m išpjova viršutiniame dešiniajame kampe
        ScaffoldInput input = new ScaffoldInput();
        input.setProjectName("L-shape test");
        input.setHouseShape(HouseShape.L_SHAPE);
        input.setHouseLength(10.0);
        input.setHouseWidth(8.0);
        input.setLCutLength(4.0);   // horizontali išpjova
        input.setLCutWidth(3.0);    // vertikali išpjova
        input.setRoofType(RoofType.HIP);
        input.setTubeSize(TubeSize.TWENTY_ONE_FOOT);
        input.setLedgerScenario(LedgerScenario.SCENARIO_ONE);

        LiftInput lift1 = new LiftInput();
        lift1.setHeight(1.5);
        lift1.setHasBoards(true);
        lift1.setBoardSize(BoardSize.THIRTEEN_FOOT);

        LiftInput lift2 = new LiftInput();
        lift2.setHeight(1.0);
        lift2.setHasBoards(false);

        input.setLifts(List.of(lift1, lift2));

        System.out.println("======= L-SHAPE TEST (10m × 8m, cut 4m × 3m) =======");
        printResult(service.calculate(input));
    }

    @Test
    void fourLiftsTest() {
        ScaffoldInput input = new ScaffoldInput();
        input.setProjectName("Four lifts test");
        input.setHouseShape(HouseShape.RECTANGULAR);
        input.setHouseLength(10.0);
        input.setHouseWidth(7.0);
        input.setRoofType(RoofType.HIP);
        input.setTubeSize(TubeSize.TWENTY_ONE_FOOT);

        LiftInput lift1 = new LiftInput();
        lift1.setHeight(1.5);
        lift1.setHasBoards(true);
        lift1.setBoardSize(BoardSize.THIRTEEN_FOOT);

        LiftInput lift2 = new LiftInput();
        lift2.setHeight(1.0);
        lift2.setHasBoards(false);

        LiftInput lift3 = new LiftInput();
        lift3.setHeight(1.5);
        lift3.setHasBoards(false);

        LiftInput lift4 = new LiftInput();
        lift4.setHeight(1.0);
        lift4.setHasBoards(false);

        input.setLifts(List.of(lift1, lift2, lift3, lift4));

        System.out.println("======= FOUR LIFTS TEST (10m × 7m) =======");
        printResult(service.calculate(input));
    }
}
