package com.radiocom.ordemservico.domain.model;

import com.radiocom.ordemservico.domain.model.enums.TipoItemConserto;
import com.radiocom.shared.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@Table(name = "os_itens_conserto")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemConserto extends BaseEntity {

    /**
     * Lado dono do relacionamento bidirecional com {@link ItemEntrada}.
     * Precisa ser bidirecional (não só @JoinColumn do lado do pai) porque
     * item_entrada_id é NOT NULL: um @OneToMany unidirecional faz o Hibernate
     * inserir a linha filha sem a FK e só preencher num UPDATE separado depois,
     * o que viola a constraint. Com o filho dono da FK, ela já vai no INSERT.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_entrada_id", nullable = false)
    private ItemEntrada itemEntrada;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoItemConserto tipo;

    @Column(name = "item_estoque_id")
    private UUID itemEstoqueId; // Para peças — referência ao estoque

    @Column(name = "descricao", length = 255)
    private String descricao; // Ex: "Bateria BP-227" ou "Mão de obra técnica"

    @NotNull
    @Positive
    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    @NotNull
    @Column(name = "valor_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorUnitario;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal;

    @PrePersist
    @PreUpdate
    public void calcularTotal() {
        if (valorUnitario == null) {
            throw new IllegalStateException(
                    "valorUnitario não pode ser null ao calcular total do ItemConserto");
        }
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalStateException(
                    "quantidade deve ser positiva ao calcular total do ItemConserto. Valor: " + quantidade);
        }
        this.valorTotal = valorUnitario
                .multiply(BigDecimal.valueOf(quantidade))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
