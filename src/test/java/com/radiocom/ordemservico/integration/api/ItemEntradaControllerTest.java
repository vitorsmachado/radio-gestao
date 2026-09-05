package com.radiocom.ordemservico.integration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiocom.auth.application.service.JwtService;
import com.radiocom.config.SecurityConfig;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.ordemservico.application.dto.*;
import com.radiocom.ordemservico.application.service.ItemEntradaApplicationService;
import com.radiocom.ordemservico.application.service.OrdemServicoApplicationService;
import com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada;
import com.radiocom.ordemservico.domain.model.enums.TipoItemConserto;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = com.radiocom.ordemservico.interfaces.rest.ItemEntradaController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@WithMockUser
@DisplayName("ItemEntradaController - Testes de API")
class ItemEntradaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemEntradaApplicationService itemService;

    @MockBean
    private OrdemServicoApplicationService osService;

    @MockBean
    private JwtService jwtService;

    private UUID itemId;
    private UUID osId;
    private ItemEntradaDTO itemDTO;

    @BeforeEach
    void setUp() {
        itemId = UUID.randomUUID();
        osId = UUID.randomUUID();
        itemDTO = ItemEntradaDTO.builder()
                .id(itemId)
                .osId(osId)
                .tipoItem(TipoItem.EQUIPAMENTO)
                .descricao("Rádio Motorola EP450")
                .status(StatusItemEntrada.PENDENTE_AVALIACAO)
                .build();
    }

    @Test
    @DisplayName("POST / deve retornar 201")
    void criar_deveRetornar201() throws Exception {
        ItemEntradaCreateDTO dto = ItemEntradaCreateDTO.builder()
                .osId(osId).tipoItem(TipoItem.EQUIPAMENTO).descricao("Rádio Motorola EP450").build();
        when(itemService.criar(any(ItemEntradaCreateDTO.class))).thenReturn(itemDTO);

        mockMvc.perform(post("/v1/itens-entrada")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao").value("Rádio Motorola EP450"));
    }

    @Test
    @DisplayName("GET ?osId= deve retornar 200 com a lista")
    void listarPorOS_deveRetornar200() throws Exception {
        when(itemService.listarPorOS(osId)).thenReturn(List.of(itemDTO));

        mockMvc.perform(get("/v1/itens-entrada").param("osId", osId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemId.toString()));
    }

    @Test
    @DisplayName("PATCH /{id}/avaliar deve retornar 200")
    void avaliar_deveRetornar200() throws Exception {
        itemDTO.setStatus(StatusItemEntrada.AVALIADO);
        itemDTO.setAvaliacaoTecnica("Capacitor queimado");
        when(itemService.avaliar(eq(itemId), any(AvaliarItemDTO.class))).thenReturn(itemDTO);

        AvaliarItemDTO dto = AvaliarItemDTO.builder().avaliacaoTecnica("Capacitor queimado").semDefeito(false).build();

        mockMvc.perform(patch("/v1/itens-entrada/{id}/avaliar", itemId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVALIADO"));
    }

    @Test
    @DisplayName("PATCH /{id}/autorizar deve retornar 200")
    void autorizar_deveRetornar200() throws Exception {
        itemDTO.setStatus(StatusItemEntrada.PENDENTE_MANUTENCAO);
        when(itemService.autorizar(itemId)).thenReturn(itemDTO);

        mockMvc.perform(patch("/v1/itens-entrada/{id}/autorizar", itemId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE_MANUTENCAO"));
    }

    @Test
    @DisplayName("PATCH /{id}/nao-autorizar deve retornar 400 quando status não permite")
    void naoAutorizar_deveRetornar400QuandoStatusInvalido() throws Exception {
        when(itemService.naoAutorizar(eq(itemId), any(MotivoDTO.class)))
                .thenThrow(new DomainException("Marcar como não autorizado requer status [...]"));

        mockMvc.perform(patch("/v1/itens-entrada/{id}/nao-autorizar", itemId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(MotivoDTO.builder().motivo("Cliente recusou").build())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /{id}/marcar-aguardando-peca deve retornar 200")
    void marcarAguardandoPeca_deveRetornar200() throws Exception {
        itemDTO.setStatus(StatusItemEntrada.AGUARDANDO_PECA);
        when(itemService.marcarAguardandoPeca(itemId)).thenReturn(itemDTO);

        mockMvc.perform(patch("/v1/itens-entrada/{id}/marcar-aguardando-peca", itemId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_PECA"));
    }

    @Test
    @DisplayName("PATCH /{id}/mover deve retornar 204 e delegar pro OrdemServicoApplicationService")
    void mover_deveRetornar204EDelegar() throws Exception {
        UUID novaOsId = UUID.randomUUID();
        MoverItemDTO dto = MoverItemDTO.builder().novaOsId(novaOsId).build();

        mockMvc.perform(patch("/v1/itens-entrada/{id}/mover", itemId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());

        verify(osService).moverItem(eq(itemId), any(MoverItemDTO.class));
    }

    @Test
    @DisplayName("POST /{id}/itens-conserto deve retornar 201")
    void adicionarItemConserto_deveRetornar201() throws Exception {
        itemDTO.setItensConserto(List.of(ItemConsertoDTO.builder()
                .tipo(TipoItemConserto.PECA).descricao("Bateria BP-227")
                .quantidade(1).valorUnitario(new BigDecimal("80.00")).valorTotal(new BigDecimal("80.00"))
                .build()));
        when(itemService.adicionarItemConserto(eq(itemId), any(ItemConsertoCreateDTO.class))).thenReturn(itemDTO);

        ItemConsertoCreateDTO dto = ItemConsertoCreateDTO.builder()
                .tipo(TipoItemConserto.PECA).descricao("Bateria BP-227")
                .quantidade(1).valorUnitario(new BigDecimal("80.00")).build();

        mockMvc.perform(post("/v1/itens-entrada/{id}/itens-conserto", itemId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itensConserto[0].descricao").value("Bateria BP-227"));
    }
}
