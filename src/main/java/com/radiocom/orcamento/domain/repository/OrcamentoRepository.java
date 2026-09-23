package com.radiocom.orcamento.domain.repository;

import com.radiocom.orcamento.domain.model.Orcamento;
import com.radiocom.orcamento.domain.model.enums.StatusOrcamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrcamentoRepository extends JpaRepository<Orcamento, UUID> {

    Optional<Orcamento> findByNumero(String numero);

    List<Orcamento> findByOsId(UUID osId);

    /**
     * Listagem geral com busca opcional (número da OS de origem, nome/documento
     * do cliente já resolvido pelo módulo Cliente, ou N/S de um item agrupado)
     * e filtro opcional por status. Os UUIDs sentinela evitam "IN ()" vazio.
     */
    @Query("SELECT o FROM Orcamento o WHERE "
            + "(:busca IS NULL OR "
            + "   o.osId IN :osIdsMatched OR "
            + "   o.clienteId IN :clienteIdsMatched OR "
            + "   EXISTS (SELECT 1 FROM ItemEntrada ie WHERE ie.orcamentoId = o.id AND "
            + "       LOWER(ie.numeroSerie) LIKE LOWER(CONCAT('%', :busca, '%')))"
            + ") AND "
            + "(:status IS NULL OR o.status = :status)")
    Page<Orcamento> buscar(
            @Param("busca") String busca,
            @Param("osIdsMatched") List<UUID> osIdsMatched,
            @Param("clienteIdsMatched") List<UUID> clienteIdsMatched,
            @Param("status") StatusOrcamento status,
            Pageable pageable);
}
