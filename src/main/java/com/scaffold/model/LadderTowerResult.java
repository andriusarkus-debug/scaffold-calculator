package com.scaffold.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LadderTowerResult {

    // Boards
    private int boards13ft;
    private int boards5ft;
    private int boards8ft;

    // Tubes
    private int tubes13ft;
    private int tubes10ft;
    private int tubes8ft;
    private int tubes5ft;

    // Fittings
    private int rightAngleCouplers;
    private int putlogCouplers;
    private int swivelCouplers;

    // Access
    private int ladders4m;
    private int ladderGates;
}
