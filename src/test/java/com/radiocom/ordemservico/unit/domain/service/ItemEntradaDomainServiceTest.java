package com.radiocom.ordemservico.unit.domain.service;

import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.estoque.domain.service.EstoqueDomainService;
import com.radiocom.ordemservico.domain.model.ItemConserto;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada;
import com.radiocom.ordemservico.domain.model.enums.TipoItemConserto;
import com.radiocom.ordemservico.domain.repository.ItemEntradaRepository;
import com.radiocom.ordemservico.domain.repository.ItemEntradaStatusHistoricoRepository;
import com.radiocom.ordemservico.domain.service.ItemEntradaDomainService;
import com.radiocom.ordemservico.garantia.domain.service.GarantiaPecaDomainService;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemEntradaDomainService - Testes Unitários")
class ItemEntradaDomainServiceTest {

    @Mock private ItemEntradaRepository itemEntradaRepository;
    @Mock private ItemEntradaStatusHistoricoRepository statusHistoricoRepository;
    @Mock private EstoqueDomainService estoqueDomainService;
    @Mock private GarantiaPecaDomainService garantiaPecaDomainService;

    @InjectMocks
    private ItemEntradaDomainService service;

    private UUID itemId;
    private ItemEntrada item;
    private UUID pecaId;

    @BeforeEach
    void setUp() {
        itemId = UUID.randomUUID();
        pecaId = UUID.randomUUID();
        item = ItemEntrada.builder()
                .osId(UUID.randomUUID())
                .tipoItem(TipoItem.EQUIPAMENTO)
                .descricao("Rádio Motorola EP450")
                .build();
        lenient().when(itemEntradaRepository.save(any(ItemEntrada.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("buscarPorId deve lançar exceção quando não existe")
    void buscarPorId_deveLancarExcecaoQuandoNaoExiste() {
        when(itemEntradaRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(itemId))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("avaliar deve delegar para o domínio e salvar")
    void avaliar_deveDelegarESalvar() {
        when(itemEntradaRepository.findById(itemId)).thenReturn(Optional.of(item));

        ItemEntrada resultado = service.avaliar(itemId, "Capacitor queimado", false);

        assertThat(resultado.getStatus()).isEqualTo(StatusItemEntrada.AVALIADO);
    }

    @Test
    @DisplayName("autorizar deve ir para PENDENTE_MANUTENCAO quando peça está disponível")
    void autorizar_devePendenteManutencaoQuandoPecaDisponivel() {
        item.avaliar("Precisa de bateria nova", false);
        item.enviarParaAutorizacao();
        item.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.PECA)
                .itemEstoqueId(pecaId)
                .descricao("Bateria BP-227")
                .quantidade(1)
                .valorUnitario(new BigDecimal("80.00"))
                .build());
        when(itemEntradaRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(estoqueDomainService.verificarDisponibilidade(pecaId, TipoItem.PECA, 1)).thenReturn(true);

        ItemEntrada resultado = service.autorizar(itemId);

        assertThat(resultado.getStatus()).isEqualTo(StatusItemEntrada.PENDENTE_MANUTENCAO);
    }

    @Test
    @DisplayName("autorizar deve ir para AGUARDANDO_PECA quando falta peça em estoque")
    void autorizar_deveAguardandoPecaQuandoFaltaPeca() {
        item.avaliar("Precisa de bateria nova", false);
        item.enviarParaAutorizacao();
        item.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.PECA)
                .itemEstoqueId(pecaId)
                .descricao("Bateria BP-227")
                .quantidade(2)
                .valorUnitario(new BigDecimal("80.00"))
                .build());
        when(itemEntradaRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(estoqueDomainService.verificarDisponibilidade(pecaId, TipoItem.PECA, 2)).thenReturn(false);

        ItemEntrada resultado = service.autorizar(itemId);

        assertThat(resultado.getStatus()).isEqualTo(StatusItemEntrada.AGUARDANDO_PECA);
    }

    @Test
    @DisplayName("autorizar deve ir direto para PENDENTE_MANUTENCAO quando não há peças no conserto")
    void autorizar_devePendenteManutencaoQuandoSemPecas() {
        item.avaliar("Só precisa de mão de obra", false);
        item.enviarParaAutorizacao();
        item.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.MAO_DE_OBRA)
                .descricao("Mão de obra técnica")
                .quantidade(1)
                .valorUnitario(new BigDecimal("50.00"))
                .build());
        when(itemEntradaRepository.findById(itemId)).thenReturn(Optional.of(item));

        ItemEntrada resultado = service.autorizar(itemId);

        assertThat(resultado.getStatus()).isEqualTo(StatusItemEntrada.PENDENTE_MANUTENCAO);
    }

    @Test
    @DisplayName("naoAutorizar deve delegar para o domínio e salvar")
    void naoAutorizar_deveDelegarESalvar() {
        item.avaliar("Placa danificada", false);
        item.enviarParaAutorizacao();
        when(itemEntradaRepository.findById(itemId)).thenReturn(Optional.of(item));

        ItemEntrada resultado = service.naoAutorizar(itemId, "Cliente recusou");

        assertThat(resultado.getStatus()).isEqualTo(StatusItemEntrada.NAO_AUTORIZADO);
        assertThat(resultado.getMotivoNaoAutorizado()).isEqualTo("Cliente recusou");
    }

    @Test
    @DisplayName("iniciarManutencao e concluirManutencao devem delegar para o domínio")
    void iniciarEConcluirManutencao_devemDelegar() {
        item.avaliar("Capacitor queimado", false);
        item.enviarParaAutorizacao();
        item.autorizar();
        item.iniciarFilaManutencao();
        when(itemEntradaRepository.findById(itemId)).thenReturn(Optional.of(item));

        service.iniciarManutencao(itemId);
        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.EM_MANUTENCAO);

        ItemEntrada resultado = service.concluirManutencao(itemId);
        assertThat(resultado.getStatus()).isEqualTo(StatusItemEntrada.MANUTENCAO_CONCLUIDA);
        org.mockito.Mockito.verify(garantiaPecaDomainService).registrarCobertura(resultado);
    }

