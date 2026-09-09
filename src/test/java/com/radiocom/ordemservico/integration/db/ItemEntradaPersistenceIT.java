package com.radiocom.ordemservico.integration.db;

import com.radiocom.cliente.domain.model.Cliente;
import com.radiocom.cliente.domain.model.enums.TipoPessoa;
import com.radiocom.cliente.domain.repository.ClienteRepository;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.ordemservico.domain.model.ItemConserto;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.OrdemServico;
import com.radiocom.ordemservico.domain.model.enums.TipoItemConserto;
import com.radiocom.ordemservico.domain.repository.ItemEntradaRepository;
import com.radiocom.ordemservico.domain.service.ItemEntradaDomainService;
import com.radiocom.ordemservico.domain.service.OrdemServicoDomainService;
import com.radiocom.shared.integration.PostgresIntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ItemConserto} pendurava num @OneToMany unidirecional com @JoinColumn
 * sobre uma coluna NOT NULL — o Hibernate insere a linha filha sem a FK
 * (pra fazer um UPDATE depois) e a constraint barra o INSERT. Isso nunca foi
 * pego porque todo teste anterior usava repositório mockado; aqui persiste
 * de verdade contra Postgres.
 */
@DisplayName("ItemEntrada / ItemConserto - Persistência real (Postgres)")
class ItemEntradaPersistenceIT extends PostgresIntegrationTestBase {

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private OrdemServicoDomainService osDomainService;
    @Autowired private ItemEntradaDomainService itemEntradaDomainService;
    @Autowired private ItemEntradaRepository itemEntradaRepository;

    @Test
    @Transactional
    @DisplayName("adicionarItemConserto deve persistir com item_entrada_id preenchido e sobreviver a um reload")
    void adicionarItemConserto_devePersistirComFkPreenchida() {
        Cliente cliente = Cliente.builder()
                .tipo(TipoPessoa.PESSOA_JURIDICA)
                .documento("11222333000181")
                .nomeRazaoSocial("Cliente IT Persistência")
                .build();
        clienteRepository.save(cliente);

        OrdemServico os = osDomainService.criar(cliente.getId(), null, null, "Solicitante IT");

        ItemEntrada item = ItemEntrada.builder()
                .osId(os.getId())
                .tipoItem(TipoItem.EQUIPAMENTO)
                .descricao("Rádio IT")
                .build();
        ItemEntrada salvo = itemEntradaDomainService.criar(item);

        ItemConserto conserto = ItemConserto.builder()
                .tipo(TipoItemConserto.PECA)
                .descricao("Peça IT")
                .quantidade(1)
                .valorUnitario(new BigDecimal("10.50"))
                .build();

        ItemEntrada atualizado = itemEntradaDomainService.adicionarItemConserto(salvo.getId(), conserto);

        assertThat(atualizado.getItensConserto()).hasSize(1);
        assertThat(atualizado.getItensConserto().get(0).getValorTotal()).isEqualByComparingTo("10.50");

        ItemEntrada recarregado = itemEntradaRepository.findById(salvo.getId()).orElseThrow();
        assertThat(recarregado.getItensConserto()).hasSize(1);
        assertThat(recarregado.getItensConserto().get(0).getDescricao()).isEqualTo("Peça IT");
    }
}
