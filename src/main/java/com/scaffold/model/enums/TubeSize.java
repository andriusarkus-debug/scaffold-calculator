package com.scaffold.model.enums;


public enum TubeSize {
    FIVE_FOOT(1.524),       //  5ft = 1.524m
    SIX_FOOT(1.829),        //  6ft = 1.829m
    EIGHT_FOOT(2.438),      //  8ft = 2.438m
    TEN_FOOT(3.048),        // 10ft = 3.048m
    THIRTEEN_FOOT(3.962),   // 13ft = 3.962m
    SIXTEEN_FOOT(4.877),    // 16ft = 4.877m
    TWENTY_ONE_FOOT(6.401); // 21ft = 6.401m

    private final double lengthM; // Vamzdžio ilgis metrais

    TubeSize(double lengthM) {
        this.lengthM = lengthM; // Priskiriame ilgio reikšmę
    }

    public double getLengthM() {
        return lengthM; // Grąžiname vamzdžio ilgį metrais
    }
}
