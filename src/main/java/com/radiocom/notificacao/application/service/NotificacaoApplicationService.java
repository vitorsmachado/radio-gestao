package com.radiocom.notificacao.application.service;

import com.radiocom.notificacao.application.dto.NotificacaoDTO;
import com.radiocom.notificacao.domain.model.Notificacao;
import com.radiocom.notificacao.domain.service.NotificacaoDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificacaoApplicationService {

    private final NotificacaoDomainService domainService;

    @Transactional(readOnly = true)
    public Page<NotificacaoDTO> listar(Boolean lida, Pageable pageable) {
        return domainService.listar(lida, pageable).map(this::toDTO);
    }

    @Transactional
    public NotificacaoDTO marcarComoLida(UUID id) {
        return toDTO(domainService.marcarComoLida(id));
    }

    @Transactional(readOnly = true)
    public long contarNaoLidas() {
        return domainService.contarNaoLidas();
    }

    private NotificacaoDTO toDTO(Notificacao n) {
        return NotificacaoDTO.builder()
                .id(n.getId())
                .tipo(n.getTipo())
                .titulo(n.getTitulo())
                .mensagem(n.getMensagem())
                .link(n.getLink())
                .lida(n.isLida())
                .dataCriacao(n.getDataCriacao())
                .build();
    }
}
