package com.radiocom.auth.integration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiocom.auth.application.dto.CriarUsuarioDTO;
import com.radiocom.auth.application.dto.LoginRequestDTO;
import com.radiocom.auth.application.dto.LoginResponseDTO;
import com.radiocom.auth.application.dto.UsuarioDTO;
import com.radiocom.auth.application.service.AuthApplicationService;
import com.radiocom.auth.application.service.JwtService;
import com.radiocom.auth.domain.model.RoleUsuario;
import com.radiocom.config.SecurityConfig;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = com.radiocom.auth.interfaces.rest.AuthController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("AuthController - Testes de API")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthApplicationService authService;

    @MockBean
    private JwtService jwtService;

    private LoginResponseDTO loginResponse;

    @BeforeEach
    void setUp() {
        loginResponse = LoginResponseDTO.builder()
                .token("token-jwt-fake")
                .tipo("Bearer")
                .usuarioId(UUID.randomUUID())
                .nome("Vitor Silva")
                .login("vitor.silva")
                .email("vitor@radiogestao.com.br")
                .role(RoleUsuario.TECNICO)
                .build();
    }

    // ===== POST /v1/auth/login =====

    @Test
    @DisplayName("POST /login deve retornar 200 e o token quando credenciais válidas")
    void login_deveRetornar200ComToken() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setLogin("vitor.silva");
        dto.setSenha("senha123");

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-jwt-fake"))
                .andExpect(jsonPath("$.role").value("TECNICO"));
    }

    @Test
    @DisplayName("POST /login deve retornar 400 quando credenciais inválidas")
    void login_deveRetornar400QuandoCredenciaisInvalidas() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setLogin("vitor.silva");
        dto.setSenha("senha-errada");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new DomainException("Credenciais inválidas"));

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
    }

    @Test
    @DisplayName("POST /login deve retornar 400 quando payload inválido")
    void login_deveRetornar400QuandoPayloadInvalido() throws Exception {
        String jsonSemSenha = "{\"login\":\"vitor.silva\"}";

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSemSenha))
                .andExpect(status().isBadRequest());
    }

    // ===== POST /v1/auth/usuarios =====

    @Test
    @DisplayName("POST /usuarios deve retornar 201 quando solicitado por ADMIN")
    @WithMockUser(roles = "ADMIN")
    void criar_deveRetornar201QuandoAdmin() throws Exception {
        CriarUsuarioDTO dto = new CriarUsuarioDTO();
        dto.setNome("Novo Usuário");
        dto.setLogin("novo.usuario");
        dto.setEmail("novo@radiogestao.com.br");
        dto.setSenha("senha12345");
        dto.setRole(RoleUsuario.AUXILIAR);

        UsuarioDTO criado = UsuarioDTO.builder()
                .id(UUID.randomUUID())
                .nome(dto.getNome())
                .login(dto.getLogin())
                .email(dto.getEmail())
                .role(RoleUsuario.AUXILIAR)
                .ativo(true)
                .build();

        when(authService.criar(any(CriarUsuarioDTO.class))).thenReturn(criado);

        mockMvc.perform(post("/v1/auth/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("novo@radiogestao.com.br"));
    }

    @Test
    @DisplayName("POST /usuarios deve retornar 403 quando solicitado por não-ADMIN")
    @WithMockUser(roles = "TECNICO")
    void criar_deveRetornar403QuandoNaoAdmin() throws Exception {
        CriarUsuarioDTO dto = new CriarUsuarioDTO();
        dto.setNome("Novo Usuário");
        dto.setLogin("novo.usuario");
        dto.setEmail("novo@radiogestao.com.br");
        dto.setSenha("senha12345");
        dto.setRole(RoleUsuario.AUXILIAR);

        mockMvc.perform(post("/v1/auth/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /usuarios deve retornar 401 quando não autenticado")
    void criar_deveRetornar401QuandoNaoAutenticado() throws Exception {
        CriarUsuarioDTO dto = new CriarUsuarioDTO();
        dto.setNome("Novo Usuário");
        dto.setLogin("novo.usuario");
        dto.setEmail("novo@radiogestao.com.br");
        dto.setSenha("senha12345");
        dto.setRole(RoleUsuario.AUXILIAR);

        mockMvc.perform(post("/v1/auth/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
