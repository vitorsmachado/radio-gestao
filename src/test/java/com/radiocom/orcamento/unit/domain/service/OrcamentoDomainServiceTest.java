package com.radiocom.orcamento.unit.domain.service;

import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.ordemservico.domain.model.ItemConserto;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.enums.TipoItemConserto;
import com.radiocom.ordemservico.domain.repository.ItemEntradaRepository;
import com.radiocom.ordemservico.domain.service.ItemEntradaDomainService;
import com.radiocom.orcamento.domain.model.Orcamento;
import com.radiocom.orcamento.domain.repository.OrcamentoRepository;
import com.radiocom.orcamento.domain.service.NumeroOrcamentoGenerator;
import com.radiocom.orcamento.domain.service.OrcamentoDomainService;
import com.radiocom.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrcamentoDomainService - Testes Unitários")
class OrcamentoDomainServiceTest {

    @Mock private OrcamentoRepository orcamentoRepository;
    @Mock private ItemEntradaRepository itemEntradaRepository;
    @Mock private ItemEntradaDomainService itemEntradaDomainService;
    @Mock private NumeroOrcamentoGenerator numeroGenerator;

    @InjectMocks
    private OrcamentoDomainService service;

    private UUID osId;
    private UUID clienteId;
    private UUID orcamentoId;
    private Orcamento orcamento;

    @BeforeEach
    void setUp() {
        osId = UUID.randomUUID();
        clienteId = UUID.randomUUID();
        orcamentoId = UUID.randomUUID();
        orcamento = Orcamento.builder().numero("ORC-2026-0001").osId(osId).clienteId(clienteId).build();
        ReflectionTestUtils.setField(orcamento, "id", orcamentoId);
    }

    private ItemEntrada criarItem(UUID donoOsId) {
        ItemEntrada item = ItemEntrada.builder()
                .osId(donoOsId)
                .tipoItem(TipoItem.EQUIPAMENTO)
                .descricao("Rádio Motorola EP450")
                .build();
        ReflectionTestUtils.setField(item, "id", UUID.randomUUID());
        return item;
    }

    @Test
    @DisplayName("buscarPorId deve lançar exceção quando não existe")
    void buscarPorId_deveLancarExcecaoQuandoNaoExiste() {
        when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(orcamentoId))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("criar deve gerar numero e salvar com desconto zero quando não informado")
    void criar_deveGerarNumeroESalvarComDescontoZero() {
        when(numeroGenerator.gerarNumero()).thenReturn("ORC-2026-0042");
        when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(inv -> inv.getArgument(0));

        Orcamento resultado = service.criar(osId, clienteId, null, "À vista", null);

        assertThat(resultado.getNumero()).isEqualTo("ORC-2026-0042");
        assertThat(resultado.getDesconto()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("adicionarItem deve atribuir o orçamento ao item quando pertence à mesma OS")
    void adicionarItem_deveAtribuirQuandoMesmaOS() {
        ItemEntrada item = criarItem(osId);
        when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
        when(itemEntradaRepository.findById(item.getId())).thenReturn(Optional.of(item));

        service.adicionarItem(orcamentoId, item.getId());

        assertThat(item.getOrcamentoId()).isEqualTo(orcamentoId);
        verify(itemEntradaRepository).save(item);
    }

    @Test
    @DisplayName("adicionarItem deve lançar exceção quando item é de outra OS")
    void adicionarItem_deveLancarExcecaoQuandoOutraOS() {
        ItemEntrada item = criarItem(UUID.randomUUID());
        when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
        when(itemEntradaRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.adicionarItem(orcamentoId, item.getId()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("mesma OS");
    }

    @Test
    @DisplayName("removerItem deve limpar o orcamentoId quando pertence a este orçamento")
    void removerItem_deveLimparQuandoPertenceAoOrcamento() {
        ItemEntrada item = criarItem(osId);
        item.atribuirOrcamento(orcamentoId);
        when(itemEntradaRepository.findById(item.getId())).thenReturn(Optional.of(item));

        service.removerItem(orcamentoId, item.getId());

        assertThat(item.getOrcamentoId()).isNull();
        verify(itemEntradaRepository).save(item);
    }

    @Test
    @DisplayName("removerItem deve lançar exceção quando item pertence a outro orçamento")
    void removerItem_deveLancarExcecaoQuandoOutroOrcamento() {
        ItemEntrada item = criarItem(osId);
        item.atribuirOrcamento(UUID.randomUUID());
        when(itemEntradaRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.removerItem(orcamentoId, item.getId()))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("calcularTotal deve somar os itens e subtrair o desconto")
    void calcularTotal_deveSomarESubtrairDesconto() {
        orcamento.setDesconto(new BigDecimal("20.00"));
        ItemEntrada item1 = criarItem(osId);
        item1.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.PECA).descricao("Bateria")
                .quantidade(1).valorUnitario(new BigDecimal("80.00")).build());
        ItemEntrada item2 = criarItem(osId);
        item2.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.MAO_DE_OBRA).descricao("Mão de obra")
                .quantidade(1).valorUnitario(new BigDecimal("50.00")).build());

        when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
        when(itemEntradaRepository.findByOrcamentoId(orcamentoId)).thenReturn(List.of(item1, item2));

        BigDecimal total = service.calcularTotal(orcamentoId);

        assertThat(total).isEqualByComparingTo("110.00");
    }

    @Test
    @DisplayName("calcularTotal nunca deve retornar negativo quando desconto é maior que o total dos itens")
    void calcularTotal_naoDeveRetornarNegativo() {
        orcamento.setDesconto(new BigDecimal("1000.00"));
        ItemEntrada item = criarItem(osId);
        item.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.PECA).descricao("Bateria")
                .quantidade(1).valorUnitario(new BigDecimal("80.00")).build());

