package com.radiocom.ordemservico.unit.domain.model;

import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.ordemservico.domain.model.ItemConserto;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada;
import com.radiocom.ordemservico.domain.model.enums.TipoItemConserto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ItemEntrada - Testes de Domínio")
class ItemEntradaTest {

    private ItemEntrada item;
    private UUID osOrigemId;

    @BeforeEach
    void setUp() {
        osOrigemId = UUID.randomUUID();
        item = ItemEntrada.builder()
                .osId(osOrigemId)
                .tipoItem(TipoItem.EQUIPAMENTO)
                .descricao("Rádio Motorola EP450")
                .numeroSerie("NS-001")
                .build();
    }

    private ItemConserto criarItemConserto() {
        return ItemConserto.builder()
                .tipo(TipoItemConserto.PECA)
                .descricao("Bateria BP-227")
                .quantidade(1)
                .valorUnitario(new BigDecimal("80.00"))
                .build();
    }

    /** Leva o item até AUTORIZADO, passando pelo fluxo completo de decisão. */
    private void avaliarEAutorizar() {
        item.avaliar("Capacitor queimado", false);
        item.enviarParaAutorizacao();
        item.autorizar();
    }

    // ===== avaliar / atualizarAvaliacao =====

    @Test
    @DisplayName("novo item deve nascer PENDENTE_AVALIACAO")
    void novoItem_deveNascerPendenteAvaliacao() {
        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.PENDENTE_AVALIACAO);
    }

    @Test
    @DisplayName("avaliar deve mudar status para AVALIADO e registrar o laudo")
    void avaliar_deveMudarStatusERegistrarLaudo() {
        item.avaliar("Capacitor queimado", false);

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.AVALIADO);
        assertThat(item.getAvaliacaoTecnica()).isEqualTo("Capacitor queimado");
        assertThat(item.isSemDefeito()).isFalse();
    }

    @Test
    @DisplayName("avaliar deve lançar exceção quando já avaliado")
    void avaliar_deveLancarExcecaoQuandoJaAvaliado() {
        item.avaliar("Capacitor queimado", false);

        assertThatThrownBy(() -> item.avaliar("Outro laudo", false))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("salvarAvaliacaoTecnica com SEM_DEFEITO deve ir direto para AGUARDANDO_ENTREGA")
    void salvarAvaliacaoTecnica_semDefeito_devePularParaAguardandoEntrega() {
        item.salvarAvaliacaoTecnica(com.radiocom.ordemservico.domain.model.enums.ResultadoAvaliacao.SEM_DEFEITO,
                null, null, null, null, null, false, java.util.Set.of());

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.AGUARDANDO_ENTREGA);
        assertThat(item.isSemDefeito()).isTrue();
    }

    @Test
    @DisplayName("salvarAvaliacaoTecnica com defeito deve parar em AVALIADO")
    void salvarAvaliacaoTecnica_comDefeito_deveParaEmAvaliado() {
        item.salvarAvaliacaoTecnica(com.radiocom.ordemservico.domain.model.enums.ResultadoAvaliacao.ORCAMENTO,
                null, "Capacitor queimado", null, null, null, false, java.util.Set.of());

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.AVALIADO);
        assertThat(item.isSemDefeito()).isFalse();
    }

    @Test
    @DisplayName("salvarAvaliacaoTecnica deve ir direto pra AGUARDANDO_ENTREGA quando toda peça está coberta por garantia")
    void salvarAvaliacaoTecnica_todaPecaCoberta_devePularParaAguardandoEntrega() {
        UUID pecaId = UUID.randomUUID();
        item.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.PECA).itemEstoqueId(pecaId).descricao("Bateria BP-227")
                .quantidade(1).valorUnitario(BigDecimal.ZERO).build());

        item.salvarAvaliacaoTecnica(com.radiocom.ordemservico.domain.model.enums.ResultadoAvaliacao.AJUSTE,
                null, "Bateria fraca", null, null, null, true, java.util.Set.of(pecaId));

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.AGUARDANDO_ENTREGA);
        assertThat(item.isGarantia()).isTrue();
    }

    @Test
    @DisplayName("salvarAvaliacaoTecnica não deve pular quando só parte das peças está coberta por garantia")
    void salvarAvaliacaoTecnica_pecaParcialCoberta_naoDevePular() {
        UUID pecaId = UUID.randomUUID();
        UUID outraPecaId = UUID.randomUUID();
        item.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.PECA).itemEstoqueId(pecaId).descricao("Bateria BP-227")
                .quantidade(1).valorUnitario(BigDecimal.ZERO).build());
        item.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.PECA).itemEstoqueId(outraPecaId).descricao("Antena UHF")
                .quantidade(1).valorUnitario(new BigDecimal("40.00")).build());

        item.salvarAvaliacaoTecnica(com.radiocom.ordemservico.domain.model.enums.ResultadoAvaliacao.ORCAMENTO,
                null, "Bateria fraca e antena danificada", null, null, null, true, java.util.Set.of(pecaId));

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.AVALIADO);
    }

    @Test
    @DisplayName("atualizarAvaliacaoCompleta deve editar os campos sem mudar o status")
    void atualizarAvaliacaoCompleta_deveEditarSemMudarStatus() {
        avaliarEAutorizar();

        item.atualizarAvaliacaoCompleta(com.radiocom.ordemservico.domain.model.enums.ResultadoAvaliacao.AJUSTE,
                "Trocar fusível", "Fusível queimado", "Sobrecarga", "Substituir fusível", "Cliente avisado", false);

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.AUTORIZADO);
        assertThat(item.getResultadoAvaliacao()).isEqualTo(com.radiocom.ordemservico.domain.model.enums.ResultadoAvaliacao.AJUSTE);
        assertThat(item.getDetalheAjuste()).isEqualTo("Trocar fusível");
        assertThat(item.getDefeitoEncontrado()).isEqualTo("Fusível queimado");
        assertThat(item.getCausaDefeito()).isEqualTo("Sobrecarga");
        assertThat(item.getSolucaoRecomendada()).isEqualTo("Substituir fusível");
        assertThat(item.getObservacoesTecnicas()).isEqualTo("Cliente avisado");
    }

    @Test
    @DisplayName("atualizarAvaliacaoCompleta deve lançar exceção quando o item já foi entregue")
    void atualizarAvaliacaoCompleta_deveLancarExcecaoQuandoEntregue() {
        item.avaliar("Sem defeito encontrado", true);
        item.aguardarEntrega();
        item.entregar();

        assertThatThrownBy(() -> item.atualizarAvaliacaoCompleta(
                com.radiocom.ordemservico.domain.model.enums.ResultadoAvaliacao.SEM_DEFEITO,
                null, null, null, null, null, false))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("atualizarAvaliacao deve permitir editar o laudo mesmo depois de autorizado")
    void atualizarAvaliacao_devePermitirEditarAposAutorizado() {
        avaliarEAutorizar();

        item.atualizarAvaliacao("Capacitor e fonte queimados", false);

        assertThat(item.getAvaliacaoTecnica()).isEqualTo("Capacitor e fonte queimados");
        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.AUTORIZADO);
    }

    @Test
    @DisplayName("atualizarAvaliacao deve lançar exceção quando já entregue")
    void atualizarAvaliacao_deveLancarExcecaoQuandoEntregue() {
        item.avaliar("Sem defeito", true);
        item.aguardarEntrega();
        item.entregar();

        assertThatThrownBy(() -> item.atualizarAvaliacao("Novo laudo", false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("já foi entregue");
    }

    // ===== enviar para autorização =====

    @Test
    @DisplayName("enviarParaAutorizacao deve mudar status para PENDENTE_AUTORIZACAO")
    void enviarParaAutorizacao_deveMudarStatus() {
        item.avaliar("Capacitor queimado", false);

        item.enviarParaAutorizacao();

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.PENDENTE_AUTORIZACAO);
    }

    @Test
    @DisplayName("enviarParaAutorizacao deve lançar exceção quando ainda não avaliado")
    void enviarParaAutorizacao_deveLancarExcecaoQuandoNaoAvaliado() {
        assertThatThrownBy(item::enviarParaAutorizacao)
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== autorizar / naoAutorizar =====

    @Test
    @DisplayName("autorizar deve mudar status para AUTORIZADO")
    void autorizar_deveMudarStatus() {
        avaliarEAutorizar();

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.AUTORIZADO);
    }

    @Test
    @DisplayName("autorizar deve lançar exceção quando ainda pendente de avaliação")
    void autorizar_deveLancarExcecaoQuandoPendente() {
        assertThatThrownBy(item::autorizar)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("naoAutorizar deve mudar status e registrar motivo")
    void naoAutorizar_deveMudarStatusERegistrarMotivo() {
        item.avaliar("Placa danificada, conserto caro", false);
        item.enviarParaAutorizacao();

        item.naoAutorizar("Cliente preferiu não consertar");

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.NAO_AUTORIZADO);
        assertThat(item.getMotivoNaoAutorizado()).isEqualTo("Cliente preferiu não consertar");
    }

    @Test
    @DisplayName("naoAutorizar deve funcionar mesmo depois de já estar na fila de manutenção")
    void naoAutorizar_deveFuncionarAposFilaDeManutencao() {
        avaliarEAutorizar();
        item.iniciarFilaManutencao();

        item.naoAutorizar("Cliente desistiu antes do reparo começar");

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.NAO_AUTORIZADO);
    }

    @Test
    @DisplayName("autorizar deve limpar motivoNaoAutorizado e permitir reverter uma decisão")
    void autorizar_devePermitirReverterNaoAutorizado() {
        item.avaliar("Placa danificada", false);
        item.enviarParaAutorizacao();
        item.naoAutorizar("Cliente recusou");

        item.autorizar();

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.AUTORIZADO);
        assertThat(item.getMotivoNaoAutorizado()).isNull();
    }

    // ===== fila de manutenção / aguardando peça =====

    @Test
    @DisplayName("iniciarFilaManutencao deve mudar status quando autorizado")
    void iniciarFilaManutencao_deveMudarStatus() {
        avaliarEAutorizar();

        item.iniciarFilaManutencao();

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.PENDENTE_MANUTENCAO);
    }

    @Test
    @DisplayName("marcarAguardandoPeca deve mudar status quando autorizado")
    void marcarAguardandoPeca_deveMudarStatusQuandoAutorizado() {
        avaliarEAutorizar();

        item.marcarAguardandoPeca();

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.AGUARDANDO_PECA);
    }

    @Test
    @DisplayName("marcarAguardandoPeca deve funcionar durante o reparo (peça faltou no meio)")
    void marcarAguardandoPeca_deveFuncionarDuranteReparo() {
        avaliarEAutorizar();
        item.iniciarFilaManutencao();
        item.iniciarManutencao();

        item.marcarAguardandoPeca();

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.AGUARDANDO_PECA);
    }

    @Test
    @DisplayName("retomarAposPeca deve voltar para PENDENTE_MANUTENCAO")
    void retomarAposPeca_deveVoltarParaPendenteManutencao() {
        avaliarEAutorizar();
        item.marcarAguardandoPeca();

        item.retomarAposPeca();

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.PENDENTE_MANUTENCAO);
    }

    @Test
    @DisplayName("iniciarManutencao deve mudar status para EM_MANUTENCAO")
    void iniciarManutencao_deveMudarStatus() {
        avaliarEAutorizar();
        item.iniciarFilaManutencao();

        item.iniciarManutencao();

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.EM_MANUTENCAO);
    }

    @Test
    @DisplayName("iniciarManutencao deve lançar exceção quando não está na fila")
    void iniciarManutencao_deveLancarExcecaoQuandoNaoNaFila() {
        avaliarEAutorizar();

        assertThatThrownBy(item::iniciarManutencao)
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== conclusão do reparo e entrega =====

    @Test
    @DisplayName("concluirManutencao deve ir direto para AGUARDANDO_ENTREGA")
    void concluirManutencao_deveIrDiretoParaAguardandoEntrega() {
        avaliarEAutorizar();
        item.iniciarFilaManutencao();
        item.iniciarManutencao();

        item.concluirManutencao();

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.AGUARDANDO_ENTREGA);
    }

    @Test
    @DisplayName("aguardarEntrega ainda deve funcionar num item legado parado em MANUTENCAO_CONCLUIDA")
    void aguardarEntrega_deveFuncionarParaLegadoManutencaoConcluida() {
        avaliarEAutorizar();
        item.iniciarFilaManutencao();
        item.iniciarManutencao();
        ReflectionTestUtils.setField(item, "status", StatusItemEntrada.MANUTENCAO_CONCLUIDA);

        item.aguardarEntrega();

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.AGUARDANDO_ENTREGA);
    }

    @Test
    @DisplayName("aguardarEntrega deve funcionar direto para item sem defeito")
    void aguardarEntrega_deveFuncionarParaSemDefeito() {
        item.avaliar("Sem defeito encontrado", true);

        item.aguardarEntrega();

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.AGUARDANDO_ENTREGA);
    }

    @Test
    @DisplayName("aguardarEntrega deve lançar exceção ao sair de AVALIADO com defeito (precisa autorização antes)")
    void aguardarEntrega_deveLancarExcecaoQuandoAvaliadoComDefeito() {
        item.avaliar("Capacitor queimado", false);

        assertThatThrownBy(item::aguardarEntrega)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Envie para autorização");
    }

    @Test
    @DisplayName("aguardarEntrega deve funcionar para item não autorizado")
    void aguardarEntrega_deveFuncionarParaNaoAutorizado() {
        item.avaliar("Placa danificada", false);
        item.enviarParaAutorizacao();
        item.naoAutorizar("Cliente recusou");

        item.aguardarEntrega();

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.AGUARDANDO_ENTREGA);
    }

    @Test
    @DisplayName("entregar deve mudar status quando aguardando entrega")
    void entregar_deveMudarStatusQuandoAguardandoEntrega() {
        item.avaliar("Sem defeito encontrado", true);
        item.aguardarEntrega();

        item.entregar();

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.ENTREGUE);
    }

    @Test
    @DisplayName("entregar deve lançar exceção quando ainda em manutenção")
    void entregar_deveLancarExcecaoQuandoEmManutencao() {
        avaliarEAutorizar();
        item.iniciarFilaManutencao();
        item.iniciarManutencao();

        assertThatThrownBy(item::entregar)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("aguardarEntrega deve lançar exceção quando aguardando peça")
    void aguardarEntrega_deveLancarExcecaoQuandoAguardandoPeca() {
        avaliarEAutorizar();
        item.marcarAguardandoPeca();

        assertThatThrownBy(item::aguardarEntrega)
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== mover entre OS =====

    @Test
    @DisplayName("moverParaOS deve reatribuir a OS do item")
    void moverParaOS_deveReatribuirOS() {
        UUID novaOsId = UUID.randomUUID();

        item.moverParaOS(novaOsId);

        assertThat(item.getOsId()).isEqualTo(novaOsId);
    }

    @Test
    @DisplayName("moverParaOS deve lançar exceção quando item já entregue")
    void moverParaOS_deveLancarExcecaoQuandoEntregue() {
        item.avaliar("Sem defeito", true);
        item.aguardarEntrega();
        item.entregar();

        assertThatThrownBy(() -> item.moverParaOS(UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("moverParaOS deve lançar exceção quando nova OS é nula")
    void moverParaOS_deveLancarExcecaoQuandoNovaOSNula() {
        assertThatThrownBy(() -> item.moverParaOS(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ===== agrupamento em orçamento =====

    @Test
    @DisplayName("atribuirOrcamento deve definir o orcamentoId")
    void atribuirOrcamento_deveDefinirOrcamentoId() {
        UUID orcamentoId = UUID.randomUUID();

        item.atribuirOrcamento(orcamentoId);

        assertThat(item.getOrcamentoId()).isEqualTo(orcamentoId);
    }

    @Test
    @DisplayName("atribuirOrcamento deve lançar exceção quando orcamentoId é nulo")
    void atribuirOrcamento_deveLancarExcecaoQuandoNulo() {
        assertThatThrownBy(() -> item.atribuirOrcamento(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("atribuirOrcamento deve lançar exceção quando item já entregue")
    void atribuirOrcamento_deveLancarExcecaoQuandoEntregue() {
        item.avaliar("Sem defeito", true);
        item.aguardarEntrega();
        item.entregar();

        assertThatThrownBy(() -> item.atribuirOrcamento(UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("removerDoOrcamento deve limpar o orcamentoId")
    void removerDoOrcamento_deveLimparOrcamentoId() {
        item.atribuirOrcamento(UUID.randomUUID());

        item.removerDoOrcamento();

        assertThat(item.getOrcamentoId()).isNull();
    }

    @Test
    @DisplayName("removerDoOrcamento deve lançar exceção quando item já entregue")
    void removerDoOrcamento_deveLancarExcecaoQuandoEntregue() {
        item.atribuirOrcamento(UUID.randomUUID());
        item.avaliar("Sem defeito", true);
        item.aguardarEntrega();
        item.entregar();

        assertThatThrownBy(item::removerDoOrcamento)
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== itens de conserto =====

    @Test
    @DisplayName("adicionarItemConserto deve adicionar à lista")
    void adicionarItemConserto_deveAdicionar() {
        item.adicionarItemConserto(criarItemConserto());

        assertThat(item.getItensConserto()).hasSize(1);
    }

    @Test
    @DisplayName("removerItemConserto deve remover da lista")
    void removerItemConserto_deveRemover() {
        ItemConserto conserto = criarItemConserto();
        ReflectionTestUtils.setField(conserto, "id", UUID.randomUUID());
        item.adicionarItemConserto(conserto);

        item.removerItemConserto(conserto.getId());

        assertThat(item.getItensConserto()).isEmpty();
    }

    @Test
    @DisplayName("adicionarItemConserto deve lançar exceção quando já entregue")
    void adicionarItemConserto_deveLancarExcecaoQuandoEntregue() {
        item.avaliar("Sem defeito", true);
        item.aguardarEntrega();
        item.entregar();

        assertThatThrownBy(() -> item.adicionarItemConserto(criarItemConserto()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("calcularTotalConserto deve somar os valores dos itens de conserto")
    void calcularTotalConserto_deveSomarValores() {
        item.adicionarItemConserto(criarItemConserto());
        item.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.MAO_DE_OBRA)
                .descricao("Mão de obra técnica")
                .quantidade(1)
                .valorUnitario(new BigDecimal("50.00"))
                .build());

        assertThat(item.calcularTotalConserto()).isEqualByComparingTo("130.00");
    }

    @Test
    @DisplayName("temConserto deve retornar false quando sem defeito")
    void temConserto_deveRetornarFalseQuandoSemDefeito() {
        item.setSemDefeito(true);
        item.adicionarItemConserto(criarItemConserto());

        assertThat(item.temConserto()).isFalse();
    }

    @Test
    @DisplayName("temConserto deve retornar true quando tem defeito e itens de conserto")
    void temConserto_deveRetornarTrueQuandoTemItens() {
        item.adicionarItemConserto(criarItemConserto());

        assertThat(item.temConserto()).isTrue();
    }
}
