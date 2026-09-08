package com.radiocom.orcamento.integration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiocom.auth.application.service.JwtService;
import com.radiocom.config.SecurityConfig;
import com.radiocom.ordemservico.application.dto.MotivoDTO;
import com.radiocom.orcamento.application.dto.AdicionarItemOrcamentoDTO;
import com.radiocom.orcamento.application.dto.OrcamentoCreateDTO;
import com.radiocom.orcamento.application.dto.OrcamentoDTO;
import com.radiocom.orcamento.application.service.OrcamentoApplicationService;
import com.radiocom.orcamento.domain.model.enums.StatusOrcamento;
import com.radiocom.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = com.radiocom.orcamento.interfaces.rest.OrcamentoController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@WithMockUser
@DisplayName("OrcamentoController - Testes de API")
class OrcamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrcamentoApplicationService service;

    @MockBean
    private JwtService jwtService;

    private UUID orcamentoId;
    private UUID osId;
    private OrcamentoDTO orcamentoDTO;

    @BeforeEach
    void setUp() {
        orcamentoId = UUID.randomUUID();
        osId = UUID.randomUUID();
        orcamentoDTO = OrcamentoDTO.builder()
                .id(orcamentoId)
                .numero("ORC-2026-0001")
                .osId(osId)
                .status(StatusOrcamento.RASCUNHO)
                .valorTotal(BigDecimal.ZERO)
                .itens(List.of())
                .build();
    }

    @Test
    @DisplayName("POST / deve retornar 201")
    void criar_deveRetornar201() throws Exception {
        OrcamentoCreateDTO dto = OrcamentoCreateDTO.builder()
                .osId(osId).clienteId(UUID.randomUUID()).build();
        when(service.criar(any(OrcamentoCreateDTO.class))).thenReturn(orcamentoDTO);

        mockMvc.perform(post("/v1/orcamentos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value("ORC-2026-0001"));
    }

    @Test
    @DisplayName("GET /{id} deve retornar 200")
    void buscarPorId_deveRetornar200() throws Exception {
        when(service.buscarPorId(orcamentoId)).thenReturn(orcamentoDTO);

        mockMvc.perform(get("/v1/orcamentos/{id}", orcamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value("ORC-2026-0001"));
    }

    @Test
    @DisplayName("GET /{id} deve retornar 400 quando não existe")
    void buscarPorId_deveRetornar400QuandoNaoExiste() throws Exception {
        when(service.buscarPorId(orcamentoId)).thenThrow(new DomainException("Orçamento não encontrado: " + orcamentoId));

        mockMvc.perform(get("/v1/orcamentos/{id}", orcamentoId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET ?osId= deve retornar 200 com a lista")
    void listarPorOS_deveRetornar200() throws Exception {
        when(service.listarPorOS(osId)).thenReturn(List.of(orcamentoDTO));

        mockMvc.perform(get("/v1/orcamentos").param("osId", osId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numero").value("ORC-2026-0001"));
    }

    @Test
    @DisplayName("POST /{id}/itens deve retornar 200 com o orçamento atualizado")
    void adicionarItem_deveRetornar200() throws Exception {
        UUID itemId = UUID.randomUUID();
        when(service.adicionarItem(eq(orcamentoId), any(AdicionarItemOrcamentoDTO.class))).thenReturn(orcamentoDTO);

        mockMvc.perform(post("/v1/orcamentos/{id}/itens", orcamentoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                AdicionarItemOrcamentoDTO.builder().itemEntradaId(itemId).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value("ORC-2026-0001"));
    }

    @Test
    @DisplayName("POST /{id}/itens deve retornar 400 quando item é de outra OS")
    void adicionarItem_deveRetornar400QuandoOutraOS() throws Exception {
        UUID itemId = UUID.randomUUID();
        when(service.adicionarItem(eq(orcamentoId), any(AdicionarItemOrcamentoDTO.class)))
                .thenThrow(new DomainException("Item não pertence à mesma OS do orçamento"));

        mockMvc.perform(post("/v1/orcamentos/{id}/itens", orcamentoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                AdicionarItemOrcamentoDTO.builder().itemEntradaId(itemId).build())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /{id}/itens/{itemEntradaId} deve retornar 200")
    void removerItem_deveRetornar200() throws Exception {
        UUID itemId = UUID.randomUUID();
        when(service.removerItem(orcamentoId, itemId)).thenReturn(orcamentoDTO);

        mockMvc.perform(delete("/v1/orcamentos/{id}/itens/{itemEntradaId}", orcamentoId, itemId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value("ORC-2026-0001"));
    }

    @Test
    @DisplayName("PATCH /{id}/enviar deve retornar 200")
    void enviar_deveRetornar200() throws Exception {
        orcamentoDTO.setStatus(StatusOrcamento.ENVIADO);
        when(service.enviar(orcamentoId)).thenReturn(orcamentoDTO);

        mockMvc.perform(patch("/v1/orcamentos/{id}/enviar", orcamentoId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENVIADO"));
    }

    @Test
    @DisplayName("PATCH /{id}/enviar deve retornar 400 quando não há itens")
    void enviar_deveRetornar400QuandoSemItens() throws Exception {
        when(service.enviar(orcamentoId)).thenThrow(new DomainException("Selecione ao menos um item para enviar o orçamento"));

        mockMvc.perform(patch("/v1/orcamentos/{id}/enviar", orcamentoId).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /{id}/cancelar deve retornar 200")
    void cancelar_deveRetornar200() throws Exception {
        orcamentoDTO.setStatus(StatusOrcamento.CANCELADO);
        when(service.cancelar(eq(orcamentoId), any(MotivoDTO.class))).thenReturn(orcamentoDTO);

        mockMvc.perform(patch("/v1/orcamentos/{id}/cancelar", orcamentoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(MotivoDTO.builder().motivo("Cliente desistiu").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }
}
