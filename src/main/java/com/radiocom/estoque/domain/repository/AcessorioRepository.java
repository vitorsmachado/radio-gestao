package com.radiocom.estoque.domain.repository;

import com.radiocom.estoque.domain.model.Acessorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AcessorioRepository extends JpaRepository<Acessorio, UUID> {
}
