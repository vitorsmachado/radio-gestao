package com.radiocom.ordemservico.application.dto;

import com.radiocom.ordemservico.domain.model.enums.ResultadoAvaliacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalvarAvaliacaoTecnicaDTO {

    @NotNull
    private ResultadoAvaliacao resultado;

    @Size(max = 500)
    private String detalheAjuste;

    @Size(max = 1000)
    private String defeitoEncontrado;

    @Size(max = 1000)
    private String causaDefeito;

    @Size(max = 1000)
    private String solucaoRecomendada;

    @Size(max = 1000)
    private String observacoesTecnicas;

    /** Cobertura de garantia (de {@code GET /itens-entrada/{id}/garantia-disponivel}) que o técnico está reivindicando, se houver. */
    private UUID garantiaPecaId;
}
