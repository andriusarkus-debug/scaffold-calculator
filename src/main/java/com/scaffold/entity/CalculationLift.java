package com.scaffold.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.scaffold.model.enums.BoardSize;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

// Nenaudojame @Data — equals/hashCode pagal `id`, kad išvengtume circular reference
// su Calculation (Calculation.lifts → Lift.calculation → Calculation...).
@Entity
@Table(name = "calculation_lifts")
@Getter
@Setter
@ToString(exclude = "calculation") // neįtraukiame tėvo, kad nebūtų begalinės kilpos
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

    // equals/hashCode pagal `id` — saugu JPA entitetams.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CalculationLift that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
