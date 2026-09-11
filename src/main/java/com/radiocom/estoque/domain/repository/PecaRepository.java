package com.radiocom.estoque.domain.repository;

import com.radiocom.estoque.domain.model.Peca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PecaRepository extends JpaRepository<Peca, UUID> {

    Page<Peca> findByQuantidadeDisponivel(Integer quantidadeDisponivel, Pageable pageable);

    @Query("SELECT p FROM Peca p WHERE p.quantidadeMinima IS NOT NULL "
            + "AND p.quantidadeDisponivel <= p.quantidadeMinima AND p.quantidadeDisponivel > 0")
    Page<Peca> findEstoqueBaixo(Pageable pageable);
}
