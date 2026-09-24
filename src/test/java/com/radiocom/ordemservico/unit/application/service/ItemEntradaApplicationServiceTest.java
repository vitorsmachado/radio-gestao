package com.radiocom.ordemservico.unit.application.service;

import com.radiocom.estoque.application.service.CatalogoModeloService;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.ordemservico.application.dto.*;
import com.radiocom.ordemservico.application.mapper.OrdemServicoMapper;
import com.radiocom.ordemservico.application.service.ItemEntradaApplicationService;
import com.radiocom.ordemservico.domain.event.ItemAvaliadoEvent;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.enums.ResultadoAvaliacao;
import com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada;
import com.radiocom.ordemservico.domain.model.enums.TipoItemConserto;
import com.radiocom.ordemservico.domain.service.ItemEntradaDomainService;
import com.radiocom.ordemservico.garantia.domain.service.GarantiaPecaDomainService;
import com.radiocom.ordemservico.sugestao.application.service.SugestaoTextoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemEntradaApplicationService - Testes Unitários")
class ItemEntradaApplicationServiceTest {

    @Mock private ItemEntradaDomainService itemDomainService;
    @Mock private CatalogoModeloService catalogoModeloService;
    @Mock private SugestaoTextoService sugestaoTextoService;
    @Mock private GarantiaPecaDomainService garantiaPecaDomainService;
    @Mock private ApplicationEventPublisher eventPublisher;

    private ItemEntradaApplicationService service;

    private UUID itemId;
    private ItemEntrada item;

    @BeforeEach
    void setUp() {
        service = new ItemEntradaApplicationService(itemDomainService, new OrdemServicoMapper(), catalogoModeloService, sugestaoTextoService, garantiaPecaDomainService, eventPublisher);
        itemId = UUID.randomUUID();
        item = ItemEntrada.builder()
                .osId(UUID.randomUUID())
                .tipoItem(TipoItem.EQUIPAMENTO)
                .descricao("Rádio Motorola EP450")
                .build();
        ReflectionTestUtils.setField(item, "id", itemId);
    }

    @Test
    @DisplayName("criar deve mapear o DTO e delegar para o domain service")
    void criar_deveMapearEDelegar() {
        ItemEntradaCreateDTO dto = ItemEntradaCreateDTO.builder()
                .osId(item.getOsId())
                .tipoItem(TipoItem.EQUIPAMENTO)
                .descricao("Rádio Motorola EP450")
                .build();
        when(itemDomainService.criar(any(ItemEntrada.class))).thenReturn(item);

        ItemEntradaDTO resultado = service.criar(dto);

        assertThat(resultado.getDescricao()).isEqualTo("Rádio Motorola EP450");
    }

    @Test
    @DisplayName("criar deve mapear o codigoCliente informado")
    void criar_deveMapearCodigoCliente() {
        ItemEntradaCreateDTO dto = ItemEntradaCreateDTO.builder()
                .osId(item.getOsId())
                .tipoItem(TipoItem.EQUIPAMENTO)
                .descricao("Rádio Motorola EP450")
                .codigoCliente("TAG-CLIENTE-042")
                .build();
        ItemEntrada itemComCodigo = ItemEntrada.builder()
                .osId(item.getOsId())
                .tipoItem(TipoItem.EQUIPAMENTO)
                .descricao("Rádio Motorola EP450")
                .codigoCliente("TAG-CLIENTE-042")
                .build();
        when(itemDomainService.criar(any(ItemEntrada.class))).thenReturn(itemComCodigo);

        ItemEntradaDTO resultado = service.criar(dto);

        assertThat(resultado.getCodigoCliente()).isEqualTo("TAG-CLIENTE-042");
    }

    @Test
    @DisplayName("avaliar deve delegar para o domain service")
    void avaliar_deveDelegar() {
        item.avaliar("Capacitor queimado", false);
        when(itemDomainService.avaliar(itemId, "Capacitor queimado", false)).thenReturn(item);

        ItemEntradaDTO resultado = service.avaliar(itemId,
                AvaliarItemDTO.builder().avaliacaoTecnica("Capacitor queimado").semDefeito(false).build());

        assertThat(resultado.getStatus()).isEqualTo(StatusItemEntrada.AVALIADO);
    }

