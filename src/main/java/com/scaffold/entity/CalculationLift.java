package com.scaffold.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.scaffold.model.enums.BoardSize;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "calculation_lifts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculationLift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculation_id", nullable = false)
    @JsonIgnore  // Neleidžiame JSON kilpai: Calculation → CalculationLift → Calculation...
    private Calculation calculation;

    private int liftNumber;
    private double height;
    private boolean hasBoards;

    @Enumerated(EnumType.STRING)
    private BoardSize boardSize;
}
