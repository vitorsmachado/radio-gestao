package com.radiocom.estoque.application.dto;

import com.radiocom.estoque.domain.model.enums.EstadoEquipamento;
import com.radiocom.estoque.domain.model.enums.ProprietarioEquipamento;
import com.radiocom.estoque.domain.model.enums.StatusItem;
import com.radiocom.estoque.domain.model.enums.TipoAcessorio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcessorioDTO {

    private UUID id;
    private String codigo;
    private String descricao;
    private TipoAcessorio tipoAcessorio;
    private ProprietarioEquipamento proprietario;
    private UUID clienteId;
    private String codigoCliente;
    private String numeroSerie;
    private String patrimonio;
    private Integer quantidadeDisponivel;
    private Integer quantidadeMinima;
    private EstadoEquipamento estado;
    private StatusItem status;
    private LocalDate garantiaFim;
    private UUID catalogoModeloId;
    private boolean possuiNumeroSerie;
    private boolean possuiPatrimonio;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
}
