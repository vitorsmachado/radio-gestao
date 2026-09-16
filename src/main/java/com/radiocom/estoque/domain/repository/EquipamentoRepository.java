package com.radiocom.estoque.domain.repository;

import com.radiocom.estoque.domain.model.Equipamento;
import com.radiocom.estoque.domain.model.enums.EstadoEquipamento;
import com.radiocom.estoque.domain.model.enums.ProprietarioEquipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EquipamentoRepository extends JpaRepository<Equipamento, UUID> {

    Optional<Equipamento> findByNumeroSerie(String numeroSerie);

    Optional<Equipamento> findByPatrimonio(String patrimonio);

    List<Equipamento> findByEstado(EstadoEquipamento estado);

    List<Equipamento> findByProprietario(ProprietarioEquipamento proprietario);

    boolean existsByNumeroSerie(String numeroSerie);

    boolean existsByPatrimonio(String patrimonio);

    /** Usado pelo módulo Cliente pra resolver, na busca geral, quais clientes têm um equipamento com esse N/S ou código próprio. */
    @Query("SELECT DISTINCT e.clienteId FROM Equipamento e WHERE e.clienteId IS NOT NULL AND ("
            + "LOWER(e.numeroSerie) LIKE LOWER(CONCAT('%', :busca, '%')) OR "
            + "LOWER(e.codigoCliente) LIKE LOWER(CONCAT('%', :busca, '%')))")
    List<UUID> buscarClienteIdsPorNumeroSerieOuCodigoCliente(@Param("busca") String busca);
}
