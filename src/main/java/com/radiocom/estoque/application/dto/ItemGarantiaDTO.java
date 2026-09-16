package com.radiocom.estoque.application.dto;

import com.radiocom.estoque.domain.model.enums.TipoItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Equipamento ou acessório de propriedade do cliente, pra exibição na aba
 * Garantia do detalhe do cliente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemGarantiaDTO {

    private UUID id;
    private TipoItem tipoItem;
    private String codigo;
    private String descricao;
    private String numeroSerie;
    private String patrimonio;
    private LocalDate garantiaFim;
    private boolean emGarantia;
}
