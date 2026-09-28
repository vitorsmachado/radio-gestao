package com.radiocom.ordemservico.unit.application.service;

import com.radiocom.cliente.application.dto.ClienteDTO;
import com.radiocom.cliente.application.service.ClienteApplicationService;
import com.radiocom.ordemservico.application.dto.*;
import com.radiocom.ordemservico.application.mapper.OrdemServicoMapper;
import com.radiocom.ordemservico.application.service.OrdemServicoApplicationService;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.OrdemServico;
import com.radiocom.ordemservico.domain.service.OrdemServicoDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrdemServicoApplicationService - Testes Unitários")
class OrdemServicoApplicationServiceTest {

    @Mock private OrdemServicoDomainService osDomainService;
    @Mock private ClienteApplicationService clienteApplicationService;

    private OrdemServicoApplicationService service;

    private UUID osId;
    private UUID clienteId;
    private OrdemServico os;

    @BeforeEach
    void setUp() {
        service = new OrdemServicoApplicationService(osDomainService, clienteApplicationService, new OrdemServicoMapper());
        osId = UUID.randomUUID();
        clienteId = UUID.randomUUID();
        os = OrdemServico.builder().numero("OS-2026-0001").clienteId(clienteId).build();
        ReflectionTestUtils.setField(os, "id", osId);
    }

    @Test
    @DisplayName("criar deve delegar para o domain service")
    void criar_deveDelegar() {
        OrdemServicoCreateDTO dto = OrdemServicoCreateDTO.builder()
                .clienteId(clienteId).solicitante("João da Silva").build();
        when(osDomainService.criar(clienteId, null, null, "João da Silva", null, null, null)).thenReturn(os);

        OrdemServicoDTO resultado = service.criar(dto);

        assertThat(resultado.getNumero()).isEqualTo("OS-2026-0001");
    }

    @Test
    @DisplayName("buscarPorId deve delegar para o domain service")
    void buscarPorId_deveDelegar() {
        when(osDomainService.buscarPorId(osId)).thenReturn(os);

        assertThat(service.buscarPorId(osId).getId()).isEqualTo(osId);
    }

    @Test
    @DisplayName("atualizar deve delegar para o domain service")
    void atualizar_deveDelegar() {
        UUID novoCliente = UUID.randomUUID();
        AtualizarOrdemServicoDTO dto = AtualizarOrdemServicoDTO.builder()
                .clienteId(novoCliente).solicitante("Maria").dataAbertura(java.time.LocalDateTime.now())
                .numeroRelatorio("REL-001").build();
        os.setClienteId(novoCliente);
        os.setSolicitante("Maria");
        os.setNumeroRelatorio("REL-001");
        when(osDomainService.atualizar(osId, novoCliente, null, null, "Maria", dto.getDataAbertura(), null, "REL-001"))
                .thenReturn(os);

        OrdemServicoDTO resultado = service.atualizar(osId, dto);

        assertThat(resultado.getClienteId()).isEqualTo(novoCliente);
        assertThat(resultado.getSolicitante()).isEqualTo("Maria");
        assertThat(resultado.getNumeroRelatorio()).isEqualTo("REL-001");
    }

    @Test
    @DisplayName("listarPorCliente deve delegar para o domain service")
    void listarPorCliente_deveDelegar() {
        when(osDomainService.listarPorCliente(clienteId)).thenReturn(List.of(os));

        List<OrdemServicoDTO> resultado = service.listarPorCliente(clienteId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNumero()).isEqualTo("OS-2026-0001");
    }

    @Test
    @DisplayName("listarHistoricoPorItemEstoque deve resolver numero e status da OS de cada passagem")
    void listarHistoricoPorItemEstoque_deveResolverDadosDaOS() {
        UUID itemEstoqueId = UUID.randomUUID();
        ItemEntrada item = ItemEntrada.builder().osId(osId)
                .status(com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada.ENTREGUE).build();
        when(osDomainService.listarItensPorItemEstoque(itemEstoqueId)).thenReturn(List.of(item));
        when(osDomainService.listarPorIds(List.of(osId))).thenReturn(List.of(os));

        var resultado = service.listarHistoricoPorItemEstoque(itemEstoqueId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getOsNumero()).isEqualTo("OS-2026-0001");
        assertThat(resultado.get(0).getItemStatus()).isEqualTo(com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada.ENTREGUE);
    }

