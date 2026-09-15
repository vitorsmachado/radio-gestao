package com.radiocom.ordemservico.application.dto;

import com.radiocom.ordemservico.domain.model.enums.StatusOS;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Linha da listagem geral de OS — inclui nome/documento do cliente (resolvidos
 * em lote a partir do módulo Cliente) pra evitar que o front precise buscar
 * cada cliente individualmente pra montar a lista.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServicoResumoDTO {

    private UUID id;
    private String numero;
    private UUID clienteId;
    private String clienteNome;
    private String clienteDocumento;
    private String solicitante;
    private StatusOS status;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataAtualizacao;
}
