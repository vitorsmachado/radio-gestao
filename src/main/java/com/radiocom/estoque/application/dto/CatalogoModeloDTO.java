package com.radiocom.estoque.application.dto;

import com.radiocom.estoque.domain.model.enums.StatusItem;
import com.radiocom.estoque.domain.model.enums.TipoAcessorio;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoModeloDTO {

    private UUID id;
    private TipoItem tipoItem;
    private TipoAcessorio tipoAcessorio;
    private String referencia;
    private String marca;
    private String modelo;
    private String descricao;
    private BigDecimal valorReferencia;
    private StatusItem status;
    private boolean controlePorSerie;
    private boolean possuiPatrimonio;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
}
