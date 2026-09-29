package com.radiocom.configuracao.domain.service;

import com.radiocom.configuracao.domain.model.Configuracao;
import com.radiocom.configuracao.domain.repository.ConfiguracaoRepository;
import com.radiocom.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfiguracaoDomainService {

    private final ConfiguracaoRepository repository;

    @Transactional(readOnly = true)
    public Configuracao buscar() {
        return repository.findFirstByOrderByDataCriacaoAsc()
                .orElseThrow(() -> new DomainException("Configurações do sistema não encontradas"));
    }

    /** @param dados valores novos (transiente, só carrega os campos editáveis) — sobrescreve os da configuração salva. */
    @Transactional
    public Configuracao atualizar(Configuracao dados) {
        Configuracao configuracao = buscar();
        configuracao.setValorMaoDeObraPadrao(dados.getValorMaoDeObraPadrao());
        configuracao.setPrazoGarantiaPecaDias(dados.getPrazoGarantiaPecaDias());
        configuracao.setPrazoGarantiaEquipamentoDias(dados.getPrazoGarantiaEquipamentoDias());
        configuracao.setPrazoGarantiaAcessorioDias(dados.getPrazoGarantiaAcessorioDias());
        configuracao.setNomeEmpresa(dados.getNomeEmpresa());
        configuracao.setRazaoSocialEmpresa(dados.getRazaoSocialEmpresa());
        configuracao.setDocumentoEmpresa(dados.getDocumentoEmpresa());
        configuracao.setInscricaoEstadualEmpresa(dados.getInscricaoEstadualEmpresa());
        configuracao.setEnderecoEmpresa(dados.getEnderecoEmpresa());
        configuracao.setBairroEmpresa(dados.getBairroEmpresa());
        configuracao.setCidadeEmpresa(dados.getCidadeEmpresa());
        configuracao.setTelefoneEmpresa(dados.getTelefoneEmpresa());
        configuracao.setEmailEmpresa(dados.getEmailEmpresa());
        return repository.save(configuracao);
    }
}
