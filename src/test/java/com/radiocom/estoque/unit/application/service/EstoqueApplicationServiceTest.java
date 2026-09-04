package com.radiocom.estoque.unit.application.service;

import com.radiocom.estoque.application.dto.*;
import com.radiocom.estoque.application.mapper.EstoqueMapper;
import com.radiocom.estoque.application.service.EstoqueApplicationService;
import com.radiocom.estoque.domain.model.CatalogoModelo;
import com.radiocom.estoque.domain.model.Equipamento;
import com.radiocom.estoque.domain.model.enums.*;
import com.radiocom.estoque.domain.repository.AcessorioRepository;
import com.radiocom.estoque.domain.repository.CatalogoModeloRepository;
import com.radiocom.estoque.domain.repository.EquipamentoRepository;
import com.radiocom.estoque.domain.repository.PecaRepository;
import com.radiocom.estoque.domain.service.EquipamentoDomainService;
import com.radiocom.estoque.domain.service.EstoqueDomainService;
import com.radiocom.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EstoqueApplicationService - Testes Unitários")
class EstoqueApplicationServiceTest {

    @Mock private EquipamentoRepository equipamentoRepository;
    @Mock private AcessorioRepository acessorioRepository;
    @Mock private PecaRepository pecaRepository;
    @Mock private CatalogoModeloRepository catalogoModeloRepository;
    @Mock private EquipamentoDomainService equipamentoService;
    @Mock private EstoqueDomainService estoqueService;

    private EstoqueApplicationService service;

    @BeforeEach
    void setUp() {
        service = new EstoqueApplicationService(
                equipamentoRepository, acessorioRepository, pecaRepository, catalogoModeloRepository,
                equipamentoService, estoqueService, new EstoqueMapper());
    }

    // ===== Equipamento =====

