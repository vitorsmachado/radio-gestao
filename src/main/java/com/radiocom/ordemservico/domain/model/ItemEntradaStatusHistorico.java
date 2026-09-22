package com.radiocom.ordemservico.domain.model;

import com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada;
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
 * Registro histórico de uma transição de status do item de entrada, com
 * motivo opcional — permite auditar a jornada completa (avaliação,
 * autorização, manutenção, entrega). Nunca é atualizado após criado.
 */
@Entity
@Table(name = "item_entrada_status_historico")
@Getter
@SuperBuilder
@NoArgsConstructor
public class ItemEntradaStatusHistorico extends BaseEntity {

    @NotNull
    @Column(name = "item_entrada_id", nullable = false)
    private UUID itemEntradaId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", nullable = false, length = 20)
    private StatusItemEntrada statusAnterior;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false, length = 20)
    private StatusItemEntrada statusNovo;

    @Column(name = "motivo", length = 500)
    private String motivo;
}
