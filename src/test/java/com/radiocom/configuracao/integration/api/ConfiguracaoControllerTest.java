package com.radiocom.configuracao.integration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiocom.auth.application.service.JwtService;
import com.radiocom.config.SecurityConfig;
import com.radiocom.configuracao.application.dto.AtualizarConfiguracaoDTO;
import com.radiocom.configuracao.application.dto.ConfiguracaoDTO;
import com.radiocom.configuracao.application.service.ConfiguracaoApplicationService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = com.radiocom.configuracao.interfaces.rest.ConfiguracaoController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("ConfiguracaoController - Testes de API")
class ConfiguracaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConfiguracaoApplicationService service;

    @MockBean
    private JwtService jwtService;

    private ConfiguracaoDTO dto;

    @BeforeEach
    void setUp() {
        dto = ConfiguracaoDTO.builder().valorMaoDeObraPadrao(new BigDecimal("50.00")).build();
    }

    @Test
    @WithMockUser
    @DisplayName("GET / deve retornar 200 pra qualquer usuário autenticado")
    void buscar_deveRetornar200() throws Exception {
        when(service.buscar()).thenReturn(dto);

        mockMvc.perform(get("/v1/configuracoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorMaoDeObraPadrao").value(50.00));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT / deve retornar 200 quando ADMIN")
    void atualizar_comoAdmin_deveRetornar200() throws Exception {
        AtualizarConfiguracaoDTO body = AtualizarConfiguracaoDTO.builder()
                .valorMaoDeObraPadrao(new BigDecimal("75.00"))
                .prazoGarantiaPecaDias(90)
                .prazoGarantiaEquipamentoDias(90)
                .prazoGarantiaAcessorioDias(90)
                .build();
        dto.setValorMaoDeObraPadrao(new BigDecimal("75.00"));
        when(service.atualizar(any(AtualizarConfiguracaoDTO.class))).thenReturn(dto);

        mockMvc.perform(put("/v1/configuracoes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorMaoDeObraPadrao").value(75.00));
    }

    @Test
    @WithMockUser(roles = "TECNICO")
    @DisplayName("PUT / deve retornar 403 quando não é ADMIN")
    void atualizar_comoTecnico_deveRetornar403() throws Exception {
        AtualizarConfiguracaoDTO body = AtualizarConfiguracaoDTO.builder()
                .valorMaoDeObraPadrao(new BigDecimal("75.00"))
                .prazoGarantiaPecaDias(90)
                .prazoGarantiaEquipamentoDias(90)
                .prazoGarantiaAcessorioDias(90)
                .build();

        mockMvc.perform(put("/v1/configuracoes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }
}
