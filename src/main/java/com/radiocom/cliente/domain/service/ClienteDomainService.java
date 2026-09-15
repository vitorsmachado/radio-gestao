package com.radiocom.cliente.domain.service;

import com.radiocom.cliente.domain.model.Cliente;
import com.radiocom.cliente.domain.model.ClienteStatusHistorico;
import com.radiocom.cliente.domain.model.enums.StatusCliente;
import com.radiocom.cliente.domain.repository.ClienteRepository;
import com.radiocom.cliente.domain.repository.ClienteStatusHistoricoRepository;
import com.radiocom.shared.exception.DomainException;
import com.radiocom.shared.validation.CpfCnpjValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteDomainService {

    private final ClienteRepository clienteRepository;
    private final ClienteStatusHistoricoRepository statusHistoricoRepository;

    @Transactional(readOnly = true)
    public Cliente buscarPorId(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new DomainException("Cliente não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorIdComRelacionamentos(UUID id) {
        return clienteRepository.findWithRelationsById(id)
                .orElseThrow(() -> new DomainException("Cliente não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public Map<UUID, String> buscarNomesPorIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        return clienteRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Cliente::getId, Cliente::getNomeRazaoSocial));
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorDocumento(String documento) {
        String docLimpo = CpfCnpjValidator.clean(documento);

        return clienteRepository.findByDocumento(docLimpo)
                .orElseThrow(() -> new DomainException(
                        "Cliente não encontrado com documento: " +
                                CpfCnpjValidator.format(documento)
                ));
    }

    @Transactional
    public Cliente inativarCliente(UUID id, String motivo) {
        Cliente cliente = buscarPorId(id);
        validarClienteAtivo(cliente);
        StatusCliente statusAnterior = cliente.getStatus();
        cliente.inativar();
        Cliente salvo = clienteRepository.save(cliente);
        registrarTransicaoStatus(id, statusAnterior, salvo.getStatus(), motivo);
        return salvo;
    }

    @Transactional
    public Cliente bloquearCliente(UUID id, String motivo) {
        Cliente cliente = buscarPorId(id);
        StatusCliente statusAnterior = cliente.getStatus();
        cliente.bloquear();
        Cliente salvo = clienteRepository.save(cliente);
        registrarTransicaoStatus(id, statusAnterior, salvo.getStatus(), motivo);
        return salvo;
    }

    @Transactional
    public Cliente ativarCliente(UUID id, String motivo) {
        Cliente cliente = buscarPorId(id);
        StatusCliente statusAnterior = cliente.getStatus();
        cliente.ativar();
        Cliente salvo = clienteRepository.save(cliente);
        registrarTransicaoStatus(id, statusAnterior, salvo.getStatus(), motivo);
        return salvo;
    }

    private void registrarTransicaoStatus(UUID clienteId, StatusCliente statusAnterior, StatusCliente statusNovo, String motivo) {
        statusHistoricoRepository.save(ClienteStatusHistorico.builder()
                .clienteId(clienteId)
                .statusAnterior(statusAnterior)
                .statusNovo(statusNovo)
                .motivo(motivo != null && !motivo.isBlank() ? motivo.trim() : null)
                .build());
    }

    public void validarDocumentoUnico(String documento, UUID idAtual) {
        String docLimpo = CpfCnpjValidator.clean(documento);

        if (!CpfCnpjValidator.isValid(docLimpo)) {
            throw new DomainException("Documento inválido: " + documento);
        }

        boolean exists = idAtual == null
                ? clienteRepository.existsByDocumento(docLimpo)
                : clienteRepository.existsByDocumentoAndIdNot(docLimpo, idAtual);

        if (exists) {
            throw new DomainException(
                    "Já existe cliente cadastrado com este documento: " +
                            CpfCnpjValidator.format(documento)
            );
        }
    }

    public void validarClienteAtivo(Cliente cliente) {
        if (!cliente.isAtivo()) {
            throw new DomainException(
                    "Cliente não está ativo: " + cliente.getNomeRazaoSocial()
            );
        }
    }
}