    @Test
    @DisplayName("autorizar deve delegar para o domain service")
    void autorizar_deveDelegar() {
        item.avaliar("Capacitor queimado", false);
        item.enviarParaAutorizacao();
        item.autorizar();
        item.iniciarFilaManutencao();
        when(itemDomainService.autorizar(itemId)).thenReturn(item);

        ItemEntradaDTO resultado = service.autorizar(itemId);

        assertThat(resultado.getStatus()).isEqualTo(StatusItemEntrada.PENDENTE_MANUTENCAO);
    }

    @Test
    @DisplayName("marcarAguardandoPeca deve delegar para o domain service")
    void marcarAguardandoPeca_deveDelegar() {
        item.avaliar("Capacitor queimado", false);
        item.enviarParaAutorizacao();
        item.autorizar();
        item.iniciarFilaManutencao();
        item.iniciarManutencao();
        item.marcarAguardandoPeca();
        when(itemDomainService.marcarAguardandoPeca(itemId)).thenReturn(item);

        ItemEntradaDTO resultado = service.marcarAguardandoPeca(itemId);

        assertThat(resultado.getStatus()).isEqualTo(StatusItemEntrada.AGUARDANDO_PECA);
    }

    @Test
    @DisplayName("naoAutorizar deve delegar e registrar o motivo")
    void naoAutorizar_deveDelegar() {
        item.avaliar("Placa danificada", false);
        item.enviarParaAutorizacao();
        item.naoAutorizar("Cliente recusou");
        when(itemDomainService.naoAutorizar(itemId, "Cliente recusou")).thenReturn(item);

        ItemEntradaDTO resultado = service.naoAutorizar(itemId, MotivoDTO.builder().motivo("Cliente recusou").build());

        assertThat(resultado.getMotivoNaoAutorizado()).isEqualTo("Cliente recusou");
    }

    @Test
    @DisplayName("adicionarItemConserto deve mapear o DTO e delegar")
    void adicionarItemConserto_deveMapearEDelegar() {
        ItemConsertoCreateDTO dto = ItemConsertoCreateDTO.builder()
                .tipo(TipoItemConserto.PECA)
                .descricao("Bateria BP-227")
                .quantidade(1)
                .valorUnitario(new BigDecimal("80.00"))
                .build();
        item.adicionarItemConserto(com.radiocom.ordemservico.domain.model.ItemConserto.builder()
                .tipo(TipoItemConserto.PECA).descricao("Bateria BP-227")
                .quantidade(1).valorUnitario(new BigDecimal("80.00")).build());
        when(itemDomainService.adicionarItemConserto(
                org.mockito.ArgumentMatchers.eq(itemId),
                any(com.radiocom.ordemservico.domain.model.ItemConserto.class))).thenReturn(item);

        ItemEntradaDTO resultado = service.adicionarItemConserto(itemId, dto);

        assertThat(resultado.getItensConserto()).hasSize(1);
        assertThat(resultado.getValorTotalConserto()).isEqualByComparingTo("80.00");
    }

    @Test
    @DisplayName("atualizarValorItemConserto deve delegar para o domain service")
    void atualizarValorItemConserto_deveDelegar() {
        UUID itemConsertoId = UUID.randomUUID();
        com.radiocom.ordemservico.domain.model.ItemConserto conserto = com.radiocom.ordemservico.domain.model.ItemConserto.builder()
                .tipo(TipoItemConserto.PECA).descricao("Bateria BP-227")
                .quantidade(1).valorUnitario(new BigDecimal("45.00")).build();
        item.adicionarItemConserto(conserto);
        AtualizarValorItemConsertoDTO dto = AtualizarValorItemConsertoDTO.builder()
                .valorUnitario(new BigDecimal("45.00")).build();
        when(itemDomainService.atualizarValorItemConserto(itemId, itemConsertoId, new BigDecimal("45.00")))
                .thenReturn(item);

        ItemEntradaDTO resultado = service.atualizarValorItemConserto(itemId, itemConsertoId, dto);

        assertThat(resultado.getItensConserto()).hasSize(1);
    }

    @Test
    @DisplayName("remover deve delegar para o domain service")
    void remover_deveDelegar() {
        service.remover(itemId);

        verify(itemDomainService).remover(itemId);
    }

