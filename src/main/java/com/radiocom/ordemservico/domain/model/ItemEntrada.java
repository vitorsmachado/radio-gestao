package com.radiocom.ordemservico.domain.model;

import com.radiocom.estoque.domain.model.enums.FaixaEquipamento;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.ordemservico.domain.model.enums.ResultadoAvaliacao;
import com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada;
import com.radiocom.shared.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Um item físico (equipamento ou acessório) trazido pelo cliente, com ciclo
 * de vida próprio — independente da OS ou do orçamento em que está agrupado
 * no momento. "Pertencer à OS X" é só o valor atual de {@code osId}: o item
 * pode ser movido pra outra OS (ver {@link #moverParaOS(UUID)}) sem perder
 * seu histórico de avaliação/autorização.
 */
@Entity
@Table(name = "os_itens_entrada")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemEntrada extends BaseEntity {

    @NotNull
    @Column(name = "os_id", nullable = false)
    private UUID osId;

    @Column(name = "orcamento_id")
    private UUID orcamentoId;

    @Column(name = "item_estoque_id")
    private UUID itemEstoqueId; // Equipamento/Acessorio no Estoque (proprietário CLIENTE)

    /** Modelo do catálogo escolhido na entrada — sugestão opcional, traz valor de referência e serve pra filtrar por modelo nos relatórios. */
    @Column(name = "catalogo_modelo_id")
    private UUID catalogoModeloId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_item", nullable = false, length = 20)
    private TipoItem tipoItem; // EQUIPAMENTO ou ACESSORIO

    @NotBlank
    @Column(name = "descricao", nullable = false, length = 255)
    private String descricao;

    @Column(name = "numero_serie", length = 50)
    private String numeroSerie;

    @Column(name = "patrimonio", length = 50)
    private String patrimonio;

    /** Identificação própria do cliente pro item (tag/código interno dele, distinto do nosso patrimônio). */
    @Column(name = "codigo_cliente", length = 100)
    private String codigoCliente;

    /**
     * Quantidade de unidades que esta linha representa. Só faz sentido
     * &gt; 1 para acessório não rastreado individualmente (sem N/S nem
     * patrimônio) — ex: "3 antenas modelo X". Equipamento e qualquer item
     * rastreado por N/S/patrimônio ficam sempre em 1.
     */
    @NotNull
    @Column(name = "quantidade", nullable = false)
    @Builder.Default
    private Integer quantidade = 1;

    @Column(name = "marca", length = 100)
    private String marca;

    @Column(name = "modelo", length = 100)
    private String modelo;

    /** Só relevante para equipamento (rádio) — faixa de frequência. */
    @Enumerated(EnumType.STRING)
    @Column(name = "faixa", length = 20)
    private FaixaEquipamento faixa;

    @Column(name = "defeito_relatado", length = 1000)
    private String defeitoRelatado; // O que o cliente relatou

    @Column(name = "avaliacao_tecnica", length = 1000)
    private String avaliacaoTecnica; // Laudo do técnico

    @Column(name = "sem_defeito", nullable = false)
    @Builder.Default
    private boolean semDefeito = false;

    @Column(name = "garantia", nullable = false)
    @Builder.Default
    private boolean garantia = false;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private StatusItemEntrada status = StatusItemEntrada.PENDENTE_AVALIACAO;

    @Column(name = "motivo_nao_autorizado", length = 500)
    private String motivoNaoAutorizado;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado_avaliacao", length = 20)
    private ResultadoAvaliacao resultadoAvaliacao;

    /** Só relevante quando resultadoAvaliacao = AJUSTE. */
    @Column(name = "detalhe_ajuste", length = 500)
    private String detalheAjuste;

    @Column(name = "defeito_encontrado", length = 1000)
    private String defeitoEncontrado;

    @Column(name = "causa_defeito", length = 1000)
    private String causaDefeito;

    @Column(name = "solucao_recomendada", length = 1000)
    private String solucaoRecomendada;

    @Column(name = "observacoes_tecnicas", length = 1000)
    private String observacoesTecnicas;

    /**
     * Marcado quando o técnico confirma "aguardando peça" de propósito (não é
     * só o status automático) — a fila de manutenção usa isso pra mandar o
     * item pro final, e pra saber que ele é o próximo assim que a peça chegar.
     */
    @Column(name = "confirmado_aguardando_peca_em")
    private LocalDateTime confirmadoAguardandoPecaEm;

    @OneToMany(mappedBy = "itemEntrada", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ItemConserto> itensConserto = new ArrayList<>();

    // ===== AVALIAÇÃO =====

    public void avaliar(String avaliacaoTecnica, boolean semDefeito) {
        validarStatus("Avaliar", StatusItemEntrada.PENDENTE_AVALIACAO, StatusItemEntrada.EM_AVALIACAO);
        this.avaliacaoTecnica = avaliacaoTecnica;
        this.semDefeito = semDefeito;
        this.status = StatusItemEntrada.AVALIADO;
    }

    /**
     * O técnico "reivindica" o item pra começar a preencher a avaliação —
     * deixa visível pros outros que já tem alguém mexendo nele. Puramente um
     * marcador de progresso; não é obrigatório salvar em seguida.
     */
    public void iniciarAvaliacao() {
        validarStatus("Iniciar avaliação", StatusItemEntrada.PENDENTE_AVALIACAO);
        this.status = StatusItemEntrada.EM_AVALIACAO;
    }

    /**
     * Salva o laudo técnico estruturado da tela do técnico (resultado,
     * detalhe do ajuste, defeito/causa/solução/observações). Deriva
     * {@code semDefeito} do resultado pra manter toda a lógica existente
     * (aguardarEntrega, temConserto) funcionando sem mudança.
     */
    public void salvarAvaliacaoTecnica(ResultadoAvaliacao resultado, String detalheAjuste,
                                        String defeitoEncontrado, String causaDefeito,
                                        String solucaoRecomendada, String observacoesTecnicas,
                                        boolean garantia) {
        validarStatus("Salvar avaliação técnica", StatusItemEntrada.PENDENTE_AVALIACAO, StatusItemEntrada.EM_AVALIACAO);
        this.resultadoAvaliacao = resultado;
        this.detalheAjuste = detalheAjuste;
        this.defeitoEncontrado = defeitoEncontrado;
        this.avaliacaoTecnica = defeitoEncontrado;
        this.causaDefeito = causaDefeito;
        this.solucaoRecomendada = solucaoRecomendada;
        this.observacoesTecnicas = observacoesTecnicas;
        this.semDefeito = resultado == ResultadoAvaliacao.SEM_DEFEITO;
        this.garantia = garantia;
        this.status = StatusItemEntrada.AVALIADO;
    }

    /**
     * Atualiza o laudo/defeito sem mudar o status — permitido em qualquer
     * momento antes da entrega, incluindo depois de autorizado ou até na
     * hora da entrega (problema novo encontrado tarde).
     */
    public void atualizarAvaliacao(String avaliacaoTecnica, boolean semDefeito) {
        validarNaoEntregue("Atualizar avaliação");
        this.avaliacaoTecnica = avaliacaoTecnica;
        this.semDefeito = semDefeito;
    }

    // ===== AUTORIZAÇÃO =====

    /**
     * Marca que o orçamento foi apresentado ao cliente e a decisão está
     * pendente. Separado de {@link #avaliar} porque pode haver um intervalo
     * (montar/enviar o orçamento) entre o laudo pronto e o cliente ser
     * de fato consultado.
     */
    public void enviarParaAutorizacao() {
        validarStatus("Enviar para autorização", StatusItemEntrada.AVALIADO);
        this.status = StatusItemEntrada.PENDENTE_AUTORIZACAO;
    }

    /**
     * Registra que o cliente aprovou o conserto. Não decide sozinho se o
     * item vai para a fila de manutenção ou fica aguardando peça — quem
     * decide isso é a camada de aplicação, que consulta o Estoque logo
     * depois de chamar este método (ver {@link #iniciarFilaManutencao} e
     * {@link #marcarAguardandoPeca}).
     */
    public void autorizar() {
        validarStatus("Autorizar", StatusItemEntrada.PENDENTE_AUTORIZACAO, StatusItemEntrada.NAO_AUTORIZADO);
        this.status = StatusItemEntrada.AUTORIZADO;
        this.motivoNaoAutorizado = null;
    }

    /**
     * Permitido até o item entrar de fato em reparo (EM_MANUTENCAO) —
     * cliente pode mudar de ideia enquanto o item só está na fila ou
     * aguardando peça. Reverter durante o reparo físico fica fora de
     * escopo por enquanto.
     */
    public void naoAutorizar(String motivo) {
        validarStatus("Marcar como não autorizado",
                StatusItemEntrada.PENDENTE_AUTORIZACAO, StatusItemEntrada.AUTORIZADO,
                StatusItemEntrada.PENDENTE_MANUTENCAO, StatusItemEntrada.AGUARDANDO_PECA);
        this.status = StatusItemEntrada.NAO_AUTORIZADO;
        this.motivoNaoAutorizado = motivo;
    }

    /**
     * Peça confirmada em estoque — item entra na fila, pronto para um
     * técnico iniciar o reparo.
     */
    public void iniciarFilaManutencao() {
        validarStatus("Iniciar fila de manutenção", StatusItemEntrada.AUTORIZADO);
        this.status = StatusItemEntrada.PENDENTE_MANUTENCAO;
    }

    /**
     * Peça em falta — seja logo após a autorização, seja descoberta no meio
     * do próprio reparo.
     */
    public void marcarAguardandoPeca() {
        validarStatus("Marcar aguardando peça", StatusItemEntrada.AUTORIZADO, StatusItemEntrada.EM_MANUTENCAO);
        this.status = StatusItemEntrada.AGUARDANDO_PECA;
    }

    /**
     * O técnico confirma que já verificou e realmente está preso esperando a
     * peça — diferente do status automático {@link #marcarAguardandoPeca},
     * essa é uma decisão deliberada que manda o item pro final da fila de
     * manutenção (fica lá até a peça chegar, quando volta automaticamente
     * como o próximo a ser feito).
     */
    public void confirmarAguardandoPeca() {
        validarStatus("Confirmar aguardando peça", StatusItemEntrada.AGUARDANDO_PECA);
        this.confirmadoAguardandoPecaEm = LocalDateTime.now();
    }

    /**
     * Peça chegou no estoque — item volta pra fila de manutenção (o técnico
     * retoma/inicia o reparo a partir daí).
     */
    public void retomarAposPeca() {
        validarStatus("Retomar após chegada de peça", StatusItemEntrada.AGUARDANDO_PECA);
        this.status = StatusItemEntrada.PENDENTE_MANUTENCAO;
    }

    public void iniciarManutencao() {
        validarStatus("Iniciar manutenção", StatusItemEntrada.PENDENTE_MANUTENCAO);
        this.status = StatusItemEntrada.EM_MANUTENCAO;
    }

    public void concluirManutencao() {
        validarStatus("Concluir manutenção", StatusItemEntrada.EM_MANUTENCAO);
        this.status = StatusItemEntrada.MANUTENCAO_CONCLUIDA;
    }

    // ===== ENTREGA =====

    /**
     * Item está pronto e só falta o cliente vir buscar — ponto de encontro
     * comum entre os três caminhos possíveis (reparo concluído, sem defeito,
     * ou não autorizado). Entrega em si nunca é imediata, por isso esse
     * status existe separado de {@link #entregar}.
     */
    public void aguardarEntrega() {
        validarStatus("Aguardar entrega", StatusItemEntrada.MANUTENCAO_CONCLUIDA,
                StatusItemEntrada.AVALIADO, StatusItemEntrada.NAO_AUTORIZADO);
        if (this.status == StatusItemEntrada.AVALIADO && !this.semDefeito) {
            throw new IllegalStateException(
                    "Aguardar entrega direto de AVALIADO só é permitido quando não há defeito. "
                            + "Envie para autorização antes de um item com defeito.");
        }
        this.status = StatusItemEntrada.AGUARDANDO_ENTREGA;
    }

    public void entregar() {
        validarStatus("Entregar", StatusItemEntrada.AGUARDANDO_ENTREGA);
        this.status = StatusItemEntrada.ENTREGUE;
    }

    // ===== MOVIMENTAÇÃO ENTRE OS =====

    public void moverParaOS(UUID novaOsId) {
        validarNaoEntregue("Mover para outra OS");
        if (novaOsId == null) {
            throw new IllegalArgumentException("Nova OS não pode ser nula");
        }
        this.osId = novaOsId;
    }

    // ===== AGRUPAMENTO EM ORÇAMENTO =====

    public void atribuirOrcamento(UUID orcamentoId) {
        validarNaoEntregue("Atribuir a um orçamento");
        if (orcamentoId == null) {
            throw new IllegalArgumentException("Orçamento não pode ser nulo");
        }
        this.orcamentoId = orcamentoId;
    }

    public void removerDoOrcamento() {
        validarNaoEntregue("Remover do orçamento");
        this.orcamentoId = null;
    }

    // ===== ITENS DE CONSERTO =====

    public void adicionarItemConserto(ItemConserto item) {
        validarNaoEntregue("Adicionar item de conserto");
        item.setItemEntrada(this);
        this.itensConserto.add(item);
    }

    public void removerItemConserto(UUID itemConsertoId) {
        validarNaoEntregue("Remover item de conserto");
        this.itensConserto.removeIf(i -> i.getId().equals(itemConsertoId));
    }

    /**
     * Define o valor de um item de conserto já existente — usado quando a
     * peça foi escolhida pelo técnico na avaliação (sem preço) e o admin
     * decide o valor depois, no orçamento.
     */
    public void atualizarValorItemConserto(UUID itemConsertoId, BigDecimal valorUnitario) {
        validarNaoEntregue("Atualizar valor do item de conserto");
        ItemConserto item = itensConserto.stream()
                .filter(i -> i.getId().equals(itemConsertoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item de conserto não encontrado: " + itemConsertoId));
        item.setValorUnitario(valorUnitario);
    }

    public BigDecimal calcularTotalConserto() {
        return itensConserto.stream()
                .peek(ItemConserto::calcularTotal)
                .map(ItemConserto::getValorTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean temConserto() {
        return !semDefeito && !itensConserto.isEmpty();
    }

    // ===== VALIDAÇÃO =====

    private void validarStatus(String operacao, StatusItemEntrada... statusPermitidos) {
        for (StatusItemEntrada s : statusPermitidos) {
            if (this.status == s) return;
        }
        throw new IllegalStateException(
                operacao + " requer status " + java.util.Arrays.toString(statusPermitidos)
                        + ". Status atual: " + this.status);
    }

    private void validarNaoEntregue(String operacao) {
        if (this.status == StatusItemEntrada.ENTREGUE) {
            throw new IllegalStateException(operacao + " não é permitido: item já foi entregue.");
        }
    }
}
