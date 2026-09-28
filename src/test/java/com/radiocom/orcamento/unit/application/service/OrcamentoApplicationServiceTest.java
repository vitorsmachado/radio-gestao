package com.radiocom.orcamento.unit.application.service;

import com.radiocom.cliente.application.service.ClienteApplicationService;
import com.radiocom.estoque.application.service.CatalogoModeloService;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.ordemservico.application.dto.MotivoDTO;
import com.radiocom.ordemservico.application.mapper.OrdemServicoMapper;
import com.radiocom.ordemservico.domain.model.ItemConserto;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.enums.TipoItemConserto;
import com.radiocom.ordemservico.domain.service.OrdemServicoDomainService;
import com.radiocom.ordemservico.garantia.domain.service.GarantiaPecaDomainService;
import com.radiocom.orcamento.application.dto.AdicionarItemOrcamentoDTO;
import com.radiocom.orcamento.application.dto.AtualizarOrcamentoDTO;
import com.radiocom.orcamento.application.dto.OrcamentoCreateDTO;
import com.radiocom.orcamento.application.dto.OrcamentoDTO;
import com.radiocom.orcamento.application.dto.OrcamentoResumoDTO;
import com.radiocom.orcamento.application.mapper.OrcamentoMapper;
import com.radiocom.orcamento.application.service.OrcamentoApplicationService;
import com.radiocom.orcamento.domain.model.Orcamento;
import com.radiocom.orcamento.domain.model.enums.StatusOrcamento;
import com.radiocom.orcamento.domain.service.OrcamentoDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrcamentoApplicationService - Testes Unitários")
class OrcamentoApplicationServiceTest {

    @Mock private OrcamentoDomainService orcamentoDomainService;
    @Mock private OrdemServicoDomainService osDomainService;
    @Mock private ClienteApplicationService clienteApplicationService;
    @Mock private CatalogoModeloService catalogoModeloService;
    @Mock private GarantiaPecaDomainService garantiaPecaDomainService;

    private OrcamentoApplicationService service;

    private UUID osId;
    private UUID clienteId;
    private UUID orcamentoId;
    private Orcamento orcamento;

    @BeforeEach
    void setUp() {
        service = new OrcamentoApplicationService(orcamentoDomainService, osDomainService, clienteApplicationService,
                catalogoModeloService, garantiaPecaDomainService, new OrcamentoMapper(), new OrdemServicoMapper());
        osId = UUID.randomUUID();
        clienteId = UUID.randomUUID();
        orcamentoId = UUID.randomUUID();
        orcamento = Orcamento.builder().numero("ORC-2026-0001").osId(osId).clienteId(clienteId).build();
        ReflectionTestUtils.setField(orcamento, "id", orcamentoId);

        lenient().when(orcamentoDomainService.listarItens(orcamentoId)).thenReturn(List.of());
        lenient().when(orcamentoDomainService.calcularTotal(orcamentoId)).thenReturn(BigDecimal.ZERO);
        lenient().when(orcamentoDomainService.calcularStatusAprovacao(orcamentoId))
                .thenReturn(com.radiocom.orcamento.domain.model.enums.StatusAprovacaoOrcamento.PENDENTE);
    }

    @Test
    @DisplayName("criar deve delegar para o domain service")
    void criar_deveDelegar() {
        OrcamentoCreateDTO dto = OrcamentoCreateDTO.builder()
                .osId(osId).clienteId(clienteId).condicoesPagamento("À vista").build();
        when(orcamentoDomainService.criar(osId, clienteId, null, "À vista", null)).thenReturn(orcamento);

        OrcamentoDTO resultado = service.criar(dto);

        assertThat(resultado.getNumero()).isEqualTo("ORC-2026-0001");
    }