    @Test
    @DisplayName("listarHistoricoPorItemEstoque deve retornar vazio quando o item nunca passou por uma OS")
    void listarHistoricoPorItemEstoque_deveRetornarVazioQuandoSemPassagens() {
        UUID itemEstoqueId = UUID.randomUUID();
        when(osDomainService.listarItensPorItemEstoque(itemEstoqueId)).thenReturn(List.of());

        assertThat(service.listarHistoricoPorItemEstoque(itemEstoqueId)).isEmpty();
    }

    @Test
    @DisplayName("listar sem busca deve passar lista sentinela de clienteId (evita IN vazio)")
    void listar_semBusca_devePassarListaSentinela() {
        var pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        when(osDomainService.buscar(eq(null), anyList(), eq(null), eq(null), eq(pageable)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0));

        service.listar(null, null, null, pageable);

        org.mockito.ArgumentCaptor<List<UUID>> captor = org.mockito.ArgumentCaptor.captor();
        verify(osDomainService).buscar(eq(null), captor.capture(), eq(null), eq(null), eq(pageable));
        assertThat(captor.getValue()).hasSize(1);
    }

    @Test
    @DisplayName("listar com busca sem cliente correspondente deve passar lista sentinela")
    void listar_comBuscaSemClienteEncontrado_devePassarListaSentinela() {
        var pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        when(clienteApplicationService.buscarPorNomeOuDocumento("OS-2026")).thenReturn(List.of());
        when(osDomainService.buscar(eq("OS-2026"), anyList(), any(), any(), eq(pageable)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0));

        service.listar("OS-2026", null, null, pageable);

        org.mockito.ArgumentCaptor<List<UUID>> captor = org.mockito.ArgumentCaptor.captor();
        verify(osDomainService).buscar(eq("OS-2026"), captor.capture(), any(), any(), eq(pageable));
        assertThat(captor.getValue()).hasSize(1);
    }

    @Test
    @DisplayName("listar com busca e cliente correspondente deve repassar os ids encontrados")
    void listar_comBuscaEClienteEncontrado_devePassarIdsEncontrados() {
        var pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        ClienteDTO clienteEncontrado = ClienteDTO.builder().id(clienteId).nomeRazaoSocial("Radio Comunicacao").build();
        when(clienteApplicationService.buscarPorNomeOuDocumento("Radio")).thenReturn(List.of(clienteEncontrado));
        when(osDomainService.buscar(eq("Radio"), anyList(), any(), any(), eq(pageable)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0));

        service.listar("Radio", null, null, pageable);

        org.mockito.ArgumentCaptor<List<UUID>> captor = org.mockito.ArgumentCaptor.captor();
        verify(osDomainService).buscar(eq("Radio"), captor.capture(), any(), any(), eq(pageable));
        assertThat(captor.getValue()).containsExactly(clienteId);
    }

    @Test
    @DisplayName("listar deve resolver nome e documento do cliente na página de resultado")
    void listar_deveResolverNomeEDocumentoDoCliente() {
        var pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        when(osDomainService.buscar(eq(null), anyList(), eq(null), eq(null), eq(pageable)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(os), pageable, 1));
        ClienteDTO cliente = ClienteDTO.builder().id(clienteId).nomeRazaoSocial("Radio Comunicacao").documento("11222333000181").build();
        when(clienteApplicationService.buscarPorIds(List.of(clienteId))).thenReturn(List.of(cliente));

        var resultado = service.listar(null, null, null, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getClienteNome()).isEqualTo("Radio Comunicacao");
        assertThat(resultado.getContent().get(0).getClienteDocumento()).isEqualTo("11222333000181");
    }

    @Test
    @DisplayName("listar deve converter período para início e fim do dia")
    void listar_deveConverterPeriodoParaInicioEFimDoDia() {
        var pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        java.time.LocalDate dataInicial = java.time.LocalDate.of(2026, 3, 1);
        java.time.LocalDate dataFinal = java.time.LocalDate.of(2026, 3, 31);
        when(osDomainService.buscar(eq(null), anyList(), any(), any(), eq(pageable)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0));

        service.listar(null, dataInicial, dataFinal, pageable);

        org.mockito.ArgumentCaptor<java.time.LocalDateTime> inicioCaptor = org.mockito.ArgumentCaptor.forClass(java.time.LocalDateTime.class);
        org.mockito.ArgumentCaptor<java.time.LocalDateTime> fimCaptor = org.mockito.ArgumentCaptor.forClass(java.time.LocalDateTime.class);
        verify(osDomainService).buscar(eq(null), anyList(), inicioCaptor.capture(), fimCaptor.capture(), eq(pageable));
        assertThat(inicioCaptor.getValue()).isEqualTo(java.time.LocalDateTime.of(2026, 3, 1, 0, 0));
        assertThat(fimCaptor.getValue().toLocalDate()).isEqualTo(dataFinal);
        assertThat(fimCaptor.getValue().getHour()).isEqualTo(23);
    }

