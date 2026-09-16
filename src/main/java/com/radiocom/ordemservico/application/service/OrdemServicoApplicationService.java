package com.radiocom.ordemservico.application.service;

import com.radiocom.cliente.application.dto.ClienteDTO;
import com.radiocom.cliente.application.service.ClienteApplicationService;
import com.radiocom.ordemservico.application.dto.*;
import com.radiocom.ordemservico.application.mapper.OrdemServicoMapper;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.OrdemServico;
import com.radiocom.ordemservico.domain.service.OrdemServicoDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
                dto.getClienteId(), dto.getPostoId(), dto.getTecnicoId(), dto.getSolicitante());
        log.info("OS criada: {} ({})", os.getId(), os.getNumero());
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
