package com.radiocom.ordemservico.application.dto;

import com.radiocom.ordemservico.domain.model.enums.StatusOS;
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
public class OrdemServicoDTO {

    private UUID id;
    private String numero;
    private UUID clienteId;
    private UUID postoId;
    private UUID tecnicoId;
    private String solicitante;
    private String recebedorNome;
    private StatusOS status;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataConclusao;
    private String observacoes;
}
