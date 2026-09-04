package com.radiocom.estoque.application.service;

import com.radiocom.estoque.application.dto.*;
import com.radiocom.estoque.application.mapper.EstoqueMapper;
import com.radiocom.estoque.domain.model.Acessorio;
import com.radiocom.estoque.domain.model.CatalogoModelo;
import com.radiocom.estoque.domain.model.Equipamento;
import com.radiocom.estoque.domain.model.Peca;
import com.radiocom.estoque.domain.model.enums.EstadoEquipamento;
import com.radiocom.estoque.domain.model.enums.ProprietarioEquipamento;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.estoque.domain.repository.AcessorioRepository;
import com.radiocom.estoque.domain.repository.CatalogoModeloRepository;
import com.radiocom.estoque.domain.repository.EquipamentoRepository;
import com.radiocom.estoque.domain.repository.PecaRepository;
import com.radiocom.estoque.domain.service.EquipamentoDomainService;
import com.radiocom.estoque.domain.service.EstoqueDomainService;
import com.radiocom.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EstoqueApplicationService {

    private final EquipamentoRepository equipamentoRepository;
    private final AcessorioRepository acessorioRepository;
    private final PecaRepository pecaRepository;
    private final CatalogoModeloRepository catalogoModeloRepository;

    private final EquipamentoDomainService equipamentoService;
    private final EstoqueDomainService estoqueService;
    private final EstoqueMapper mapper;

    // ========== EQUIPAMENTO ==========

    @Transactional
    public EquipamentoDTO criarEquipamento(EquipamentoCreateDTO dto) {
        log.info("Criando equipamento NS: {}", dto.getNumeroSerie());

        equipamentoService.validarDuplicidadeNS(dto.getNumeroSerie());
        if (dto.getPatrimonio() != null && !dto.getPatrimonio().isBlank()) {
            equipamentoService.validarDuplicidadePatrimonio(dto.getPatrimonio());
        }

        Equipamento equipamento = mapper.toEntity(dto);
        equipamento.setCodigo(gerarCodigo());
        equipamento.setCatalogoModelo(
                resolverCatalogoModelo(TipoItem.EQUIPAMENTO, dto.getCatalogoModeloId(), dto.getMarca(), dto.getModelo(), dto.getDescricao(), null));

        Equipamento salvo = equipamentoRepository.save(equipamento);
        log.info("Equipamento criado: {} ({})", salvo.getId(), salvo.getCodigo());
        return mapper.toDTO(salvo);
    }

    @Transactional(readOnly = true)
    public EquipamentoDTO buscarEquipamentoPorId(UUID id) {
        return mapper.toDTO(equipamentoService.buscarPorId(id));
    }

    @Transactional(readOnly = true)
    public EquipamentoDTO buscarEquipamentoPorNS(String ns) {
        return mapper.toDTO(equipamentoService.buscarPorNumeroSerie(ns));
    }

    @Transactional(readOnly = true)
    public EquipamentoDTO buscarEquipamentoPorPatrimonio(String patrimonio) {
        return mapper.toDTO(equipamentoService.buscarPorPatrimonio(patrimonio));
    }

    @Transactional(readOnly = true)
    public List<EquipamentoDTO> listarEquipamentosPorEstado(EstadoEquipamento estado) {
        return equipamentoService.listarPorEstado(estado).stream().map(mapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<EquipamentoDTO> listarEquipamentosPorProprietario(ProprietarioEquipamento proprietario) {
        return equipamentoService.listarPorProprietario(proprietario).stream().map(mapper::toDTO).toList();
    }

    @Transactional
    public EquipamentoDTO atualizarEquipamento(UUID id, EquipamentoUpdateDTO dto) {
        Equipamento equipamento = equipamentoService.buscarPorId(id);
        mapper.updateEntityFromDTO(dto, equipamento);
        return mapper.toDTO(equipamentoRepository.save(equipamento));
    }

    @Transactional
    public EquipamentoDTO enviarEquipamentoManutencao(UUID id) {
        return mapper.toDTO(equipamentoService.enviarManutencao(id));
    }

    @Transactional
    public EquipamentoDTO concluirManutencaoEquipamento(UUID id) {
        return mapper.toDTO(equipamentoService.concluirManutencao(id));
    }

    @Transactional
    public EquipamentoDTO marcarEquipamentoDescartado(UUID id) {
        return mapper.toDTO(equipamentoService.marcarDescartado(id));
    }

    // ========== ACESSORIO ==========

    @Transactional
    public AcessorioDTO criarAcessorio(AcessorioCreateDTO dto) {
        log.info("Criando acessório: {}", dto.getDescricao());

        if (dto.getNumeroSerie() != null && !dto.getNumeroSerie().isBlank()) {
            acessorioRepository.findByNumeroSerie(dto.getNumeroSerie()).ifPresent(a -> {
                throw new DomainException("Número de série já cadastrado: " + dto.getNumeroSerie());
            });
        }
        if (dto.getPatrimonio() != null && !dto.getPatrimonio().isBlank()) {
            acessorioRepository.findByPatrimonio(dto.getPatrimonio()).ifPresent(a -> {
                throw new DomainException("Patrimônio já cadastrado: " + dto.getPatrimonio());
            });
        }

        Acessorio acessorio = mapper.toEntity(dto);
        acessorio.setCodigo(gerarCodigo());
        acessorio.setCatalogoModelo(
                resolverCatalogoModelo(TipoItem.ACESSORIO, dto.getCatalogoModeloId(), dto.getMarca(), dto.getModelo(), dto.getDescricao(), dto.getTipoAcessorio()));

        if (acessorio.getCatalogoModelo() != null && acessorio.getCatalogoModelo().getTipoAcessorio() != null) {
            acessorio.setTipoAcessorio(acessorio.getCatalogoModelo().getTipoAcessorio());
        }

        Acessorio salvo = acessorioRepository.save(acessorio);
        log.info("Acessório criado: {} ({})", salvo.getId(), salvo.getCodigo());
        return mapper.toDTO(salvo);
    }

    @Transactional(readOnly = true)
    public AcessorioDTO buscarAcessorioPorId(UUID id) {
        return mapper.toDTO(estoqueService.buscarAcessorioPorId(id));
    }

    @Transactional
    public AcessorioDTO atualizarAcessorio(UUID id, AcessorioUpdateDTO dto) {
        Acessorio acessorio = estoqueService.buscarAcessorioPorId(id);
        mapper.updateEntityFromDTO(dto, acessorio);
        return mapper.toDTO(acessorioRepository.save(acessorio));
    }

    // ========== PECA ==========

    @Transactional
    public PecaDTO criarPeca(PecaCreateDTO dto) {
        log.info("Criando peça: {}", dto.getDescricao());

        Peca peca = mapper.toEntity(dto);
        peca.setCodigo(gerarCodigo());
        peca.setCatalogoModelo(
                resolverCatalogoModelo(TipoItem.PECA, dto.getCatalogoModeloId(), dto.getMarca(), dto.getModelo(), dto.getDescricao(), null));

        Peca salva = pecaRepository.save(peca);
        log.info("Peça criada: {} ({})", salva.getId(), salva.getCodigo());
        return mapper.toDTO(salva);
    }

    @Transactional(readOnly = true)
    public PecaDTO buscarPecaPorId(UUID id) {
        return mapper.toDTO(estoqueService.buscarPecaPorId(id));
    }

    @Transactional
    public PecaDTO atualizarPeca(UUID id, PecaUpdateDTO dto) {
        Peca peca = estoqueService.buscarPecaPorId(id);
        mapper.updateEntityFromDTO(dto, peca);
        return mapper.toDTO(pecaRepository.save(peca));
    }

    // ========== MOVIMENTAÇÃO (ACESSORIO / PECA) ==========

    @Transactional(readOnly = true)
    public Integer consultarSaldo(UUID itemId, TipoItem tipoItem) {
        return estoqueService.consultarSaldo(itemId, tipoItem);
    }

    @Transactional
    public Integer darEntrada(UUID itemId, TipoItem tipoItem, Integer quantidade) {
        Integer saldo = estoqueService.darEntrada(itemId, tipoItem, quantidade);
        log.info("Entrada de {} unidade(s) em {} {}. Saldo: {}", quantidade, tipoItem, itemId, saldo);
        return saldo;
    }

    @Transactional
    public Integer darSaida(UUID itemId, TipoItem tipoItem, Integer quantidade) {
        Integer saldo = estoqueService.darSaida(itemId, tipoItem, quantidade);
        log.info("Saída de {} unidade(s) em {} {}. Saldo: {}", quantidade, tipoItem, itemId, saldo);
        return saldo;
    }

    @Transactional
    public Integer ajustarQuantidade(UUID itemId, TipoItem tipoItem, Integer novaQuantidade) {
        Integer saldo = estoqueService.ajustar(itemId, tipoItem, novaQuantidade);
        log.info("Ajuste de estoque em {} {}. Novo saldo: {}", tipoItem, itemId, saldo);
        return saldo;
    }

    // ========== AUXILIAR ==========

    /**
     * Resolve o CatalogoModelo pelo id informado, ou pelo par marca+modelo
     * (criando uma entrada nova no catálogo se ainda não existir). Retorna
     * null se nenhuma das duas formas foi informada — item fica sem vínculo
     * de catálogo.
     */
    private CatalogoModelo resolverCatalogoModelo(TipoItem tipoItem, UUID catalogoModeloId,
                                                   String marca, String modelo, String descricao,
                                                   com.radiocom.estoque.domain.model.enums.TipoAcessorio tipoAcessorio) {
        if (catalogoModeloId != null) {
            return catalogoModeloRepository.findById(catalogoModeloId)
                    .orElseThrow(() -> new DomainException("Modelo não encontrado no catálogo: " + catalogoModeloId));
        }
        if (marca != null && !marca.isBlank() && modelo != null && !modelo.isBlank()) {
            return catalogoModeloRepository
                    .findByTipoItemAndMarcaIgnoreCaseAndModeloIgnoreCase(tipoItem, marca, modelo)
                    .orElseGet(() -> catalogoModeloRepository.save(
                            CatalogoModelo.builder()
                                    .tipoItem(tipoItem)
                                    .tipoAcessorio(tipoAcessorio)
                                    .marca(marca.trim())
                                    .modelo(modelo.trim())
                                    .descricao(descricao)
                                    .build()));
        }
        return null;
    }

    private String gerarCodigo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
