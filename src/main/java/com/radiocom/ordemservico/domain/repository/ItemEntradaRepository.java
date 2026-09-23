package com.radiocom.ordemservico.domain.repository;

import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItemEntradaRepository extends JpaRepository<ItemEntrada, UUID> {

    List<ItemEntrada> findByOsId(UUID osId);

    List<ItemEntrada> findByOrcamentoId(UUID orcamentoId);

    /** Usado pela fila de manutenção do técnico — todos os itens relevantes, de qualquer OS. */
    List<ItemEntrada> findByStatusIn(List<StatusItemEntrada> status);

    /** Usado pela aba Garantia do cliente — todas as passagens de um equipamento/acessório por uma OS. */
    List<ItemEntrada> findByItemEstoqueIdOrderByDataCriacaoDesc(UUID itemEstoqueId);

    /**
     * Itens aguardando uma peça específica — usado quando a peça recebe
     * entrada no estoque, para saber quais itens podem voltar pra fila.
     */
    @Query("SELECT DISTINCT ie FROM ItemEntrada ie JOIN ie.itensConserto ic " +
           "WHERE ie.status = :status AND ic.itemEstoqueId = :itemEstoqueId")
    List<ItemEntrada> findByStatusAndItemEstoqueId(
            @Param("status") StatusItemEntrada status,
            @Param("itemEstoqueId") UUID itemEstoqueId);
}
