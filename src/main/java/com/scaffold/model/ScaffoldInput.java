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
}
