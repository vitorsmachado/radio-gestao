package com.radiocom.ordemservico.garantia.domain.service;

import com.radiocom.configuracao.domain.service.ConfiguracaoDomainService;
import com.radiocom.ordemservico.domain.model.ItemConserto;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.enums.TipoItemConserto;
import com.radiocom.ordemservico.garantia.domain.model.GarantiaPeca;
import com.radiocom.ordemservico.garantia.domain.repository.GarantiaPecaRepository;
import com.radiocom.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GarantiaPecaDomainService {

    private final GarantiaPecaRepository repository;
    private final ConfiguracaoDomainService configuracaoDomainService;

    /**
     * Chamado ao concluir uma manutenção — registra cobertura para cada peça
     * efetivamente trocada no reparo (ignora itens sem equipamento rastreado
     * e itens de conserto que não sejam peça de estoque).
     */
    @Transactional
    public void registrarCobertura(ItemEntrada item) {
        if (item.getItemEstoqueId() == null) return;
        LocalDate hoje = LocalDate.now();
        LocalDate fim = hoje.plusDays(configuracaoDomainService.buscar().getPrazoGarantiaPecaDias());
        for (ItemConserto conserto : item.getItensConserto()) {
            if (conserto.getTipo() != TipoItemConserto.PECA || conserto.getItemEstoqueId() == null) continue;
            repository.save(GarantiaPeca.builder()
                    .itemEstoqueId(item.getItemEstoqueId())
                    .pecaEstoqueId(conserto.getItemEstoqueId())
                    .descricaoPeca(conserto.getDescricao())
                    .dataInicio(hoje)
                    .dataFim(fim)
                    .itemEntradaOrigemId(item.getId())
                    .build());
        }
    }

    @Transactional(readOnly = true)
    public List<GarantiaPeca> listarCoberturaAtiva(UUID itemEstoqueId) {
        return repository.findByItemEstoqueIdAndDataFimGreaterThanEqual(itemEstoqueId, LocalDate.now());
    }

    /** Valida que a cobertura existe, pertence a esse equipamento e ainda está no prazo. */
    @Transactional(readOnly = true)
    public GarantiaPeca validarCoberturaAtiva(UUID itemEstoqueId, UUID garantiaPecaId) {
        return listarCoberturaAtiva(itemEstoqueId).stream()
                .filter(g -> g.getId().equals(garantiaPecaId))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "Cobertura de garantia não encontrada ou expirada: " + garantiaPecaId));
    }
}