    @Test
    @DisplayName("confirmarEntrega deve delegar e retornar o recebedor")
    void confirmarEntrega_deveDelegar() {
        os.confirmarEntrega("Maria Souza");
        when(osDomainService.confirmarEntrega(osId, "Maria Souza")).thenReturn(os);

        OrdemServicoDTO resultado = service.confirmarEntrega(osId, ConfirmarEntregaDTO.builder()
                .nomeRecebedor("Maria Souza").build());

        assertThat(resultado.getRecebedorNome()).isEqualTo("Maria Souza");
    }

    @Test
    @DisplayName("moverItem deve delegar para o domain service")
    void moverItem_deveDelegar() {
        UUID itemId = UUID.randomUUID();
        UUID novaOsId = UUID.randomUUID();

        service.moverItem(itemId, MoverItemDTO.builder().novaOsId(novaOsId).build());

        verify(osDomainService).moverItem(itemId, novaOsId);
    }

    @Test
    @DisplayName("separar deve delegar e retornar as novas OS")
    void separar_deveDelegar() {
        UUID item1 = UUID.randomUUID();
        UUID item2 = UUID.randomUUID();
        OrdemServico novaOS1 = OrdemServico.builder().numero("OS-2026-0002").clienteId(clienteId).build();
        OrdemServico novaOS2 = OrdemServico.builder().numero("OS-2026-0003").clienteId(clienteId).build();
        when(osDomainService.separarEmGrupos(eq(osId), eq(List.of(List.of(item1), List.of(item2))), eq("Técnico João")))
                .thenReturn(List.of(novaOS1, novaOS2));

        List<OrdemServicoDTO> resultado = service.separar(osId, SepararOSDTO.builder()
                .grupos(List.of(
                        SepararOSDTO.GrupoItensDTO.builder().itemIds(List.of(item1)).build(),
                        SepararOSDTO.GrupoItensDTO.builder().itemIds(List.of(item2)).build()))
                .solicitante("Técnico João")
                .build());

        assertThat(resultado).extracting(OrdemServicoDTO::getNumero)
                .containsExactly("OS-2026-0002", "OS-2026-0003");
    }

    @Test
    @DisplayName("unir deve delegar e retornar a OS destino")
    void unir_deveDelegar() {
        UUID origemId = UUID.randomUUID();
        when(osDomainService.unir(osId, List.of(origemId))).thenReturn(os);

        OrdemServicoDTO resultado = service.unir(osId, UnirOSDTO.builder().osOrigemIds(List.of(origemId)).build());

        assertThat(resultado.getId()).isEqualTo(osId);
    }

    // ===== listarFilaManutencao / reordenarFila =====

