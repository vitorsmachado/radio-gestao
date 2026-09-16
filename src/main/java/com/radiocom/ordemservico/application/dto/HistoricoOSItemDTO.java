package com.radiocom.ordemservico.application.dto;

import com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada;
import com.radiocom.ordemservico.domain.model.enums.StatusOS;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Uma passagem de um equipamento/acessório do cliente por uma OS — usado na
 * aba Garantia do detalhe do cliente pra mostrar o histórico de cada item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoOSItemDTO {

    private UUID osId;
    private String osNumero;
    private StatusOS osStatus;
    private StatusItemEntrada itemStatus;
    private LocalDateTime dataAbertura;
}
