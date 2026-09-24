package com.radiocom.configuracao.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarConfiguracaoDTO {

    @NotNull
    @PositiveOrZero
    private BigDecimal valorMaoDeObraPadrao;
}
