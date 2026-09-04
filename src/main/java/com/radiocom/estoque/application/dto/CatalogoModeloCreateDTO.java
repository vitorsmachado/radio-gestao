package com.radiocom.estoque.application.dto;

import com.radiocom.estoque.domain.model.enums.TipoAcessorio;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoModeloCreateDTO {

    @NotNull
    private TipoItem tipoItem;

    private TipoAcessorio tipoAcessorio;

    @Size(max = 50)
    private String referencia;

    @NotBlank
    @Size(max = 100)
    private String marca;

    @NotBlank
    @Size(max = 100)
    private String modelo;

    @Size(max = 255)
    private String descricao;

    private boolean controlePorSerie;

    private boolean possuiPatrimonio;
}
