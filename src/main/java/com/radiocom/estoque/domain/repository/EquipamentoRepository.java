package com.radiocom.estoque.domain.repository;

import com.radiocom.estoque.domain.model.Equipamento;
import com.radiocom.estoque.domain.model.enums.EstadoEquipamento;
import com.radiocom.estoque.domain.model.enums.ProprietarioEquipamento;
import org.springframework.data.jpa.repository.JpaRepository;
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

    List<Equipamento> findByProprietarioAndEstado(ProprietarioEquipamento proprietario, EstadoEquipamento estado);

    boolean existsByCodigo(String codigo);

    boolean existsByNumeroSerie(String numeroSerie);

    boolean existsByPatrimonio(String patrimonio);
}
