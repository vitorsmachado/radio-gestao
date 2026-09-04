package com.radiocom.ordemservico.domain.repository;

import com.radiocom.ordemservico.domain.model.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, UUID> {

    Optional<OrdemServico> findByNumero(String numero);

    List<OrdemServico> findByClienteId(UUID clienteId);
}
