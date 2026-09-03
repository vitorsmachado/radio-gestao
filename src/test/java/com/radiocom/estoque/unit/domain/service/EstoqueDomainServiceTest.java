package com.radiocom.estoque.unit.domain.service;

import com.radiocom.estoque.domain.model.Acessorio;
import com.radiocom.estoque.domain.model.Peca;
import com.radiocom.estoque.domain.model.enums.TipoAcessorio;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.estoque.domain.repository.AcessorioRepository;
import com.radiocom.estoque.domain.repository.PecaRepository;
import com.radiocom.estoque.domain.service.EstoqueDomainService;
import com.radiocom.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EstoqueDomainService - Testes Unitários")
class EstoqueDomainServiceTest {

    @Mock private AcessorioRepository acessorioRepository;
    @Mock private PecaRepository pecaRepository;

    @InjectMocks
    private EstoqueDomainService service;

    private UUID pecaId;
    private Peca peca;

    @BeforeEach
    void setUp() {
        pecaId = UUID.randomUUID();
        peca = Peca.builder()
                .codigo("PC-001")
                .descricao("Bateria BP-227")
                .tipo(TipoItem.PECA)
                .quantidadeDisponivel(10)
                .build();
    }

    @Test
    @DisplayName("verificarDisponibilidade deve retornar true quando quantidade suficiente")
    void verificarDisponibilidade_deveRetornarTrueQuandoSuficiente() {
        when(pecaRepository.findById(pecaId)).thenReturn(Optional.of(peca));

        assertThat(service.verificarDisponibilidade(pecaId, TipoItem.PECA, 5)).isTrue();
    }

    @Test
    @DisplayName("verificarDisponibilidade deve retornar false quando item não existe")
    void verificarDisponibilidade_deveRetornarFalseQuandoNaoExiste() {
        when(pecaRepository.findById(pecaId)).thenReturn(Optional.empty());

        assertThat(service.verificarDisponibilidade(pecaId, TipoItem.PECA, 1)).isFalse();
    }

    @Test
    @DisplayName("darEntrada deve aumentar quantidade e salvar")
    void darEntrada_deveAumentarQuantidadeESalvar() {
        when(pecaRepository.findById(pecaId)).thenReturn(Optional.of(peca));

        service.darEntrada(pecaId, TipoItem.PECA, 5);

        assertThat(peca.getQuantidadeDisponivel()).isEqualTo(15);
    }

    @Test
    @DisplayName("darSaida deve diminuir quantidade e salvar")
    void darSaida_deveDiminuirQuantidadeESalvar() {
        when(pecaRepository.findById(pecaId)).thenReturn(Optional.of(peca));

        service.darSaida(pecaId, TipoItem.PECA, 4);

        assertThat(peca.getQuantidadeDisponivel()).isEqualTo(6);
    }

    @Test
    @DisplayName("darSaida deve propagar exceção quando quantidade insuficiente")
    void darSaida_devePropagarExcecaoQuandoInsuficiente() {
        when(pecaRepository.findById(pecaId)).thenReturn(Optional.of(peca));

        assertThatThrownBy(() -> service.darSaida(pecaId, TipoItem.PECA, 20))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("movimentar EQUIPAMENTO deve lançar exceção — rastreado por N/S")
    void movimentar_deveLancarExcecaoParaEquipamento() {
        assertThatThrownBy(() -> service.darEntrada(UUID.randomUUID(), TipoItem.EQUIPAMENTO, 1))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("rastreados por N/S");
    }

    @Test
    @DisplayName("movimentar SERVICO deve lançar exceção — sem estoque físico")
    void movimentar_deveLancarExcecaoParaServico() {
        assertThatThrownBy(() -> service.darEntrada(UUID.randomUUID(), TipoItem.SERVICO, 1))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("não possuem estoque físico");
    }

    @Test
    @DisplayName("buscarAcessorioPorId deve lançar exceção quando não existe")
    void buscarAcessorioPorId_deveLancarExcecaoQuandoNaoExiste() {
        UUID id = UUID.randomUUID();
        when(acessorioRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarAcessorioPorId(id))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Acessório não encontrado");
    }

    @Test
    @DisplayName("ajustar deve definir quantidade exata e salvar")
    void ajustar_deveDefinirQuantidadeExataESalvar() {
        Acessorio acessorio = Acessorio.builder()
                .codigo("AC-001")
                .descricao("Bateria")
                .tipo(TipoItem.ACESSORIO)
                .tipoAcessorio(TipoAcessorio.BATERIA)
                .quantidadeDisponivel(10)
                .build();
        UUID acessorioId = UUID.randomUUID();
        when(acessorioRepository.findById(acessorioId)).thenReturn(Optional.of(acessorio));

        service.ajustar(acessorioId, TipoItem.ACESSORIO, 3);

        assertThat(acessorio.getQuantidadeDisponivel()).isEqualTo(3);
    }
}
