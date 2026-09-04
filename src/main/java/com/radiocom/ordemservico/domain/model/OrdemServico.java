package com.radiocom.ordemservico.domain.model;

import com.radiocom.ordemservico.domain.model.enums.StatusOS;
import com.radiocom.shared.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Envelope da jornada de manutenção do cliente — número, cliente, técnico e
 * um status agregado simples. O progresso fino (avaliado, autorizado,
 * aguardando peça, entregue) mora em cada {@link ItemEntrada}, não aqui.
 */
@Entity
@Table(name = "ordens_servico", indexes = {
        @Index(name = "idx_os_numero", columnList = "numero", unique = true),
        @Index(name = "idx_os_cliente", columnList = "cliente_id"),
        @Index(name = "idx_os_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServico extends BaseEntity {

    @NotBlank
    @Column(name = "numero", nullable = false, unique = true, length = 20)
    private String numero; // OS-2024-001

    @NotNull
    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "posto_id")
    private UUID postoId;

    @Column(name = "tecnico_id")
    private UUID tecnicoId;

    @Column(name = "solicitante", length = 100)
    private String solicitante; // Quem deixou o(s) item(ns)

    @Column(name = "recebedor_nome", length = 100)
    private String recebedorNome; // Quem retirou na entrega

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private StatusOS status = StatusOS.ABERTA;

    @Column(name = "data_abertura", nullable = false)
    @Builder.Default
    private LocalDateTime dataAbertura = LocalDateTime.now();

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    @Column(name = "observacoes", length = 2000)
    private String observacoes;

    // ===== TRANSIÇÕES =====

    public void iniciarAndamento() {
        validarStatus("Iniciar andamento", StatusOS.ABERTA);
        this.status = StatusOS.EM_ANDAMENTO;
    }

    /**
     * OS só se conclui via entrega — por isso pede quem retirou os itens.
     */
    public void confirmarEntrega(String nomeRecebedor) {
        validarStatus("Confirmar entrega", StatusOS.ABERTA, StatusOS.EM_ANDAMENTO);
        if (nomeRecebedor == null || nomeRecebedor.isBlank()) {
            throw new IllegalArgumentException("Nome de quem recebeu é obrigatório para concluir a OS");
        }
        this.recebedorNome = nomeRecebedor;
        this.status = StatusOS.CONCLUIDA;
        this.dataConclusao = LocalDateTime.now();
    }

    public void cancelar(String motivo) {
        validarStatus("Cancelar", StatusOS.ABERTA, StatusOS.EM_ANDAMENTO);
        this.status = StatusOS.CANCELADA;
        this.observacoes = (this.observacoes != null ? this.observacoes + "\n" : "")
                + "Cancelada em " + LocalDateTime.now() + ": " + motivo;
    }

    public boolean isEncerrada() {
        return this.status == StatusOS.CONCLUIDA || this.status == StatusOS.CANCELADA;
    }

    private void validarStatus(String operacao, StatusOS... statusPermitidos) {
        for (StatusOS s : statusPermitidos) {
            if (this.status == s) return;
        }
        throw new IllegalStateException(
                operacao + " requer status " + java.util.Arrays.toString(statusPermitidos)
                        + ". Status atual: " + this.status);
    }
}
