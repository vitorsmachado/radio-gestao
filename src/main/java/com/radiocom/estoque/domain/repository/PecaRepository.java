package com.radiocom.estoque.domain.repository;

import com.radiocom.estoque.domain.model.Peca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PecaRepository extends JpaRepository<Peca, UUID> {
}
