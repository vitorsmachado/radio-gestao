package com.radiocom.estoque.domain.model;

import com.radiocom.estoque.domain.model.enums.StatusItem;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.shared.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class ItemEstoque extends BaseEntity {

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @NotBlank
    @Column(name = "descricao", nullable = false, length = 255)
    private String descricao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoItem tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalogo_modelo_id")
    private CatalogoModelo catalogoModelo;

    @PositiveOrZero
    @Column(name = "quantidade_disponivel", nullable = false)
    @Builder.Default
    private Integer quantidadeDisponivel = 0;

    @PositiveOrZero
    @Column(name = "quantidade_minima")
    private Integer quantidadeMinima; // Alerta de reposição

    @Column(name = "localizacao_fisica", length = 50)
    private String localizacaoFisica; // Prateleira, depósito, etc.

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private StatusItem status = StatusItem.ATIVO;

    @Column(name = "observacoes", length = 1000)
    private String observacoes;

    @Column(name = "codigo_cliente", length = 100)
    private String codigoCliente;

    public void entrada(Integer quantidade) {
        this.quantidadeDisponivel += quantidade;
    }

    public void saida(Integer quantidade) {
        if (quantidade > this.quantidadeDisponivel) {
            throw new IllegalStateException("Quantidade indisponível para saída");
        }
        this.quantidadeDisponivel -= quantidade;
    }

    public void ajustar(Integer novaQuantidade) {
        if (novaQuantidade == null || novaQuantidade < 0) {
            throw new IllegalArgumentException(
                    "Nova quantidade não pode ser nula ou negativa. Valor: " + novaQuantidade);
        }
        this.quantidadeDisponivel = novaQuantidade;
    }

    public abstract boolean possuiNumeroSerie();
    public abstract boolean possuiPatrimonio();
}
