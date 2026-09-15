package com.radiocom.cliente.domain.repository;

import com.radiocom.cliente.domain.model.ClienteStatusHistorico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClienteStatusHistoricoRepository extends JpaRepository<ClienteStatusHistorico, UUID> {

    Page<ClienteStatusHistorico> findByClienteId(UUID clienteId, Pageable pageable);
}
