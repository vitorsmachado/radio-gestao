package com.radiocom.cliente.unit.domain.model;

import com.radiocom.cliente.domain.model.Cliente;
import com.radiocom.cliente.domain.model.Contato;
import com.radiocom.cliente.domain.model.Posto;
import com.radiocom.cliente.domain.model.enums.StatusCliente;
import com.radiocom.cliente.domain.model.enums.TipoContato;
import com.radiocom.cliente.domain.model.enums.TipoPessoa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cliente - Testes de Domínio")
class ClienteTest {

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder()
                .tipo(TipoPessoa.PESSOA_JURIDICA)
                .documento("12345678000199")
                .nomeRazaoSocial("Radio Comunicacao LTDA")
                .build();
    }

    private Posto criarPosto(String nome) {
        return Posto.builder().nome(nome).build();
    }

    private Contato criarContato(String nome, boolean principal) {
        return Contato.builder().nome(nome).tipo(TipoContato.COMERCIAL).principal(principal).build();
    }

    // ===== postos =====

    @Test
    @DisplayName("adicionarPosto deve associar o posto ao cliente")
    void adicionarPosto_deveAssociarPostoAoCliente() {
        Posto posto = criarPosto("Matriz");

        cliente.adicionarPosto(posto);

        assertThat(cliente.getPostos()).containsExactly(posto);
        assertThat(posto.getCliente()).isEqualTo(cliente);
    }

    @Test
    @DisplayName("removerPostoPorId deve lançar exceção quando posto não existe")
    void removerPostoPorId_deveLancarExcecaoQuandoNaoExiste() {
        assertThatThrownBy(() -> cliente.removerPostoPorId(UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Posto não encontrado");
    }

    // ===== contatos =====

    @Test
    @DisplayName("adicionarContato deve associar o contato ao cliente")
    void adicionarContato_deveAssociarContatoAoCliente() {
        Contato contato = criarContato("João", false);

        cliente.adicionarContato(contato);

        assertThat(cliente.getContatos()).containsExactly(contato);
        assertThat(contato.getCliente()).isEqualTo(cliente);
    }

    @Test
    @DisplayName("adicionarContato deve lançar exceção ao tentar adicionar dois contatos principais")
    void adicionarContato_deveLancarExcecaoComDoisPrincipais() {
        cliente.adicionarContato(criarContato("João", true));

        assertThatThrownBy(() -> cliente.adicionarContato(criarContato("Maria", true)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Já existe um contato principal");
    }

    @Test
    @DisplayName("atualizarContatoPrincipal deve trocar o contato principal")
    void atualizarContatoPrincipal_deveTrocarPrincipal() {
        Contato joao = criarContato("João", true);
        Contato maria = criarContato("Maria", false);
        ReflectionTestUtils.setField(joao, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(maria, "id", UUID.randomUUID());
        cliente.adicionarContato(joao);
        cliente.adicionarContato(maria);

        cliente.atualizarContatoPrincipal(maria.getId());

        assertThat(joao.isPrincipal()).isFalse();
        assertThat(maria.isPrincipal()).isTrue();
    }

    @Test
    @DisplayName("removerContatoPorId deve lançar exceção quando contato não existe")
    void removerContatoPorId_deveLancarExcecaoQuandoNaoExiste() {
        assertThatThrownBy(() -> cliente.removerContatoPorId(UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Contato não encontrado");
    }

    // ===== status =====

    @Test
    @DisplayName("novo cliente deve nascer ATIVO por padrão")
    void novoCliente_deveNascerAtivo() {
        assertThat(cliente.isAtivo()).isTrue();
        assertThat(cliente.getStatus()).isEqualTo(StatusCliente.ATIVO);
    }

    @Test
    @DisplayName("inativar deve mudar status para INATIVO")
    void inativar_deveMudarStatus() {
        cliente.inativar();

        assertThat(cliente.getStatus()).isEqualTo(StatusCliente.INATIVO);
        assertThat(cliente.isAtivo()).isFalse();
    }

    @Test
    @DisplayName("bloquear deve mudar status para BLOQUEADO")
    void bloquear_deveMudarStatus() {
        cliente.bloquear();

        assertThat(cliente.getStatus()).isEqualTo(StatusCliente.BLOQUEADO);
    }

    @Test
    @DisplayName("ativar deve mudar status para ATIVO")
    void ativar_deveMudarStatus() {
        cliente.bloquear();

        cliente.ativar();

        assertThat(cliente.isAtivo()).isTrue();
    }

    // ===== tipo =====

    @Test
    @DisplayName("isPessoaJuridica deve retornar true para PESSOA_JURIDICA")
    void isPessoaJuridica_deveRetornarTrue() {
        assertThat(cliente.isPessoaJuridica()).isTrue();
        assertThat(cliente.isPessoaFisica()).isFalse();
    }

    @Test
    @DisplayName("isPessoaFisica deve retornar true para PESSOA_FISICA")
    void isPessoaFisica_deveRetornarTrue() {
        cliente.setTipo(TipoPessoa.PESSOA_FISICA);

        assertThat(cliente.isPessoaFisica()).isTrue();
        assertThat(cliente.isPessoaJuridica()).isFalse();
    }
}
