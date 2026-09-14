package com.radiocom.estoque.application.dto;

import com.radiocom.estoque.domain.model.enums.TipoMovimentacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoEstoqueDTO {

    private UUID id;
    private TipoMovimentacao tipoMovimentacao;
    private Integer saldoAnterior;
    private Integer saldoNovo;
    private String motivo;
    private LocalDateTime dataCriacao;
}
