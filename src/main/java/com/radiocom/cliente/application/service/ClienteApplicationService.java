package com.radiocom.cliente.application.service;

import com.radiocom.cliente.application.dto.ClienteCreateDTO;
import com.radiocom.cliente.application.dto.ClienteDTO;
import com.radiocom.cliente.application.dto.ClienteUpdateDTO;
import com.radiocom.cliente.application.dto.ConsultaCnpjDTO;
import com.radiocom.cliente.application.dto.ContatoDTO;
import com.radiocom.cliente.application.dto.EnderecoDTO;
import com.radiocom.cliente.application.dto.MotivoDTO;
import com.radiocom.cliente.application.dto.ContatoUpdateDTO;
import com.radiocom.cliente.application.dto.PostoDTO;
import com.radiocom.cliente.application.dto.PostoUpdateDTO;
import com.radiocom.cliente.application.mapper.ClienteMapper;
import com.radiocom.cliente.domain.model.Cliente;
import com.radiocom.cliente.domain.model.Contato;
import com.radiocom.cliente.domain.model.Posto;
import com.radiocom.cliente.domain.model.enums.StatusCliente;
import com.radiocom.cliente.domain.model.enums.TipoPessoa;
import com.radiocom.cliente.domain.repository.ClienteRepository;
import com.radiocom.cliente.domain.service.ClienteDomainService;
import com.radiocom.cliente.domain.service.NumeroClienteGenerator;
import com.radiocom.cliente.infrastructure.external.ReceitaWSClient;
import com.radiocom.cliente.infrastructure.external.dto.ReceitaWSResponse;
import com.radiocom.estoque.application.service.EstoqueApplicationService;
import com.radiocom.shared.exception.DomainException;
import com.radiocom.shared.validation.CpfCnpjValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteApplicationService {

    /** UUID sentinela — usado no lugar de uma lista vazia pra evitar "IN ()" no SQL quando nenhum item bate com a busca. */
    private static final UUID ID_INEXISTENTE = new UUID(0, 0);

    private final ClienteRepository clienteRepository;
    private final ClienteDomainService domainService;
    private final NumeroClienteGenerator numeroClienteGenerator;
    private final EstoqueApplicationService estoqueApplicationService;
    private final ReceitaWSClient receitaWSClient;
    private final ClienteMapper mapper;

    @Transactional
    public ClienteDTO criar(ClienteCreateDTO dto) {
        log.info("Criando cliente: {}", dto.getDocumento());

        String documentoLimpo = CpfCnpjValidator.clean(dto.getDocumento());

        if (!CpfCnpjValidator.isValid(documentoLimpo)) {
            throw new DomainException("Documento inválido: " + dto.getDocumento());
        }

        TipoPessoa tipo = dto.getTipo() != null ?
                dto.getTipo() :
                CpfCnpjValidator.getTipo(documentoLimpo);

        domainService.validarDocumentoUnico(documentoLimpo, null);

        Cliente cliente = mapper.toEntity(dto);
        cliente.setDocumento(documentoLimpo);
        cliente.setTipo(tipo);
        cliente.setNumeroIdentificacao(numeroClienteGenerator.gerarNumero());

        Cliente salvo = clienteRepository.save(cliente);
        log.info("Cliente criado: {} - {}", salvo.getId(),
                CpfCnpjValidator.format(salvo.getDocumento()));

        return mapper.toDTO(salvo);
    }

    @Transactional(readOnly = true)
    public ClienteDTO buscarPorId(UUID id) {
        return mapper.toDTO(domainService.buscarPorId(id));
    }

    /** Consulta dados públicos de um CNPJ na ReceitaWS pra pré-preencher o formulário de cliente. */
    @Transactional(readOnly = true)
    public ConsultaCnpjDTO consultarCNPJ(String cnpj) {
        String cnpjLimpo = CpfCnpjValidator.clean(cnpj);
        ReceitaWSResponse dados = receitaWSClient.consultarCNPJ(cnpjLimpo)
                .orElseThrow(() -> new DomainException("Não foi possível consultar o CNPJ na Receita Federal: " + cnpj));

        boolean temEndereco = dados.getLogradouro() != null && !dados.getLogradouro().isBlank();

        return ConsultaCnpjDTO.builder()
                .nomeRazaoSocial(dados.getNome())
                .nomeFantasia(dados.getFantasia())
                .endereco(temEndereco
                        ? EnderecoDTO.builder()
                                .cep(dados.getCep() != null ? dados.getCep().replaceAll("\\D", "") : null)
                                .logradouro(dados.getLogradouro())
                                .numero(dados.getNumero())
                                .complemento(dados.getComplemento())
                                .bairro(dados.getBairro())
                                .cidade(dados.getMunicipio())
                                .estado(dados.getUf())
                                .build()
                        : null)
                .build();
    }

    /** Equipamentos e acessórios de propriedade do cliente, pra aba Garantia do detalhe. */
    @Transactional(readOnly = true)
    public List<com.radiocom.estoque.application.dto.ItemGarantiaDTO> listarItensGarantia(UUID id) {
        domainService.buscarPorId(id); // valida que o cliente existe
        return estoqueApplicationService.listarItensDoCliente(id);
    }

    @Transactional(readOnly = true)
    public ClienteDTO buscarPorIdCompleto(UUID id) {
        return mapper.toDTO(domainService.buscarPorIdComRelacionamentos(id));
    }

    @Transactional(readOnly = true)
    public ClienteDTO buscarPorDocumento(String documento) {
        return mapper.toDTO(domainService.buscarPorDocumento(documento));
    }

    /**
     * Listagem geral com busca opcional (nome/razão social, nome fantasia,
     * documento, número de identificação, nome de posto ou de contato) e
     * filtro opcional por status.
     */
    @Transactional(readOnly = true)
    public Page<ClienteDTO> listar(String busca, StatusCliente status, Pageable pageable) {
        String buscaTratada = busca != null && !busca.isBlank() ? busca.trim() : null;

        List<UUID> itemClienteIdsMatched = List.of(ID_INEXISTENTE);
        if (buscaTratada != null) {
            List<UUID> encontrados = estoqueApplicationService.buscarClienteIdsPorNumeroSerieOuCodigoCliente(buscaTratada);
            if (!encontrados.isEmpty()) {
                itemClienteIdsMatched = encontrados;
            }
        }

        return clienteRepository.buscar(buscaTratada, itemClienteIdsMatched, status, pageable).map(mapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> listarPorStatus(StatusCliente status) {
        return clienteRepository.findByStatus(status, Pageable.unpaged())
                .getContent()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ClienteDTO> buscarPorNome(String nome, Pageable pageable) {
        return clienteRepository.findByNomeRazaoSocialContainingIgnoreCase(nome, pageable)
                .map(mapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ClienteDTO> buscarPorTipo(TipoPessoa tipo, Pageable pageable) {
        return clienteRepository.findByTipo(tipo, pageable)
                .map(mapper::toDTO);
    }

    /** Usado por outros módulos (ex: busca de OS) pra resolver clientes por nome ou documento. */
    @Transactional(readOnly = true)
    public List<ClienteDTO> buscarPorNomeOuDocumento(String busca) {
        return clienteRepository.buscarPorNomeOuDocumento(busca).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    /** Usado por outros módulos pra resolver em lote os dados de exibição (nome/documento) de um conjunto de clientes. */
    @Transactional(readOnly = true)
    public List<ClienteDTO> buscarPorIds(List<UUID> ids) {
        return clienteRepository.findAllById(ids).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClienteDTO atualizar(UUID id, ClienteUpdateDTO dto) {
        log.info("Atualizando cliente: {}", id);

        Cliente cliente = domainService.buscarPorId(id);

        if (dto.getNumeroIdentificacao() != null
                && !dto.getNumeroIdentificacao().equals(cliente.getNumeroIdentificacao())
                && clienteRepository.existsByNumeroIdentificacaoAndIdNot(dto.getNumeroIdentificacao(), id)) {
            throw new DomainException("Número de identificação já está em uso: " + dto.getNumeroIdentificacao());
        }

        // tipo e documento são imutáveis — use os endpoints dedicados para
        // transições de status (/ativar, /inativar, /bloquear)
        mapper.updateEntityFromDTO(dto, cliente);

        Cliente atualizado = clienteRepository.save(cliente);
        log.info("Cliente atualizado: {} - {}", atualizado.getId(),
                CpfCnpjValidator.format(atualizado.getDocumento()));

        return mapper.toDTO(atualizado);
    }

    @Transactional
    public ClienteDTO ativar(UUID id, MotivoDTO dto) {
        return mapper.toDTO(domainService.ativarCliente(id, dto != null ? dto.getMotivo() : null));
    }

    @Transactional
    public ClienteDTO bloquear(UUID id, MotivoDTO dto) {
        return mapper.toDTO(domainService.bloquearCliente(id, dto != null ? dto.getMotivo() : null));
    }

    @Transactional
    public ClienteDTO inativar(UUID id, MotivoDTO dto) {
        return mapper.toDTO(domainService.inativarCliente(id, dto != null ? dto.getMotivo() : null));
    }

    // ========== GESTÃO DE POSTOS ==========

    @Transactional(readOnly = true)
    public List<PostoDTO> listarPostos(UUID clienteId) {
        Cliente cliente = domainService.buscarPorIdComRelacionamentos(clienteId);
        return mapper.toPostoDTOList(cliente.getPostos());
    }

    @Transactional
    public PostoDTO adicionarPosto(UUID clienteId, PostoDTO dto) {
        Cliente cliente = domainService.buscarPorId(clienteId);
        Posto novoPosto = mapper.toEntity(dto);
        cliente.adicionarPosto(novoPosto);
        clienteRepository.save(cliente);
        return mapper.toDTO(novoPosto); // JPA preenche o id no novoPosto via cascade
    }

    @Transactional
    public ClienteDTO removerPosto(UUID clienteId, UUID postoId) {
        Cliente cliente = domainService.buscarPorId(clienteId);
        cliente.removerPostoPorId(postoId);
        return mapper.toDTO(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteDTO atualizarPosto(UUID clienteId, UUID postoId, PostoUpdateDTO dto) {
        Cliente cliente = domainService.buscarPorId(clienteId);

        Posto posto = cliente.getPostos().stream()
                .filter(p -> p.getId().equals(postoId))
                .findFirst()
                .orElseThrow(() -> new DomainException("Posto não encontrado: " + postoId));

        if (dto.getNome() != null) posto.setNome(dto.getNome());
        if (dto.getResponsavel() != null) posto.setResponsavel(dto.getResponsavel());
        if (dto.getEndereco() != null) posto.setEndereco(mapper.toEntity(dto.getEndereco()));
        if (Boolean.TRUE.equals(dto.getPadrao())) {
            // Garante que só um posto seja padrão por cliente
            cliente.getPostos().forEach(p -> p.setPadrao(false));
            posto.setPadrao(true);
        }

        return mapper.toDTO(clienteRepository.save(cliente));
    }

    // ========== GESTÃO DE CONTATOS ==========

    @Transactional
    public ClienteDTO adicionarContato(UUID clienteId, ContatoDTO dto) {
        Cliente cliente = domainService.buscarPorId(clienteId);
        cliente.adicionarContato(mapper.toEntity(dto));
        return mapper.toDTO(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteDTO removerContato(UUID clienteId, UUID contatoId) {
        Cliente cliente = domainService.buscarPorId(clienteId);
        cliente.removerContatoPorId(contatoId);
        return mapper.toDTO(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteDTO atualizarContato(UUID clienteId, UUID contatoId, ContatoUpdateDTO dto) {
        Cliente cliente = domainService.buscarPorId(clienteId);

        Contato contato = cliente.getContatos().stream()
                .filter(c -> c.getId().equals(contatoId))
                .findFirst()
                .orElseThrow(() -> new DomainException("Contato não encontrado: " + contatoId));

        if (dto.getNome() != null) contato.setNome(dto.getNome());
        if (dto.getTipo() != null) contato.setTipo(dto.getTipo());
        if (dto.getTelefone() != null) contato.setTelefone(dto.getTelefone());
        if (dto.getEmail() != null) contato.setEmail(dto.getEmail());
        if (dto.getCargo() != null) contato.setCargo(dto.getCargo());

        return mapper.toDTO(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteDTO atualizarContatoPrincipal(UUID clienteId, UUID contatoId) {
        Cliente cliente = domainService.buscarPorId(clienteId);
        cliente.atualizarContatoPrincipal(contatoId);
        return mapper.toDTO(clienteRepository.save(cliente));
    }
}
