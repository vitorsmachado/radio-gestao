package com.radiocom.ordemservico.application.dto;

import com.radiocom.estoque.domain.model.enums.FaixaEquipamento;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import jakarta.validation.constraints.NotBlank;
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
public class ItemEntradaCreateDTO {

    @NotNull
    private UUID osId;

    private UUID itemEstoqueId;

    /** Modelo do catálogo escolhido na entrada — opcional, só uma sugestão/referência. */
    private UUID catalogoModeloId;

    @NotNull
    private TipoItem tipoItem;

    @NotBlank
    @Size(max = 255)
    private String descricao;

    @Size(max = 50)
    private String numeroSerie;

    @Size(max = 50)
    private String patrimonio;

    @Size(max = 100)
    private String codigoCliente;

    /** Opcional — se ausente, assume 1. &gt; 1 só é permitido sem número de série/patrimônio. */
    private Integer quantidade;

    @Size(max = 100)
    private String marca;

    @Size(max = 100)
    private String modelo;

    /** Só relevante para equipamento (rádio) — faixa de frequência. */
    private FaixaEquipamento faixa;

    @Size(max = 1000)
    private String defeitoRelatado;

    private boolean garantia;
}
