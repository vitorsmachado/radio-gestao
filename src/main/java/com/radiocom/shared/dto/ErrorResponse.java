package com.radiocom.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "Resposta padronizada de erro")
public class ErrorResponse {

    @Schema(description = "Timestamp do erro", example = "2026-01-15T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Código HTTP", example = "400")
    private int status;

    @Schema(description = "Tipo do erro", example = "Bad Request")
    private String error;

    @Schema(description = "Mensagem detalhada", example = "Cliente não encontrado")
    private String message;

    @Schema(description = "Caminho da requisição", example = "/api/v1/clientes")
    private String path;

    @Schema(description = "Lista de erros de validação")
    private List<ValidationError> errors;

    @Data
    @Builder
    @Schema(description = "Erro de campo específico")
    public static class ValidationError {
        @Schema(description = "Nome do campo", example = "documento")
        private String field;

        @Schema(description = "Mensagem de erro", example = "Documento inválido")
        private String message;
    }
}
