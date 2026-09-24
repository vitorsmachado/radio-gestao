package com.radiocom.estoque.application.dto;

import com.radiocom.estoque.domain.model.enums.StatusItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PecaDTO {

    private UUID id;
    private String codigo;
    private String descricao;
    private Integer quantidadeDisponivel;
    private Integer quantidadeMinima;
    private BigDecimal valorUnitario;
    private StatusItem status;
    private UUID catalogoModeloId;
    private String observacoes;
    private String localizacaoFisica;
    private boolean emFalta;
    private boolean estoqueBaixo;
    private List<CatalogoModeloDTO> modelosCompativeis;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
}
