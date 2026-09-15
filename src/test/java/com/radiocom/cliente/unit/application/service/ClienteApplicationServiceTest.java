package com.radiocom.cliente.unit.application.service;

import com.radiocom.cliente.application.dto.ClienteCreateDTO;
import com.radiocom.cliente.application.dto.ClienteDTO;
import com.radiocom.cliente.application.dto.ClienteUpdateDTO;
import com.radiocom.cliente.application.dto.ContatoDTO;
import com.radiocom.cliente.application.dto.MotivoDTO;
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
import com.radiocom.cliente.domain.service.NumeroClienteGenerator;
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
    @Mock private NumeroClienteGenerator numeroClienteGenerator;
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
    @DisplayName("criar deve atribuir o numeroIdentificacao gerado")
    void criar_deveAtribuirNumeroIdentificacaoGerado() {
        ClienteCreateDTO dto = ClienteCreateDTO.builder()
                .documento("11222333000181")
                .nomeRazaoSocial("Radio Comunicacao LTDA")
                .build();

        Cliente entidadeMapeada = new Cliente();
        when(mapper.toEntity(dto)).thenReturn(entidadeMapeada);
        when(numeroClienteGenerator.gerarNumero()).thenReturn(42);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDTO(any(Cliente.class))).thenReturn(clienteDTO);

        service.criar(dto);

        assertThat(entidadeMapeada.getNumeroIdentificacao()).isEqualTo(42);
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

    @Test
    @DisplayName("atualizar deve permitir alterar o numeroIdentificacao quando não há conflito")
    void atualizar_devePermitirAlterarNumeroIdentificacao() {
        ReflectionTestUtils.setField(cliente, "numeroIdentificacao", 10);
        ClienteUpdateDTO dto = ClienteUpdateDTO.builder().numeroIdentificacao(20).build();

        when(domainService.buscarPorId(clienteId)).thenReturn(cliente);
        when(clienteRepository.existsByNumeroIdentificacaoAndIdNot(20, clienteId)).thenReturn(false);
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(mapper.toDTO(cliente)).thenReturn(clienteDTO);

        service.atualizar(clienteId, dto);

        verify(mapper).updateEntityFromDTO(dto, cliente);
    }

    @Test
    @DisplayName("atualizar deve rejeitar numeroIdentificacao já usado por outro cliente")
    void atualizar_deveRejeitarNumeroIdentificacaoDuplicado() {
        ReflectionTestUtils.setField(cliente, "numeroIdentificacao", 10);
        ClienteUpdateDTO dto = ClienteUpdateDTO.builder().numeroIdentificacao(99).build();

        when(domainService.buscarPorId(clienteId)).thenReturn(cliente);
        when(clienteRepository.existsByNumeroIdentificacaoAndIdNot(99, clienteId)).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(clienteId, dto))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Número de identificação já está em uso");

        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualizar não deve validar duplicidade quando o numeroIdentificacao enviado é o mesmo já cadastrado")
    void atualizar_naoDeveValidarQuandoNumeroIdentificacaoIgual() {
        ReflectionTestUtils.setField(cliente, "numeroIdentificacao", 10);
        ClienteUpdateDTO dto = ClienteUpdateDTO.builder().numeroIdentificacao(10).build();

        when(domainService.buscarPorId(clienteId)).thenReturn(cliente);
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(mapper.toDTO(cliente)).thenReturn(clienteDTO);

        service.atualizar(clienteId, dto);

        verify(clienteRepository, never()).existsByNumeroIdentificacaoAndIdNot(any(), any());
    }

    // ===== listar =====

    @Test
    @DisplayName("listar deve repassar busca e status pro repositório")
    void listar_deveRepassarBuscaEStatus() {
        var pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        when(clienteRepository.buscar("Radio", StatusCliente.ATIVO, pageable))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(cliente), pageable, 1));
        when(mapper.toDTO(cliente)).thenReturn(clienteDTO);

        var resultado = service.listar("Radio", StatusCliente.ATIVO, pageable);

        assertThat(resultado.getContent()).containsExactly(clienteDTO);
    }

    @Test
    @DisplayName("listar deve tratar busca em branco como nula")
    void listar_deveTratarBuscaEmBrancoComoNula() {
        var pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        when(clienteRepository.buscar(null, null, pageable))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(cliente), pageable, 1));
        when(mapper.toDTO(cliente)).thenReturn(clienteDTO);

        service.listar("   ", null, pageable);

        verify(clienteRepository).buscar(null, null, pageable);
    }

    // ===== status =====

    @Test
    @DisplayName("ativar deve delegar para o domain service repassando o motivo")
    void ativar_deveDelegarParaDomainService() {
        when(domainService.ativarCliente(clienteId, "Pagamento regularizado")).thenReturn(cliente);
        when(mapper.toDTO(cliente)).thenReturn(clienteDTO);

        MotivoDTO dto = MotivoDTO.builder().motivo("Pagamento regularizado").build();

        assertThat(service.ativar(clienteId, dto)).isEqualTo(clienteDTO);
    }

    @Test
    @DisplayName("ativar sem corpo deve repassar motivo nulo")
    void ativar_semCorpo_deveRepassarMotivoNulo() {
        when(domainService.ativarCliente(clienteId, null)).thenReturn(cliente);
        when(mapper.toDTO(cliente)).thenReturn(clienteDTO);

        assertThat(service.ativar(clienteId, null)).isEqualTo(clienteDTO);
    }

    @Test
    @DisplayName("bloquear deve delegar para o domain service repassando o motivo")
    void bloquear_deveDelegarParaDomainService() {
        when(domainService.bloquearCliente(clienteId, "Inadimplência")).thenReturn(cliente);
        when(mapper.toDTO(cliente)).thenReturn(clienteDTO);

        MotivoDTO dto = MotivoDTO.builder().motivo("Inadimplência").build();

        assertThat(service.bloquear(clienteId, dto)).isEqualTo(clienteDTO);
    }

    @Test
    @DisplayName("inativar deve delegar para o domain service repassando o motivo")
    void inativar_deveDelegarParaDomainService() {
        when(domainService.inativarCliente(clienteId, "Encerramento de contrato")).thenReturn(cliente);
        when(mapper.toDTO(cliente)).thenReturn(clienteDTO);

        MotivoDTO dto = MotivoDTO.builder().motivo("Encerramento de contrato").build();

        assertThat(service.inativar(clienteId, dto)).isEqualTo(clienteDTO);
    }

    // ===== busca por outros módulos =====

    @Test
    @DisplayName("buscarPorNomeOuDocumento deve delegar pro repositório e mapear a lista")
    void buscarPorNomeOuDocumento_deveDelegarEMapear() {
        when(clienteRepository.buscarPorNomeOuDocumento("Radio")).thenReturn(java.util.List.of(cliente));
        when(mapper.toDTO(cliente)).thenReturn(clienteDTO);

        var resultado = service.buscarPorNomeOuDocumento("Radio");

        assertThat(resultado).containsExactly(clienteDTO);
    }

    @Test
    @DisplayName("buscarPorIds deve delegar pro repositório e mapear a lista")
    void buscarPorIds_deveDelegarEMapear() {
        when(clienteRepository.findAllById(java.util.List.of(clienteId))).thenReturn(java.util.List.of(cliente));
        when(mapper.toDTO(cliente)).thenReturn(clienteDTO);

        var resultado = service.buscarPorIds(java.util.List.of(clienteId));

        assertThat(resultado).containsExactly(clienteDTO);
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
