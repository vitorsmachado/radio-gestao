package com.radiocom.cliente.application.mapper;

import com.radiocom.cliente.application.dto.*;
import com.radiocom.cliente.domain.model.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClienteMapper {

    private final ModelMapper modelMapper;

    public ClienteMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    // ========== PARA ENTITY ==========

    /**
     * Mapeamento manual do CreateDTO — contatos e postos são adicionados via
     * métodos de domínio para garantir que os relacionamentos bidirecionais
     * fiquem corretos.
     */
    public Cliente toEntity(ClienteCreateDTO dto) {
        if (dto == null) return null;

        Cliente cliente = new Cliente();

        if (dto.getNomeRazaoSocial() != null) {
            cliente.setNomeRazaoSocial(dto.getNomeRazaoSocial());
        }
        if (dto.getNomeFantasia() != null) {
            cliente.setNomeFantasia(dto.getNomeFantasia());
        }
        if (dto.getInscricaoEstadual() != null) {
            cliente.setInscricaoEstadual(dto.getInscricaoEstadual());
        }
        if (dto.getEndereco() != null) {
            cliente.setEndereco(toEntity(dto.getEndereco()));
        }

        if (dto.getContatos() != null) {
            dto.getContatos().forEach(c -> cliente.adicionarContato(toEntity(c)));
        }
        if (dto.getPostos() != null) {
            dto.getPostos().forEach(p -> cliente.adicionarPosto(toEntity(p)));
        }

        return cliente;
    }

    public Endereco toEntity(EnderecoDTO dto) {
        if (dto == null) return null;
        Endereco endereco = modelMapper.map(dto, Endereco.class);

        endereco.setCep(normalizarCep(endereco.getCep())); // sobrescreve após o map
        return endereco;
    }

    public Contato toEntity(ContatoDTO dto) {
        if (dto == null) return null;
        Contato contato = new Contato();

        contato.setNome(dto.getNome());
        contato.setTipo(dto.getTipo());
        contato.setTelefone(normalizarTelefone(dto.getTelefone()));
        contato.setEmail(dto.getEmail());
        contato.setCargo(dto.getCargo());
        contato.setPrincipal(dto.isPrincipal());
        return contato;
    }

    public Posto toEntity(PostoDTO dto) {
        if (dto == null) return null;
        Posto posto = new Posto();

        posto.setNome(dto.getNome());
        posto.setResponsavel(dto.getResponsavel());
        posto.setPadrao(dto.isPadrao());
        if (dto.getEndereco() != null) {
            posto.setEndereco(toEntity(dto.getEndereco()));
        }
        return posto;
    }

    // ========== PARA DTO ==========

    public ClienteDTO toDTO(Cliente entity) {
        return modelMapper.map(entity, ClienteDTO.class);
    }

    public EnderecoDTO toDTO(Endereco entity) {
        return modelMapper.map(entity, EnderecoDTO.class);
    }

    public ContatoDTO toDTO(Contato entity) {
        return modelMapper.map(entity, ContatoDTO.class);
    }

    public PostoDTO toDTO(Posto entity) {
        return modelMapper.map(entity, PostoDTO.class);
    }

    // ========== COLEÇÕES ==========

    public List<ClienteDTO> toDTOList(Collection<Cliente> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ContatoDTO> toContatoDTOList(Collection<Contato> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PostoDTO> toPostoDTOList(Collection<Posto> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ========== ATUALIZAÇÃO ==========

    /**
     * Atualização parcial: aplica apenas campos não-nulos.
     * tipo, documento, id e campos de auditoria são imutáveis.
     */
    public void updateEntityFromDTO(ClienteUpdateDTO dto, Cliente entity) {
        if (dto == null) return;
        if (dto.getNomeRazaoSocial() != null) entity.setNomeRazaoSocial(dto.getNomeRazaoSocial());
        if (dto.getNomeFantasia() != null) entity.setNomeFantasia(dto.getNomeFantasia());
        if (dto.getInscricaoEstadual() != null) entity.setInscricaoEstadual(dto.getInscricaoEstadual());
        if (dto.getEndereco() != null) entity.setEndereco(toEntity(dto.getEndereco()));
    }

    private String normalizarTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) return null;
        return telefone.replaceAll("[^\\d]", "");
    }

    private String normalizarCep(String cep) {
        if (cep == null || cep.isBlank()) return null;
        return cep.replaceAll("[^\\d]", "");
    }
}