        when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
        when(itemEntradaRepository.findByOrcamentoId(orcamentoId)).thenReturn(List.of(item));

        BigDecimal total = service.calcularTotal(orcamentoId);

        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("enviar deve lançar exceção quando não há itens")
    void enviar_deveLancarExcecaoQuandoSemItens() {
        when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
        when(itemEntradaRepository.findByOrcamentoId(orcamentoId)).thenReturn(List.of());

        assertThatThrownBy(() -> service.enviar(orcamentoId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("ao menos um item");
    }

    @Test
    @DisplayName("enviar deve mudar status dos itens AVALIADO e do orçamento")
    void enviar_deveMudarStatusDosItensAvaliadosEOrcamento() {
        ItemEntrada itemAvaliado = criarItem(osId);
        itemAvaliado.avaliar("Capacitor queimado", false);
        ItemEntrada itemJaEnviado = criarItem(osId);
        itemJaEnviado.avaliar("Placa danificada", false);
        itemJaEnviado.enviarParaAutorizacao();

        when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
        when(itemEntradaRepository.findByOrcamentoId(orcamentoId))
                .thenReturn(List.of(itemAvaliado, itemJaEnviado));
        when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(inv -> inv.getArgument(0));

        Orcamento resultado = service.enviar(orcamentoId);

        verify(itemEntradaDomainService).enviarParaAutorizacao(itemAvaliado.getId());
        verify(itemEntradaDomainService, never()).enviarParaAutorizacao(itemJaEnviado.getId());
        assertThat(resultado.getStatus()).isEqualTo(com.radiocom.orcamento.domain.model.enums.StatusOrcamento.ENVIADO);
    }

    @Test
    @DisplayName("reabrir deve voltar orçamento ENVIADO para RASCUNHO")
    void reabrir_deveVoltarParaRascunho() {
        orcamento.enviar();
        when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
        when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(inv -> inv.getArgument(0));

        Orcamento resultado = service.reabrir(orcamentoId);

        assertThat(resultado.getStatus()).isEqualTo(com.radiocom.orcamento.domain.model.enums.StatusOrcamento.RASCUNHO);
    }

    @Test
    @DisplayName("reabrir deve lançar exceção quando orçamento não está ENVIADO")
    void reabrir_deveLancarExcecaoQuandoNaoEnviado() {
        when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));

        assertThatThrownBy(() -> service.reabrir(orcamentoId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== calcularStatusAprovacao =====

    @Test
    @DisplayName("calcularStatusAprovacao deve retornar PENDENTE quando não há itens")
    void calcularStatusAprovacao_deveRetornarPendenteQuandoSemItens() {
        when(itemEntradaRepository.findByOrcamentoId(orcamentoId)).thenReturn(List.of());

        assertThat(service.calcularStatusAprovacao(orcamentoId))
                .isEqualTo(com.radiocom.orcamento.domain.model.enums.StatusAprovacaoOrcamento.PENDENTE);
    }

    @Test
    @DisplayName("calcularStatusAprovacao deve retornar PENDENTE quando só há itens sem defeito")
    void calcularStatusAprovacao_deveRetornarPendenteQuandoApenasSemDefeito() {
        ItemEntrada item = criarItem(osId);
        item.avaliar("Sem defeito", true);
        when(itemEntradaRepository.findByOrcamentoId(orcamentoId)).thenReturn(List.of(item));

        assertThat(service.calcularStatusAprovacao(orcamentoId))
                .isEqualTo(com.radiocom.orcamento.domain.model.enums.StatusAprovacaoOrcamento.PENDENTE);
    }

    @Test
    @DisplayName("calcularStatusAprovacao deve retornar AUTORIZADO quando todos os itens relevantes foram autorizados")
    void calcularStatusAprovacao_deveRetornarAutorizadoQuandoTodosAutorizados() {
        ItemEntrada item1 = criarItem(osId);
        item1.avaliar("Capacitor queimado", false);
        item1.enviarParaAutorizacao();
        item1.autorizar();
        ItemEntrada item2 = criarItem(osId);
        item2.avaliar("Placa danificada", false);
        item2.enviarParaAutorizacao();
        item2.autorizar();
        item2.iniciarFilaManutencao();
        item2.iniciarManutencao();
        item2.concluirManutencao();

        when(itemEntradaRepository.findByOrcamentoId(orcamentoId)).thenReturn(List.of(item1, item2));

        assertThat(service.calcularStatusAprovacao(orcamentoId))
                .isEqualTo(com.radiocom.orcamento.domain.model.enums.StatusAprovacaoOrcamento.AUTORIZADO);
    }

    @Test
    @DisplayName("calcularStatusAprovacao deve retornar NAO_AUTORIZADO quando todos os itens relevantes foram rejeitados")
    void calcularStatusAprovacao_deveRetornarNaoAutorizadoQuandoTodosRejeitados() {
        ItemEntrada item1 = criarItem(osId);
        item1.avaliar("Capacitor queimado", false);
        item1.enviarParaAutorizacao();
        item1.naoAutorizar("Cliente recusou");
        ItemEntrada item2 = criarItem(osId);
        item2.avaliar("Placa danificada", false);
        item2.enviarParaAutorizacao();
        item2.naoAutorizar("Cliente recusou");
        item2.aguardarEntrega();

        when(itemEntradaRepository.findByOrcamentoId(orcamentoId)).thenReturn(List.of(item1, item2));

        assertThat(service.calcularStatusAprovacao(orcamentoId))
                .isEqualTo(com.radiocom.orcamento.domain.model.enums.StatusAprovacaoOrcamento.NAO_AUTORIZADO);
    }

    @Test
    @DisplayName("calcularStatusAprovacao deve retornar PARCIALMENTE_AUTORIZADO quando há mistura de decisões")
    void calcularStatusAprovacao_deveRetornarParcialQuandoMisto() {
        ItemEntrada autorizado = criarItem(osId);
        autorizado.avaliar("Capacitor queimado", false);
        autorizado.enviarParaAutorizacao();
        autorizado.autorizar();
        ItemEntrada rejeitado = criarItem(osId);
        rejeitado.avaliar("Placa danificada", false);
        rejeitado.enviarParaAutorizacao();
        rejeitado.naoAutorizar("Cliente recusou");

        when(itemEntradaRepository.findByOrcamentoId(orcamentoId)).thenReturn(List.of(autorizado, rejeitado));

        assertThat(service.calcularStatusAprovacao(orcamentoId))
                .isEqualTo(com.radiocom.orcamento.domain.model.enums.StatusAprovacaoOrcamento.PARCIALMENTE_AUTORIZADO);
    }

    @Test
    @DisplayName("calcularStatusAprovacao deve retornar PENDENTE quando nenhum item relevante foi decidido ainda")
    void calcularStatusAprovacao_deveRetornarPendenteQuandoNadaDecidido() {
        ItemEntrada item = criarItem(osId);
        item.avaliar("Capacitor queimado", false);
        item.enviarParaAutorizacao();

        when(itemEntradaRepository.findByOrcamentoId(orcamentoId)).thenReturn(List.of(item));

        assertThat(service.calcularStatusAprovacao(orcamentoId))
                .isEqualTo(com.radiocom.orcamento.domain.model.enums.StatusAprovacaoOrcamento.PENDENTE);
    }

    @Test
    @DisplayName("cancelar deve delegar para o domínio e salvar")
    void cancelar_deveDelegarESalvar() {
        when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
        when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(inv -> inv.getArgument(0));

        Orcamento resultado = service.cancelar(orcamentoId, "Cliente desistiu");

        assertThat(resultado.getStatus()).isEqualTo(com.radiocom.orcamento.domain.model.enums.StatusOrcamento.CANCELADO);
    }

    @Test
    @DisplayName("atualizar deve aplicar as condições e salvar")
    void atualizar_deveAplicarCondicoesESalvar() {
        when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
        when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(inv -> inv.getArgument(0));

        Orcamento resultado = service.atualizar(orcamentoId, null, "50% na aprovação", new BigDecimal("15.00"));

        assertThat(resultado.getCondicoesPagamento()).isEqualTo("50% na aprovação");
        assertThat(resultado.getDesconto()).isEqualByComparingTo("15.00");
    }

    @Test
    @DisplayName("buscarOuCriarRascunho deve reaproveitar o orçamento RASCUNHO existente da OS")
    void buscarOuCriarRascunho_deveReaproveitarRascunhoExistente() {
        when(orcamentoRepository.findByOsId(osId)).thenReturn(List.of(orcamento));

        Orcamento resultado = service.buscarOuCriarRascunho(osId, clienteId);

        assertThat(resultado.getId()).isEqualTo(orcamentoId);
        verify(numeroGenerator, never()).gerarNumero();
    }

    @Test
    @DisplayName("buscarOuCriarRascunho deve reabrir o orçamento já enviado em vez de criar um segundo")
    void buscarOuCriarRascunho_deveReabrirQuandoJaEnviado() {
        orcamento.enviar();
        when(orcamentoRepository.findByOsId(osId)).thenReturn(List.of(orcamento));
        when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(inv -> inv.getArgument(0));

        Orcamento resultado = service.buscarOuCriarRascunho(osId, clienteId);

        assertThat(resultado.getId()).isEqualTo(orcamentoId);
        assertThat(resultado.getStatus()).isEqualTo(com.radiocom.orcamento.domain.model.enums.StatusOrcamento.RASCUNHO);
        verify(numeroGenerator, never()).gerarNumero();
    }

    @Test
    @DisplayName("buscarOuCriarRascunho deve criar um novo orçamento quando o único existente foi cancelado")
    void buscarOuCriarRascunho_deveCriarQuandoUnicoFoiCancelado() {
        orcamento.cancelar("Cliente desistiu");
        when(orcamentoRepository.findByOsId(osId)).thenReturn(List.of(orcamento));
        when(numeroGenerator.gerarNumero()).thenReturn("ORC-2026-0099");
        when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(inv -> inv.getArgument(0));

        Orcamento resultado = service.buscarOuCriarRascunho(osId, clienteId);

        assertThat(resultado.getNumero()).isEqualTo("ORC-2026-0099");
        assertThat(resultado.getStatus()).isEqualTo(com.radiocom.orcamento.domain.model.enums.StatusOrcamento.RASCUNHO);
    }

    @Test
    @DisplayName("buscarOuCriarRascunho deve criar um novo orçamento quando a OS não tem nenhum")
    void buscarOuCriarRascunho_deveCriarQuandoNaoHaNenhum() {
        when(orcamentoRepository.findByOsId(osId)).thenReturn(List.of());
        when(numeroGenerator.gerarNumero()).thenReturn("ORC-2026-0099");
        when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(inv -> inv.getArgument(0));

        Orcamento resultado = service.buscarOuCriarRascunho(osId, clienteId);

        assertThat(resultado.getNumero()).isEqualTo("ORC-2026-0099");
    }
}
