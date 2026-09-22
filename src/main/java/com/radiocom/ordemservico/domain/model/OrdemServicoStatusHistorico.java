package com.radiocom.ordemservico.domain.model;

import com.radiocom.ordemservico.domain.model.enums.StatusOS;
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
 * Registro histórico de uma transição de status da OS, com motivo opcional —
 * permite auditar a jornada completa (abertura, andamento, entrega,
 * cancelamento). Nunca é atualizado após criado.
 */
@Entity
@Table(name = "ordem_servico_status_historico")
@Getter
@SuperBuilder
@NoArgsConstructor
public class OrdemServicoStatusHistorico extends BaseEntity {

    @NotNull
    @Column(name = "ordem_servico_id", nullable = false)
    private UUID ordemServicoId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", nullable = false, length = 20)
    private StatusOS statusAnterior;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false, length = 20)
    private StatusOS statusNovo;

    @Column(name = "motivo", length = 500)
    private String motivo;
}
