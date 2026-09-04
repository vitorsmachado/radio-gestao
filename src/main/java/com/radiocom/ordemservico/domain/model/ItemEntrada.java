package com.radiocom.ordemservico.domain.model;

import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada;
import com.radiocom.shared.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
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

    @Column(name = "marca", length = 100)
    private String marca;

    @Column(name = "modelo", length = 100)
    private String modelo;

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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "item_entrada_id")
    @Builder.Default
    private List<ItemConserto> itensConserto = new ArrayList<>();

    // ===== AVALIAÇÃO =====

    public void avaliar(String avaliacaoTecnica, boolean semDefeito) {
        validarStatus("Avaliar", StatusItemEntrada.PENDENTE_AVALIACAO);
        this.avaliacaoTecnica = avaliacaoTecnica;
        this.semDefeito = semDefeito;
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

    // ===== ITENS DE CONSERTO =====

    public void adicionarItemConserto(ItemConserto item) {
        validarNaoEntregue("Adicionar item de conserto");
        this.itensConserto.add(item);
    }

    public void removerItemConserto(UUID itemConsertoId) {
        validarNaoEntregue("Remover item de conserto");
        this.itensConserto.removeIf(i -> i.getId().equals(itemConsertoId));
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