    @Test
    @DisplayName("criarEquipamento deve resolver catálogo por marca+modelo e salvar")
    void criarEquipamento_deveResolverCatalogoESalvar() {
        EquipamentoCreateDTO dto = EquipamentoCreateDTO.builder()
                .proprietario(ProprietarioEquipamento.NOSSO)
                .numeroSerie("NS-001")
                .patrimonio("PAT-001")
                .faixa(FaixaEquipamento.VHF)
                .descricao("Rádio VHF")
                .marca("Motorola")
                .modelo("EP450")
                .build();

        when(catalogoModeloRepository.findByTipoItemAndMarcaIgnoreCaseAndModeloIgnoreCase(
                TipoItem.EQUIPAMENTO, "Motorola", "EP450")).thenReturn(Optional.empty());
        when(catalogoModeloRepository.save(any(CatalogoModelo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(equipamentoRepository.save(any(Equipamento.class))).thenAnswer(inv -> inv.getArgument(0));

        EquipamentoDTO resultado = service.criarEquipamento(dto);

        assertThat(resultado.getNumeroSerie()).isEqualTo("NS-001");
        assertThat(resultado.getCodigo()).isNotBlank();
    }

    @Test
    @DisplayName("criarEquipamento deve lançar exceção quando NS já cadastrado")
    void criarEquipamento_deveLancarExcecaoQuandoNSDuplicado() {
        EquipamentoCreateDTO dto = EquipamentoCreateDTO.builder()
                .proprietario(ProprietarioEquipamento.NOSSO)
                .numeroSerie("NS-001")
                .faixa(FaixaEquipamento.VHF)
                .build();

        doThrow(new DomainException("Número de série já cadastrado"))
                .when(equipamentoService).validarDuplicidadeNS("NS-001");

        assertThatThrownBy(() -> service.criarEquipamento(dto))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("já cadastrado");
    }

    @Test
    @DisplayName("buscarEquipamentoPorId deve delegar para o domain service")
    void buscarEquipamentoPorId_deveDelegar() {
        UUID id = UUID.randomUUID();
        Equipamento equipamento = Equipamento.builder()
                .codigo("EQ-1").descricao("Rádio").tipo(TipoItem.EQUIPAMENTO)
                .proprietario(ProprietarioEquipamento.NOSSO).faixa(FaixaEquipamento.VHF)
                .numeroSerie("NS-1").build();
        when(equipamentoService.buscarPorId(id)).thenReturn(equipamento);

        assertThat(service.buscarEquipamentoPorId(id).getNumeroSerie()).isEqualTo("NS-1");
    }

    @Test
    @DisplayName("buscarEquipamentoPorPatrimonio deve delegar para o domain service")
    void buscarEquipamentoPorPatrimonio_deveDelegar() {
        Equipamento equipamento = Equipamento.builder()
                .codigo("EQ-1").descricao("Rádio").tipo(TipoItem.EQUIPAMENTO)
                .proprietario(ProprietarioEquipamento.NOSSO).faixa(FaixaEquipamento.VHF)
                .numeroSerie("NS-1").patrimonio("PAT-1").build();
        when(equipamentoService.buscarPorPatrimonio("PAT-1")).thenReturn(equipamento);

        assertThat(service.buscarEquipamentoPorPatrimonio("PAT-1").getPatrimonio()).isEqualTo("PAT-1");
    }

    @Test
    @DisplayName("enviarEquipamentoManutencao deve delegar para o domain service")
    void enviarEquipamentoManutencao_deveDelegar() {
        UUID id = UUID.randomUUID();
        Equipamento equipamento = Equipamento.builder()
                .codigo("EQ-1").descricao("Rádio").tipo(TipoItem.EQUIPAMENTO)
                .proprietario(ProprietarioEquipamento.NOSSO).faixa(FaixaEquipamento.VHF)
                .numeroSerie("NS-1").estado(EstadoEquipamento.MANUTENCAO).build();
        when(equipamentoService.enviarManutencao(id)).thenReturn(equipamento);

        assertThat(service.enviarEquipamentoManutencao(id).getEstado()).isEqualTo(EstadoEquipamento.MANUTENCAO);
    }

    // ===== Acessorio =====

    @Test
    @DisplayName("criarAcessorio deve sobrescrever tipoAcessorio a partir do catálogo")
    void criarAcessorio_deveSobrescreverTipoAcessorioDoCatalogo() {
        AcessorioCreateDTO dto = AcessorioCreateDTO.builder()
                .descricao("Bateria BP-227")
                .tipoAcessorio(TipoAcessorio.OUTRO)
                .quantidadeDisponivel(5)
                .catalogoModeloId(UUID.randomUUID())
                .build();

        CatalogoModelo catalogo = CatalogoModelo.builder()
                .tipoItem(TipoItem.ACESSORIO)
                .tipoAcessorio(TipoAcessorio.BATERIA)
                .marca("Motorola")
                .modelo("BP-227")
                .build();

        when(catalogoModeloRepository.findById(dto.getCatalogoModeloId())).thenReturn(Optional.of(catalogo));
        when(acessorioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AcessorioDTO resultado = service.criarAcessorio(dto);

        assertThat(resultado.getTipoAcessorio()).isEqualTo(TipoAcessorio.BATERIA);
    }

    @Test
    @DisplayName("criarAcessorio deve lançar exceção quando número de série já cadastrado")
    void criarAcessorio_deveLancarExcecaoQuandoNSDuplicado() {
        AcessorioCreateDTO dto = AcessorioCreateDTO.builder()
                .descricao("Bateria")
                .tipoAcessorio(TipoAcessorio.BATERIA)
                .numeroSerie("NS-500")
                .build();

        when(acessorioRepository.findByNumeroSerie("NS-500"))
                .thenReturn(Optional.of(com.radiocom.estoque.domain.model.Acessorio.builder().build()));

        assertThatThrownBy(() -> service.criarAcessorio(dto))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Número de série já cadastrado");
    }

    // ===== Peca =====

    @Test
    @DisplayName("criarPeca deve gerar código e salvar")
    void criarPeca_deveGerarCodigoESalvar() {
        PecaCreateDTO dto = PecaCreateDTO.builder()
                .descricao("Antena UHF")
                .quantidadeDisponivel(20)
                .build();

        when(pecaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PecaDTO resultado = service.criarPeca(dto);

        assertThat(resultado.getCodigo()).isNotBlank();
        assertThat(resultado.getQuantidadeDisponivel()).isEqualTo(20);
    }

    // ===== Movimentação =====

    @Test
    @DisplayName("consultarSaldo deve delegar para o domain service")
    void consultarSaldo_deveDelegar() {
        UUID id = UUID.randomUUID();
        when(estoqueService.consultarSaldo(id, TipoItem.PECA)).thenReturn(7);

        assertThat(service.consultarSaldo(id, TipoItem.PECA)).isEqualTo(7);
    }

    @Test
    @DisplayName("darEntrada deve delegar para o domain service e retornar o saldo")
    void darEntrada_deveDelegar() {
        UUID id = UUID.randomUUID();
        when(estoqueService.darEntrada(id, TipoItem.PECA, 5)).thenReturn(15);

        assertThat(service.darEntrada(id, TipoItem.PECA, 5)).isEqualTo(15);
    }

    @Test
    @DisplayName("darSaida deve delegar para o domain service e retornar o saldo")
    void darSaida_deveDelegar() {
        UUID id = UUID.randomUUID();
        when(estoqueService.darSaida(id, TipoItem.ACESSORIO, 3)).thenReturn(2);

        assertThat(service.darSaida(id, TipoItem.ACESSORIO, 3)).isEqualTo(2);
    }

    @Test
    @DisplayName("ajustarQuantidade deve delegar para o domain service e retornar o saldo")
    void ajustarQuantidade_deveDelegar() {
        UUID id = UUID.randomUUID();
        when(estoqueService.ajustar(id, TipoItem.PECA, 0)).thenReturn(0);

        assertThat(service.ajustarQuantidade(id, TipoItem.PECA, 0)).isEqualTo(0);
    }
}
