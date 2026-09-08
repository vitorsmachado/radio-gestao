package com.radiocom.orcamento.domain.repository;

import com.radiocom.orcamento.domain.model.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrcamentoRepository extends JpaRepository<Orcamento, UUID> {

    Optional<Orcamento> findByNumero(String numero);

    List<Orcamento> findByOsId(UUID osId);
}
