package com.radiocom.estoque.domain.repository;

import com.radiocom.estoque.domain.model.Acessorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcessorioRepository extends JpaRepository<Acessorio, UUID> {

    Optional<Acessorio> findByNumeroSerie(String numeroSerie);

    Optional<Acessorio> findByPatrimonio(String patrimonio);

    /** Usado pelo módulo Cliente pra resolver, na busca geral, quais clientes têm um acessório com esse N/S ou código próprio. */
    @Query("SELECT DISTINCT a.clienteId FROM Acessorio a WHERE a.clienteId IS NOT NULL AND ("
            + "LOWER(a.numeroSerie) LIKE LOWER(CONCAT('%', :busca, '%')) OR "
            + "LOWER(a.codigoCliente) LIKE LOWER(CONCAT('%', :busca, '%')))")
    List<UUID> buscarClienteIdsPorNumeroSerieOuCodigoCliente(@Param("busca") String busca);
}
