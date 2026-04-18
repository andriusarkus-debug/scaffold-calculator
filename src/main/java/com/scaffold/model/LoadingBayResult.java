package com.scaffold.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoadingBayResult {

    // Boards
    private int boards13ft;
    private int boards5ft;

    // Tubes
    private int tubes21ft;
    private int tubes13ft;
    private int tubes10ft;
    private int tubes8ft;
    private int tubes6ft;
    private int tubes5ft;

    // Other
    private int soleBoards;
    private int loadingBayGates;

    // Fittings
    private int rightAngleCouplers;
    private int putlogCouplers;
    private int swivelCouplers;
}
