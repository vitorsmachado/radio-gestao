package com.radiocom.estoque.domain.repository;

import com.radiocom.estoque.domain.model.MovimentacaoEstoque;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, UUID> {

    Page<MovimentacaoEstoque> findByItemId(UUID itemId, Pageable pageable);
}