    @Test
    @DisplayName("marcarAguardandoPeca deve delegar para o domínio quando falta peça no meio do reparo")
    void marcarAguardandoPeca_deveDelegarDuranteOReparo() {
        item.avaliar("Capacitor queimado", false);
        item.enviarParaAutorizacao();
        item.autorizar();
        item.iniciarFilaManutencao();
        item.iniciarManutencao();
        when(itemEntradaRepository.findById(itemId)).thenReturn(Optional.of(item));

        ItemEntrada resultado = service.marcarAguardandoPeca(itemId);

        assertThat(resultado.getStatus()).isEqualTo(StatusItemEntrada.AGUARDANDO_PECA);
    }

    @Test
    @DisplayName("aguardarEntrega e entregar devem delegar para o domínio")
    void aguardarEntregaEEntregar_devemDelegar() {
        item.avaliar("Sem defeito", true);
        when(itemEntradaRepository.findById(itemId)).thenReturn(Optional.of(item));

        service.aguardarEntrega(itemId);
        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.AGUARDANDO_ENTREGA);

        ItemEntrada resultado = service.entregar(itemId);
        assertThat(resultado.getStatus()).isEqualTo(StatusItemEntrada.ENTREGUE);
    }

    @Test
    @DisplayName("retomarItensAguardandoPeca deve voltar pra fila os itens com peça já disponível")
    void retomarItensAguardandoPeca_deveVoltarParaFila() {
        item.avaliar("Precisa de bateria", false);
        item.enviarParaAutorizacao();
        item.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.PECA)
                .itemEstoqueId(pecaId)
                .descricao("Bateria BP-227")
                .quantidade(1)
                .valorUnitario(new BigDecimal("80.00"))
                .build());
        item.autorizar();
        item.marcarAguardandoPeca();

        when(itemEntradaRepository.findByStatusAndItemEstoqueId(StatusItemEntrada.AGUARDANDO_PECA, pecaId))
                .thenReturn(List.of(item));
        when(estoqueDomainService.verificarDisponibilidade(pecaId, TipoItem.PECA, 1)).thenReturn(true);

        service.retomarItensAguardandoPeca(pecaId);

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.PENDENTE_MANUTENCAO);
    }

    @Test
    @DisplayName("retomarItensAguardandoPeca não deve mudar status quando peça ainda insuficiente")
    void retomarItensAguardandoPeca_naoDeveMudarQuandoAindaInsuficiente() {
        item.avaliar("Precisa de bateria", false);
        item.enviarParaAutorizacao();
        item.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.PECA)
                .itemEstoqueId(pecaId)
                .descricao("Bateria BP-227")
                .quantidade(3)
                .valorUnitario(new BigDecimal("80.00"))
                .build());
        item.autorizar();
        item.marcarAguardandoPeca();

        when(itemEntradaRepository.findByStatusAndItemEstoqueId(StatusItemEntrada.AGUARDANDO_PECA, pecaId))
                .thenReturn(List.of(item));
        when(estoqueDomainService.verificarDisponibilidade(pecaId, TipoItem.PECA, 3)).thenReturn(false);

        service.retomarItensAguardandoPeca(pecaId);

        assertThat(item.getStatus()).isEqualTo(StatusItemEntrada.AGUARDANDO_PECA);
    }

    @Test
    @DisplayName("adicionarItemConserto e removerItemConserto devem delegar para o domínio")
    void adicionarERemoverItemConserto_devemDelegar() {
        when(itemEntradaRepository.findById(itemId)).thenReturn(Optional.of(item));
        ItemConserto conserto = ItemConserto.builder()
                .tipo(TipoItemConserto.PECA)
                .descricao("Bateria")
                .quantidade(1)
                .valorUnitario(new BigDecimal("80.00"))
                .build();
        ReflectionTestUtils.setField(conserto, "id", UUID.randomUUID());

        service.adicionarItemConserto(itemId, conserto);
        assertThat(item.getItensConserto()).hasSize(1);

        service.removerItemConserto(itemId, conserto.getId());
        assertThat(item.getItensConserto()).isEmpty();
    }

    @Test
    @DisplayName("atualizarValorItemConserto deve definir o valor de um item existente")
    void atualizarValorItemConserto_deveDefinirValor() {
        when(itemEntradaRepository.findById(itemId)).thenReturn(Optional.of(item));
        ItemConserto conserto = ItemConserto.builder()
                .tipo(TipoItemConserto.PECA).descricao("Bateria")
                .quantidade(1).valorUnitario(BigDecimal.ZERO).build();
        ReflectionTestUtils.setField(conserto, "id", UUID.randomUUID());
        item.adicionarItemConserto(conserto);

        ItemEntrada resultado = service.atualizarValorItemConserto(itemId, conserto.getId(), new BigDecimal("45.00"));

        assertThat(resultado.getItensConserto().get(0).getValorUnitario()).isEqualByComparingTo("45.00");
    }

    @Test
    @DisplayName("atualizarValorItemConserto deve lançar exceção quando o item de conserto não existe")
    void atualizarValorItemConserto_deveLancarExcecaoQuandoNaoExiste() {
        when(itemEntradaRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.atualizarValorItemConserto(itemId, UUID.randomUUID(), BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("salvarAvaliacaoTecnica com garantiaPecaId válido deve marcar o item como garantia")
    void salvarAvaliacaoTecnica_comGarantiaPecaIdValido_deveMarcarGarantia() {
        UUID itemEstoqueId = UUID.randomUUID();
        UUID garantiaPecaId = UUID.randomUUID();
        ReflectionTestUtils.setField(item, "itemEstoqueId", itemEstoqueId);
        item.iniciarAvaliacao();
        when(itemEntradaRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(garantiaPecaDomainService.validarCoberturaAtiva(itemEstoqueId, garantiaPecaId))
                .thenReturn(com.radiocom.ordemservico.garantia.domain.model.GarantiaPeca.builder().build());

        ItemEntrada resultado = service.salvarAvaliacaoTecnica(itemId,
                com.radiocom.ordemservico.domain.model.enums.ResultadoAvaliacao.AJUSTE,
                "Ajuste simples", "Bateria fraca", null, null, null, garantiaPecaId);

        assertThat(resultado.isGarantia()).isTrue();
    }

    @Test
    @DisplayName("remover deve excluir o item quando está pendente de avaliação")
    void remover_devePermitirQuandoPendenteAvaliacao() {
        when(itemEntradaRepository.findById(itemId)).thenReturn(Optional.of(item));

        service.remover(itemId);

        org.mockito.Mockito.verify(itemEntradaRepository).delete(item);
    }

    @Test
    @DisplayName("remover deve permitir quando o item está em avaliação (só marca que alguém abriu)")
    void remover_devePermitirQuandoEmAvaliacao() {
        item.iniciarAvaliacao();
        when(itemEntradaRepository.findById(itemId)).thenReturn(Optional.of(item));

        service.remover(itemId);

        org.mockito.Mockito.verify(itemEntradaRepository).delete(item);
    }

    @Test
    @DisplayName("remover deve lançar exceção quando o item já foi avaliado")
    void remover_deveLancarExcecaoQuandoJaAvaliado() {
        item.avaliar("Capacitor queimado", false);
        when(itemEntradaRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.remover(itemId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("salvarAvaliacaoTecnica com garantiaPecaId deve lançar exceção quando item não tem equipamento vinculado")
    void salvarAvaliacaoTecnica_comGarantiaPecaIdSemItemEstoque_deveLancarExcecao() {
        UUID garantiaPecaId = UUID.randomUUID();
        item.iniciarAvaliacao();
        when(itemEntradaRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.salvarAvaliacaoTecnica(itemId,
                com.radiocom.ordemservico.domain.model.enums.ResultadoAvaliacao.AJUSTE,
                null, null, null, null, null, garantiaPecaId))
                .isInstanceOf(DomainException.class);
    }
}
