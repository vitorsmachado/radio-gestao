package com.radiocom.ordemservico.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReordenarFilaDTO {

    @NotNull
    private AcaoReordenarFila acao;

    /** Índice (0-based) dentro do bloco atual da OS — obrigatório quando acao = POSICAO. */
    private Integer posicao;
}
