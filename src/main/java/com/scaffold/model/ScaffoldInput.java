package com.scaffold.model;

import com.scaffold.model.enums.HouseShape;
import com.scaffold.model.enums.LedgerScenario;
import com.scaffold.model.enums.RoofType;
import com.scaffold.model.enums.TubeSize;
import lombok.Data;

import java.util.List;

// Visa informacija, kurią vartotojas įveda skaičiuoklėje
@Data
public class ScaffoldInput {

    private String projectName;     // Projekto pavadinimas (pvz. "Jonų namas")

    private HouseShape houseShape;  // Namo forma: RECTANGULAR arba L_SHAPE

    private double houseLength;     // Namo ilgis metrais (bendra išorinė matmuo)
    private double houseWidth;      // Namo plotis metrais (bendra išorinė matmuo)

    // L formos namo papildomi matmenys — naudojami tik kai houseShape = L_SHAPE
    private double lCutLength;      // L išpjovos ilgis metrais (horizontali matmuo)
    private double lCutWidth;       // L išpjovos plotis metrais (vertikali matmuo)

    private List<LiftInput> lifts;


    private RoofType roofType;

    private double roofPitch;       // Stogo nuolydis laipsniais — naudojamas tik GABLE tipo stogui
    private int gableEnds;          // Frontonų skaičius (1 arba 2) — naudojamas tik GABLE tipo stogui

    private TubeSize tubeSize;      // Pasirinktas vamzdžio ilgis — naudojamas stovų (standards) gabalų skaičiui

    // Ledgerių scenarijus — nustato kurių sienų ledgeriai viršuje ties kampais.
    // Viršutinis ledgeris ties kampu veikia kaip transonas (taupo 2 transonus per kampą).
    // Jei nenurodytas — naudojamas SCENARIO_ONE pagal nutylėjimą.
    private LedgerScenario ledgerScenario;
}
