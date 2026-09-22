package com.radiocom.ordemservico.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class OrdemServicoCreateDTO {

    @NotNull
    private UUID clienteId;

    private UUID postoId;

    private UUID tecnicoId;

    @Size(max = 100)
    private String solicitante;

    /** Opcional — se ausente, mantém o momento da criação (now()). */
    private LocalDateTime dataAbertura;

    @Size(max = 2000)
    private String observacoes;
}
