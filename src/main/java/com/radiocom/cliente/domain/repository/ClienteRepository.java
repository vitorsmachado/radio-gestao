package com.radiocom.cliente.domain.repository;

import com.radiocom.cliente.domain.model.Cliente;
import com.radiocom.cliente.domain.model.enums.StatusCliente;
import com.radiocom.cliente.domain.model.enums.TipoPessoa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    Optional<Cliente> findByDocumento(String documento);

    Optional<Cliente> findByDocumentoAndTipo(String documento, TipoPessoa tipo);

    boolean existsByDocumento(String documento);

    boolean existsByNumeroIdentificacaoAndIdNot(Integer numeroIdentificacao, UUID id);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cliente c WHERE c.documento = :documento AND c.id != :id")
    boolean existsByDocumentoAndIdNot(@Param("documento") String documento, @Param("id") UUID id);

    Page<Cliente> findByStatus(StatusCliente status, Pageable pageable);

    Page<Cliente> findByTipo(TipoPessoa tipo, Pageable pageable);

    Page<Cliente> findByNomeRazaoSocialContainingIgnoreCase(String nome, Pageable pageable);

    /** Usado por outros módulos (ex: busca de OS) pra resolver clientes por nome ou documento, sem paginação. */
    @Query("SELECT c FROM Cliente c WHERE "
            + "LOWER(c.nomeRazaoSocial) LIKE LOWER(CONCAT('%', :busca, '%')) OR "
            + "c.documento LIKE CONCAT('%', :busca, '%')")
    java.util.List<Cliente> buscarPorNomeOuDocumento(@Param("busca") String busca);

    /**
     * Listagem geral com busca opcional (nome/razão social, nome fantasia,
     * documento, número de identificação, nome de posto ou de contato, ou
     * clienteIdsMatched — resolvido pelo módulo Estoque a partir de N/S ou
     * código do cliente de um equipamento/acessório) e filtro opcional por
     * status. clienteIdsMatched nunca deve ser vazio — quando não há
     * equipamento/acessório correspondente, passe uma lista com um UUID que
     * nunca existirá (evita "IN ()" vazio no SQL).
     */
    @Query("SELECT c FROM Cliente c WHERE "
            + "(:busca IS NULL OR "
            + "   LOWER(c.nomeRazaoSocial) LIKE LOWER(CONCAT('%', :busca, '%')) OR "
            + "   LOWER(c.nomeFantasia) LIKE LOWER(CONCAT('%', :busca, '%')) OR "
            + "   c.documento LIKE CONCAT('%', :busca, '%') OR "
            + "   CAST(c.numeroIdentificacao AS string) LIKE CONCAT('%', :busca, '%') OR "
            + "   EXISTS (SELECT 1 FROM Posto p WHERE p.cliente = c AND LOWER(p.nome) LIKE LOWER(CONCAT('%', :busca, '%'))) OR "
            + "   EXISTS (SELECT 1 FROM Contato ct WHERE ct.cliente = c AND LOWER(ct.nome) LIKE LOWER(CONCAT('%', :busca, '%'))) OR "
            + "   c.id IN :itemClienteIdsMatched"
            + ") AND "
            + "(:status IS NULL OR c.status = :status)")
    Page<Cliente> buscar(@Param("busca") String busca, @Param("itemClienteIdsMatched") java.util.List<UUID> itemClienteIdsMatched,
                          @Param("status") StatusCliente status, Pageable pageable);

    @EntityGraph(attributePaths = {"contatos", "postos"})
    Optional<Cliente> findWithRelationsById(UUID id);
}
