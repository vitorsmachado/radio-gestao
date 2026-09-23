package com.radiocom.notificacao.application.dto;

import com.radiocom.notificacao.domain.model.enums.TipoNotificacao;
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
public class NotificacaoDTO {

    private UUID id;
    private TipoNotificacao tipo;
    private String titulo;
    private String mensagem;
    private String link;
    private boolean lida;
    private LocalDateTime dataCriacao;
}
