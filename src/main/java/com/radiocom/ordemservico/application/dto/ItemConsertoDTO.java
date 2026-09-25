package com.radiocom.ordemservico.application.dto;

import com.radiocom.ordemservico.domain.model.enums.TipoItemConserto;
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
public class ItemConsertoDTO {

    private UUID id;
    private TipoItemConserto tipo;
    private UUID itemEstoqueId;
    private String descricao;
    private Integer quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal valorTotal;
    /** Se essa peça tem cobertura de garantia ativa agora — só informativo, não muda o preço nem o fluxo. */
    private boolean coberto;
}
