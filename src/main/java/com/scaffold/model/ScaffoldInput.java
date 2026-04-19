package com.scaffold.model;

import com.scaffold.model.enums.BoardSize;
import com.scaffold.model.enums.HouseShape;
import com.scaffold.model.enums.LedgerScenario;
import com.scaffold.model.enums.RoofType;
import com.scaffold.model.enums.TubeSize;
import lombok.Data;

import java.util.List;

// Visa informacija, kurią vartotojas įveda skaičiuoklėje
@Data
public class ScaffoldInput {

    private String projectName;

    private HouseShape houseShape;

    private double houseLength;
    private double houseWidth;


    private double lCutLength;
    private double lCutWidth;

    private List<LiftInput> lifts;


    private RoofType roofType;

    private double roofPitch;
    private int gableEnds;

    private TubeSize tubeSize;


    private LedgerScenario ledgerScenario;

    /**
     * Paruošia formos duomenis skaičiavimui:
     * <ul>
     *   <li>palieka tik pirmuosius {@code liftCount} liftų (forma visada siunčia 4)</li>
     *   <li>bortams priskiria numatytą dydį (13ft — standartas namams)</li>
     *   <li>standartinių vamzdžių dydį nustato 21ft (tikrasis dydis parenkamas pagal liftų skaičių skaičiavimo metu)</li>
     * </ul>
     * Metodas yra ant modelio, nes operacija yra grynai duomenų paruošimas —
     * nesinaudoja jokiais Spring komponentais ir nepatenka į verslo logiką.
     */
    public void normalizeForCalculation(int liftCount) {
        if (lifts != null && liftCount > 0 && liftCount <= lifts.size()) {
            lifts = lifts.subList(0, liftCount);
        }
        if (lifts != null) {
            lifts.forEach(lift -> {
                if (lift.isHasBoards()) {
                    lift.setBoardSize(BoardSize.THIRTEEN_FOOT);
                }
            });
        }
        tubeSize = TubeSize.TWENTY_ONE_FOOT;
    }
}