    @Test
    @DisplayName("listarFilaManutencao deve ordenar por bloco, depois posicaoFila asc")
    void listarFilaManutencao_deveOrdenarPorBlocoEPosicaoFila() {
        OrdemServico osEmAvaliacao = criarOS("OS-2026-0100", 100L);
        OrdemServico osManutencao = criarOS("OS-2026-0101", 100L);
        OrdemServico osAvaliacaoPrimeira = criarOS("OS-2026-0102", 1L);
        OrdemServico osAvaliacaoSegunda = criarOS("OS-2026-0103", 2L);
        OrdemServico osAvaliacaoTerceira = criarOS("OS-2026-0104", 3L);
        OrdemServico osAguardandoPecaConfirmado = criarOS("OS-2026-0105", 100L);

        ItemEntrada itemEmAvaliacao = criarItem(osEmAvaliacao.getId(), com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada.EM_AVALIACAO);
        ItemEntrada itemManutencao = criarItem(osManutencao.getId(), com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada.PENDENTE_MANUTENCAO);
        ItemEntrada itemAvaliacaoPrimeira = criarItem(osAvaliacaoPrimeira.getId(), com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada.PENDENTE_AVALIACAO);
        ItemEntrada itemAvaliacaoSegunda = criarItem(osAvaliacaoSegunda.getId(), com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada.PENDENTE_AVALIACAO);
        ItemEntrada itemAvaliacaoTerceira = criarItem(osAvaliacaoTerceira.getId(), com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada.PENDENTE_AVALIACAO);
        ItemEntrada itemAguardandoPecaConfirmado = criarItem(osAguardandoPecaConfirmado.getId(), com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada.AGUARDANDO_PECA);
        itemAguardandoPecaConfirmado.setConfirmadoAguardandoPecaEm(java.time.LocalDateTime.now());

        List<ItemEntrada> todosOsItens = List.of(itemAvaliacaoTerceira, itemAguardandoPecaConfirmado, itemManutencao,
                itemAvaliacaoPrimeira, itemEmAvaliacao, itemAvaliacaoSegunda);
        when(osDomainService.listarItensNaFilaManutencao()).thenReturn(todosOsItens);

        List<OrdemServico> todasAsOS = List.of(osEmAvaliacao, osManutencao, osAvaliacaoPrimeira,
                osAvaliacaoSegunda, osAvaliacaoTerceira, osAguardandoPecaConfirmado);
        when(osDomainService.listarPorIds(anyList())).thenReturn(todasAsOS);
        when(clienteApplicationService.buscarPorIds(anyList())).thenReturn(List.of());

        List<FilaManutencaoOSDTO> fila = service.listarFilaManutencao();

        assertThat(fila).extracting(FilaManutencaoOSDTO::getOsNumero).containsExactly(
                "OS-2026-0100", // em avaliação — bloco 1
                "OS-2026-0101", // pendente manutenção — bloco 2
                "OS-2026-0102", // pendente avaliação, posicaoFila 1
                "OS-2026-0103", // pendente avaliação, posicaoFila 2
                "OS-2026-0104", // pendente avaliação, posicaoFila 3
                "OS-2026-0105"  // aguardando peça confirmado — sempre por último
        );
    }

    @Test
    @DisplayName("reordenarFila com SUBIR deve trocar de posição com a OS anterior do mesmo bloco")
    void reordenarFila_subir_deveTrocarComAnterior() {
        OrdemServico osPrimeira = criarOS("OS-2026-0200", 1L);
        OrdemServico osSegunda = criarOS("OS-2026-0201", 2L);

        ItemEntrada itemPrimeira = criarItem(osPrimeira.getId(), com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada.PENDENTE_AVALIACAO);
        ItemEntrada itemSegunda = criarItem(osSegunda.getId(), com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada.PENDENTE_AVALIACAO);

        when(osDomainService.listarItensNaFilaManutencao()).thenReturn(List.of(itemPrimeira, itemSegunda));
        when(osDomainService.listarPorIds(anyList())).thenReturn(List.of(osPrimeira, osSegunda));
        when(clienteApplicationService.buscarPorIds(anyList())).thenReturn(List.of());

        List<FilaManutencaoOSDTO> fila = service.reordenarFila(osSegunda.getId(),
                ReordenarFilaDTO.builder().acao(AcaoReordenarFila.SUBIR).build());

        assertThat(fila).extracting(FilaManutencaoOSDTO::getOsNumero).containsExactly("OS-2026-0201", "OS-2026-0200");
        verify(osDomainService).salvarTodas(anyList());
    }

    private OrdemServico criarOS(String numero, long posicaoFila) {
        OrdemServico novaOs = OrdemServico.builder().numero(numero).clienteId(UUID.randomUUID()).posicaoFila(posicaoFila).build();
        ReflectionTestUtils.setField(novaOs, "id", UUID.randomUUID());
        return novaOs;
    }

    private ItemEntrada criarItem(UUID osIdDoItem, com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada status) {
        ItemEntrada item = ItemEntrada.builder()
                .osId(osIdDoItem)
                .tipoItem(com.radiocom.estoque.domain.model.enums.TipoItem.EQUIPAMENTO)
                .descricao("Rádio")
                .status(status)
                .build();
        ReflectionTestUtils.setField(item, "id", UUID.randomUUID());
        return item;
    }
}
