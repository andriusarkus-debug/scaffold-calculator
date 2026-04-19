package com.scaffold;

import com.scaffold.model.*;
import com.scaffold.model.enums.BoardSize;
import com.scaffold.model.enums.HouseShape;
import com.scaffold.model.enums.LedgerScenario;
import com.scaffold.model.enums.RoofType;
import com.scaffold.model.enums.TubeSize;
import com.scaffold.service.AccessTowerService;
import com.scaffold.service.GableService;
import com.scaffold.service.TubeAndCouplerService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sanity (smoke) testai — patikrina, kad skaičiavimas nesugriūna ir grąžina
 * logiškus rezultatus pagrindiniams scenarijams (2 liftai, 4 liftai, L-shape).
 * Tikslesnius skaičių lūkesčius laiko {@link com.scaffold.TubeAndCouplerServiceTest}.
 * Čia tikriname tik invariantus (perimetrą, ne-neigiamus kiekius, bays > 0 ir pan.).
 */
public class CalculatorTest {

    private final TubeAndCouplerService service =
            new TubeAndCouplerService(new GableService(), new AccessTowerService());

    // Tolerancija double palyginimams (1 mm)
    private static final double EPS = 0.001;

    /** Bendri invariantai, kurie turi galioti KIEKVIENAM skaičiavimui. */
    private void assertResultIsSane(MaterialResult r) {
        assertNotNull(r, "Rezultatas negali būti null");
        assertTrue(r.getPerimeter() > 0, "Perimetras > 0");
        assertTrue(r.getBays() > 0, "Bays > 0");
        assertTrue(r.getTotalHeight() > 0, "Bendras aukštis > 0");
        assertTrue(r.getStandards() > 0, "Standartai > 0");
        assertTrue(r.getBasePlates() > 0, "Base plates > 0");
        assertTrue(r.getSoleBoards() > 0, "Sole boards > 0");
        assertTrue(r.getTransoms() >= 0, "Transomiai ne-neigiami");
        assertTrue(r.getBoards() >= 0, "Bortai ne-neigiami");
        assertTrue(r.getTubesPerStandard() > 0, "Tubes per standard > 0");
        assertNotNull(r.getLedgerTubeSummary(), "Ledger suvestinė neturi būti null");
        assertNotNull(r.getHandrailTubeSummary(), "Handrail suvestinė neturi būti null");
    }

    @Test
    void twoLiftsTest() {
        ScaffoldInput input = new ScaffoldInput();
        input.setProjectName("Two lifts test");
        input.setHouseShape(HouseShape.RECTANGULAR);
        input.setHouseLength(12.0);
        input.setHouseWidth(12.0);
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

        MaterialResult r = service.calculate(input);

        assertResultIsSane(r);
        // RECTANGULAR 12×12 → perimetras = 2*(12+12) = 48 m
        assertEquals(48.0, r.getPerimeter(), EPS, "Stačiakampio perimetras");
        // HIP stogas → nėra gable standartų
        assertEquals(0, r.getGableStandards(), "HIP stogui neturi būti gable standartų");
        // RECTANGULAR → 4 grįžimai (po vieną kiekviename kampe)
        assertEquals(4, r.getReturnCount(), "Stačiakampis turi 4 return kampus");
        // 2 liftai su bortais viename → bortai > 0
        assertTrue(r.getBoards() > 0, "Liftas su bortais → bortai > 0");
    }

    @Test
    void lShapeTest() {
        // L-shape: 10m × 8m namas su 4m × 3m išpjova viršutiniame dešiniajame kampe
        ScaffoldInput input = new ScaffoldInput();
        input.setProjectName("L-shape test");
        input.setHouseShape(HouseShape.L_SHAPE);
        input.setHouseLength(10.0);
        input.setHouseWidth(8.0);
        input.setLCutLength(4.0);
        input.setLCutWidth(3.0);
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

        MaterialResult r = service.calculate(input);

        assertResultIsSane(r);
        // L-shape perimetras = 2*(L+W) = 2*(10+8) = 36 m (per projekto taisyklę)
        assertEquals(36.0, r.getPerimeter(), EPS, "L-shape perimetras lygus ribojančiam stačiakampiui");
        // L-shape turi 5 return kampus (D kampas — vidinis, praleistas)
        assertEquals(5, r.getReturnCount(), "L-shape: 5 return kampai (D praleistas)");
        assertNotNull(r.getLedgerScenario(), "Ledger scenario turi būti užpildytas");
        // Scenarijus → sutaupytų transomų > 0 (bent 1 kampas su TOP ledger)
        assertTrue(r.getTransomsSavedByTopLedgers() > 0,
                "Bent vienas TOP ledger kampas sutaupo transomus");
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

        MaterialResult r = service.calculate(input);

        assertResultIsSane(r);
        // RECTANGULAR 10×7 → perimetras = 2*(10+7) = 34 m
        assertEquals(34.0, r.getPerimeter(), EPS, "Stačiakampio perimetras");
        // 4 liftai → bendras aukštis = 1.5+1.0+1.5+1.0 = 5.0 m
        assertEquals(5.0, r.getTotalHeight(), EPS, "Bendras liftų aukštis");
        // 3+ liftai → visi standartai 21ft (pagal projekto taisyklę)
        assertEquals("21ft", r.getStandardTubeSize(),
                "3+ liftai → visi standartai 21ft");
    }
}
