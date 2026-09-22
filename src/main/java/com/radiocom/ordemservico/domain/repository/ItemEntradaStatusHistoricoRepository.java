package com.radiocom.ordemservico.domain.repository;

import com.radiocom.ordemservico.domain.model.ItemEntradaStatusHistorico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItemEntradaStatusHistoricoRepository extends JpaRepository<ItemEntradaStatusHistorico, UUID> {

    Page<ItemEntradaStatusHistorico> findByItemEntradaId(UUID itemEntradaId, Pageable pageable);
}
