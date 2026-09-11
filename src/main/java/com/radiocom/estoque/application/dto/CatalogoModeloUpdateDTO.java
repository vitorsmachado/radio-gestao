package com.radiocom.estoque.application.dto;

import com.radiocom.estoque.domain.model.enums.StatusItem;
import com.radiocom.estoque.domain.model.enums.TipoAcessorio;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoModeloUpdateDTO {

    private TipoAcessorio tipoAcessorio;

    @Size(max = 50)
    private String referencia;

    @Size(max = 255)
    private String descricao;

    @PositiveOrZero
    private BigDecimal valorReferencia;

    private StatusItem status;

    private Boolean controlePorSerie;

    private Boolean possuiPatrimonio;
}