    @Test
    @DisplayName("buscarPorId deve incluir itens e valor total")
    void buscarPorId_deveIncluirItensEValorTotal() {
        ItemEntrada item = ItemEntrada.builder()
                .osId(osId).tipoItem(TipoItem.EQUIPAMENTO).descricao("Rádio").build();
        item.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.PECA).descricao("Bateria")
                .quantidade(1).valorUnitario(new BigDecimal("80.00")).build());

        when(orcamentoDomainService.buscarPorId(orcamentoId)).thenReturn(orcamento);
        when(orcamentoDomainService.listarItens(orcamentoId)).thenReturn(List.of(item));
        when(orcamentoDomainService.calcularTotal(orcamentoId)).thenReturn(new BigDecimal("80.00"));
        when(orcamentoDomainService.calcularStatusAprovacao(orcamentoId))
                .thenReturn(com.radiocom.orcamento.domain.model.enums.StatusAprovacaoOrcamento.AUTORIZADO);

        OrcamentoDTO resultado = service.buscarPorId(orcamentoId);

        assertThat(resultado.getItens()).hasSize(1);
        assertThat(resultado.getStatusAprovacao())
                .isEqualTo(com.radiocom.orcamento.domain.model.enums.StatusAprovacaoOrcamento.AUTORIZADO);
        assertThat(resultado.getValorTotal()).isEqualByComparingTo("80.00");
    }

    @Test
    @DisplayName("adicionarItem deve delegar e retornar o orçamento atualizado")
    void adicionarItem_deveDelegarERetornarAtualizado() {
        UUID itemId = UUID.randomUUID();
        when(orcamentoDomainService.buscarPorId(orcamentoId)).thenReturn(orcamento);

        OrcamentoDTO resultado = service.adicionarItem(orcamentoId,
                AdicionarItemOrcamentoDTO.builder().itemEntradaId(itemId).build());

        assertThat(resultado.getNumero()).isEqualTo("ORC-2026-0001");
    }

    @Test
    @DisplayName("enviar deve delegar para o domain service")
    void enviar_deveDelegar() {
        orcamento.enviar();
        when(orcamentoDomainService.enviar(orcamentoId)).thenReturn(orcamento);

        OrcamentoDTO resultado = service.enviar(orcamentoId);

        assertThat(resultado.getStatus()).isEqualTo(StatusOrcamento.ENVIADO);
    }

    @Test
    @DisplayName("reabrir deve delegar para o domain service")
    void reabrir_deveDelegar() {
        orcamento.enviar();
        orcamento.reabrir();
        when(orcamentoDomainService.reabrir(orcamentoId)).thenReturn(orcamento);

        OrcamentoDTO resultado = service.reabrir(orcamentoId);

        assertThat(resultado.getStatus()).isEqualTo(StatusOrcamento.RASCUNHO);
    }

    @Test
    @DisplayName("cancelar deve delegar e registrar o motivo")
    void cancelar_deveDelegarERegistrarMotivo() {
        orcamento.cancelar("Cliente desistiu");
        when(orcamentoDomainService.cancelar(orcamentoId, "Cliente desistiu")).thenReturn(orcamento);

        OrcamentoDTO resultado = service.cancelar(orcamentoId, MotivoDTO.builder().motivo("Cliente desistiu").build());

        assertThat(resultado.getStatus()).isEqualTo(StatusOrcamento.CANCELADO);
    }

    @Test
    @DisplayName("atualizar deve delegar para o domain service")
    void atualizar_deveDelegar() {
        when(orcamentoDomainService.atualizar(orcamentoId, null, "À vista", null)).thenReturn(orcamento);

        OrcamentoDTO resultado = service.atualizar(orcamentoId, AtualizarOrcamentoDTO.builder().condicoesPagamento("À vista").build());

        assertThat(resultado.getNumero()).isEqualTo("ORC-2026-0001");
    }

    @Test
    @DisplayName("listar sem busca deve montar o resumo de cada orçamento da página")
    void listar_semBusca_deveMontarResumo() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Orcamento> pagina = new PageImpl<>(List.of(orcamento), pageable, 1);
        when(orcamentoDomainService.buscar(any(), any(), any(), any(), any())).thenReturn(pagina);
        when(osDomainService.listarPorIds(any())).thenReturn(List.of());
        when(clienteApplicationService.buscarPorIds(any())).thenReturn(List.of());

        Page<OrcamentoResumoDTO> resultado = service.listar(null, null, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getNumero()).isEqualTo("ORC-2026-0001");
    }
}
