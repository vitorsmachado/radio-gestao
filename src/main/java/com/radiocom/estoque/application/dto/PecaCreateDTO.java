package com.radiocom.estoque.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PecaCreateDTO {

    @NotBlank
    @Size(max = 255)
    private String descricao;

    @PositiveOrZero
    private Integer quantidadeDisponivel;

    @PositiveOrZero
    private Integer quantidadeMinima;

    @PositiveOrZero
    private BigDecimal valorUnitario;

    private UUID catalogoModeloId; // Se ausente, resolve/cria pelo par marca+modelo

    @Size(max = 100)
    private String marca;

    @Size(max = 100)
    private String modelo;

    /** Modelos de equipamento (CatalogoModelo do tipo EQUIPAMENTO) com os quais a peça é compatível. */
    private List<UUID> modelosCompativeisIds;
}
