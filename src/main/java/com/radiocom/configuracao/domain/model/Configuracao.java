package com.radiocom.configuracao.domain.model;

import com.radiocom.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Configurações gerais do sistema — linha única (sempre existe uma, criada
 * pela migration). Pensado pra crescer conforme surgem outros valores
 * configuráveis, sem precisar de uma tela nova pra cada um.
 */
@Entity
@Table(name = "configuracoes")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Configuracao extends BaseEntity {

    @NotNull
    @Column(name = "valor_mao_de_obra_padrao", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorMaoDeObraPadrao;

    /** Prazo de cobertura (dias) de uma peça trocada num reparo — ver GarantiaPecaDomainService.registrarCobertura. */
    @NotNull
    @Positive
    @Column(name = "prazo_garantia_peca_dias", nullable = false)
    private Integer prazoGarantiaPecaDias;

    /** Prazo padrão (dias) de garantia de fábrica/venda aplicado a um equipamento novo cadastrado automaticamente. */
    @NotNull
    @Positive
    @Column(name = "prazo_garantia_equipamento_dias", nullable = false)
    private Integer prazoGarantiaEquipamentoDias;

    /** Mesma ideia, para acessório. */
    @NotNull
    @Positive
    @Column(name = "prazo_garantia_acessorio_dias", nullable = false)
    private Integer prazoGarantiaAcessorioDias;

    // ===== Dados da empresa — usados no cabeçalho dos documentos gerados (OS, orçamento) =====

    @NotBlank
    @Column(name = "nome_empresa", nullable = false)
    private String nomeEmpresa;

    @Column(name = "razao_social_empresa")
    private String razaoSocialEmpresa;

    @NotBlank
    @Column(name = "documento_empresa", nullable = false, length = 14)
    private String documentoEmpresa;

    @Column(name = "inscricao_estadual_empresa", length = 20)
    private String inscricaoEstadualEmpresa;

    /** Rua/logradouro e número — sem bairro/cidade, que ficam em campos próprios. */
    @Column(name = "endereco_empresa", length = 500)
    private String enderecoEmpresa;

    @Column(name = "bairro_empresa", length = 100)
    private String bairroEmpresa;

    @Column(name = "cidade_empresa", length = 100)
    private String cidadeEmpresa;

    @Column(name = "telefone_empresa", length = 20)
    private String telefoneEmpresa;

    @Column(name = "email_empresa")
    private String emailEmpresa;
}
