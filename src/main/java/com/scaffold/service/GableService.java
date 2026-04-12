package com.scaffold.service;

import com.scaffold.model.ScaffoldConstants;
import com.scaffold.model.ScaffoldInput;
import org.springframework.stereotype.Service;

@Service
public class GableService {

    public int calculateGableStandards(ScaffoldInput input) {
        int gableBays = (int) Math.ceil(input.getHouseWidth() / ScaffoldConstants.BAY_SPACING);
        int standardsPerGable = gableBays + 1;
        return standardsPerGable * input.getGableEnds();
    }

    public int calculateGableCouplers(ScaffoldInput input) {
        int gableBays = (int) Math.ceil(input.getHouseWidth() / ScaffoldConstants.BAY_SPACING);
        int couplersPerGable = gableBays * 6;
        return couplersPerGable * input.getGableEnds();
    }
}
