package com.scaffold.service;

import com.scaffold.model.LadderTowerResult;
import com.scaffold.model.LoadingBayResult;
import org.springframework.stereotype.Service;

@Service
public class AccessTowerService {

    /**
     * Apskaičiuoja loading bay medžiagas pagal liftų skaičių.
     * Lift 1: pilnas bazinis rinkinys + gate
     * Lift 2: fittings + tubes (kaip 4)
     * Lift 3: fittings + tubes (skirtingi)
     * Lift 4: tas pats kaip lift 2
     */
    public LoadingBayResult calculateLoadingBay(int liftCount) {
        int boards13ft = 0, boards5ft = 0;
        int tubes21ft = 0, tubes13ft = 0, tubes10ft = 0;
        int tubes8ft = 0, tubes6ft = 0, tubes5ft = 0;
        int soleBoards = 0, gates = 0;
        int raCoups = 0, putlogCoups = 0, swivelCoups = 0;

        // --- Lift 1 ---
        boards13ft += 6;
        boards5ft  += 8;
        tubes21ft  += 4;
        tubes13ft  += 4;
        tubes8ft   += 3;
        tubes10ft  += 2;
        tubes5ft   += 31;
        soleBoards += 10;
        gates      += 1;
        raCoups    += 40;
        putlogCoups += 45;
        swivelCoups += 4;

        // --- Lift 2 ---
        if (liftCount >= 2) {
            raCoups     += 40;
            swivelCoups += 2;
            putlogCoups += 30;
            tubes5ft    += 24;
            tubes10ft   += 2;
            tubes6ft    += 3;
        }

        // --- Lift 3 ---
        if (liftCount >= 3) {
            raCoups     += 40;
            swivelCoups += 2;
            putlogCoups += 30;
            tubes10ft   += 2;
            tubes8ft    += 3;
            tubes6ft    += 6;
            tubes5ft    += 16;
        }

        // --- Lift 4 (tas pats kaip lift 2) ---
        if (liftCount >= 4) {
            raCoups     += 40;
            swivelCoups += 2;
            putlogCoups += 30;
            tubes5ft    += 24;
            tubes10ft   += 2;
            tubes6ft    += 3;
        }

        return LoadingBayResult.builder()
                .boards13ft(boards13ft)
                .boards5ft(boards5ft)
                .tubes21ft(tubes21ft)
                .tubes13ft(tubes13ft)
                .tubes10ft(tubes10ft)
                .tubes8ft(tubes8ft)
                .tubes6ft(tubes6ft)
                .tubes5ft(tubes5ft)
                .soleBoards(soleBoards)
                .loadingBayGates(gates)
                .rightAngleCouplers(raCoups)
                .putlogCouplers(putlogCoups)
                .swivelCouplers(swivelCoups)
                .build();
    }

    /**
     * Apskaičiuoja ladder tower medžiagas pagal liftų skaičių.
     * 1-2 liftai: bazinis rinkinys
     * 3-4 liftai: bazinis + papildomi (1 kopėčios, 1 gate, 4x5ft, 6x RA)
     */
    public LadderTowerResult calculateLadderTower(int liftCount) {
        // Bazinis rinkinys (1-2 liftai)
        int boards13ft  = 7;
        int boards5ft   = 2;
        int boards8ft   = 1;
        int tubes13ft   = 3;
        int tubes10ft   = 1;
        int tubes8ft    = 3;
        int tubes5ft    = 14;
        int raCoups     = 26;
        int putlogCoups = 18;
        int swivelCoups = 6;
        int ladders     = 1;
        int gates       = 1;

        // Papildomi komponentai 3-4 liftams
        if (liftCount >= 3) {
            tubes5ft += 4;
            raCoups  += 6;
            ladders  += 1;
            gates    += 1;
        }

        return LadderTowerResult.builder()
                .boards13ft(boards13ft)
                .boards5ft(boards5ft)
                .boards8ft(boards8ft)
                .tubes13ft(tubes13ft)
                .tubes10ft(tubes10ft)
                .tubes8ft(tubes8ft)
                .tubes5ft(tubes5ft)
                .rightAngleCouplers(raCoups)
                .putlogCouplers(putlogCoups)
                .swivelCouplers(swivelCoups)
                .ladders4m(ladders)
                .ladderGates(gates)
                .build();
    }
}
