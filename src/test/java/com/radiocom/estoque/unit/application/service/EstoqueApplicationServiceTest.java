package com.radiocom.estoque.unit.application.service;

import com.radiocom.estoque.application.dto.*;
import com.radiocom.estoque.application.mapper.EstoqueMapper;
import com.radiocom.estoque.application.service.EstoqueApplicationService;
import com.radiocom.estoque.domain.model.Acessorio;
import com.radiocom.estoque.domain.model.CatalogoModelo;
import com.radiocom.estoque.domain.model.Equipamento;
import com.radiocom.estoque.domain.model.Peca;
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
    @Mock private org.springframework.context.ApplicationEventPublisher eventPublisher;

    private EstoqueApplicationService service;

    @BeforeEach
    void setUp() {
        service = new EstoqueApplicationService(
                equipamentoRepository, acessorioRepository, pecaRepository, catalogoModeloRepository,
                equipamentoService, estoqueService, new EstoqueMapper(), eventPublisher);
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
    @DisplayName("criarEquipamento deve mapear o codigoCliente informado")
    void criarEquipamento_deveMapearCodigoCliente() {
        EquipamentoCreateDTO dto = EquipamentoCreateDTO.builder()
                .proprietario(ProprietarioEquipamento.CLIENTE)
                .clienteId(UUID.randomUUID())
                .codigoCliente("TAG-CLIENTE-001")
                .numeroSerie("NS-002")
                .faixa(FaixaEquipamento.VHF)
                .descricao("Rádio VHF")
                .build();

        when(equipamentoRepository.save(any(Equipamento.class))).thenAnswer(inv -> inv.getArgument(0));

        EquipamentoDTO resultado = service.criarEquipamento(dto);

        assertThat(resultado.getCodigoCliente()).isEqualTo("TAG-CLIENTE-001");
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
    @DisplayName("resolverEquipamentoPorNS deve reaproveitar quando ja existe pro mesmo cliente")
    void resolverEquipamentoPorNS_deveReaproveitarQuandoMesmoCliente() {
        UUID clienteId = UUID.randomUUID();
        Equipamento existente = Equipamento.builder()
                .codigo("EQ-1").descricao("Rádio").tipo(TipoItem.EQUIPAMENTO)
                .proprietario(ProprietarioEquipamento.CLIENTE).clienteId(clienteId)
                .faixa(FaixaEquipamento.VHF).numeroSerie("NS-001").build();
        when(equipamentoRepository.findByNumeroSerie("NS-001")).thenReturn(Optional.of(existente));

        ResolverEquipamentoPorNSDTO dto = ResolverEquipamentoPorNSDTO.builder()
                .numeroSerie("NS-001").clienteId(clienteId).faixa(FaixaEquipamento.VHF).build();

        EquipamentoDTO resultado = service.resolverEquipamentoPorNS(dto);

        assertThat(resultado.getNumeroSerie()).isEqualTo("NS-001");
        org.mockito.Mockito.verify(equipamentoRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    @DisplayName("resolverEquipamentoPorNS deve lançar exceção quando NS pertence a outro cliente")
    void resolverEquipamentoPorNS_deveLancarExcecaoQuandoOutroCliente() {
        Equipamento existente = Equipamento.builder()
                .codigo("EQ-1").descricao("Rádio").tipo(TipoItem.EQUIPAMENTO)
                .proprietario(ProprietarioEquipamento.CLIENTE).clienteId(UUID.randomUUID())
                .faixa(FaixaEquipamento.VHF).numeroSerie("NS-001").build();
        when(equipamentoRepository.findByNumeroSerie("NS-001")).thenReturn(Optional.of(existente));

        ResolverEquipamentoPorNSDTO dto = ResolverEquipamentoPorNSDTO.builder()
                .numeroSerie("NS-001").clienteId(UUID.randomUUID()).faixa(FaixaEquipamento.VHF).build();

        assertThatThrownBy(() -> service.resolverEquipamentoPorNS(dto))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("outro cliente");
    }

    @Test
    @DisplayName("resolverEquipamentoPorNS deve cadastrar um novo quando o NS ainda não existe")
    void resolverEquipamentoPorNS_deveCriarQuandoNaoExiste() {
        UUID clienteId = UUID.randomUUID();
        when(equipamentoRepository.findByNumeroSerie("NS-002")).thenReturn(Optional.empty());
        when(equipamentoRepository.save(any(Equipamento.class))).thenAnswer(inv -> inv.getArgument(0));

        ResolverEquipamentoPorNSDTO dto = ResolverEquipamentoPorNSDTO.builder()
                .numeroSerie("NS-002").clienteId(clienteId).faixa(FaixaEquipamento.VHF)
                .descricao("Rádio Motorola").marca("Motorola").modelo("EP450").build();

        EquipamentoDTO resultado = service.resolverEquipamentoPorNS(dto);

        assertThat(resultado.getNumeroSerie()).isEqualTo("NS-002");
        assertThat(resultado.getCodigo()).isNotBlank();
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
    @DisplayName("criarAcessorio deve mapear o codigoCliente informado")
    void criarAcessorio_deveMapearCodigoCliente() {
        AcessorioCreateDTO dto = AcessorioCreateDTO.builder()
                .descricao("Bateria BP-227")
                .tipoAcessorio(TipoAcessorio.BATERIA)
                .clienteId(UUID.randomUUID())
                .codigoCliente("TAG-CLIENTE-002")
                .quantidadeDisponivel(5)
                .build();

        when(acessorioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AcessorioDTO resultado = service.criarAcessorio(dto);

        assertThat(resultado.getCodigoCliente()).isEqualTo("TAG-CLIENTE-002");
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

    @Test
    @DisplayName("criarPeca deve vincular os modelos compatíveis informados")
    void criarPeca_deveVincularModelosCompativeis() {
        UUID modeloId = UUID.randomUUID();
        CatalogoModelo modelo = CatalogoModelo.builder().tipoItem(TipoItem.EQUIPAMENTO).marca("Motorola").modelo("EP450").build();
        org.springframework.test.util.ReflectionTestUtils.setField(modelo, "id", modeloId);

        PecaCreateDTO dto = PecaCreateDTO.builder()
                .descricao("Bateria BP-227")
                .modelosCompativeisIds(java.util.List.of(modeloId))
                .build();

        when(catalogoModeloRepository.findById(modeloId)).thenReturn(Optional.of(modelo));
        when(pecaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PecaDTO resultado = service.criarPeca(dto);

        assertThat(resultado.getModelosCompativeis()).hasSize(1);
        assertThat(resultado.getModelosCompativeis().get(0).getModelo()).isEqualTo("EP450");
    }

    @Test
    @DisplayName("criarPeca deve salvar o valor unitário informado")
    void criarPeca_deveSalvarValorUnitario() {
        PecaCreateDTO dto = PecaCreateDTO.builder()
                .descricao("Bateria BP-227")
                .valorUnitario(new java.math.BigDecimal("45.00"))
                .build();

        when(pecaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PecaDTO resultado = service.criarPeca(dto);

        assertThat(resultado.getValorUnitario()).isEqualByComparingTo("45.00");
    }

    @Test
    @DisplayName("atualizarPeca deve atualizar o valor unitário")
    void atualizarPeca_deveAtualizarValorUnitario() {
        Peca peca = Peca.builder().descricao("Antena UHF").codigo("PC-001").build();
        UUID pecaId = UUID.randomUUID();
        org.springframework.test.util.ReflectionTestUtils.setField(peca, "id", pecaId);

        when(estoqueService.buscarPecaPorId(pecaId)).thenReturn(peca);
        when(pecaRepository.save(any(Peca.class))).thenAnswer(inv -> inv.getArgument(0));

        PecaDTO resultado = service.atualizarPeca(pecaId, PecaUpdateDTO.builder()
                .valorUnitario(new java.math.BigDecimal("52.90")).build());

        assertThat(resultado.getValorUnitario()).isEqualByComparingTo("52.90");
    }

    @Test
    @DisplayName("vincularModeloCompativel deve vincular quando o modelo é EQUIPAMENTO")
    void vincularModeloCompativel_deveVincularQuandoEquipamento() {
        Peca peca = Peca.builder().descricao("Bateria BP-227").build();
        UUID pecaId = UUID.randomUUID();
        org.springframework.test.util.ReflectionTestUtils.setField(peca, "id", pecaId);

        CatalogoModelo modelo = CatalogoModelo.builder().tipoItem(TipoItem.EQUIPAMENTO).marca("Motorola").modelo("EP450").build();
        UUID modeloId = UUID.randomUUID();
        org.springframework.test.util.ReflectionTestUtils.setField(modelo, "id", modeloId);

        when(estoqueService.buscarPecaPorId(pecaId)).thenReturn(peca);
        when(catalogoModeloRepository.findById(modeloId)).thenReturn(Optional.of(modelo));
        when(pecaRepository.save(any(Peca.class))).thenAnswer(inv -> inv.getArgument(0));

        PecaDTO resultado = service.vincularModeloCompativel(pecaId, modeloId);

        assertThat(resultado.getModelosCompativeis()).hasSize(1);
        assertThat(resultado.getModelosCompativeis().get(0).getModelo()).isEqualTo("EP450");
    }

    @Test
    @DisplayName("vincularModeloCompativel deve lançar exceção quando o modelo não é EQUIPAMENTO")
    void vincularModeloCompativel_deveLancarExcecaoQuandoNaoEquipamento() {
        Peca peca = Peca.builder().descricao("Bateria BP-227").build();
        UUID pecaId = UUID.randomUUID();
        org.springframework.test.util.ReflectionTestUtils.setField(peca, "id", pecaId);

        CatalogoModelo modelo = CatalogoModelo.builder().tipoItem(TipoItem.PECA).marca("Genérica").modelo("Capacitor").build();
        UUID modeloId = UUID.randomUUID();
        org.springframework.test.util.ReflectionTestUtils.setField(modelo, "id", modeloId);

        when(estoqueService.buscarPecaPorId(pecaId)).thenReturn(peca);
        when(catalogoModeloRepository.findById(modeloId)).thenReturn(Optional.of(modelo));

        assertThatThrownBy(() -> service.vincularModeloCompativel(pecaId, modeloId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("EQUIPAMENTO");
    }

    @Test
    @DisplayName("desvincularModeloCompativel deve remover o vínculo")
    void desvincularModeloCompativel_deveRemover() {
        CatalogoModelo modelo = CatalogoModelo.builder().tipoItem(TipoItem.EQUIPAMENTO).marca("Motorola").modelo("EP450").build();
        UUID modeloId = UUID.randomUUID();
        org.springframework.test.util.ReflectionTestUtils.setField(modelo, "id", modeloId);

        Peca peca = Peca.builder().descricao("Bateria BP-227").build();
        UUID pecaId = UUID.randomUUID();
        org.springframework.test.util.ReflectionTestUtils.setField(peca, "id", pecaId);
        peca.vincularModeloCompativel(modelo);

        when(estoqueService.buscarPecaPorId(pecaId)).thenReturn(peca);
        when(pecaRepository.save(any(Peca.class))).thenAnswer(inv -> inv.getArgument(0));

        PecaDTO resultado = service.desvincularModeloCompativel(pecaId, modeloId);

        assertThat(resultado.getModelosCompativeis()).isEmpty();
    }

    @Test
    @DisplayName("atualizarPeca deve permitir alterar o código quando não há conflito")
    void atualizarPeca_deveAlterarCodigo() {
        Peca peca = Peca.builder().descricao("Antena UHF").codigo("PC-001").build();
        UUID pecaId = UUID.randomUUID();
        org.springframework.test.util.ReflectionTestUtils.setField(peca, "id", pecaId);

        when(estoqueService.buscarPecaPorId(pecaId)).thenReturn(peca);
        when(pecaRepository.existsByCodigoIgnoreCase("PC-002")).thenReturn(false);
        when(pecaRepository.save(any(Peca.class))).thenAnswer(inv -> inv.getArgument(0));

        PecaDTO resultado = service.atualizarPeca(pecaId, PecaUpdateDTO.builder().codigo("PC-002").build());

        assertThat(resultado.getCodigo()).isEqualTo("PC-002");
    }

    @Test
    @DisplayName("atualizarPeca deve rejeitar código já cadastrado em outra peça")
    void atualizarPeca_deveRejeitarCodigoDuplicado() {
        Peca peca = Peca.builder().descricao("Antena UHF").codigo("PC-001").build();
        UUID pecaId = UUID.randomUUID();
        org.springframework.test.util.ReflectionTestUtils.setField(peca, "id", pecaId);

        when(estoqueService.buscarPecaPorId(pecaId)).thenReturn(peca);
        when(pecaRepository.existsByCodigoIgnoreCase("PC-999")).thenReturn(true);

        assertThatThrownBy(() -> service.atualizarPeca(pecaId, PecaUpdateDTO.builder().codigo("PC-999").build()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Código já cadastrado");
    }

    @Test
    @DisplayName("atualizarPeca deve retornar observacoes e localizacaoFisica atualizadas")
    void atualizarPeca_deveRetornarObservacoesELocalizacao() {
        Peca peca = Peca.builder().descricao("Antena UHF").codigo("PC-001").build();
        UUID pecaId = UUID.randomUUID();
        org.springframework.test.util.ReflectionTestUtils.setField(peca, "id", pecaId);

        when(estoqueService.buscarPecaPorId(pecaId)).thenReturn(peca);
        when(pecaRepository.save(any(Peca.class))).thenAnswer(inv -> inv.getArgument(0));

        PecaDTO resultado = service.atualizarPeca(pecaId, PecaUpdateDTO.builder()
                .observacoes("Verificar contatos oxidados")
                .localizacaoFisica("Prateleira B3")
                .build());

        assertThat(resultado.getObservacoes()).isEqualTo("Verificar contatos oxidados");
        assertThat(resultado.getLocalizacaoFisica()).isEqualTo("Prateleira B3");
    }

    @Test
    @DisplayName("atualizarPeca não deve validar duplicidade quando o código enviado é o mesmo já cadastrado")
    void atualizarPeca_naoDeveValidarQuandoCodigoIgual() {
        Peca peca = Peca.builder().descricao("Antena UHF").codigo("PC-001").build();
        UUID pecaId = UUID.randomUUID();
        org.springframework.test.util.ReflectionTestUtils.setField(peca, "id", pecaId);

        when(estoqueService.buscarPecaPorId(pecaId)).thenReturn(peca);
        when(pecaRepository.save(any(Peca.class))).thenAnswer(inv -> inv.getArgument(0));

        PecaDTO resultado = service.atualizarPeca(pecaId, PecaUpdateDTO.builder().codigo("pc-001").descricao("Antena UHF revisada").build());

        assertThat(resultado.getDescricao()).isEqualTo("Antena UHF revisada");
    }

    @Test
    @DisplayName("listarMovimentacoesPeca deve verificar existência da peça e delegar pro domain service")
    void listarMovimentacoesPeca_deveVerificarExistenciaEDelegar() {
        UUID pecaId = UUID.randomUUID();
        Peca peca = Peca.builder().descricao("Antena UHF").codigo("PC-001").build();
        var pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        com.radiocom.estoque.domain.model.MovimentacaoEstoque mov = com.radiocom.estoque.domain.model.MovimentacaoEstoque.builder()
                .itemId(pecaId).tipoItem(TipoItem.PECA)
                .tipoMovimentacao(com.radiocom.estoque.domain.model.enums.TipoMovimentacao.ENTRADA)
                .saldoAnterior(0).saldoNovo(5).motivo("Reposição").build();

        when(estoqueService.buscarPecaPorId(pecaId)).thenReturn(peca);
        when(estoqueService.listarMovimentacoes(pecaId, pageable))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(mov), pageable, 1));

        var resultado = service.listarMovimentacoesPeca(pecaId, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getMotivo()).isEqualTo("Reposição");
    }

    @Test
    @DisplayName("listarMovimentacoesPeca deve lançar exceção quando a peça não existe")
    void listarMovimentacoesPeca_deveLancarExcecaoQuandoPecaNaoExiste() {
        UUID pecaId = UUID.randomUUID();
        var pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        when(estoqueService.buscarPecaPorId(pecaId)).thenThrow(new DomainException("Peça não encontrada: " + pecaId));

        assertThatThrownBy(() -> service.listarMovimentacoesPeca(pecaId, pageable))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("listarPecas sem filtros deve chamar buscar com emFalta/estoqueBaixo false e modelo nulo")
    void listarPecas_semFiltros_deveChamarBuscarSemRestricao() {
        Peca peca = Peca.builder().descricao("Antena UHF").quantidadeDisponivel(5).build();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        when(pecaRepository.buscar(null, null, false, false, false, pageable)).thenReturn(
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(peca), pageable, 1));

        var resultado = service.listarPecas(pageable, null, null, null);

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).getDescricao()).isEqualTo("Antena UHF");
    }

    @Test
    @DisplayName("listarPecas com EM_FALTA deve chamar buscar com emFalta=true")
    void listarPecas_emFalta_deveChamarBuscarComEmFaltaTrue() {
        Peca peca = Peca.builder().descricao("Antena UHF").quantidadeDisponivel(0).build();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        when(pecaRepository.buscar(null, null, true, false, false, pageable)).thenReturn(
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(peca), pageable, 1));

        var resultado = service.listarPecas(pageable, CriticidadeEstoque.EM_FALTA, null, null);

        assertThat(resultado.getContent().get(0).isEmFalta()).isTrue();
    }

    @Test
    @DisplayName("listarPecas com ESTOQUE_BAIXO deve chamar buscar com estoqueBaixo=true")
    void listarPecas_estoqueBaixo_deveChamarBuscarComEstoqueBaixoTrue() {
        Peca peca = Peca.builder().descricao("Antena UHF").quantidadeDisponivel(2).quantidadeMinima(5).build();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        when(pecaRepository.buscar(null, null, false, true, false, pageable)).thenReturn(
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(peca), pageable, 1));

        var resultado = service.listarPecas(pageable, CriticidadeEstoque.ESTOQUE_BAIXO, null, null);

        assertThat(resultado.getContent().get(0).isEstoqueBaixo()).isTrue();
    }

    @Test
    @DisplayName("listarPecas com CRITICO deve chamar buscar com critico=true")
    void listarPecas_critico_deveChamarBuscarComCriticoTrue() {
        Peca peca = Peca.builder().descricao("Antena UHF").quantidadeDisponivel(0).build();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        when(pecaRepository.buscar(null, null, false, false, true, pageable)).thenReturn(
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(peca), pageable, 1));

        var resultado = service.listarPecas(pageable, CriticidadeEstoque.CRITICO, null, null);

        assertThat(resultado.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("listarPecas com modeloCompativelId deve repassar o filtro pro repositório")
    void listarPecas_comModeloCompativelId_deveRepassarFiltro() {
        UUID modeloId = UUID.randomUUID();
        Peca peca = Peca.builder().descricao("Bateria BP-227").build();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        when(pecaRepository.buscar(modeloId, null, false, false, false, pageable)).thenReturn(
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(peca), pageable, 1));

        var resultado = service.listarPecas(pageable, null, modeloId, null);

        assertThat(resultado.getContent()).hasSize(1);
    }

    // ===== Garantia (usada pelo módulo Cliente) =====

    @Test
    @DisplayName("listarItensDoCliente deve unir equipamentos e acessórios do cliente")
    void listarItensDoCliente_deveUnirEquipamentosEAcessorios() {
        UUID clienteId = UUID.randomUUID();
        Equipamento equipamento = Equipamento.builder()
                .codigo("EQ-1").descricao("Rádio").tipo(TipoItem.EQUIPAMENTO)
                .proprietario(ProprietarioEquipamento.CLIENTE).clienteId(clienteId)
                .faixa(FaixaEquipamento.VHF).numeroSerie("NS-1").build();
        Acessorio acessorio = Acessorio.builder()
                .codigo("AC-1").descricao("Bateria").tipo(TipoItem.ACESSORIO)
                .proprietario(ProprietarioEquipamento.CLIENTE).clienteId(clienteId)
                .tipoAcessorio(TipoAcessorio.BATERIA).build();

        when(equipamentoRepository.findByClienteId(clienteId)).thenReturn(java.util.List.of(equipamento));
        when(acessorioRepository.findByClienteId(clienteId)).thenReturn(java.util.List.of(acessorio));

        var resultado = service.listarItensDoCliente(clienteId);

        assertThat(resultado).extracting("codigo").containsExactlyInAnyOrder("EQ-1", "AC-1");
    }

    @Test
    @DisplayName("listarItensDoCliente deve retornar vazio quando o cliente não tem itens")
    void listarItensDoCliente_deveRetornarVazioQuandoSemItens() {
        UUID clienteId = UUID.randomUUID();
        when(equipamentoRepository.findByClienteId(clienteId)).thenReturn(java.util.List.of());
        when(acessorioRepository.findByClienteId(clienteId)).thenReturn(java.util.List.of());

        assertThat(service.listarItensDoCliente(clienteId)).isEmpty();
    }

    // ===== Busca cruzada (usada pelo módulo Cliente) =====

    @Test
    @DisplayName("buscarClienteIdsPorNumeroSerieOuCodigoCliente deve unir e deduplicar resultados de equipamento e acessório")
    void buscarClienteIdsPorNumeroSerieOuCodigoCliente_deveUnirEDeduplicar() {
        UUID clienteComum = UUID.randomUUID();
        UUID clienteSoEquipamento = UUID.randomUUID();
        UUID clienteSoAcessorio = UUID.randomUUID();

        when(equipamentoRepository.buscarClienteIdsPorNumeroSerieOuCodigoCliente("NS-123"))
                .thenReturn(java.util.List.of(clienteComum, clienteSoEquipamento));
        when(acessorioRepository.buscarClienteIdsPorNumeroSerieOuCodigoCliente("NS-123"))
                .thenReturn(java.util.List.of(clienteComum, clienteSoAcessorio));

        var resultado = service.buscarClienteIdsPorNumeroSerieOuCodigoCliente("NS-123");

        assertThat(resultado).containsExactlyInAnyOrder(clienteComum, clienteSoEquipamento, clienteSoAcessorio);
    }

    @Test
    @DisplayName("buscarClienteIdsPorNumeroSerieOuCodigoCliente deve retornar vazio quando nenhum item corresponde")
    void buscarClienteIdsPorNumeroSerieOuCodigoCliente_deveRetornarVazioQuandoSemCorrespondencia() {
        when(equipamentoRepository.buscarClienteIdsPorNumeroSerieOuCodigoCliente("inexistente")).thenReturn(java.util.List.of());
        when(acessorioRepository.buscarClienteIdsPorNumeroSerieOuCodigoCliente("inexistente")).thenReturn(java.util.List.of());

        assertThat(service.buscarClienteIdsPorNumeroSerieOuCodigoCliente("inexistente")).isEmpty();
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
    @DisplayName("darEntrada de PECA deve delegar, retornar o saldo e publicar evento")
    void darEntrada_devePublicarEventoParaPeca() {
        UUID id = UUID.randomUUID();
        when(estoqueService.darEntrada(id, TipoItem.PECA, 5, "Reposição")).thenReturn(15);

        assertThat(service.darEntrada(id, TipoItem.PECA, 5, "Reposição")).isEqualTo(15);

        org.mockito.Mockito.verify(eventPublisher).publishEvent(
                org.mockito.ArgumentMatchers.any(
                        com.radiocom.estoque.domain.event.PecaEntradaEstoqueEvent.class));
    }

    @Test
    @DisplayName("darEntrada de ACESSORIO deve delegar e retornar o saldo sem publicar evento")
    void darEntrada_naoDevePublicarEventoParaAcessorio() {
        UUID id = UUID.randomUUID();
        when(estoqueService.darEntrada(id, TipoItem.ACESSORIO, 3, null)).thenReturn(8);

        assertThat(service.darEntrada(id, TipoItem.ACESSORIO, 3, null)).isEqualTo(8);

        org.mockito.Mockito.verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("darSaida deve delegar para o domain service e retornar o saldo")
    void darSaida_deveDelegar() {
        UUID id = UUID.randomUUID();
        when(estoqueService.darSaida(id, TipoItem.ACESSORIO, 3, "Uso em conserto")).thenReturn(2);

        assertThat(service.darSaida(id, TipoItem.ACESSORIO, 3, "Uso em conserto")).isEqualTo(2);
    }

    @Test
    @DisplayName("ajustarQuantidade deve delegar para o domain service e retornar o saldo")
    void ajustarQuantidade_deveDelegar() {
        UUID id = UUID.randomUUID();
        when(estoqueService.ajustar(id, TipoItem.PECA, 0, "Contagem de inventário")).thenReturn(0);

        assertThat(service.ajustarQuantidade(id, TipoItem.PECA, 0, "Contagem de inventário")).isEqualTo(0);
    }
}
