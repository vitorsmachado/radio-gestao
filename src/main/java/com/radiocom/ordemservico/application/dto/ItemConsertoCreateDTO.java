package com.radiocom.ordemservico.application.dto;

import com.radiocom.ordemservico.domain.model.enums.TipoItemConserto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemConsertoCreateDTO {

    @NotNull
    private TipoItemConserto tipo;

    private UUID itemEstoqueId; // Para peças

    @Size(max = 255)
    private String descricao;

    @NotNull
    @Positive
    private Integer quantidade;

    @NotNull
    private BigDecimal valorUnitario;
}
