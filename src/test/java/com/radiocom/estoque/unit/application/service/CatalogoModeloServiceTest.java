package com.radiocom.estoque.unit.application.service;

import com.radiocom.estoque.application.dto.CatalogoModeloCreateDTO;
import com.radiocom.estoque.application.dto.CatalogoModeloDTO;
import com.radiocom.estoque.application.dto.CatalogoModeloUpdateDTO;
import com.radiocom.estoque.application.mapper.EstoqueMapper;
import com.radiocom.estoque.application.service.CatalogoModeloService;
import com.radiocom.estoque.domain.model.CatalogoModelo;
import com.radiocom.estoque.domain.model.enums.StatusItem;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.estoque.domain.repository.CatalogoModeloRepository;
import com.radiocom.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogoModeloService - Testes Unitários")
class CatalogoModeloServiceTest {

    @Mock private CatalogoModeloRepository repository;
    private final EstoqueMapper mapper = new EstoqueMapper();

    private CatalogoModeloService service;

    private UUID modeloId;
    private CatalogoModelo modelo;

    @BeforeEach
    void setUp() {
        service = new CatalogoModeloService(repository, mapper);
        modeloId = UUID.randomUUID();
        modelo = CatalogoModelo.builder()
                .tipoItem(TipoItem.EQUIPAMENTO)
                .marca("Motorola")
                .modelo("EP450")
                .status(StatusItem.ATIVO)
                .build();
    }

    @Test
    @DisplayName("criar deve salvar quando não há duplicidade")
    void criar_deveSalvarQuandoNaoHaDuplicidade() {
        CatalogoModeloCreateDTO dto = CatalogoModeloCreateDTO.builder()
                .tipoItem(TipoItem.EQUIPAMENTO)
                .marca("Motorola")
                .modelo("EP450")
                .build();

        when(repository.findByTipoItemAndMarcaIgnoreCaseAndModeloIgnoreCase(
                TipoItem.EQUIPAMENTO, "Motorola", "EP450")).thenReturn(Optional.empty());
        when(repository.save(any(CatalogoModelo.class))).thenAnswer(inv -> inv.getArgument(0));

        CatalogoModeloDTO resultado = service.criar(dto);

        assertThat(resultado.getMarca()).isEqualTo("Motorola");
        assertThat(resultado.getModelo()).isEqualTo("EP450");
    }

    @Test
    @DisplayName("criar deve salvar o valor de referência quando informado")
    void criar_deveSalvarValorReferencia() {
        CatalogoModeloCreateDTO dto = CatalogoModeloCreateDTO.builder()
                .tipoItem(TipoItem.EQUIPAMENTO)
                .marca("Motorola")
                .modelo("EP450")
                .valorReferencia(new java.math.BigDecimal("1250.00"))
                .build();

        when(repository.findByTipoItemAndMarcaIgnoreCaseAndModeloIgnoreCase(
                TipoItem.EQUIPAMENTO, "Motorola", "EP450")).thenReturn(Optional.empty());
        when(repository.save(any(CatalogoModelo.class))).thenAnswer(inv -> inv.getArgument(0));

        CatalogoModeloDTO resultado = service.criar(dto);

        assertThat(resultado.getValorReferencia()).isEqualByComparingTo("1250.00");
    }

    @Test
    @DisplayName("criar deve lançar exceção quando já existe no catálogo")
    void criar_deveLancarExcecaoQuandoJaExiste() {
        CatalogoModeloCreateDTO dto = CatalogoModeloCreateDTO.builder()
                .tipoItem(TipoItem.EQUIPAMENTO)
                .marca("Motorola")
                .modelo("EP450")
                .build();

        when(repository.findByTipoItemAndMarcaIgnoreCaseAndModeloIgnoreCase(
                TipoItem.EQUIPAMENTO, "Motorola", "EP450")).thenReturn(Optional.of(modelo));

        assertThatThrownBy(() -> service.criar(dto))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Já existe no catálogo");
    }

    @Test
    @DisplayName("buscarPorId deve lançar exceção quando não existe")
    void buscarPorId_deveLancarExcecaoQuandoNaoExiste() {
        when(repository.findById(modeloId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(modeloId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Entrada de catálogo não encontrada");
    }

    @Test
    @DisplayName("atualizar deve aplicar mudanças não-nulas")
    void atualizar_deveAplicarMudancasNaoNulas() {
        CatalogoModeloUpdateDTO dto = CatalogoModeloUpdateDTO.builder()
                .descricao("Rádio portátil UHF")
                .status(StatusItem.OBSOLETO)
                .build();

        when(repository.findById(modeloId)).thenReturn(Optional.of(modelo));
        when(repository.save(any(CatalogoModelo.class))).thenAnswer(inv -> inv.getArgument(0));

        CatalogoModeloDTO resultado = service.atualizar(modeloId, dto);

        assertThat(resultado.getDescricao()).isEqualTo("Rádio portátil UHF");
        assertThat(resultado.getStatus()).isEqualTo(StatusItem.OBSOLETO);
    }

    @Test
    @DisplayName("atualizar deve aplicar o valor de referência quando informado")
    void atualizar_deveAplicarValorReferencia() {
        CatalogoModeloUpdateDTO dto = CatalogoModeloUpdateDTO.builder()
                .valorReferencia(new java.math.BigDecimal("80.00"))
                .build();

        when(repository.findById(modeloId)).thenReturn(Optional.of(modelo));
        when(repository.save(any(CatalogoModelo.class))).thenAnswer(inv -> inv.getArgument(0));

        CatalogoModeloDTO resultado = service.atualizar(modeloId, dto);

        assertThat(resultado.getValorReferencia()).isEqualByComparingTo("80.00");
    }

    @Test
    @DisplayName("listar deve delegar busca/tipo/status pro repositório e mapear a página")
    void listar_deveDelegarERetornarPaginaMapeada() {
        Pageable pageable = PageRequest.of(0, 20);
        when(repository.buscar("Motorola", TipoItem.EQUIPAMENTO, StatusItem.ATIVO, pageable))
                .thenReturn(new PageImpl<>(List.of(modelo), pageable, 1));

        var resultado = service.listar("Motorola", TipoItem.EQUIPAMENTO, StatusItem.ATIVO, pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).getMarca()).isEqualTo("Motorola");
    }

    @Test
    @DisplayName("listar deve funcionar sem nenhum filtro informado")
    void listar_deveFuncionarSemFiltros() {
        Pageable pageable = PageRequest.of(0, 20);
        when(repository.buscar(null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(modelo), pageable, 1));

        var resultado = service.listar(null, null, null, pageable);

        assertThat(resultado.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("listarMarcas deve delegar pro repositório")
    void listarMarcas_deveDelegar() {
        when(repository.listarMarcas()).thenReturn(List.of("Icom", "Motorola"));

        assertThat(service.listarMarcas()).containsExactly("Icom", "Motorola");
    }

    @Test
    @DisplayName("deletar deve lançar exceção quando não existe")
    void deletar_deveLancarExcecaoQuandoNaoExiste() {
        when(repository.findById(modeloId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deletar(modeloId))
                .isInstanceOf(DomainException.class);
    }
}
