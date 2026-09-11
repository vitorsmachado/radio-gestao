package com.radiocom.estoque.application.mapper;

import com.radiocom.estoque.application.dto.*;
import com.radiocom.estoque.domain.model.*;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import org.springframework.stereotype.Component;

@Component
public class EstoqueMapper {

    // ========== EQUIPAMENTO ==========

    public Equipamento toEntity(EquipamentoCreateDTO dto) {
        if (dto == null) return null;
        return Equipamento.builder()
                .tipo(TipoItem.EQUIPAMENTO)
                .descricao(dto.getDescricao())
                .proprietario(dto.getProprietario())
                .numeroSerie(dto.getNumeroSerie())
                .patrimonio(dto.getPatrimonio())
                .clienteId(dto.getClienteId())
                .faixa(dto.getFaixa())
                .garantiaFim(dto.getGarantiaFim())
                .especificacoes(dto.getEspecificacoes() != null
                        ? new java.util.HashMap<>(dto.getEspecificacoes())
                        : new java.util.HashMap<>())
                .build();
    }

    public EquipamentoDTO toDTO(Equipamento e) {
        if (e == null) return null;
        return EquipamentoDTO.builder()
                .id(e.getId())
                .codigo(e.getCodigo())
                .descricao(e.getDescricao())
                .proprietario(e.getProprietario())
                .numeroSerie(e.getNumeroSerie())
                .patrimonio(e.getPatrimonio())
                .clienteId(e.getClienteId())
                .faixa(e.getFaixa())
                .estado(e.getEstado())
                .status(e.getStatus())
                .garantiaFim(e.getGarantiaFim())
                .especificacoes(e.getEspecificacoes())
                .catalogoModeloId(e.getCatalogoModelo() != null ? e.getCatalogoModelo().getId() : null)
                .dataCriacao(e.getDataCriacao())
                .dataAtualizacao(e.getDataAtualizacao())
                .build();
    }

    public void updateEntityFromDTO(EquipamentoUpdateDTO dto, Equipamento e) {
        if (dto == null) return;
        if (dto.getDescricao() != null) e.setDescricao(dto.getDescricao());
        if (dto.getGarantiaFim() != null) e.setGarantiaFim(dto.getGarantiaFim());
        if (dto.getEspecificacoes() != null) e.setEspecificacoes(dto.getEspecificacoes());
        if (dto.getObservacoes() != null) e.setObservacoes(dto.getObservacoes());
        if (dto.getLocalizacaoFisica() != null) e.setLocalizacaoFisica(dto.getLocalizacaoFisica());
    }

    // ========== ACESSORIO ==========

    public Acessorio toEntity(AcessorioCreateDTO dto) {
        if (dto == null) return null;
        return Acessorio.builder()
                .tipo(TipoItem.ACESSORIO)
                .descricao(dto.getDescricao())
                .tipoAcessorio(dto.getTipoAcessorio())
                .proprietario(dto.getProprietario())
                .clienteId(dto.getClienteId())
                .numeroSerie(dto.getNumeroSerie())
                .patrimonio(dto.getPatrimonio())
                .quantidadeDisponivel(dto.getQuantidadeDisponivel() != null ? dto.getQuantidadeDisponivel() : 0)
                .quantidadeMinima(dto.getQuantidadeMinima())
                .garantiaFim(dto.getGarantiaFim())
                .build();
    }

    public AcessorioDTO toDTO(Acessorio a) {
        if (a == null) return null;
        return AcessorioDTO.builder()
                .id(a.getId())
                .codigo(a.getCodigo())
                .descricao(a.getDescricao())
                .tipoAcessorio(a.getTipoAcessorio())
                .proprietario(a.getProprietario())
                .clienteId(a.getClienteId())
                .numeroSerie(a.getNumeroSerie())
                .patrimonio(a.getPatrimonio())
                .quantidadeDisponivel(a.getQuantidadeDisponivel())
                .quantidadeMinima(a.getQuantidadeMinima())
                .estado(a.getEstado())
                .status(a.getStatus())
                .garantiaFim(a.getGarantiaFim())
                .catalogoModeloId(a.getCatalogoModelo() != null ? a.getCatalogoModelo().getId() : null)
                .possuiNumeroSerie(a.possuiNumeroSerie())
                .possuiPatrimonio(a.possuiPatrimonio())
                .dataCriacao(a.getDataCriacao())
                .dataAtualizacao(a.getDataAtualizacao())
                .build();
    }

