package com.radiocom.ordemservico.application.service;

import com.radiocom.ordemservico.application.dto.*;
import com.radiocom.ordemservico.application.mapper.OrdemServicoMapper;
import com.radiocom.ordemservico.domain.model.OrdemServico;
import com.radiocom.ordemservico.domain.service.OrdemServicoDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdemServicoApplicationService {

    private final OrdemServicoDomainService osDomainService;
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
