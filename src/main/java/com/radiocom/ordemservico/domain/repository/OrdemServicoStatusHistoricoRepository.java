package com.radiocom.ordemservico.domain.repository;

import com.radiocom.ordemservico.domain.model.OrdemServicoStatusHistorico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrdemServicoStatusHistoricoRepository extends JpaRepository<OrdemServicoStatusHistorico, UUID> {

    Page<OrdemServicoStatusHistorico> findByOrdemServicoId(UUID ordemServicoId, Pageable pageable);
}
