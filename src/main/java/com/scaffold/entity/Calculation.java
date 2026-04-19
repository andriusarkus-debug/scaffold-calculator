package com.scaffold.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.scaffold.entity.converter.MapToJsonConverter;
import com.scaffold.entity.converter.MapStringToJsonConverter;
import com.scaffold.model.enums.HouseShape;
import com.scaffold.model.enums.LedgerScenario;
import com.scaffold.model.enums.RoofType;
import com.scaffold.model.enums.TubeSize;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Vietoj @Data naudojame @Getter/@Setter — @Data generuoja equals/hashCode iš visų laukų
// (įskaitant `lifts` kolekciją), kas gali sukelti LazyInitializationException ir klaidingai
// veikti LinkedHashSet deduplicate(). equals/hashCode rašome rankiniu būdu, remdamiesi `id`.
@Entity
@Table(name = "calculations")
@Getter
@Setter
@ToString(exclude = "lifts") // neįtraukiame lazy kolekcijos į toString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Calculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Input: project info ---
    private String projectName;

    @Enumerated(EnumType.STRING)
    private HouseShape houseShape;

    private double houseLength;
    private double houseWidth;
    private double lCutLength;
    private double lCutWidth;

    @Enumerated(EnumType.STRING)
    private RoofType roofType;

    private double roofPitch;
    private int gableEnds;

    @Enumerated(EnumType.STRING)
    private TubeSize tubeSize;

    @Enumerated(EnumType.STRING)
    private LedgerScenario ledgerScenario;

    // --- Input: lifts ---
    @OneToMany(mappedBy = "calculation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CalculationLift> lifts;

    // --- Calculations ---
    private double perimeter;
    private int bays;
    private double totalHeight;
    private int tubesPerStandard;

    // --- Main components ---
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

    // --- Other elements ---
    private int swayBracing;
    private int ledgerBracing;
    private int toeboards;

    // --- Gables ---
    private int gableStandards;
    private int gableCouplers;

    // --- Returns ---
    private int returnCount;
    private int returnPlatformBoards;
    private int returnPlatformTransoms;
    private int returnPlatformLedgers;

    // --- Tube counts ---
    private int tubeCount5ft;
    private int tubeCount6ft;
    private int tubeCount8ft;
    private int tubeCountStandardSize;
    private String standardTubeSize;

    // --- Sway bracing ---
    private int swayBraceTubeCount;
    private String swayBraceTubeSize;

    // --- Ledger scenario ---
    private int transomsSavedByTopLedgers;

    // --- Map fields stored as JSON text ---
    @Convert(converter = MapToJsonConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Integer> boardSummary;

    @Convert(converter = MapToJsonConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Integer> ledgerTubeSummary;

    @Convert(converter = MapToJsonConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Integer> handrailTubeSummary;

    @Convert(converter = MapToJsonConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Integer> ledgerBraceTubeSummary;

    @Convert(converter = MapToJsonConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Integer> standardTubeSummary;

    // --- Per-wall breakdowns ---
    @Convert(converter = MapStringToJsonConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, String> faceLedgerTubeBreakdown;

    @Convert(converter = MapStringToJsonConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, String> faceBoardBreakdown;

    // equals/hashCode pagal `id` — saugu JPA entitetams su lazy kolekcijomis.
    // Kol id == null (dar neišsaugota), du skirtingi objektai lygūs tik jei tai tas pats instance.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Calculation that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
