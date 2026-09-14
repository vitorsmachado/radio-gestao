package com.radiocom.estoque.domain.model;

import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.estoque.domain.model.enums.TipoMovimentacao;
import com.radiocom.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Registro histórico de uma entrada/saída/ajuste de quantidade em Acessório
 * ou Peça — permite justificar (motivo) e auditar por que o saldo mudou.
 * Nunca é atualizado após criado.
 */
@Entity
@Table(name = "movimentacoes_estoque")
@Getter
@SuperBuilder
@NoArgsConstructor
public class MovimentacaoEstoque extends BaseEntity {

    @NotNull
    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_item", nullable = false, length = 20)
    private TipoItem tipoItem;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimentacao", nullable = false, length = 20)
    private TipoMovimentacao tipoMovimentacao;

    @NotNull
    @Column(name = "saldo_anterior", nullable = false)
    private Integer saldoAnterior;

    @NotNull
    @Column(name = "saldo_novo", nullable = false)
    private Integer saldoNovo;

    @Column(name = "motivo", length = 500)
    private String motivo;
}
