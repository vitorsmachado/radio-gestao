package com.radiocom.cliente.integration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiocom.auth.application.service.JwtService;
import com.radiocom.cliente.application.dto.ClienteCreateDTO;
import com.radiocom.cliente.application.dto.ClienteDTO;
import com.radiocom.cliente.application.dto.ClienteUpdateDTO;
import com.radiocom.cliente.application.dto.ContatoDTO;
import com.radiocom.cliente.application.dto.PostoDTO;
import com.radiocom.cliente.application.service.ClienteApplicationService;
import com.radiocom.cliente.domain.model.enums.StatusCliente;
import com.radiocom.cliente.domain.model.enums.TipoContato;
import com.radiocom.cliente.domain.model.enums.TipoPessoa;
import com.radiocom.config.SecurityConfig;
import com.radiocom.shared.exception.DomainException;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = com.radiocom.cliente.interfaces.rest.ClienteController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@WithMockUser
@DisplayName("ClienteController - Testes de API")
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteApplicationService clienteService;

    @MockBean
    private JwtService jwtService;

    private UUID clienteId;
    private ClienteDTO clienteDTO;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        clienteDTO = ClienteDTO.builder()
                .id(clienteId)
                .tipo(TipoPessoa.PESSOA_JURIDICA)
                .documento("11222333000181")
                .nomeRazaoSocial("Radio Comunicacao LTDA")
                .status(StatusCliente.ATIVO)
                .build();
    }

    @Test
    @DisplayName("POST /v1/clientes deve retornar 201 ao criar cliente")
    void criar_deveRetornar201() throws Exception {
        ClienteCreateDTO dto = ClienteCreateDTO.builder()
                .documento("11222333000181")
                .nomeRazaoSocial("Radio Comunicacao LTDA")
                .build();

        when(clienteService.criar(any(ClienteCreateDTO.class))).thenReturn(clienteDTO);

        mockMvc.perform(post("/v1/clientes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documento").value("11222333000181"));
    }

    @Test
    @DisplayName("POST /v1/clientes deve retornar 400 quando documento inválido")
    void criar_deveRetornar400QuandoDocumentoObrigatorioAusente() throws Exception {
        String jsonSemDocumento = "{\"nomeRazaoSocial\":\"Cliente Teste\"}";

        mockMvc.perform(post("/v1/clientes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSemDocumento))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /v1/clientes deve retornar 400 quando documento já cadastrado")
    void criar_deveRetornar400QuandoDocumentoJaCadastrado() throws Exception {
        ClienteCreateDTO dto = ClienteCreateDTO.builder()
                .documento("11222333000181")
                .nomeRazaoSocial("Radio Comunicacao LTDA")
                .build();

        when(clienteService.criar(any(ClienteCreateDTO.class)))
                .thenThrow(new DomainException("Já existe cliente cadastrado com este documento"));

        mockMvc.perform(post("/v1/clientes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Já existe cliente cadastrado com este documento"));
    }

    @Test
    @DisplayName("GET /v1/clientes/{id} deve retornar 200 com o cliente")
    void buscarPorId_deveRetornar200() throws Exception {
        when(clienteService.buscarPorId(clienteId)).thenReturn(clienteDTO);

        mockMvc.perform(get("/v1/clientes/{id}", clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clienteId.toString()));
    }

    @Test
    @DisplayName("GET /v1/clientes/{id} deve retornar 400 quando cliente não existe")
    void buscarPorId_deveRetornar400QuandoNaoExiste() throws Exception {
        when(clienteService.buscarPorId(clienteId))
                .thenThrow(new DomainException("Cliente não encontrado: " + clienteId));

        mockMvc.perform(get("/v1/clientes/{id}", clienteId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /v1/clientes deve retornar 200 com página de clientes")
    void listarTodos_deveRetornar200() throws Exception {
        Page<ClienteDTO> pagina = new PageImpl<>(List.of(clienteDTO), PageRequest.of(0, 20), 1);
        when(clienteService.listarTodos(any())).thenReturn(pagina);

        mockMvc.perform(get("/v1/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(clienteId.toString()));
    }

    @Test
    @DisplayName("PUT /v1/clientes/{id} deve retornar 200 ao atualizar")
    void atualizar_deveRetornar200() throws Exception {
        ClienteUpdateDTO dto = ClienteUpdateDTO.builder().nomeFantasia("Novo Nome").build();

        when(clienteService.atualizar(any(UUID.class), any(ClienteUpdateDTO.class))).thenReturn(clienteDTO);

        mockMvc.perform(put("/v1/clientes/{id}", clienteId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /v1/clientes/{id}/ativar deve retornar 200")
    void ativar_deveRetornar200() throws Exception {
        when(clienteService.ativar(clienteId)).thenReturn(clienteDTO);

        mockMvc.perform(patch("/v1/clientes/{id}/ativar", clienteId).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /v1/clientes/{id}/postos deve retornar 201")
    void adicionarPosto_deveRetornar201() throws Exception {
        PostoDTO dto = PostoDTO.builder().nome("Matriz").build();

        when(clienteService.adicionarPosto(any(UUID.class), any(PostoDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/v1/clientes/{id}/postos", clienteId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Matriz"));
    }

    @Test
    @DisplayName("POST /v1/clientes/{id}/contatos deve retornar 201")
    void adicionarContato_deveRetornar201() throws Exception {
        ContatoDTO dto = ContatoDTO.builder().nome("João").tipo(TipoContato.COMERCIAL).build();

        when(clienteService.adicionarContato(any(UUID.class), any(ContatoDTO.class))).thenReturn(clienteDTO);

        mockMvc.perform(post("/v1/clientes/{id}/contatos", clienteId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /v1/clientes/{id} sem autenticação deve retornar 401")
    @org.springframework.security.test.context.support.WithAnonymousUser
    void buscarPorId_semAutenticacao_deveRetornar401() throws Exception {
        mockMvc.perform(get("/v1/clientes/{id}", clienteId))
                .andExpect(status().isUnauthorized());
    }
}
