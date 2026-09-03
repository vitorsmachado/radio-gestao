package com.radiocom.estoque.unit.domain.service;

import com.radiocom.estoque.domain.model.Equipamento;
import com.radiocom.estoque.domain.model.enums.*;
import com.radiocom.estoque.domain.repository.EquipamentoRepository;
import com.radiocom.estoque.domain.service.EquipamentoDomainService;
import com.radiocom.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EquipamentoDomainService - Testes Unitários")
class EquipamentoDomainServiceTest {

    @Mock private EquipamentoRepository equipamentoRepository;

    @InjectMocks
    private EquipamentoDomainService service;

    private UUID equipamentoId;
    private Equipamento equipamento;

    @BeforeEach
    void setUp() {
        equipamentoId = UUID.randomUUID();
        equipamento = Equipamento.builder()
                .codigo("EQ-001")
                .descricao("Rádio VHF")
                .tipo(TipoItem.EQUIPAMENTO)
                .proprietario(ProprietarioEquipamento.NOSSO)
                .faixa(FaixaEquipamento.VHF)
                .numeroSerie("NS-001")
                .patrimonio("PAT-001")
                .estado(EstadoEquipamento.DISPONIVEL)
                .build();
    }

    @Test
    @DisplayName("buscarPorId deve lançar exceção quando não existe")
    void buscarPorId_deveLancarExcecaoQuandoNaoExiste() {
        when(equipamentoRepository.findById(equipamentoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(equipamentoId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Equipamento não encontrado");
    }

    @Test
    @DisplayName("buscarPorId deve retornar equipamento quando existe")
    void buscarPorId_deveRetornarQuandoExiste() {
        when(equipamentoRepository.findById(equipamentoId)).thenReturn(Optional.of(equipamento));

        assertThat(service.buscarPorId(equipamentoId)).isEqualTo(equipamento);
    }

    @Test
    @DisplayName("buscarPorNumeroSerie deve retornar equipamento quando existe")
    void buscarPorNumeroSerie_deveRetornarQuandoExiste() {
        when(equipamentoRepository.findByNumeroSerie("NS-001")).thenReturn(Optional.of(equipamento));

        assertThat(service.buscarPorNumeroSerie("NS-001")).isEqualTo(equipamento);
    }

    @Test
    @DisplayName("buscarPorNumeroSerie deve lançar exceção quando não existe")
    void buscarPorNumeroSerie_deveLancarExcecaoQuandoNaoExiste() {
        when(equipamentoRepository.findByNumeroSerie("NS-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorNumeroSerie("NS-999"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Equipamento não encontrado com NS");
    }

    @Test
    @DisplayName("buscarPorPatrimonio deve retornar equipamento quando existe")
    void buscarPorPatrimonio_deveRetornarQuandoExiste() {
        when(equipamentoRepository.findByPatrimonio("PAT-001")).thenReturn(Optional.of(equipamento));

        assertThat(service.buscarPorPatrimonio("PAT-001")).isEqualTo(equipamento);
    }

    @Test
    @DisplayName("buscarPorPatrimonio deve lançar exceção quando não existe")
    void buscarPorPatrimonio_deveLancarExcecaoQuandoNaoExiste() {
        when(equipamentoRepository.findByPatrimonio("PAT-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorPatrimonio("PAT-999"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Patrimônio não encontrado");
    }

    @Test
    @DisplayName("listarPorEstado deve delegar para o repository")
    void listarPorEstado_deveDelegarParaRepository() {
        when(equipamentoRepository.findByEstado(EstadoEquipamento.DISPONIVEL)).thenReturn(List.of(equipamento));

        assertThat(service.listarPorEstado(EstadoEquipamento.DISPONIVEL)).containsExactly(equipamento);
    }

    @Test
    @DisplayName("listarPorProprietario deve delegar para o repository")
    void listarPorProprietario_deveDelegarParaRepository() {
        when(equipamentoRepository.findByProprietario(ProprietarioEquipamento.NOSSO)).thenReturn(List.of(equipamento));

        assertThat(service.listarPorProprietario(ProprietarioEquipamento.NOSSO)).containsExactly(equipamento);
    }

    @Test
    @DisplayName("listarPorProprietarioEEstado deve delegar para o repository")
    void listarPorProprietarioEEstado_deveDelegarParaRepository() {
        when(equipamentoRepository.findByProprietarioAndEstado(ProprietarioEquipamento.NOSSO, EstadoEquipamento.DISPONIVEL))
                .thenReturn(List.of(equipamento));

        assertThat(service.listarPorProprietarioEEstado(ProprietarioEquipamento.NOSSO, EstadoEquipamento.DISPONIVEL))
                .containsExactly(equipamento);
    }

    @Test
    @DisplayName("validarDuplicidadePatrimonio deve lançar exceção quando já existe")
    void validarDuplicidadePatrimonio_deveLancarExcecaoQuandoJaExiste() {
        when(equipamentoRepository.existsByPatrimonio("PAT-001")).thenReturn(true);

        assertThatThrownBy(() -> service.validarDuplicidadePatrimonio("PAT-001"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("já cadastrado");
    }

    @Test
    @DisplayName("validarDuplicidadeCodigo deve lançar exceção quando já existe")
    void validarDuplicidadeCodigo_deveLancarExcecaoQuandoJaExiste() {
        when(equipamentoRepository.existsByCodigo("EQ-001")).thenReturn(true);

        assertThatThrownBy(() -> service.validarDuplicidadeCodigo("EQ-001"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("já cadastrado");
    }

    @Test
    @DisplayName("validarDuplicidadeNS não deve lançar exceção quando disponível")
    void validarDuplicidadeNS_naoDeveLancarQuandoDisponivel() {
        when(equipamentoRepository.existsByNumeroSerie("NS-999")).thenReturn(false);

        service.validarDuplicidadeNS("NS-999");
    }

    @Test
    @DisplayName("enviarManutencao deve mudar estado e salvar")
    void enviarManutencao_deveMudarEstadoESalvar() {
        when(equipamentoRepository.findById(equipamentoId)).thenReturn(Optional.of(equipamento));
        when(equipamentoRepository.save(any(Equipamento.class))).thenAnswer(inv -> inv.getArgument(0));

        Equipamento resultado = service.enviarManutencao(equipamentoId);

        assertThat(resultado.getEstado()).isEqualTo(EstadoEquipamento.MANUTENCAO);
    }

    @Test
    @DisplayName("concluirManutencao deve mudar estado e salvar")
    void concluirManutencao_deveMudarEstadoESalvar() {
        equipamento.setEstado(EstadoEquipamento.MANUTENCAO);
        when(equipamentoRepository.findById(equipamentoId)).thenReturn(Optional.of(equipamento));
        when(equipamentoRepository.save(any(Equipamento.class))).thenAnswer(inv -> inv.getArgument(0));

        Equipamento resultado = service.concluirManutencao(equipamentoId);

        assertThat(resultado.getEstado()).isEqualTo(EstadoEquipamento.DISPONIVEL);
    }

    @Test
    @DisplayName("marcarDescartado deve mudar estado e salvar")
    void marcarDescartado_deveMudarEstadoESalvar() {
        when(equipamentoRepository.findById(equipamentoId)).thenReturn(Optional.of(equipamento));
        when(equipamentoRepository.save(any(Equipamento.class))).thenAnswer(inv -> inv.getArgument(0));

        Equipamento resultado = service.marcarDescartado(equipamentoId);

        assertThat(resultado.getEstado()).isEqualTo(EstadoEquipamento.DESCARTADO);
    }
}
