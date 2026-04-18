package com.scaffold.repository;

import com.scaffold.entity.Calculation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CalculationRepository extends JpaRepository<Calculation, Long> {

    // Grąžina visus konkretaus vartotojo skaičiavimus su liftais viena užklausa
    @Query("SELECT DISTINCT c FROM Calculation c LEFT JOIN FETCH c.lifts WHERE c.user.id = :userId ORDER BY c.createdAt DESC")
    List<Calculation> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // Grąžina visus skaičiavimus su user ir liftais viena užklausa
    @Query("SELECT DISTINCT c FROM Calculation c LEFT JOIN FETCH c.lifts LEFT JOIN FETCH c.user ORDER BY c.createdAt DESC")
    List<Calculation> findAllByOrderByCreatedAtDesc();
}
