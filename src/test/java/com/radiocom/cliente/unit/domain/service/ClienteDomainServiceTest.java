package com.radiocom.cliente.unit.domain.service;

import com.radiocom.cliente.domain.model.Cliente;
import com.radiocom.cliente.domain.model.enums.StatusCliente;
import com.radiocom.cliente.domain.model.enums.TipoPessoa;
import com.radiocom.cliente.domain.repository.ClienteRepository;
import com.radiocom.cliente.domain.service.ClienteDomainService;
import com.radiocom.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteDomainService - Testes Unitários")
class ClienteDomainServiceTest {

    @Mock private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteDomainService service;

    private UUID clienteId;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        cliente = Cliente.builder()
                .tipo(TipoPessoa.PESSOA_JURIDICA)
                .documento("11222333000181")
                .nomeRazaoSocial("Radio Comunicacao LTDA")
                .build();
    }

    // ===== buscarPorId =====

    @Test
    @DisplayName("buscarPorId deve retornar cliente quando existe")
    void buscarPorId_deveRetornarClienteQuandoExiste() {
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        assertThat(service.buscarPorId(clienteId)).isEqualTo(cliente);
    }

    @Test
    @DisplayName("buscarPorId deve lançar exceção quando não existe")
    void buscarPorId_deveLancarExcecaoQuandoNaoExiste() {
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(clienteId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Cliente não encontrado");
    }

    // ===== buscarPorDocumento =====

    @Test
    @DisplayName("buscarPorDocumento deve limpar máscara antes de buscar")
    void buscarPorDocumento_deveLimparMascaraAntesDeBuscar() {
        when(clienteRepository.findByDocumento("11222333000181")).thenReturn(Optional.of(cliente));

        assertThat(service.buscarPorDocumento("11.222.333/0001-81")).isEqualTo(cliente);
    }

    @Test
    @DisplayName("buscarPorDocumento deve lançar exceção quando não encontrado")
    void buscarPorDocumento_deveLancarExcecaoQuandoNaoEncontrado() {
        when(clienteRepository.findByDocumento("11222333000181")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorDocumento("11222333000181"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Cliente não encontrado com documento");
    }

    // ===== status =====

    @Test
    @DisplayName("inativarCliente deve mudar status e salvar")
    void inativarCliente_deveMudarStatusESalvar() {
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        Cliente resultado = service.inativarCliente(clienteId);

        assertThat(resultado.getStatus()).isEqualTo(StatusCliente.INATIVO);
    }

    @Test
    @DisplayName("inativarCliente deve lançar exceção quando cliente já não está ativo")
    void inativarCliente_deveLancarExcecaoQuandoJaNaoAtivo() {
        cliente.bloquear();
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> service.inativarCliente(clienteId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("não está ativo");
    }

    @Test
    @DisplayName("bloquearCliente deve mudar status e salvar")
    void bloquearCliente_deveMudarStatusESalvar() {
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        Cliente resultado = service.bloquearCliente(clienteId);

        assertThat(resultado.getStatus()).isEqualTo(StatusCliente.BLOQUEADO);
    }

    @Test
    @DisplayName("ativarCliente deve mudar status e salvar")
    void ativarCliente_deveMudarStatusESalvar() {
        cliente.bloquear();
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        Cliente resultado = service.ativarCliente(clienteId);

        assertThat(resultado.getStatus()).isEqualTo(StatusCliente.ATIVO);
    }

    // ===== validarDocumentoUnico =====

    @Test
    @DisplayName("validarDocumentoUnico deve lançar exceção quando documento inválido")
    void validarDocumentoUnico_deveLancarExcecaoQuandoDocumentoInvalido() {
        assertThatThrownBy(() -> service.validarDocumentoUnico("123", null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Documento inválido");
    }

    @Test
    @DisplayName("validarDocumentoUnico deve lançar exceção quando documento já cadastrado (criação)")
    void validarDocumentoUnico_deveLancarExcecaoQuandoJaCadastradoNaCriacao() {
        when(clienteRepository.existsByDocumento("11222333000181")).thenReturn(true);

        assertThatThrownBy(() -> service.validarDocumentoUnico("11222333000181", null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Já existe cliente cadastrado");
    }

    @Test
    @DisplayName("validarDocumentoUnico deve lançar exceção quando documento pertence a outro cliente (edição)")
    void validarDocumentoUnico_deveLancarExcecaoQuandoPertenceAOutroNaEdicao() {
        when(clienteRepository.existsByDocumentoAndIdNot("11222333000181", clienteId)).thenReturn(true);

        assertThatThrownBy(() -> service.validarDocumentoUnico("11222333000181", clienteId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Já existe cliente cadastrado");
    }

    @Test
    @DisplayName("validarDocumentoUnico não deve lançar exceção quando documento válido e disponível")
    void validarDocumentoUnico_naoDeveLancarQuandoValidoEDisponivel() {
        when(clienteRepository.existsByDocumento("11222333000181")).thenReturn(false);

        service.validarDocumentoUnico("11222333000181", null);
    }

    // ===== validarClienteAtivo =====

    @Test
    @DisplayName("validarClienteAtivo deve lançar exceção quando cliente inativo")
    void validarClienteAtivo_deveLancarExcecaoQuandoInativo() {
        cliente.inativar();

        assertThatThrownBy(() -> service.validarClienteAtivo(cliente))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("não está ativo");
    }

    @Test
    @DisplayName("validarClienteAtivo não deve lançar exceção quando cliente ativo")
    void validarClienteAtivo_naoDeveLancarQuandoAtivo() {
        service.validarClienteAtivo(cliente);
    }
}
