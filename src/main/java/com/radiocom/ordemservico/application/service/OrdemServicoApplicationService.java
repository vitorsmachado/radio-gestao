package com.radiocom.ordemservico.application.service;

import com.radiocom.cliente.application.dto.ClienteDTO;
import com.radiocom.cliente.application.service.ClienteApplicationService;
import com.radiocom.ordemservico.application.dto.*;
import com.radiocom.ordemservico.application.mapper.OrdemServicoMapper;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.OrdemServico;
import com.radiocom.ordemservico.domain.service.OrdemServicoDomainService;
import com.radiocom.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdemServicoApplicationService {

    /** UUID sentinela — usado no lugar de uma lista vazia pra evitar "IN ()" no SQL quando nenhum cliente casa com a busca. */
    private static final UUID CLIENTE_ID_INEXISTENTE = new UUID(0, 0);

    private final OrdemServicoDomainService osDomainService;
    private final ClienteApplicationService clienteApplicationService;
    private final OrdemServicoMapper mapper;

    @Transactional
    public OrdemServicoDTO criar(OrdemServicoCreateDTO dto) {
        log.info("Criando OS para cliente: {}", dto.getClienteId());
        OrdemServico os = osDomainService.criar(
                dto.getClienteId(), dto.getPostoId(), dto.getTecnicoId(), dto.getSolicitante(),
                dto.getDataAbertura(), dto.getObservacoes());
        log.info("OS criada: {} ({})", os.getId(), os.getNumero());
        return mapper.toDTO(os);
    }

    @Transactional
    public OrdemServicoDTO atualizar(UUID id, AtualizarOrdemServicoDTO dto) {
        OrdemServico os = osDomainService.atualizar(id, dto.getClienteId(), dto.getPostoId(), dto.getTecnicoId(),
                dto.getSolicitante(), dto.getDataAbertura(), dto.getObservacoes(), dto.getNumeroRelatorio());
        log.info("OS {} atualizada", os.getNumero());
        return mapper.toDTO(os);
    }

    @Transactional(readOnly = true)
    public OrdemServicoDTO buscarPorId(UUID id) {
        return mapper.toDTO(osDomainService.buscarPorId(id));
    }

    @Transactional(readOnly = true)
    public OrdemServicoDTO buscarPorNumero(String numero) {
        return mapper.toDTO(osDomainService.buscarPorNumero(numero));
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoDTO> listarPorCliente(UUID clienteId) {
        return mapper.toDTOList(osDomainService.listarPorCliente(clienteId));
    }

    /** Usado pela aba Garantia do cliente — todas as passagens de um equipamento/acessório por uma OS. */
    @Transactional(readOnly = true)
    public List<HistoricoOSItemDTO> listarHistoricoPorItemEstoque(UUID itemEstoqueId) {
        List<ItemEntrada> itens = osDomainService.listarItensPorItemEstoque(itemEstoqueId);
        if (itens.isEmpty()) return List.of();

        List<UUID> osIds = itens.stream().map(ItemEntrada::getOsId).distinct().collect(Collectors.toList());
        Map<UUID, OrdemServico> osPorId = osDomainService.listarPorIds(osIds).stream()
                .collect(Collectors.toMap(OrdemServico::getId, Function.identity()));

        return itens.stream().map(item -> {
            OrdemServico os = osPorId.get(item.getOsId());
            return HistoricoOSItemDTO.builder()
                    .osId(item.getOsId())
                    .osNumero(os != null ? os.getNumero() : null)
                    .osStatus(os != null ? os.getStatus() : null)
                    .itemStatus(item.getStatus())
                    .dataAbertura(os != null ? os.getDataAbertura() : null)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<OrdemServicoResumoDTO> listar(String busca, LocalDate dataInicial, LocalDate dataFinal, Pageable pageable) {
        String buscaTratada = busca != null && !busca.isBlank() ? busca.trim() : null;

        List<UUID> clienteIdsMatched = List.of(CLIENTE_ID_INEXISTENTE);
        if (buscaTratada != null) {
            List<UUID> encontrados = clienteApplicationService.buscarPorNomeOuDocumento(buscaTratada).stream()
                    .map(ClienteDTO::getId)
                    .collect(Collectors.toList());
            if (!encontrados.isEmpty()) {
                clienteIdsMatched = encontrados;
            }
        }

        LocalDateTime dataInicialDT = dataInicial != null ? dataInicial.atStartOfDay() : null;
        LocalDateTime dataFinalDT = dataFinal != null ? dataFinal.atTime(LocalTime.MAX) : null;

        Page<OrdemServico> pagina = osDomainService.buscar(buscaTratada, clienteIdsMatched, dataInicialDT, dataFinalDT, pageable);

        List<UUID> clienteIdsDaPagina = pagina.getContent().stream()
                .map(OrdemServico::getClienteId)
                .distinct()
                .collect(Collectors.toList());
        Map<UUID, ClienteDTO> clientesPorId = clienteIdsDaPagina.isEmpty()
                ? Map.of()
                : clienteApplicationService.buscarPorIds(clienteIdsDaPagina).stream()
                        .collect(Collectors.toMap(ClienteDTO::getId, Function.identity()));

        return pagina.map(os -> {
            ClienteDTO cliente = clientesPorId.get(os.getClienteId());
            return mapper.toResumoDTO(os, cliente != null ? cliente.getNomeRazaoSocial() : null,
                    cliente != null ? cliente.getDocumento() : null);
        });
    }

    @Transactional
    public OrdemServicoDTO iniciarAndamento(UUID id) {
        return mapper.toDTO(osDomainService.iniciarAndamento(id));
    }

    @Transactional
    public OrdemServicoDTO confirmarEntrega(UUID id, ConfirmarEntregaDTO dto) {
        OrdemServico os = osDomainService.confirmarEntrega(id, dto.getNomeRecebedor());
        log.info("OS {} entregue para: {}", os.getNumero(), os.getRecebedorNome());
        return mapper.toDTO(os);
    }

    @Transactional
    public OrdemServicoDTO cancelar(UUID id, MotivoDTO dto) {
        return mapper.toDTO(osDomainService.cancelar(id, dto.getMotivo()));
    }

    private record EntradaFila(OrdemServico os, List<ItemEntrada> itens, int bloco) {
    }

    /**
     * Fila de manutenção do técnico: uma linha por OS, cada uma trazendo só
     * os itens em status relevante (em avaliação, aguardando avaliação,
     * aguardando manutenção, aguardando peça). Ordem: bloco (o mais urgente
     * entre os itens da OS) → posição manual dentro do bloco (setas/arrastar,
     * só admin — ver {@link #reordenarFila}).
     *
     * Blocos, do mais urgente ao menos urgente:
     *   1 = EM_AVALIACAO
     *   2 = PENDENTE_MANUTENCAO ou AGUARDANDO_PECA ainda não confirmado
     *       (item recém-autorizado "pula a fila" de aguardando avaliação —
     *       já foi orçado/aprovado, é trabalho pronto pra fazer)
     *   3 = PENDENTE_AVALIACAO
     *   4 = AGUARDANDO_PECA confirmado pelo técnico (fica no final até a peça
     *       chegar, quando volta pro bloco 2 como próximo a ser feito)
     */
    @Transactional(readOnly = true)
    public List<FilaManutencaoOSDTO> listarFilaManutencao() {
        return montarDTOs(calcularEntradasFila());
    }

    /**
     * Reordena a OS dentro do bloco em que ela está agora — sobe/desce uma
     * posição, ou vai pra um índice específico (arrastar). Renumera só as OS
     * daquele bloco e devolve a fila inteira já atualizada.
     */
    @Transactional
    public List<FilaManutencaoOSDTO> reordenarFila(UUID osId, ReordenarFilaDTO dto) {
        List<EntradaFila> entradas = calcularEntradasFila();
        EntradaFila alvo = entradas.stream()
                .filter(e -> e.os().getId().equals(osId))
                .findFirst()
                .orElseThrow(() -> new DomainException("OS não encontrada na fila de manutenção: " + osId));

        List<EntradaFila> doBloco = entradas.stream()
                .filter(e -> e.bloco() == alvo.bloco())
                .collect(Collectors.toList());
        int indiceAtual = doBloco.indexOf(alvo);

        int indiceNovo = switch (dto.getAcao()) {
            case SUBIR -> Math.max(0, indiceAtual - 1);
            case DESCER -> Math.min(doBloco.size() - 1, indiceAtual + 1);
            case POSICAO -> {
                if (dto.getPosicao() == null) {
                    throw new DomainException("Informe a posição de destino");
                }
                yield Math.max(0, Math.min(doBloco.size() - 1, dto.getPosicao()));
            }
        };

        if (indiceNovo != indiceAtual) {
            EntradaFila movida = doBloco.remove(indiceAtual);
            doBloco.add(indiceNovo, movida);
            long posicao = 0;
            List<OrdemServico> alteradas = new ArrayList<>();
            for (EntradaFila e : doBloco) {
                e.os().definirPosicaoFila(posicao++);
                alteradas.add(e.os());
            }
            osDomainService.salvarTodas(alteradas);
        }

        return listarFilaManutencao();
    }

    private List<EntradaFila> calcularEntradasFila() {
        List<ItemEntrada> itens = osDomainService.listarItensNaFilaManutencao();
        if (itens.isEmpty()) return List.of();

        Map<UUID, List<ItemEntrada>> itensPorOS = itens.stream()
                .collect(Collectors.groupingBy(ItemEntrada::getOsId));

        List<UUID> osIds = new ArrayList<>(itensPorOS.keySet());
        Map<UUID, OrdemServico> osPorId = osDomainService.listarPorIds(osIds).stream()
                .collect(Collectors.toMap(OrdemServico::getId, Function.identity()));

        List<EntradaFila> entradas = new ArrayList<>();
        for (Map.Entry<UUID, List<ItemEntrada>> entry : itensPorOS.entrySet()) {
            OrdemServico os = osPorId.get(entry.getKey());
            if (os == null) continue;
            int bloco = entry.getValue().stream().mapToInt(this::blocoDoItem).min().orElseThrow();
            entradas.add(new EntradaFila(os, entry.getValue(), bloco));
        }

        entradas.sort(Comparator.<EntradaFila>comparingInt(EntradaFila::bloco)
                .thenComparing(e -> e.os().getPosicaoFila()));

        return entradas;
    }

    private List<FilaManutencaoOSDTO> montarDTOs(List<EntradaFila> entradas) {
        if (entradas.isEmpty()) return List.of();

        List<UUID> clienteIds = entradas.stream()
                .map(e -> e.os().getClienteId()).distinct().collect(Collectors.toList());
        Map<UUID, String> nomesClientes = clienteApplicationService.buscarPorIds(clienteIds).stream()
                .collect(Collectors.toMap(ClienteDTO::getId, ClienteDTO::getNomeRazaoSocial));

        return entradas.stream().map(e -> FilaManutencaoOSDTO.builder()
                .osId(e.os().getId())
                .osNumero(e.os().getNumero())
                .clienteId(e.os().getClienteId())
                .clienteNome(nomesClientes.get(e.os().getClienteId()))
                .bloco(e.bloco())
                .itens(mapper.toItemDTOList(e.itens()))
                .build()
        ).collect(Collectors.toList());
    }

    private int blocoDoItem(ItemEntrada item) {
        return switch (item.getStatus()) {
            case EM_AVALIACAO -> 1;
            case PENDENTE_MANUTENCAO -> 2;
            case AGUARDANDO_PECA -> item.getConfirmadoAguardandoPecaEm() == null ? 2 : 4;
            case PENDENTE_AVALIACAO -> 3;
            default -> 5;
        };
    }

    // ===== MOVIMENTAÇÃO DE ITENS =====

    @Transactional
    public void moverItem(UUID itemId, MoverItemDTO dto) {
        osDomainService.moverItem(itemId, dto.getNovaOsId());
    }

    @Transactional
    public OrdemServicoDTO dividir(UUID osOrigemId, DividirOSDTO dto) {
        OrdemServico novaOS = osDomainService.dividir(osOrigemId, dto.getItemIds(), dto.getSolicitante());
        log.info("OS {} dividida — nova OS: {}", osOrigemId, novaOS.getNumero());
        return mapper.toDTO(novaOS);
    }

    @Transactional
    public OrdemServicoDTO unir(UUID osDestinoId, UnirOSDTO dto) {
        OrdemServico destino = osDomainService.unir(osDestinoId, dto.getOsOrigemIds());
        log.info("OS(s) {} unidas em: {}", dto.getOsOrigemIds(), destino.getNumero());
        return mapper.toDTO(destino);
    }
}
