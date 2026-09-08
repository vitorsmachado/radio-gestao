package com.radiocom.orcamento.domain.model;

import com.radiocom.orcamento.domain.model.enums.StatusOrcamento;
import com.radiocom.shared.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Envelope que agrupa {@link com.radiocom.ordemservico.domain.model.ItemEntrada}
 * de uma mesma OS para apresentar uma proposta formal ao cliente. Não duplica
 * a aprovação/rejeição — isso já mora em cada item (ver StatusItemEntrada);
 * aqui só fica o que é do envelope em si: validade, condições e desconto.
 */
@Entity
@Table(name = "orcamentos", indexes = {
        @Index(name = "idx_orcamento_numero", columnList = "numero", unique = true),
        @Index(name = "idx_orcamento_os", columnList = "os_id"),
        @Index(name = "idx_orcamento_cliente", columnList = "cliente_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Orcamento extends BaseEntity {

    @NotBlank
    @Column(name = "numero", nullable = false, unique = true, length = 20)
    private String numero; // ORC-2024-001

    @NotNull
    @Column(name = "os_id", nullable = false)
    private UUID osId;

    @NotNull
    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "validade")
    private LocalDate validade;

    @Column(name = "condicoes_pagamento", length = 500)
    private String condicoesPagamento;

    @Column(name = "desconto", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal desconto = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private StatusOrcamento status = StatusOrcamento.RASCUNHO;

    @Column(name = "data_emissao", nullable = false)
    @Builder.Default
    private LocalDateTime dataEmissao = LocalDateTime.now();

    @Column(name = "observacoes", length = 2000)
    private String observacoes;

    public void enviar() {
        validarStatus("Enviar orçamento", StatusOrcamento.RASCUNHO);
        this.status = StatusOrcamento.ENVIADO;
    }

    public void cancelar(String motivo) {
        validarStatus("Cancelar", StatusOrcamento.RASCUNHO, StatusOrcamento.ENVIADO);
        this.status = StatusOrcamento.CANCELADO;
        this.observacoes = (this.observacoes != null ? this.observacoes + "\n" : "")
                + "Cancelado em " + LocalDateTime.now() + ": " + motivo;
    }

    public boolean isExpirado() {
        return validade != null && status == StatusOrcamento.ENVIADO && LocalDate.now().isAfter(validade);
    }

    private void validarStatus(String operacao, StatusOrcamento... statusPermitidos) {
        for (StatusOrcamento s : statusPermitidos) {
            if (this.status == s) return;
        }
        throw new IllegalStateException(
                operacao + " requer status " + java.util.Arrays.toString(statusPermitidos)
                        + ". Status atual: " + this.status);
    }
}