    @Test
    @DisplayName("salvarAvaliacaoTecnica deve publicar ItemAvaliadoEvent quando resultado não é SEM_DEFEITO")
    void salvarAvaliacaoTecnica_devePublicarEventoQuandoNaoSemDefeito() {
        item.iniciarAvaliacao();
        item.salvarAvaliacaoTecnica(ResultadoAvaliacao.ORCAMENTO, null, "Capacitor queimado", null, null, null, false);
        SalvarAvaliacaoTecnicaDTO dto = SalvarAvaliacaoTecnicaDTO.builder()
                .resultado(ResultadoAvaliacao.ORCAMENTO).defeitoEncontrado("Capacitor queimado").build();
        when(itemDomainService.salvarAvaliacaoTecnica(itemId, ResultadoAvaliacao.ORCAMENTO, null,
                "Capacitor queimado", null, null, null, null)).thenReturn(item);

        service.salvarAvaliacaoTecnica(itemId, dto);

        verify(eventPublisher).publishEvent(any(ItemAvaliadoEvent.class));
    }

    @Test
    @DisplayName("salvarAvaliacaoTecnica não deve publicar evento quando resultado é SEM_DEFEITO")
    void salvarAvaliacaoTecnica_naoDevePublicarEventoQuandoSemDefeito() {
        item.iniciarAvaliacao();
        item.salvarAvaliacaoTecnica(ResultadoAvaliacao.SEM_DEFEITO, null, null, null, null, null, false);
        SalvarAvaliacaoTecnicaDTO dto = SalvarAvaliacaoTecnicaDTO.builder()
                .resultado(ResultadoAvaliacao.SEM_DEFEITO).build();
        when(itemDomainService.salvarAvaliacaoTecnica(itemId, ResultadoAvaliacao.SEM_DEFEITO, null,
                null, null, null, null, null)).thenReturn(item);

        service.salvarAvaliacaoTecnica(itemId, dto);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("salvarAvaliacaoTecnica não deve publicar orçamento quando item foi coberto por garantia")
    void salvarAvaliacaoTecnica_naoDevePublicarOrcamentoQuandoGarantia() {
        UUID garantiaPecaId = UUID.randomUUID();
        item.iniciarAvaliacao();
        item.salvarAvaliacaoTecnica(ResultadoAvaliacao.AJUSTE, null, "Bateria fraca", null, null, null, true);
        SalvarAvaliacaoTecnicaDTO dto = SalvarAvaliacaoTecnicaDTO.builder()
                .resultado(ResultadoAvaliacao.AJUSTE).defeitoEncontrado("Bateria fraca").garantiaPecaId(garantiaPecaId).build();
        when(itemDomainService.salvarAvaliacaoTecnica(itemId, ResultadoAvaliacao.AJUSTE, null,
                "Bateria fraca", null, null, null, garantiaPecaId)).thenReturn(item);

        service.salvarAvaliacaoTecnica(itemId, dto);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("salvarAvaliacaoTecnica deve publicar GarantiaConflitoEvent quando equipamento tem outra cobertura ativa")
    void salvarAvaliacaoTecnica_devePublicarConflitoQuandoHaOutraCobertura() {
        UUID itemEstoqueId = UUID.randomUUID();
        ReflectionTestUtils.setField(item, "itemEstoqueId", itemEstoqueId);
        item.salvarAvaliacaoTecnica(ResultadoAvaliacao.ORCAMENTO, null, "Tela quebrada", null, null, null, false);
        SalvarAvaliacaoTecnicaDTO dto = SalvarAvaliacaoTecnicaDTO.builder()
                .resultado(ResultadoAvaliacao.ORCAMENTO).defeitoEncontrado("Tela quebrada").build();
        when(itemDomainService.salvarAvaliacaoTecnica(itemId, ResultadoAvaliacao.ORCAMENTO, null,
                "Tela quebrada", null, null, null, null)).thenReturn(item);
        when(garantiaPecaDomainService.listarCoberturaAtiva(itemEstoqueId))
                .thenReturn(List.of(com.radiocom.ordemservico.garantia.domain.model.GarantiaPeca.builder().build()));

        service.salvarAvaliacaoTecnica(itemId, dto);

        verify(eventPublisher).publishEvent(any(ItemAvaliadoEvent.class));
        verify(eventPublisher).publishEvent(any(com.radiocom.ordemservico.domain.event.GarantiaConflitoEvent.class));
    }
}
