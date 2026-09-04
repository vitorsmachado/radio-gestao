package com.radiocom.ordemservico.application.mapper;

import com.radiocom.ordemservico.application.dto.*;
import com.radiocom.ordemservico.domain.model.ItemConserto;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.OrdemServico;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrdemServicoMapper {

    // ========== ORDEM DE SERVICO ==========

    public OrdemServicoDTO toDTO(OrdemServico os) {
        if (os == null) return null;
        return OrdemServicoDTO.builder()
                .id(os.getId())
                .numero(os.getNumero())
                .clienteId(os.getClienteId())
                .postoId(os.getPostoId())
                .tecnicoId(os.getTecnicoId())
                .solicitante(os.getSolicitante())
                .recebedorNome(os.getRecebedorNome())
                .status(os.getStatus())
                .dataAbertura(os.getDataAbertura())
                .dataConclusao(os.getDataConclusao())
                .observacoes(os.getObservacoes())
                .build();
    }

    public List<OrdemServicoDTO> toDTOList(Collection<OrdemServico> entities) {
        return entities.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ========== ITEM DE ENTRADA ==========

    public ItemEntrada toEntity(ItemEntradaCreateDTO dto) {
        if (dto == null) return null;
        return ItemEntrada.builder()
                .osId(dto.getOsId())
                .itemEstoqueId(dto.getItemEstoqueId())
                .tipoItem(dto.getTipoItem())
                .descricao(dto.getDescricao())
                .numeroSerie(dto.getNumeroSerie())
                .patrimonio(dto.getPatrimonio())
                .marca(dto.getMarca())
                .modelo(dto.getModelo())
                .defeitoRelatado(dto.getDefeitoRelatado())
                .garantia(dto.isGarantia())
                .build();
    }

    public ItemEntradaDTO toDTO(ItemEntrada item) {
        if (item == null) return null;
        return ItemEntradaDTO.builder()
                .id(item.getId())
                .osId(item.getOsId())
                .orcamentoId(item.getOrcamentoId())
                .itemEstoqueId(item.getItemEstoqueId())
                .tipoItem(item.getTipoItem())
                .descricao(item.getDescricao())
                .numeroSerie(item.getNumeroSerie())
                .patrimonio(item.getPatrimonio())
                .marca(item.getMarca())
                .modelo(item.getModelo())
                .defeitoRelatado(item.getDefeitoRelatado())
                .avaliacaoTecnica(item.getAvaliacaoTecnica())
                .semDefeito(item.isSemDefeito())
                .garantia(item.isGarantia())
                .status(item.getStatus())
                .motivoNaoAutorizado(item.getMotivoNaoAutorizado())
                .itensConserto(toConsertoDTOList(item.getItensConserto()))
                .build();
    }

    public List<ItemEntradaDTO> toItemDTOList(Collection<ItemEntrada> entities) {
        return entities.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ========== ITEM DE CONSERTO ==========

    public ItemConserto toEntity(ItemConsertoCreateDTO dto) {
        if (dto == null) return null;
        return ItemConserto.builder()
                .tipo(dto.getTipo())
                .itemEstoqueId(dto.getItemEstoqueId())
                .descricao(dto.getDescricao())
                .quantidade(dto.getQuantidade())
                .valorUnitario(dto.getValorUnitario())
                .build();
    }

    public ItemConsertoDTO toDTO(ItemConserto item) {
        if (item == null) return null;
        return ItemConsertoDTO.builder()
                .id(item.getId())
                .tipo(item.getTipo())
                .itemEstoqueId(item.getItemEstoqueId())
                .descricao(item.getDescricao())
                .quantidade(item.getQuantidade())
                .valorUnitario(item.getValorUnitario())
                .valorTotal(item.getValorTotal())
                .build();
    }

    public List<ItemConsertoDTO> toConsertoDTOList(Collection<ItemConserto> entities) {
        return entities.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
