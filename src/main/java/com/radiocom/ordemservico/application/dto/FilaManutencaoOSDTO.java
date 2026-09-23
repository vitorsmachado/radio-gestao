package com.radiocom.ordemservico.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Uma OS na fila de manutenção do técnico, já com os itens relevantes e na
 * ordem final de exibição. {@code bloco} agrupa visualmente (1 = em
 * avaliação, 2 = pronta pra manutenção, 3 = aguardando avaliação, 4 =
 * aguardando peça confirmado) — setas/arrastar reordenam só dentro do mesmo
 * bloco.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilaManutencaoOSDTO {

    private UUID osId;
    private String osNumero;
    private UUID clienteId;
    private String clienteNome;
    private int bloco;
    private List<ItemEntradaDTO> itens;
}