    public void updateEntityFromDTO(AcessorioUpdateDTO dto, Acessorio a) {
        if (dto == null) return;
        if (dto.getDescricao() != null) a.setDescricao(dto.getDescricao());
        if (dto.getQuantidadeMinima() != null) a.setQuantidadeMinima(dto.getQuantidadeMinima());
        if (dto.getGarantiaFim() != null) a.setGarantiaFim(dto.getGarantiaFim());
        if (dto.getObservacoes() != null) a.setObservacoes(dto.getObservacoes());
        if (dto.getLocalizacaoFisica() != null) a.setLocalizacaoFisica(dto.getLocalizacaoFisica());
    }

    // ========== PECA ==========

    public Peca toEntity(PecaCreateDTO dto) {
        if (dto == null) return null;
        return Peca.builder()
                .tipo(TipoItem.PECA)
                .descricao(dto.getDescricao())
                .quantidadeDisponivel(dto.getQuantidadeDisponivel() != null ? dto.getQuantidadeDisponivel() : 0)
                .quantidadeMinima(dto.getQuantidadeMinima())
                .build();
    }

    public PecaDTO toDTO(Peca p) {
        if (p == null) return null;
        return PecaDTO.builder()
                .id(p.getId())
                .codigo(p.getCodigo())
                .descricao(p.getDescricao())
                .quantidadeDisponivel(p.getQuantidadeDisponivel())
                .quantidadeMinima(p.getQuantidadeMinima())
                .status(p.getStatus())
                .catalogoModeloId(p.getCatalogoModelo() != null ? p.getCatalogoModelo().getId() : null)
                .dataCriacao(p.getDataCriacao())
                .dataAtualizacao(p.getDataAtualizacao())
                .build();
    }

    public void updateEntityFromDTO(PecaUpdateDTO dto, Peca p) {
        if (dto == null) return;
        if (dto.getDescricao() != null) p.setDescricao(dto.getDescricao());
        if (dto.getQuantidadeMinima() != null) p.setQuantidadeMinima(dto.getQuantidadeMinima());
        if (dto.getObservacoes() != null) p.setObservacoes(dto.getObservacoes());
        if (dto.getLocalizacaoFisica() != null) p.setLocalizacaoFisica(dto.getLocalizacaoFisica());
    }

    // ========== CATALOGO MODELO ==========

    public CatalogoModelo toEntity(CatalogoModeloCreateDTO dto) {
        if (dto == null) return null;
        return CatalogoModelo.builder()
                .tipoItem(dto.getTipoItem())
                .tipoAcessorio(dto.getTipoAcessorio())
                .referencia(dto.getReferencia() != null ? dto.getReferencia().trim() : null)
                .marca(dto.getMarca().trim())
                .modelo(dto.getModelo().trim())
                .descricao(dto.getDescricao())
                .valorReferencia(dto.getValorReferencia())
                .controlePorSerie(dto.isControlePorSerie())
                .possuiPatrimonio(dto.isPossuiPatrimonio())
                .build();
    }

    public CatalogoModeloDTO toDTO(CatalogoModelo cm) {
        if (cm == null) return null;
        return CatalogoModeloDTO.builder()
                .id(cm.getId())
                .tipoItem(cm.getTipoItem())
                .tipoAcessorio(cm.getTipoAcessorio())
                .referencia(cm.getReferencia())
                .marca(cm.getMarca())
                .modelo(cm.getModelo())
                .descricao(cm.getDescricao())
                .valorReferencia(cm.getValorReferencia())
                .status(cm.getStatus())
                .controlePorSerie(cm.isControlePorSerie())
                .possuiPatrimonio(cm.isPossuiPatrimonio())
                .dataCriacao(cm.getDataCriacao())
                .dataAtualizacao(cm.getDataAtualizacao())
                .build();
    }

    public void updateEntityFromDTO(CatalogoModeloUpdateDTO dto, CatalogoModelo cm) {
        if (dto == null) return;
        if (dto.getTipoAcessorio() != null) cm.setTipoAcessorio(dto.getTipoAcessorio());
        if (dto.getReferencia() != null) cm.setReferencia(dto.getReferencia().isBlank() ? null : dto.getReferencia().trim());
        if (dto.getDescricao() != null) cm.setDescricao(dto.getDescricao());
        if (dto.getValorReferencia() != null) cm.setValorReferencia(dto.getValorReferencia());
        if (dto.getStatus() != null) cm.setStatus(dto.getStatus());
        if (dto.getControlePorSerie() != null) cm.setControlePorSerie(dto.getControlePorSerie());
        if (dto.getPossuiPatrimonio() != null) cm.setPossuiPatrimonio(dto.getPossuiPatrimonio());
    }
}
