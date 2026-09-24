package com.radiocom.configuracao.domain.service;

import com.radiocom.configuracao.domain.model.Configuracao;
import com.radiocom.configuracao.domain.repository.ConfiguracaoRepository;
import com.radiocom.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ConfiguracaoDomainService {

    private final ConfiguracaoRepository repository;

    @Transactional(readOnly = true)
    public Configuracao buscar() {
        return repository.findFirstByOrderByDataCriacaoAsc()
                .orElseThrow(() -> new DomainException("Configurações do sistema não encontradas"));
    }

    @Transactional
    public Configuracao atualizarValorMaoDeObraPadrao(BigDecimal valor) {
        Configuracao configuracao = buscar();
        configuracao.setValorMaoDeObraPadrao(valor);
        return repository.save(configuracao);
    }
}
