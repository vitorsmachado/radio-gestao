package com.radiocom.auth.application.service;

import com.radiocom.auth.application.dto.CriarUsuarioDTO;
import com.radiocom.auth.application.dto.LoginRequestDTO;
import com.radiocom.auth.application.dto.LoginResponseDTO;
import com.radiocom.auth.application.dto.UsuarioDTO;
import com.radiocom.auth.domain.model.Usuario;
import com.radiocom.auth.domain.repository.UsuarioRepository;
import com.radiocom.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthApplicationService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Autentica o usuário e retorna um JWT.
     */
    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository
                .findByLoginAndAtivoTrue(dto.getLogin())
                .orElseThrow(() -> new DomainException("Credenciais inválidas"));

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenhaHash())) {
            throw new DomainException("Credenciais inválidas");
        }

        String token = jwtService.gerarToken(usuario);

        log.info("Login realizado: {} ({})", usuario.getEmail(), usuario.getRole());

        return LoginResponseDTO.builder()
                .token(token)
                .tipo("Bearer")
                .usuarioId(usuario.getId())
                .nome(usuario.getNome())
                .login(usuario.getLogin())
                .email(usuario.getEmail())
                .role(usuario.getRole())
                .build();
    }

    /**
     * Cria um novo usuário.
     */
    @Transactional
    public UsuarioDTO criar(CriarUsuarioDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new DomainException("Email já cadastrado: " + dto.getEmail());
        }

        if (usuarioRepository.existsByLogin(dto.getLogin())) {
            throw new DomainException("Login já cadastrado: " + dto.getLogin());
        }

        Usuario usuario = Usuario.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .login(dto.getLogin())
                .senhaHash(passwordEncoder.encode(dto.getSenha()))
                .role(dto.getRole())
                .ativo(true)
                .build();

        Usuario salvo = usuarioRepository.save(usuario);
        log.info("Usuário criado: {} ({})", salvo.getEmail(), salvo.getRole());
        return toDTO(salvo);
    }

    private UsuarioDTO toDTO(Usuario u) {
        return UsuarioDTO.builder()
                .id(u.getId())
                .nome(u.getNome())
                .email(u.getEmail())
                .role(u.getRole())
                .ativo(u.isAtivo())
                .dataCriacao(u.getDataCriacao())
                .build();
    }
}
