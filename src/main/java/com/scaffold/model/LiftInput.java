package com.scaffold.model;

import com.scaffold.model.enums.BoardSize;
import lombok.Data;


@Data
public class LiftInput {

    private double height;
    private boolean hasBoards;
    private BoardSize boardSize;
}
