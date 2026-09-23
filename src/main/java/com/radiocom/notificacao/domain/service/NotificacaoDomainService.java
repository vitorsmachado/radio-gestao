package com.radiocom.notificacao.domain.service;

import com.radiocom.notificacao.domain.model.Notificacao;
import com.radiocom.notificacao.domain.model.enums.TipoNotificacao;
import com.radiocom.notificacao.domain.repository.NotificacaoRepository;
import com.radiocom.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificacaoDomainService {

    private final NotificacaoRepository repository;

    @Transactional
    public Notificacao criar(TipoNotificacao tipo, String titulo, String mensagem, String link) {
        return repository.save(Notificacao.builder()
                .tipo(tipo)
                .titulo(titulo)
                .mensagem(mensagem)
                .link(link)
                .build());
    }

    @Transactional(readOnly = true)
    public Page<Notificacao> listar(Boolean lida, Pageable pageable) {
        return lida != null
                ? repository.findByLidaOrderByDataCriacaoDesc(lida, pageable)
                : repository.findAllByOrderByDataCriacaoDesc(pageable);
    }

    @Transactional
    public Notificacao marcarComoLida(UUID id) {
        Notificacao notificacao = repository.findById(id)
                .orElseThrow(() -> new DomainException("Notificação não encontrada: " + id));
        notificacao.marcarComoLida();
        return repository.save(notificacao);
    }

    @Transactional(readOnly = true)
    public long contarNaoLidas() {
        return repository.countByLidaFalse();
    }
}
