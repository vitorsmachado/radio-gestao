package com.radiocom.cliente.integration.db;

import com.radiocom.cliente.application.service.ClienteApplicationService;
import com.radiocom.cliente.domain.model.Cliente;
import com.radiocom.cliente.domain.model.Contato;
import com.radiocom.cliente.domain.model.Posto;
import com.radiocom.cliente.domain.model.enums.StatusCliente;
import com.radiocom.cliente.domain.model.enums.TipoContato;
import com.radiocom.cliente.domain.model.enums.TipoPessoa;
import com.radiocom.cliente.domain.repository.ClienteRepository;
import com.radiocom.shared.integration.PostgresIntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A busca geral de clientes combina "(:param IS NULL OR ...)" com CAST de
 * numeroIdentificacao pra string e EXISTS correlacionados em Posto/Contato —
 * exatamente o tipo de query que já quebrou em produção contra Postgres real
 * ("could not determine data type of parameter"). Este teste roda contra
 * Postgres de verdade pra garantir que continua funcionando.
 */
@DisplayName("Listagem geral de Clientes - Teste de Integração (Postgres real)")
class ClienteListagemIT extends PostgresIntegrationTestBase {

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private ClienteApplicationService clienteService;

    @Test
    @DisplayName("listar sem nenhum filtro deve executar sem erro de tipo de parâmetro")
    void listar_semFiltros_naoDeveLancarErroDeTipo() {
        var pagina = clienteService.listar(null, null, PageRequest.of(0, 20));

        assertThat(pagina).isNotNull();
    }

    @Test
    @DisplayName("listar por nome fantasia deve encontrar o cliente")
    void listar_porNomeFantasia_deveEncontrar() {
        String fantasia = "Fantasia Unica " + UUID.randomUUID();
        Cliente cliente = clienteRepository.save(Cliente.builder()
                .numeroIdentificacao(Math.abs(UUID.randomUUID().hashCode()))
                .tipo(TipoPessoa.PESSOA_JURIDICA)
                .documento(documentoUnico())
                .nomeRazaoSocial("Razao Social Teste")
                .nomeFantasia(fantasia)
                .build());

        var pagina = clienteService.listar(fantasia, null, PageRequest.of(0, 20));

        assertThat(pagina.getContent()).extracting("id").containsExactly(cliente.getId());
    }

    @Test
    @DisplayName("listar por numero de identificacao deve encontrar o cliente")
    void listar_porNumeroIdentificacao_deveEncontrar() {
        int numero = Math.abs(UUID.randomUUID().hashCode());
        Cliente cliente = clienteRepository.save(Cliente.builder()
                .numeroIdentificacao(numero)
                .tipo(TipoPessoa.PESSOA_JURIDICA)
                .documento(documentoUnico())
                .nomeRazaoSocial("Cliente Numero Identificacao")
                .build());

        var pagina = clienteService.listar(String.valueOf(numero), null, PageRequest.of(0, 20));

        assertThat(pagina.getContent()).extracting("id").containsExactly(cliente.getId());
    }

    @Test
    @DisplayName("listar por nome de posto deve encontrar o cliente")
    void listar_porNomePosto_deveEncontrar() {
        String nomePosto = "Posto Unico " + UUID.randomUUID();
        Cliente cliente = Cliente.builder()
                .numeroIdentificacao(Math.abs(UUID.randomUUID().hashCode()))
                .tipo(TipoPessoa.PESSOA_JURIDICA)
                .documento(documentoUnico())
                .nomeRazaoSocial("Cliente Com Posto")
                .build();
        cliente.adicionarPosto(Posto.builder().nome(nomePosto).build());
        cliente = clienteRepository.save(cliente);

        var pagina = clienteService.listar(nomePosto, null, PageRequest.of(0, 20));

        assertThat(pagina.getContent()).extracting("id").containsExactly(cliente.getId());
    }

    @Test
    @DisplayName("listar por nome de contato deve encontrar o cliente")
    void listar_porNomeContato_deveEncontrar() {
        String nomeContato = "Contato Unico " + UUID.randomUUID();
        Cliente cliente = Cliente.builder()
                .numeroIdentificacao(Math.abs(UUID.randomUUID().hashCode()))
                .tipo(TipoPessoa.PESSOA_JURIDICA)
                .documento(documentoUnico())
                .nomeRazaoSocial("Cliente Com Contato")
                .build();
        cliente.adicionarContato(Contato.builder().nome(nomeContato).tipo(TipoContato.COMERCIAL).build());
        cliente = clienteRepository.save(cliente);

        var pagina = clienteService.listar(nomeContato, null, PageRequest.of(0, 20));

        assertThat(pagina.getContent()).extracting("id").containsExactly(cliente.getId());
    }

    @Test
    @DisplayName("listar com filtro de status deve retornar apenas clientes com aquele status")
    void listar_comFiltroDeStatus_deveFiltrar() {
        String nomeUnico = "Cliente Bloqueado " + UUID.randomUUID();
        Cliente cliente = Cliente.builder()
                .numeroIdentificacao(Math.abs(UUID.randomUUID().hashCode()))
                .tipo(TipoPessoa.PESSOA_JURIDICA)
                .documento(documentoUnico())
                .nomeRazaoSocial(nomeUnico)
                .status(StatusCliente.BLOQUEADO)
                .build();
        clienteRepository.save(cliente);

        var pagina = clienteService.listar(nomeUnico, StatusCliente.BLOQUEADO, PageRequest.of(0, 20));
        var paginaVazia = clienteService.listar(nomeUnico, StatusCliente.ATIVO, PageRequest.of(0, 20));

        assertThat(pagina.getContent()).hasSize(1);
        assertThat(paginaVazia.getContent()).isEmpty();
    }

    private String documentoUnico() {
        return String.valueOf(10_000_000_000_000L + Math.abs(UUID.randomUUID().hashCode()) % 89_999_999_999L);
    }
}
