package com.scaffold.model.enums;

// Pastolių lentų standartiniai ilgiai pėdomis
public enum BoardSize {
    FIVE_FOOT(1.524, 3),     //  5ft = 1.524m — 3 transomų
    SIX_FOOT(1.829, 3),      //  6ft = 1.829m — 3 transomų
    EIGHT_FOOT(2.438, 4),    //  8ft = 2.438m — 4 transomų
    TEN_FOOT(3.048, 4),      // 10ft = 3.048m — 4 transomų
    THIRTEEN_FOOT(3.962, 5); // 13ft = 3.962m — 5 transomų

    private final double lengthM;   // Lentos ilgis metrais
    private final int transomCount; // Transomų skaičius šiai lentai

    BoardSize(double lengthM, int transomCount) {
        this.lengthM = lengthM;
        this.transomCount = transomCount;
    }

    public double getLengthM() {
        return lengthM;
    }

    public int getTransomCount() {
        return transomCount;
    }
}
