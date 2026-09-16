package com.radiocom.estoque.application.dto;

import com.radiocom.estoque.domain.model.enums.EstadoEquipamento;
import com.radiocom.estoque.domain.model.enums.FaixaEquipamento;
import com.radiocom.estoque.domain.model.enums.ProprietarioEquipamento;
import com.radiocom.estoque.domain.model.enums.StatusItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipamentoDTO {

    private UUID id;
    private String codigo;
    private String descricao;
    private ProprietarioEquipamento proprietario;
    private String numeroSerie;
    private String patrimonio;
    private UUID clienteId;
    private String codigoCliente;
    private FaixaEquipamento faixa;
    private EstadoEquipamento estado;
    private StatusItem status;
    private LocalDate garantiaFim;
    private Map<String, String> especificacoes;
    private UUID catalogoModeloId;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
}
