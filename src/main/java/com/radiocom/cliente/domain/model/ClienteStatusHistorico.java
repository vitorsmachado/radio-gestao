package com.radiocom.cliente.domain.model;

import com.radiocom.cliente.domain.model.enums.StatusCliente;
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
 * Registro histórico de uma transição de status do cliente (ativar/inativar/
 * bloquear), com motivo opcional — permite auditar por que o status mudou.
 * Nunca é atualizado após criado.
 */
@Entity
@Table(name = "cliente_status_historico")
@Getter
@SuperBuilder
@NoArgsConstructor
public class ClienteStatusHistorico extends BaseEntity {

    @NotNull
    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", nullable = false, length = 20)
    private StatusCliente statusAnterior;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false, length = 20)
    private StatusCliente statusNovo;

    @Column(name = "motivo", length = 500)
    private String motivo;
}
