package com.radiocom.estoque.domain.service;

import com.radiocom.estoque.domain.model.Equipamento;
import com.radiocom.estoque.domain.model.enums.EstadoEquipamento;
import com.radiocom.estoque.domain.model.enums.ProprietarioEquipamento;
import com.radiocom.estoque.domain.repository.EquipamentoRepository;
import com.radiocom.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EquipamentoDomainService {

    private final EquipamentoRepository equipamentoRepository;

    // ===== BUSCA =====

    @Transactional(readOnly = true)
    public Equipamento buscarPorId(UUID id) {
        return equipamentoRepository.findById(id)
                .orElseThrow(() -> new DomainException("Equipamento não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public Equipamento buscarPorNumeroSerie(String ns) {
        return equipamentoRepository.findByNumeroSerie(ns)
                .orElseThrow(() -> new DomainException("Equipamento não encontrado com NS: " + ns));
    }

    @Transactional(readOnly = true)
    public Equipamento buscarPorPatrimonio(String patrimonio) {
        return equipamentoRepository.findByPatrimonio(patrimonio)
                .orElseThrow(() -> new DomainException("Patrimônio não encontrado: " + patrimonio));
    }

    // ===== LISTAGEM =====

    @Transactional(readOnly = true)
    public List<Equipamento> listarPorEstado(EstadoEquipamento estado) {
        return equipamentoRepository.findByEstado(estado);
    }

    @Transactional(readOnly = true)
    public List<Equipamento> listarPorProprietario(ProprietarioEquipamento proprietario) {
        return equipamentoRepository.findByProprietario(proprietario);
    }

    // ===== VALIDAÇÃO =====

    public void validarDuplicidadeNS(String ns) {
        if (equipamentoRepository.existsByNumeroSerie(ns)) {
            throw new DomainException("Número de série já cadastrado");
        }
    }

    public void validarDuplicidadePatrimonio(String patrimonio) {
        if (equipamentoRepository.existsByPatrimonio(patrimonio)) {
            throw new DomainException("Patrimônio já cadastrado");
        }
    }

    // ===== AÇÕES =====

    @Transactional
    public Equipamento enviarManutencao(UUID id) {
        Equipamento equipamento = buscarPorId(id);
        equipamento.enviarManutencao();
        return equipamentoRepository.save(equipamento);
    }

    @Transactional
    public Equipamento concluirManutencao(UUID id) {
        Equipamento equipamento = buscarPorId(id);
        equipamento.concluirManutencao();
        return equipamentoRepository.save(equipamento);
    }

    @Transactional
    public Equipamento marcarDescartado(UUID id) {
        Equipamento equipamento = buscarPorId(id);
        equipamento.marcarDescartado();
        return equipamentoRepository.save(equipamento);
    }
}
