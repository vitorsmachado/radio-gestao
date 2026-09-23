package com.radiocom.ordemservico.domain.repository;

import com.radiocom.ordemservico.domain.model.OrdemServico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, UUID> {

    Optional<OrdemServico> findByNumero(String numero);

    List<OrdemServico> findByClienteIdOrderByDataAberturaDesc(UUID clienteId);

    /**
     * Listagem geral com busca opcional (número da OS, NS/código do cliente
     * dos itens, ou cliente — via clienteIdsMatched já resolvido pelo módulo
     * Cliente) e filtro opcional por período de abertura. clienteIdsMatched
     * nunca deve ser vazio — quando não há cliente correspondente, passe uma
     * lista com um UUID que nunca existirá (evita "IN ()" vazio no SQL).
     */
    @Query("SELECT os FROM OrdemServico os WHERE "
            + "(:busca IS NULL OR "
            + "   LOWER(os.numero) LIKE LOWER(CONCAT('%', :busca, '%')) OR "
            + "   os.clienteId IN :clienteIdsMatched OR "
            + "   EXISTS (SELECT 1 FROM ItemEntrada ie WHERE ie.osId = os.id AND ("
            + "       LOWER(ie.numeroSerie) LIKE LOWER(CONCAT('%', :busca, '%')) OR "
            + "       LOWER(ie.codigoCliente) LIKE LOWER(CONCAT('%', :busca, '%'))))"
            + ") AND "
            + "(:dataInicial IS NULL OR os.dataAbertura >= :dataInicial) AND "
            + "(:dataFinal IS NULL OR os.dataAbertura <= :dataFinal)")
    Page<OrdemServico> buscar(
            @Param("busca") String busca,
            @Param("clienteIdsMatched") List<UUID> clienteIdsMatched,
            @Param("dataInicial") LocalDateTime dataInicial,
            @Param("dataFinal") LocalDateTime dataFinal,
            Pageable pageable);

    /** Usado por outros módulos (Orçamento) pra resolver OS cujo número casa com uma busca textual. */
    @Query("SELECT os.id FROM OrdemServico os WHERE LOWER(os.numero) LIKE LOWER(CONCAT('%', :busca, '%'))")
    List<UUID> buscarIdsPorNumero(@Param("busca") String busca);
}
