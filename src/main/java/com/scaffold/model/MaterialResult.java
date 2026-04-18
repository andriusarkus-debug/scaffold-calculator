package com.scaffold.model;

import com.scaffold.model.enums.LedgerScenario;
import lombok.Builder;
import lombok.Data;

import java.util.Map;


@Data
@Builder
public class MaterialResult {

    // --- Main components  ---
    private int standards;
    private int ledgers;
    private int handrails;
    private int advanceGuardRailSets;
    private int transoms;
    private int boards;
    private int basePlates;
    private int soleBoards;

    // --- Fittings ---
    private int rightAngleCouplers;
    private int swivelCouplers;
    private int sleeveCouplers;
    private int putlogCouplers;

    // --- Others elements  ---
    private int swayBracing;
    private int ledgerBracing;
    private int toeboards;

    // --- Gables ---
    private int gableStandards;
    private int gableCouplers;

    // --- Returns ---
    private int returnCount;             // Corners count
    private int returnPlatformBoards;    // Corner boards
    private int returnPlatformTransoms;  // Corner transoms
    private int returnPlatformLedgers;   // Corner ledgers

    // --- Boards, Ledger, Brace, Handrails summary  ---
    private Map<String, Integer> boardSummary;
    private Map<String, Integer> ledgerTubeSummary;
    private Map<String, Integer> handrailTubeSummary;
    private Map<String, Integer> ledgerBraceTubeSummary;
    private int swayBraceTubeCount;
    private String swayBraceTubeSize;

    // --- Tubes size in order  ---
    private int tubeCount5ft;          // transoms
    private int tubeCount6ft;          // ledger bracing
    private int tubeCount8ft;          // 8ft vamzdžiai — sway bracingui
    private int tubeCountStandardSize; // Standarts
    private String standardTubeSize;   // Standarts size

    // --- Standards tube breakdown (inside vs outside rows) ---
    // 2 lifts:  {"10ft": X, "13ft": Y}  inside row = 10ft, outside row = 13ft
    // 3+ lifts: {"21ft": Z}             all standards = 21ft
    private Map<String, Integer> standardTubeSummary;

    // --- Ledger scenario ---
    private LedgerScenario ledgerScenario;   // Pasirinktas scenarijus (ONE arba TWO)
    private int transomsSavedByTopLedgers;   // Sutaupytų transomų skaičius (kampai × 2)

    // --- Per-face ledger tube breakdown ---
    // Key:   "Wall 1 – Bottom/Front (15.0m) – TOP"
    // Value: "21ft + 16ft + 13ft + 10ft  [run: 18.05m]"
    private Map<String, String> faceLedgerTubeBreakdown;

    // --- Per-face board breakdown ---
    // Key:   "Wall 1 – Bottom/Front (15.0m) – TOP"
    // Value: "24× 13ft + 6× 8ft  (18.05m)"
    private Map<String, String> faceBoardBreakdown;

    // --- Calculations  ---
    private double perimeter;
    private int bays;
    private double totalHeight;
    private int tubesPerStandard;

    // --- Access towers ---
    private LoadingBayResult loadingBay;
    private LadderTowerResult ladderTower;
}
