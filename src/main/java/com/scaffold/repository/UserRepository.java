package com.scaffold.repository;

import com.scaffold.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository suteikia nemokamus metodus: save(), findById(), findAll(), delete() ir kt.
// Žemiau pridedame tik mums reikalingas papildomas užklausas.
public interface UserRepository extends JpaRepository<User, Long> { // Vartotojų repozitorija — sąsaja su duomenų baze

    // Spring automatiškai generuoja SQL užklausą iš metodo pavadinimo
    Optional<User> findByUsername(String username); // Randa vartotoją pagal vartotojo vardą
    Optional<User> findByEmail(String email); // Randa vartotoją pagal el. paštą
    boolean existsByUsername(String username); // Tikrina, ar vartotojo vardas jau egzistuoja
    boolean existsByEmail(String email); // Tikrina, ar el. paštas jau egzistuoja
}