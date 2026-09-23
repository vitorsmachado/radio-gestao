package com.radiocom.ordemservico.garantia.domain.model;

import com.radiocom.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Cobertura de garantia de uma peça trocada num reparo, para um equipamento/
 * acessório específico do cliente ({@code itemEstoqueId}). Se o mesmo
 * equipamento voltar com defeito nessa mesma peça dentro do prazo, o técnico
 * resolve direto sem passar por orçamento (ver
 * {@code ItemEntradaDomainService.salvarAvaliacaoTecnica}).
 */
@Entity
@Table(name = "garantia_pecas", indexes = {
        @Index(name = "idx_garantia_peca_item_estoque", columnList = "item_estoque_id")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class GarantiaPeca extends BaseEntity {

    @NotNull
    @Column(name = "item_estoque_id", nullable = false)
    private UUID itemEstoqueId;

    @NotNull
    @Column(name = "peca_estoque_id", nullable = false)
    private UUID pecaEstoqueId;

    @Column(name = "descricao_peca", length = 255)
    private String descricaoPeca;

    @NotNull
    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @NotNull
    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @NotNull
    @Column(name = "item_entrada_origem_id", nullable = false)
    private UUID itemEntradaOrigemId;
}
