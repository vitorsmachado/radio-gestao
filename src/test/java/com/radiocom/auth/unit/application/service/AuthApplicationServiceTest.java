package com.radiocom.auth.unit.application.service;

import com.radiocom.auth.application.dto.CriarUsuarioDTO;
import com.radiocom.auth.application.dto.LoginRequestDTO;
import com.radiocom.auth.application.dto.LoginResponseDTO;
import com.radiocom.auth.application.dto.UsuarioDTO;
import com.radiocom.auth.application.service.AuthApplicationService;
import com.radiocom.auth.application.service.JwtService;
import com.radiocom.auth.domain.model.RoleUsuario;
import com.radiocom.auth.domain.model.Usuario;
import com.radiocom.auth.domain.repository.UsuarioRepository;
import com.radiocom.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthApplicationService - Testes Unitários")
class AuthApplicationServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthApplicationService service;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .nome("Vitor Silva")
                .email("vitor@radiogestao.com.br")
                .login("vitor.silva")
                .senhaHash("senha-hash-armazenada")
                .role(RoleUsuario.TECNICO)
                .build();
    }

    // ===== login =====

    @Test
    @DisplayName("login deve retornar token quando credenciais são válidas")
    void login_deveRetornarTokenQuandoCredenciaisValidas() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setLogin("vitor.silva");
        dto.setSenha("senha123");

        when(usuarioRepository.findByLoginAndAtivoTrue("vitor.silva")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha123", "senha-hash-armazenada")).thenReturn(true);
        when(jwtService.gerarToken(usuario)).thenReturn("token-jwt-fake");

        LoginResponseDTO response = service.login(dto);

        assertThat(response.getToken()).isEqualTo("token-jwt-fake");
        assertThat(response.getTipo()).isEqualTo("Bearer");
        assertThat(response.getLogin()).isEqualTo("vitor.silva");
        assertThat(response.getRole()).isEqualTo(RoleUsuario.TECNICO);
    }

    @Test
    @DisplayName("login deve lançar exceção quando login não existe")
    void login_deveLancarExcecaoQuandoLoginNaoExiste() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setLogin("inexistente");
        dto.setSenha("senha123");

        when(usuarioRepository.findByLoginAndAtivoTrue("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(dto))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Credenciais inválidas");
    }

    @Test
    @DisplayName("login deve lançar exceção quando senha está incorreta")
    void login_deveLancarExcecaoQuandoSenhaIncorreta() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setLogin("vitor.silva");
        dto.setSenha("senha-errada");

        when(usuarioRepository.findByLoginAndAtivoTrue("vitor.silva")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha-errada", "senha-hash-armazenada")).thenReturn(false);

        assertThatThrownBy(() -> service.login(dto))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Credenciais inválidas");
    }

    // ===== criar =====

    @Test
    @DisplayName("criar deve salvar usuário com senha codificada")
    void criar_deveSalvarUsuarioComSenhaCodificada() {
        CriarUsuarioDTO dto = new CriarUsuarioDTO();
        dto.setNome("Novo Usuário");
        dto.setLogin("novo.usuario");
        dto.setEmail("novo@radiogestao.com.br");
        dto.setSenha("senha12345");
        dto.setRole(RoleUsuario.AUXILIAR);

        when(usuarioRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(usuarioRepository.existsByLogin(dto.getLogin())).thenReturn(false);
        when(passwordEncoder.encode("senha12345")).thenReturn("senha-codificada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioDTO resultado = service.criar(dto);

        assertThat(resultado.getEmail()).isEqualTo("novo@radiogestao.com.br");
        assertThat(resultado.getRole()).isEqualTo(RoleUsuario.AUXILIAR);
        assertThat(resultado.isAtivo()).isTrue();
    }

    @Test
    @DisplayName("criar deve lançar exceção quando email já cadastrado")
    void criar_deveLancarExcecaoQuandoEmailJaCadastrado() {
        CriarUsuarioDTO dto = new CriarUsuarioDTO();
        dto.setEmail("duplicado@radiogestao.com.br");
        dto.setLogin("qualquer");

        when(usuarioRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> service.criar(dto))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Email já cadastrado");
    }

    @Test
    @DisplayName("criar deve lançar exceção quando login já cadastrado")
    void criar_deveLancarExcecaoQuandoLoginJaCadastrado() {
        CriarUsuarioDTO dto = new CriarUsuarioDTO();
        dto.setEmail("outro@radiogestao.com.br");
        dto.setLogin("login.existente");

        when(usuarioRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(usuarioRepository.existsByLogin(dto.getLogin())).thenReturn(true);

        assertThatThrownBy(() -> service.criar(dto))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Login já cadastrado");
    }
}
