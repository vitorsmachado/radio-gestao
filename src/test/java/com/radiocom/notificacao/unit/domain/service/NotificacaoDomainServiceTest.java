package com.radiocom.notificacao.unit.domain.service;

import com.radiocom.notificacao.domain.model.Notificacao;
import com.radiocom.notificacao.domain.model.enums.TipoNotificacao;
import com.radiocom.notificacao.domain.repository.NotificacaoRepository;
import com.radiocom.notificacao.domain.service.NotificacaoDomainService;
import com.radiocom.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificacaoDomainService - Testes Unitários")
class NotificacaoDomainServiceTest {

    @Mock private NotificacaoRepository repository;

    @InjectMocks
    private NotificacaoDomainService service;

    private UUID notificacaoId;
    private Notificacao notificacao;

    @BeforeEach
    void setUp() {
        notificacaoId = UUID.randomUUID();
        notificacao = Notificacao.builder()
                .tipo(TipoNotificacao.GARANTIA_CONFLITO).titulo("Conflito").mensagem("msg").link("/manutencao/x")
                .build();
        ReflectionTestUtils.setField(notificacao, "id", notificacaoId);
    }

    @Test
    @DisplayName("criar deve salvar uma nova notificação")
    void criar_deveSalvar() {
        when(repository.save(any(Notificacao.class))).thenAnswer(inv -> inv.getArgument(0));

        Notificacao resultado = service.criar(TipoNotificacao.GARANTIA_CONFLITO, "Título", "Mensagem", "/link");

        assertThat(resultado.getTitulo()).isEqualTo("Título");
        assertThat(resultado.isLida()).isFalse();
    }

    @Test
    @DisplayName("listar com lida=null deve trazer todas")
    void listar_comLidaNull_deveTrazerTodas() {
        Page<Notificacao> pagina = new PageImpl<>(java.util.List.of(notificacao));
        when(repository.findAllByOrderByDataCriacaoDesc(any())).thenReturn(pagina);

        Page<Notificacao> resultado = service.listar(null, PageRequest.of(0, 20));

        assertThat(resultado.getContent()).containsExactly(notificacao);
    }

    @Test
    @DisplayName("listar com lida=false deve filtrar só as não lidas")
    void listar_comLidaFalse_deveFiltrar() {
        Page<Notificacao> pagina = new PageImpl<>(java.util.List.of(notificacao));
        when(repository.findByLidaOrderByDataCriacaoDesc(false, PageRequest.of(0, 20))).thenReturn(pagina);

        Page<Notificacao> resultado = service.listar(false, PageRequest.of(0, 20));

        assertThat(resultado.getContent()).containsExactly(notificacao);
    }

    @Test
    @DisplayName("marcarComoLida deve marcar e salvar")
    void marcarComoLida_deveMarcarESalvar() {
        when(repository.findById(notificacaoId)).thenReturn(Optional.of(notificacao));
        when(repository.save(any(Notificacao.class))).thenAnswer(inv -> inv.getArgument(0));

        Notificacao resultado = service.marcarComoLida(notificacaoId);

        assertThat(resultado.isLida()).isTrue();
        verify(repository).save(notificacao);
    }

    @Test
    @DisplayName("marcarComoLida deve lançar exceção quando não existe")
    void marcarComoLida_deveLancarExcecaoQuandoNaoExiste() {
        when(repository.findById(notificacaoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.marcarComoLida(notificacaoId))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("contarNaoLidas deve delegar para o repositório")
    void contarNaoLidas_deveDelegar() {
        when(repository.countByLidaFalse()).thenReturn(3L);

        assertThat(service.contarNaoLidas()).isEqualTo(3L);
    }
}
