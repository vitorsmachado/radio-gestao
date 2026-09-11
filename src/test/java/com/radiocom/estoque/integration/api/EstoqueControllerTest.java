package com.radiocom.estoque.integration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiocom.auth.application.service.JwtService;
import com.radiocom.config.SecurityConfig;
import com.radiocom.estoque.application.dto.*;
import com.radiocom.estoque.application.service.EstoqueApplicationService;
import com.radiocom.estoque.domain.model.enums.*;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = com.radiocom.estoque.interfaces.rest.EstoqueController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@WithMockUser
@DisplayName("EstoqueController - Testes de API")
class EstoqueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EstoqueApplicationService estoqueService;

    @MockBean
    private JwtService jwtService;

    private UUID equipamentoId;
    private EquipamentoDTO equipamentoDTO;

    @BeforeEach
    void setUp() {
        equipamentoId = UUID.randomUUID();
        equipamentoDTO = EquipamentoDTO.builder()
                .id(equipamentoId)
                .codigo("EQ-001")
                .descricao("Rádio VHF")
                .proprietario(ProprietarioEquipamento.NOSSO)
                .numeroSerie("NS-001")
                .faixa(FaixaEquipamento.VHF)
                .estado(EstadoEquipamento.DISPONIVEL)
                .build();
    }

    @Test
    @DisplayName("POST /equipamentos deve retornar 201")
    void criarEquipamento_deveRetornar201() throws Exception {
        EquipamentoCreateDTO dto = EquipamentoCreateDTO.builder()
                .proprietario(ProprietarioEquipamento.NOSSO)
                .numeroSerie("NS-001")
                .patrimonio("PAT-001")
                .faixa(FaixaEquipamento.VHF)
                .build();

        when(estoqueService.criarEquipamento(any(EquipamentoCreateDTO.class))).thenReturn(equipamentoDTO);

        mockMvc.perform(post("/v1/estoque/equipamentos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroSerie").value("NS-001"));
    }

    @Test
    @DisplayName("POST /equipamentos deve retornar 400 quando NS duplicado")
    void criarEquipamento_deveRetornar400QuandoNSDuplicado() throws Exception {
        EquipamentoCreateDTO dto = EquipamentoCreateDTO.builder()
                .proprietario(ProprietarioEquipamento.NOSSO)
                .numeroSerie("NS-001")
                .faixa(FaixaEquipamento.VHF)
                .build();

        when(estoqueService.criarEquipamento(any(EquipamentoCreateDTO.class)))
                .thenThrow(new DomainException("Número de série já cadastrado"));

        mockMvc.perform(post("/v1/estoque/equipamentos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Número de série já cadastrado"));
    }

    @Test
    @DisplayName("GET /equipamentos/{id} deve retornar 200")
    void buscarEquipamentoPorId_deveRetornar200() throws Exception {
        when(estoqueService.buscarEquipamentoPorId(equipamentoId)).thenReturn(equipamentoDTO);

        mockMvc.perform(get("/v1/estoque/equipamentos/{id}", equipamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("EQ-001"));
    }

    @Test
    @DisplayName("PATCH /equipamentos/{id}/enviar-manutencao deve retornar 200")
    void enviarEquipamentoManutencao_deveRetornar200() throws Exception {
        when(estoqueService.enviarEquipamentoManutencao(equipamentoId)).thenReturn(equipamentoDTO);

        mockMvc.perform(patch("/v1/estoque/equipamentos/{id}/enviar-manutencao", equipamentoId).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /acessorios deve retornar 201")
    void criarAcessorio_deveRetornar201() throws Exception {
        AcessorioCreateDTO dto = AcessorioCreateDTO.builder()
                .descricao("Bateria BP-227")
                .tipoAcessorio(TipoAcessorio.BATERIA)
                .quantidadeDisponivel(5)
                .build();

        AcessorioDTO acessorioDTO = AcessorioDTO.builder()
                .id(UUID.randomUUID())
                .codigo("AC-001")
                .descricao("Bateria BP-227")
                .tipoAcessorio(TipoAcessorio.BATERIA)
                .build();

        when(estoqueService.criarAcessorio(any(AcessorioCreateDTO.class))).thenReturn(acessorioDTO);

        mockMvc.perform(post("/v1/estoque/acessorios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoAcessorio").value("BATERIA"));
    }

    @Test
    @DisplayName("POST /pecas deve retornar 201")
    void criarPeca_deveRetornar201() throws Exception {
        PecaCreateDTO dto = PecaCreateDTO.builder()
                .descricao("Antena UHF")
                .quantidadeDisponivel(10)
                .build();

        PecaDTO pecaDTO = PecaDTO.builder()
                .id(UUID.randomUUID())
                .codigo("PC-001")
                .descricao("Antena UHF")
                .quantidadeDisponivel(10)
                .build();

        when(estoqueService.criarPeca(any(PecaCreateDTO.class))).thenReturn(pecaDTO);

        mockMvc.perform(post("/v1/estoque/pecas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantidadeDisponivel").value(10));
    }

    @Test
    @DisplayName("GET /pecas deve retornar 200 com a página")
    void listarPecas_deveRetornar200() throws Exception {
        PecaDTO pecaDTO = PecaDTO.builder().id(UUID.randomUUID()).descricao("Antena UHF").build();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        when(estoqueService.listarPecas(any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(pecaDTO), pageable, 1));

        mockMvc.perform(get("/v1/estoque/pecas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].descricao").value("Antena UHF"));
    }

    @Test
    @DisplayName("GET /equipamentos/{id} sem autenticação deve retornar 401")
    @WithAnonymousUser
    void buscarEquipamentoPorId_semAutenticacao_deveRetornar401() throws Exception {
        mockMvc.perform(get("/v1/estoque/equipamentos/{id}", equipamentoId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /equipamentos/patrimonio/{patrimonio} deve retornar 200")
    void buscarEquipamentoPorPatrimonio_deveRetornar200() throws Exception {
        when(estoqueService.buscarEquipamentoPorPatrimonio("PAT-001")).thenReturn(equipamentoDTO);

        mockMvc.perform(get("/v1/estoque/equipamentos/patrimonio/{patrimonio}", "PAT-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("EQ-001"));
    }

    @Test
    @DisplayName("POST /pecas/{id}/entrada deve retornar 200 com o novo saldo")
    void darEntradaPeca_deveRetornar200ComNovoSaldo() throws Exception {
        UUID pecaId = UUID.randomUUID();
        MovimentacaoQuantidadeDTO dto = MovimentacaoQuantidadeDTO.builder().quantidade(5).build();

        when(estoqueService.darEntrada(eq(pecaId), eq(TipoItem.PECA), eq(5))).thenReturn(15);

        mockMvc.perform(post("/v1/estoque/pecas/{id}/entrada", pecaId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(15));
    }

    @Test
    @DisplayName("POST /pecas/{id}/entrada deve retornar 400 quando quantidade inválida")
    void darEntradaPeca_deveRetornar400QuandoQuantidadeInvalida() throws Exception {
        UUID pecaId = UUID.randomUUID();
        String jsonQuantidadeZero = "{\"quantidade\":0}";

        mockMvc.perform(post("/v1/estoque/pecas/{id}/entrada", pecaId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonQuantidadeZero))
                .andExpect(status().isBadRequest());
    }
}
