package com.radiocom.cliente.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * Atualização parcial de cliente (PATCH semântico via PUT) — apenas campos
 * não-nulos são aplicados.
 *
 * Campos intencionalmente ausentes:
 *   - tipo: imutável após cadastro (PF não vira PJ e vice-versa)
 *   - documento: imutável após cadastro
 *   - status: use os endpoints dedicados PATCH /{id}/ativar, /inativar, /bloquear
 *   - contatos/postos: use os endpoints dedicados de gestão de contatos e postos
 *   - id, dataCriacao, dataAtualizacao: somente leitura
 */
@Data
@Builder
public class ClienteUpdateDTO {

    @Size(max = 255, message = "Nome/Razão Social deve ter no máximo 255 caracteres")
    private String nomeRazaoSocial;

    @Size(max = 255, message = "Nome Fantasia deve ter no máximo 255 caracteres")
    private String nomeFantasia;

    @Size(max = 20, message = "Inscrição Estadual deve ter no máximo 20 caracteres")
    private String inscricaoEstadual;

    @Valid
    private EnderecoDTO endereco;
}
