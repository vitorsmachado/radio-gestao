package com.radiocom.cliente.unit.application.service;

import com.radiocom.cliente.application.dto.ClienteCreateDTO;
import com.radiocom.cliente.application.dto.ClienteDTO;
import com.radiocom.cliente.application.dto.ClienteUpdateDTO;
import com.radiocom.cliente.application.dto.ContatoDTO;
import com.radiocom.cliente.application.dto.PostoDTO;
import com.radiocom.cliente.application.dto.PostoUpdateDTO;
import com.radiocom.cliente.application.mapper.ClienteMapper;
import com.radiocom.cliente.application.service.ClienteApplicationService;
import com.radiocom.cliente.domain.model.Cliente;
import com.radiocom.cliente.domain.model.Contato;
import com.radiocom.cliente.domain.model.Posto;
import com.radiocom.cliente.domain.model.enums.StatusCliente;
import com.radiocom.cliente.domain.model.enums.TipoContato;
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

import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteApplicationService - Testes Unitários")
class ClienteApplicationServiceTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private ClienteDomainService domainService;
    @Mock private ClienteMapper mapper;

    @InjectMocks
    private ClienteApplicationService service;

    private UUID clienteId;
    private Cliente cliente;
    private ClienteDTO clienteDTO;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        cliente = Cliente.builder()
                .tipo(TipoPessoa.PESSOA_JURIDICA)
                .documento("11222333000181")
                .nomeRazaoSocial("Radio Comunicacao LTDA")
                .build();
        ReflectionTestUtils.setField(cliente, "id", clienteId);

        clienteDTO = ClienteDTO.builder()
                .id(clienteId)
                .tipo(TipoPessoa.PESSOA_JURIDICA)
                .documento("11222333000181")
                .nomeRazaoSocial("Radio Comunicacao LTDA")
                .status(StatusCliente.ATIVO)
                .build();
    }

    // ===== criar =====

    @Test
    @DisplayName("criar deve salvar cliente com documento limpo")
    void criar_deveSalvarClienteComDocumentoLimpo() {
        ClienteCreateDTO dto = ClienteCreateDTO.builder()
                .documento("11.222.333/0001-81")
                .nomeRazaoSocial("Radio Comunicacao LTDA")
                .build();

        when(mapper.toEntity(dto)).thenReturn(new Cliente());
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDTO(any(Cliente.class))).thenReturn(clienteDTO);

        ClienteDTO resultado = service.criar(dto);

        verify(domainService).validarDocumentoUnico("11222333000181", null);
        assertThat(resultado).isEqualTo(clienteDTO);
    }

    @Test
    @DisplayName("criar deve inferir tipo PESSOA_JURIDICA quando não informado")
    void criar_deveInferirTipoQuandoNaoInformado() {
        ClienteCreateDTO dto = ClienteCreateDTO.builder()
                .documento("11222333000181")
                .nomeRazaoSocial("Radio Comunicacao LTDA")
                .build();

        Cliente entidadeMapeada = new Cliente();
        when(mapper.toEntity(dto)).thenReturn(entidadeMapeada);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDTO(any(Cliente.class))).thenReturn(clienteDTO);

        service.criar(dto);

        assertThat(entidadeMapeada.getTipo()).isEqualTo(TipoPessoa.PESSOA_JURIDICA);
    }

    @Test
    @DisplayName("criar deve lançar exceção quando documento é inválido")
    void criar_deveLancarExcecaoQuandoDocumentoInvalido() {
        ClienteCreateDTO dto = ClienteCreateDTO.builder()
                .documento("123")
                .nomeRazaoSocial("Cliente Teste")
                .build();

        assertThatThrownBy(() -> service.criar(dto))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Documento inválido");

        verifyNoInteractions(clienteRepository);
    }

    // ===== buscarPorId =====

    @Test
    @DisplayName("buscarPorId deve retornar o cliente mapeado")
    void buscarPorId_deveRetornarClienteMapeado() {
        when(domainService.buscarPorId(clienteId)).thenReturn(cliente);
        when(mapper.toDTO(cliente)).thenReturn(clienteDTO);

        assertThat(service.buscarPorId(clienteId)).isEqualTo(clienteDTO);
    }

    // ===== atualizar =====

    @Test
    @DisplayName("atualizar deve aplicar as mudanças e salvar")
    void atualizar_deveAplicarMudancasESalvar() {
        ClienteUpdateDTO dto = ClienteUpdateDTO.builder().nomeFantasia("Novo Nome").build();

        when(domainService.buscarPorId(clienteId)).thenReturn(cliente);
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(mapper.toDTO(cliente)).thenReturn(clienteDTO);

        service.atualizar(clienteId, dto);

        verify(mapper).updateEntityFromDTO(dto, cliente);
        verify(clienteRepository).save(cliente);
    }

    // ===== status =====

    @Test
    @DisplayName("ativar deve delegar para o domain service")
    void ativar_deveDelegarParaDomainService() {
        when(domainService.ativarCliente(clienteId)).thenReturn(cliente);
        when(mapper.toDTO(cliente)).thenReturn(clienteDTO);

        assertThat(service.ativar(clienteId)).isEqualTo(clienteDTO);
    }

    // ===== postos =====

    @Test
    @DisplayName("adicionarPosto deve associar o posto ao cliente e salvar")
    void adicionarPosto_deveAssociarESalvar() {
        PostoDTO dto = PostoDTO.builder().nome("Matriz").build();
        Posto posto = Posto.builder().nome("Matriz").build();
        PostoDTO postoDTO = PostoDTO.builder().nome("Matriz").build();

        when(domainService.buscarPorId(clienteId)).thenReturn(cliente);
        when(mapper.toEntity(dto)).thenReturn(posto);
        when(mapper.toDTO(posto)).thenReturn(postoDTO);

        PostoDTO resultado = service.adicionarPosto(clienteId, dto);

        assertThat(cliente.getPostos()).contains(posto);
        assertThat(resultado).isEqualTo(postoDTO);
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("atualizarPosto deve marcar como padrão e desmarcar os demais")
    void atualizarPosto_deveMarcarComoPadraoEDesmarcarDemais() {
        Posto postoAntigo = Posto.builder().nome("Antigo").padrao(true).build();
        Posto postoNovo = Posto.builder().nome("Novo").padrao(false).build();
        UUID postoNovoId = UUID.randomUUID();
        ReflectionTestUtils.setField(postoAntigo, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(postoNovo, "id", postoNovoId);
        cliente.adicionarPosto(postoAntigo);
        cliente.adicionarPosto(postoNovo);

        PostoUpdateDTO dto = PostoUpdateDTO.builder().padrao(true).build();

        when(domainService.buscarPorId(clienteId)).thenReturn(cliente);
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(mapper.toDTO(cliente)).thenReturn(clienteDTO);

        service.atualizarPosto(clienteId, postoNovoId, dto);

        assertThat(postoAntigo.isPadrao()).isFalse();
        assertThat(postoNovo.isPadrao()).isTrue();
    }

    // ===== contatos =====

    @Test
    @DisplayName("adicionarContato deve associar o contato ao cliente e salvar")
    void adicionarContato_deveAssociarESalvar() {
        ContatoDTO dto = ContatoDTO.builder().nome("João").tipo(TipoContato.COMERCIAL).build();
        Contato contato = Contato.builder().nome("João").tipo(TipoContato.COMERCIAL).build();

        when(domainService.buscarPorId(clienteId)).thenReturn(cliente);
        when(mapper.toEntity(dto)).thenReturn(contato);
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(mapper.toDTO(cliente)).thenReturn(clienteDTO);

        service.adicionarContato(clienteId, dto);

        assertThat(cliente.getContatos()).contains(contato);
    }

    @Test
    @DisplayName("atualizarContatoPrincipal deve delegar para o método de domínio")
    void atualizarContatoPrincipal_deveDelegarParaDominio() {
        Contato contato = Contato.builder().nome("João").tipo(TipoContato.COMERCIAL).principal(true).build();
        UUID contatoId = UUID.randomUUID();
        ReflectionTestUtils.setField(contato, "id", contatoId);
        cliente.adicionarContato(contato);

        when(domainService.buscarPorId(clienteId)).thenReturn(cliente);
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(mapper.toDTO(cliente)).thenReturn(clienteDTO);

        service.atualizarContatoPrincipal(clienteId, contatoId);

        assertThat(contato.isPrincipal()).isTrue();
    }
}
