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

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cliente c WHERE c.documento = :documento AND c.id != :id")
    boolean existsByDocumentoAndIdNot(@Param("documento") String documento, @Param("id") UUID id);

    Page<Cliente> findByStatus(StatusCliente status, Pageable pageable);

    Page<Cliente> findByTipo(TipoPessoa tipo, Pageable pageable);

    Page<Cliente> findByNomeRazaoSocialContainingIgnoreCase(String nome, Pageable pageable);

    @EntityGraph(attributePaths = {"contatos", "postos"})
    Optional<Cliente> findWithRelationsById(UUID id);
}
