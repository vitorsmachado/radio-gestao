package com.radiocom.notificacao.integration.api;

import com.radiocom.auth.application.service.JwtService;
import com.radiocom.config.SecurityConfig;
import com.radiocom.notificacao.application.dto.NotificacaoDTO;
import com.radiocom.notificacao.application.service.NotificacaoApplicationService;
import com.radiocom.notificacao.domain.model.enums.TipoNotificacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = com.radiocom.notificacao.interfaces.rest.NotificacaoController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@WithMockUser
@DisplayName("NotificacaoController - Testes de API")
class NotificacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificacaoApplicationService service;

    @MockBean
    private JwtService jwtService;

    private UUID notificacaoId;
    private NotificacaoDTO dto;

    @BeforeEach
    void setUp() {
        notificacaoId = UUID.randomUUID();
        dto = NotificacaoDTO.builder()
                .id(notificacaoId).tipo(TipoNotificacao.GARANTIA_CONFLITO)
                .titulo("Conflito de garantia na OS OS-2026-0001").lida(false).build();
    }

    @Test
    @DisplayName("GET / deve retornar 200 com a página de notificações")
    void listar_deveRetornar200() throws Exception {
        Page<NotificacaoDTO> pagina = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        when(service.listar(any(), any())).thenReturn(pagina);

        mockMvc.perform(get("/v1/notificacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(notificacaoId.toString()));
    }

    @Test
    @DisplayName("GET /contagem-nao-lidas deve retornar 200 com o total")
    void contarNaoLidas_deveRetornar200() throws Exception {
        when(service.contarNaoLidas()).thenReturn(2L);

        mockMvc.perform(get("/v1/notificacoes/contagem-nao-lidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(2));
    }

    @Test
    @DisplayName("PATCH /{id}/marcar-lida deve retornar 200 com a notificação atualizada")
    void marcarComoLida_deveRetornar200() throws Exception {
        dto.setLida(true);
        when(service.marcarComoLida(notificacaoId)).thenReturn(dto);

        mockMvc.perform(patch("/v1/notificacoes/{id}/marcar-lida", notificacaoId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lida").value(true));
    }
}
