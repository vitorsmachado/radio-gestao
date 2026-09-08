package com.radiocom.orcamento.application.mapper;

import com.radiocom.orcamento.application.dto.OrcamentoDTO;
import com.radiocom.orcamento.domain.model.Orcamento;
import org.springframework.stereotype.Component;

@Component
public class OrcamentoMapper {

    public OrcamentoDTO toDTO(Orcamento orcamento) {
        if (orcamento == null) return null;
        return OrcamentoDTO.builder()
                .id(orcamento.getId())
                .numero(orcamento.getNumero())
                .osId(orcamento.getOsId())
                .clienteId(orcamento.getClienteId())
                .validade(orcamento.getValidade())
                .condicoesPagamento(orcamento.getCondicoesPagamento())
                .desconto(orcamento.getDesconto())
                .status(orcamento.getStatus())
                .dataEmissao(orcamento.getDataEmissao())
                .observacoes(orcamento.getObservacoes())
                .expirado(orcamento.isExpirado())
                .build();
    }
}
