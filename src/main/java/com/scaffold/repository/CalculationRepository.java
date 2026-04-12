package com.scaffold.repository;

import com.scaffold.entity.Calculation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalculationRepository extends JpaRepository<Calculation, Long> {

    // Grąžina visus konkretaus vartotojo skaičiavimus, naujausius pirma
    List<Calculation> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Grąžina visus skaičiavimus (ROLE_MANAGER+), naujausius pirma
    List<Calculation> findAllByOrderByCreatedAtDesc();
}
