package com.radiocom.ordemservico.integration.db;

import com.radiocom.cliente.domain.model.Cliente;
import com.radiocom.cliente.domain.model.enums.TipoPessoa;
import com.radiocom.cliente.domain.repository.ClienteRepository;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.ordemservico.application.dto.OrdemServicoResumoDTO;
import com.radiocom.ordemservico.application.service.OrdemServicoApplicationService;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.OrdemServico;
import com.radiocom.ordemservico.domain.service.ItemEntradaDomainService;
import com.radiocom.ordemservico.domain.service.OrdemServicoDomainService;
import com.radiocom.shared.integration.PostgresIntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A listagem geral de OS combina um filtro opcional "(:param IS NULL OR ...)"
 * com um IN sobre uma lista de clienteIds resolvida em outro módulo, e um
 * EXISTS correlacionado em ItemEntrada — exatamente o tipo de query que já
 * quebrou em produção contra Postgres real (poderia gerar
 * "could not determine data type of parameter"). Este teste roda contra
 * Postgres de verdade pra garantir que continua funcionando.
 */
@DisplayName("Listagem geral de OS - Teste de Integração (Postgres real)")
class OrdemServicoListagemIT extends PostgresIntegrationTestBase {

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private OrdemServicoDomainService osDomainService;
    @Autowired private ItemEntradaDomainService itemEntradaDomainService;
    @Autowired private OrdemServicoApplicationService osService;

    @Test
    @DisplayName("listar sem nenhum filtro deve executar sem erro de tipo de parâmetro")
    void listar_semFiltros_naoDeveLancarErroDeTipo() {
        Cliente cliente = clienteRepository.save(Cliente.builder()
                .tipo(TipoPessoa.PESSOA_JURIDICA)
                .documento("11222333000181")
                .nomeRazaoSocial("Cliente Listagem IT")
                .build());
        osDomainService.criar(cliente.getId(), null, null, "Solicitante IT");

        var pagina = osService.listar(null, null, null, PageRequest.of(0, 20));

        assertThat(pagina.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("listar por numero da OS deve encontrar a OS correspondente")
    void listar_porNumero_deveEncontrar() {
        Cliente cliente = clienteRepository.save(Cliente.builder()
                .tipo(TipoPessoa.PESSOA_JURIDICA)
                .documento("22333444000155")
                .nomeRazaoSocial("Cliente Busca Numero")
                .build());
        OrdemServico os = osDomainService.criar(cliente.getId(), null, null, "Solicitante");

        var pagina = osService.listar(os.getNumero(), null, null, PageRequest.of(0, 20));

        assertThat(pagina.getContent()).extracting(OrdemServicoResumoDTO::getId).containsExactly(os.getId());
    }

    @Test
    @DisplayName("listar por NS do item deve encontrar a OS correspondente")
    void listar_porNumeroSerieDoItem_deveEncontrar() {
        Cliente cliente = clienteRepository.save(Cliente.builder()
                .tipo(TipoPessoa.PESSOA_JURIDICA)
                .documento("33444555000122")
                .nomeRazaoSocial("Cliente Busca NS")
                .build());
        OrdemServico os = osDomainService.criar(cliente.getId(), null, null, "Solicitante");
        String ns = "NS-" + UUID.randomUUID();
        itemEntradaDomainService.criar(ItemEntrada.builder()
                .osId(os.getId()).tipoItem(TipoItem.EQUIPAMENTO)
                .descricao("Rádio").numeroSerie(ns).build());

        var pagina = osService.listar(ns, null, null, PageRequest.of(0, 20));

        assertThat(pagina.getContent()).extracting(OrdemServicoResumoDTO::getId).containsExactly(os.getId());
    }

    @Test
    @DisplayName("listar por codigo do cliente do item deve encontrar a OS correspondente")
    void listar_porCodigoClienteDoItem_deveEncontrar() {
        Cliente cliente = clienteRepository.save(Cliente.builder()
                .tipo(TipoPessoa.PESSOA_JURIDICA)
                .documento("44555666000199")
                .nomeRazaoSocial("Cliente Busca Codigo")
                .build());
        OrdemServico os = osDomainService.criar(cliente.getId(), null, null, "Solicitante");
        String codigoCliente = "TAG-" + UUID.randomUUID();
        itemEntradaDomainService.criar(ItemEntrada.builder()
                .osId(os.getId()).tipoItem(TipoItem.EQUIPAMENTO)
                .descricao("Rádio").codigoCliente(codigoCliente).build());

        var pagina = osService.listar(codigoCliente, null, null, PageRequest.of(0, 20));

        assertThat(pagina.getContent()).extracting(OrdemServicoResumoDTO::getId).containsExactly(os.getId());
    }

    @Test
    @DisplayName("listar por nome do cliente deve encontrar e preencher clienteNome/clienteDocumento")
    void listar_porNomeDoCliente_deveEncontrarEPreencherDadosDoCliente() {
        String nomeUnico = "Cliente Unico " + UUID.randomUUID();
        Cliente cliente = clienteRepository.save(Cliente.builder()
                .tipo(TipoPessoa.PESSOA_JURIDICA)
                .documento("55666777000133")
                .nomeRazaoSocial(nomeUnico)
                .build());
        OrdemServico os = osDomainService.criar(cliente.getId(), null, null, "Solicitante");

        var pagina = osService.listar(nomeUnico, null, null, PageRequest.of(0, 20));

        assertThat(pagina.getContent()).hasSize(1);
        assertThat(pagina.getContent().get(0).getId()).isEqualTo(os.getId());
        assertThat(pagina.getContent().get(0).getClienteNome()).isEqualTo(nomeUnico);
        assertThat(pagina.getContent().get(0).getClienteDocumento()).isEqualTo("55666777000133");
    }

    @Test
    @DisplayName("listar com busca sem nenhuma OS correspondente deve retornar vazio sem erro")
    void listar_semCorrespondencia_deveRetornarVazio() {
        var pagina = osService.listar("busca-sem-nenhuma-correspondencia-" + UUID.randomUUID(), null, null, PageRequest.of(0, 20));

        assertThat(pagina.getContent()).isEmpty();
    }

    @Test
    @DisplayName("listar com periodo deve filtrar pela data de abertura")
    void listar_comPeriodo_deveFiltrarPelaDataDeAbertura() {
        Cliente cliente = clienteRepository.save(Cliente.builder()
                .tipo(TipoPessoa.PESSOA_JURIDICA)
                .documento("66777888000144")
                .nomeRazaoSocial("Cliente Periodo")
                .build());
        OrdemServico os = osDomainService.criar(cliente.getId(), null, null, "Solicitante");

        var dentroDoPeriodo = osService.listar(os.getNumero(), LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), PageRequest.of(0, 20));
        var foraDoPeriodo = osService.listar(os.getNumero(), LocalDate.now().plusDays(5), LocalDate.now().plusDays(10), PageRequest.of(0, 20));

        assertThat(dentroDoPeriodo.getContent()).extracting(OrdemServicoResumoDTO::getId).containsExactly(os.getId());
        assertThat(foraDoPeriodo.getContent()).isEmpty();
    }
}
