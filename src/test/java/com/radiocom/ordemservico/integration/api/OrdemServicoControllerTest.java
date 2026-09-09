package com.radiocom.ordemservico.integration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiocom.auth.application.service.JwtService;
import com.radiocom.config.SecurityConfig;
import com.radiocom.ordemservico.application.dto.*;
import com.radiocom.ordemservico.application.service.OrdemServicoApplicationService;
import com.radiocom.ordemservico.application.service.OrdemServicoPdfService;
import com.radiocom.ordemservico.domain.model.enums.StatusOS;
import com.radiocom.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = com.radiocom.ordemservico.interfaces.rest.OrdemServicoController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@WithMockUser
@DisplayName("OrdemServicoController - Testes de API")
class OrdemServicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrdemServicoApplicationService service;

    @MockBean
    private OrdemServicoPdfService pdfService;

    @MockBean
    private JwtService jwtService;

    private UUID osId;
    private UUID clienteId;
    private OrdemServicoDTO osDTO;

    @BeforeEach
    void setUp() {
        osId = UUID.randomUUID();
        clienteId = UUID.randomUUID();
        osDTO = OrdemServicoDTO.builder()
                .id(osId)
                .numero("OS-2026-0001")
                .clienteId(clienteId)
                .status(StatusOS.ABERTA)
                .build();
    }

    @Test
    @DisplayName("POST / deve retornar 201")
    void criar_deveRetornar201() throws Exception {
        OrdemServicoCreateDTO dto = OrdemServicoCreateDTO.builder()
                .clienteId(clienteId).solicitante("João da Silva").build();

        when(service.criar(any(OrdemServicoCreateDTO.class))).thenReturn(osDTO);

        mockMvc.perform(post("/v1/ordens-servico")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value("OS-2026-0001"));
    }

    @Test
    @DisplayName("GET /{id} deve retornar 200")
    void buscarPorId_deveRetornar200() throws Exception {
        when(service.buscarPorId(osId)).thenReturn(osDTO);

        mockMvc.perform(get("/v1/ordens-servico/{id}", osId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value("OS-2026-0001"));
    }

    @Test
    @DisplayName("GET /{id} deve retornar 400 quando não existe")
    void buscarPorId_deveRetornar400QuandoNaoExiste() throws Exception {
        when(service.buscarPorId(osId)).thenThrow(new DomainException("Ordem de Serviço não encontrada: " + osId));

        mockMvc.perform(get("/v1/ordens-servico/{id}", osId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET ?clienteId= deve retornar 200 com a lista")
    void listarPorCliente_deveRetornar200() throws Exception {
        when(service.listarPorCliente(clienteId)).thenReturn(List.of(osDTO));

        mockMvc.perform(get("/v1/ordens-servico").param("clienteId", clienteId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numero").value("OS-2026-0001"));
    }

    @Test
    @DisplayName("PATCH /{id}/confirmar-entrega deve retornar 200")
    void confirmarEntrega_deveRetornar200() throws Exception {
        osDTO.setStatus(StatusOS.CONCLUIDA);
        osDTO.setRecebedorNome("Maria Souza");
        when(service.confirmarEntrega(eq(osId), any(ConfirmarEntregaDTO.class))).thenReturn(osDTO);

        mockMvc.perform(patch("/v1/ordens-servico/{id}/confirmar-entrega", osId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                ConfirmarEntregaDTO.builder().nomeRecebedor("Maria Souza").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recebedorNome").value("Maria Souza"));
    }

    @Test
    @DisplayName("POST /{id}/dividir deve retornar 201 com a nova OS")
    void dividir_deveRetornar201() throws Exception {
        OrdemServicoDTO novaOS = OrdemServicoDTO.builder()
                .id(UUID.randomUUID()).numero("OS-2026-0002").clienteId(clienteId).build();
        when(service.dividir(eq(osId), any(DividirOSDTO.class))).thenReturn(novaOS);

        DividirOSDTO dto = DividirOSDTO.builder()
                .itemIds(List.of(UUID.randomUUID())).solicitante("Técnico João").build();

        mockMvc.perform(post("/v1/ordens-servico/{id}/dividir", osId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value("OS-2026-0002"));
    }

    @Test
    @DisplayName("GET /{id} sem autenticação deve retornar 401")
    @WithAnonymousUser
    void buscarPorId_semAutenticacao_deveRetornar401() throws Exception {
        mockMvc.perform(get("/v1/ordens-servico/{id}", osId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /{id}/pdf deve retornar 200 com application/pdf")
    void gerarPdf_deveRetornar200ComApplicationPdf() throws Exception {
        byte[] pdfFalso = "%PDF-1.4 fake".getBytes();
        when(pdfService.gerarPdf(osId)).thenReturn(pdfFalso);

        mockMvc.perform(get("/v1/ordens-servico/{id}/pdf", osId))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .contentType(MediaType.APPLICATION_PDF));
    }
}
