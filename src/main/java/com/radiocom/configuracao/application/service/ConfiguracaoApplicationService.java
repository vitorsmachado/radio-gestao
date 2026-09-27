package com.radiocom.configuracao.application.service;

import com.radiocom.configuracao.application.dto.AtualizarConfiguracaoDTO;
import com.radiocom.configuracao.application.dto.ConfiguracaoDTO;
import com.radiocom.configuracao.domain.model.Configuracao;
import com.radiocom.configuracao.domain.service.ConfiguracaoDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfiguracaoApplicationService {

    private final ConfiguracaoDomainService domainService;

    @Transactional(readOnly = true)
    public ConfiguracaoDTO buscar() {
        return toDTO(domainService.buscar());
    }

    @Transactional
    public ConfiguracaoDTO atualizar(AtualizarConfiguracaoDTO dto) {
        return toDTO(domainService.atualizar(dto.getValorMaoDeObraPadrao(), dto.getPrazoGarantiaPecaDias(),
                dto.getPrazoGarantiaEquipamentoDias(), dto.getPrazoGarantiaAcessorioDias()));
    }

    private ConfiguracaoDTO toDTO(Configuracao c) {
        return ConfiguracaoDTO.builder()
                .valorMaoDeObraPadrao(c.getValorMaoDeObraPadrao())
                .prazoGarantiaPecaDias(c.getPrazoGarantiaPecaDias())
                .prazoGarantiaEquipamentoDias(c.getPrazoGarantiaEquipamentoDias())
                .prazoGarantiaAcessorioDias(c.getPrazoGarantiaAcessorioDias())
                .build();
    }
}
