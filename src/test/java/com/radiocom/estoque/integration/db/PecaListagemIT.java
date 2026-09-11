package com.radiocom.estoque.integration.db;

import com.radiocom.estoque.application.dto.CatalogoModeloCreateDTO;
import com.radiocom.estoque.application.dto.CatalogoModeloDTO;
import com.radiocom.estoque.application.dto.PecaCreateDTO;
import com.radiocom.estoque.application.dto.PecaDTO;
import com.radiocom.estoque.application.service.CatalogoModeloService;
import com.radiocom.estoque.application.service.EstoqueApplicationService;
import com.radiocom.estoque.domain.model.enums.CriticidadeEstoque;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.shared.integration.PostgresIntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A query combinada de listarPecas (LEFT JOIN com modelosCompativeis + filtro
 * opcional "(:param IS NULL OR ...)") já quebrou em produção contra Postgres
 * real com "could not determine data type of parameter $1" — um erro que os
 * testes com repositório mockado nunca poderiam pegar. Este teste roda a
 * query de verdade pra garantir que continua funcionando.
 */
@DisplayName("Listagem de Peças - Teste de Integração (Postgres real)")
class PecaListagemIT extends PostgresIntegrationTestBase {

    @Autowired
    private EstoqueApplicationService estoqueService;

    @Autowired
    private CatalogoModeloService catalogoModeloService;

    @Test
    @DisplayName("listarPecas sem nenhum filtro deve executar sem erro de tipo de parâmetro")
    void listarPecas_semFiltros_naoDeveLancarErroDeTipo() {
        estoqueService.criarPeca(PecaCreateDTO.builder()
                .descricao("Antena UHF " + UUID.randomUUID())
                .quantidadeDisponivel(5)
                .build());

        var pagina = estoqueService.listarPecas(PageRequest.of(0, 20), null, null);

        assertThat(pagina.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("listarPecas com criticidade EM_FALTA deve executar sem erro de tipo de parâmetro")
    void listarPecas_comCriticidade_naoDeveLancarErroDeTipo() {
        estoqueService.criarPeca(PecaCreateDTO.builder()
                .descricao("Capacitor " + UUID.randomUUID())
                .quantidadeDisponivel(0)
                .build());

        var pagina = estoqueService.listarPecas(PageRequest.of(0, 20), CriticidadeEstoque.EM_FALTA, null);

        assertThat(pagina.getContent()).allMatch(PecaDTO::isEmFalta);
    }

    @Test
    @DisplayName("listarPecas com modeloCompativelId deve executar sem erro de tipo de parâmetro e filtrar corretamente")
    void listarPecas_comModeloCompativel_naoDeveLancarErroEDeveFiltrar() {
        CatalogoModeloDTO modelo = catalogoModeloService.criar(CatalogoModeloCreateDTO.builder()
                .tipoItem(TipoItem.EQUIPAMENTO)
                .marca("Motorola-" + UUID.randomUUID())
                .modelo("EP450")
                .build());

        PecaDTO pecaCompativel = estoqueService.criarPeca(PecaCreateDTO.builder()
                .descricao("Bateria BP-227 " + UUID.randomUUID())
                .modelosCompativeisIds(List.of(modelo.getId()))
                .build());

        estoqueService.criarPeca(PecaCreateDTO.builder()
                .descricao("Peça sem compatibilidade " + UUID.randomUUID())
                .build());

        var pagina = estoqueService.listarPecas(PageRequest.of(0, 20), null, modelo.getId());

        assertThat(pagina.getContent()).extracting(PecaDTO::getId).containsExactly(pecaCompativel.getId());
    }
}
