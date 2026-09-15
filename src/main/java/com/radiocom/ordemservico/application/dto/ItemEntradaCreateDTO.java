package com.radiocom.ordemservico.application.dto;

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

    @Size(max = 100)
    private String marca;

    @Size(max = 100)
    private String modelo;

    @Size(max = 1000)
    private String defeitoRelatado;

    private boolean garantia;
}
