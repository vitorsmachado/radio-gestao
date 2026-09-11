package com.radiocom.estoque.integration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiocom.auth.application.service.JwtService;
import com.radiocom.config.SecurityConfig;
import com.radiocom.estoque.application.dto.CatalogoModeloCreateDTO;
import com.radiocom.estoque.application.dto.CatalogoModeloDTO;
import com.radiocom.estoque.application.service.CatalogoModeloService;
import com.radiocom.estoque.domain.model.enums.StatusItem;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.shared.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
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

@WebMvcTest(controllers = com.radiocom.estoque.interfaces.rest.CatalogoModeloController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@WithMockUser
@DisplayName("CatalogoModeloController - Testes de API")
class CatalogoModeloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CatalogoModeloService service;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("POST /v1/catalogo-modelos deve retornar 201")
    void criar_deveRetornar201() throws Exception {
        CatalogoModeloCreateDTO dto = CatalogoModeloCreateDTO.builder()
                .tipoItem(TipoItem.EQUIPAMENTO)
                .marca("Motorola")
                .modelo("EP450")
                .build();

        CatalogoModeloDTO resultado = CatalogoModeloDTO.builder()
                .id(UUID.randomUUID())
                .tipoItem(TipoItem.EQUIPAMENTO)
                .marca("Motorola")
                .modelo("EP450")
                .build();

        when(service.criar(any(CatalogoModeloCreateDTO.class))).thenReturn(resultado);

        mockMvc.perform(post("/v1/catalogo-modelos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.marca").value("Motorola"));
    }

    @Test
    @DisplayName("POST /v1/catalogo-modelos deve retornar 400 quando já existe")
    void criar_deveRetornar400QuandoJaExiste() throws Exception {
        CatalogoModeloCreateDTO dto = CatalogoModeloCreateDTO.builder()
                .tipoItem(TipoItem.EQUIPAMENTO)
                .marca("Motorola")
                .modelo("EP450")
                .build();

        when(service.criar(any(CatalogoModeloCreateDTO.class)))
                .thenThrow(new DomainException("Já existe no catálogo: EQUIPAMENTO Motorola EP450"));

        mockMvc.perform(post("/v1/catalogo-modelos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /v1/catalogo-modelos/{id} deve retornar 200")
    void buscarPorId_deveRetornar200() throws Exception {
        UUID id = UUID.randomUUID();
        CatalogoModeloDTO resultado = CatalogoModeloDTO.builder().id(id).marca("Motorola").modelo("EP450").build();
        when(service.buscarPorId(id)).thenReturn(resultado);

        mockMvc.perform(get("/v1/catalogo-modelos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("EP450"));
    }

    @Test
    @DisplayName("GET /v1/catalogo-modelos deve retornar 200 com a página")
    void listar_deveRetornar200() throws Exception {
        CatalogoModeloDTO item = CatalogoModeloDTO.builder()
                .id(UUID.randomUUID()).tipoItem(TipoItem.EQUIPAMENTO).marca("Motorola").modelo("EP450").build();

        when(service.listar(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/v1/catalogo-modelos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].marca").value("Motorola"));
    }

    @Test
    @DisplayName("GET /v1/catalogo-modelos deve repassar busca, tipo e status como filtros")
    void listar_deveRepassarFiltros() throws Exception {
        when(service.listar(eq("Motorola"), eq(TipoItem.EQUIPAMENTO), eq(StatusItem.ATIVO), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/v1/catalogo-modelos")
                        .param("busca", "Motorola")
                        .param("tipoItem", "EQUIPAMENTO")
                        .param("status", "ATIVO"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /v1/catalogo-modelos/{id} deve retornar 204")
    void deletar_deveRetornar204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/v1/catalogo-modelos/{id}", id).with(csrf()))
                .andExpect(status().isNoContent());
    }
}
