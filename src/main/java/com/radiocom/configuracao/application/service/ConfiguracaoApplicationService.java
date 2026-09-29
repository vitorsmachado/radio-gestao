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
        Configuracao dados = Configuracao.builder()
                .valorMaoDeObraPadrao(dto.getValorMaoDeObraPadrao())
                .prazoGarantiaPecaDias(dto.getPrazoGarantiaPecaDias())
                .prazoGarantiaEquipamentoDias(dto.getPrazoGarantiaEquipamentoDias())
                .prazoGarantiaAcessorioDias(dto.getPrazoGarantiaAcessorioDias())
                .nomeEmpresa(dto.getNomeEmpresa())
                .razaoSocialEmpresa(dto.getRazaoSocialEmpresa())
                .documentoEmpresa(dto.getDocumentoEmpresa())
                .inscricaoEstadualEmpresa(dto.getInscricaoEstadualEmpresa())
                .enderecoEmpresa(dto.getEnderecoEmpresa())
                .bairroEmpresa(dto.getBairroEmpresa())
                .cidadeEmpresa(dto.getCidadeEmpresa())
                .telefoneEmpresa(dto.getTelefoneEmpresa())
                .emailEmpresa(dto.getEmailEmpresa())
                .build();
        return toDTO(domainService.atualizar(dados));
    }

    private ConfiguracaoDTO toDTO(Configuracao c) {
        return ConfiguracaoDTO.builder()
                .valorMaoDeObraPadrao(c.getValorMaoDeObraPadrao())
                .prazoGarantiaPecaDias(c.getPrazoGarantiaPecaDias())
                .prazoGarantiaEquipamentoDias(c.getPrazoGarantiaEquipamentoDias())
                .prazoGarantiaAcessorioDias(c.getPrazoGarantiaAcessorioDias())
                .nomeEmpresa(c.getNomeEmpresa())
                .razaoSocialEmpresa(c.getRazaoSocialEmpresa())
                .documentoEmpresa(c.getDocumentoEmpresa())
                .inscricaoEstadualEmpresa(c.getInscricaoEstadualEmpresa())
                .enderecoEmpresa(c.getEnderecoEmpresa())
                .bairroEmpresa(c.getBairroEmpresa())
                .cidadeEmpresa(c.getCidadeEmpresa())
                .telefoneEmpresa(c.getTelefoneEmpresa())
                .emailEmpresa(c.getEmailEmpresa())
                .build();
    }
}
