package com.radiocom.ordemservico.garantia.unit.domain.service;

import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.ordemservico.domain.model.ItemConserto;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.enums.TipoItemConserto;
import com.radiocom.ordemservico.garantia.domain.model.GarantiaPeca;
import com.radiocom.ordemservico.garantia.domain.repository.GarantiaPecaRepository;
import com.radiocom.ordemservico.garantia.domain.service.GarantiaPecaDomainService;
import com.radiocom.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GarantiaPecaDomainService - Testes Unitários")
class GarantiaPecaDomainServiceTest {

    @Mock private GarantiaPecaRepository repository;

    @InjectMocks
    private GarantiaPecaDomainService service;

    private UUID itemEstoqueId;
    private UUID itemEntradaId;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "prazoDias", 90);
        itemEstoqueId = UUID.randomUUID();
        itemEntradaId = UUID.randomUUID();
    }

    private ItemEntrada criarItem() {
        ItemEntrada item = ItemEntrada.builder()
                .osId(UUID.randomUUID())
                .tipoItem(TipoItem.EQUIPAMENTO)
                .descricao("Rádio Motorola EP450")
                .itemEstoqueId(itemEstoqueId)
                .build();
        ReflectionTestUtils.setField(item, "id", itemEntradaId);
        return item;
    }

    @Test
    @DisplayName("registrarCobertura deve criar uma cobertura por peça de estoque usada no conserto")
    void registrarCobertura_deveCriarCoberturaPorPeca() {
        UUID pecaId = UUID.randomUUID();
        ItemEntrada item = criarItem();
        item.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.PECA).itemEstoqueId(pecaId).descricao("Bateria BP-227")
                .quantidade(1).valorUnitario(new BigDecimal("80.00")).build());
        item.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.MAO_DE_OBRA).descricao("Mão de obra")
                .quantidade(1).valorUnitario(new BigDecimal("50.00")).build());

        service.registrarCobertura(item);

        ArgumentCaptor<GarantiaPeca> captor = ArgumentCaptor.forClass(GarantiaPeca.class);
        verify(repository, times(1)).save(captor.capture());
        GarantiaPeca salva = captor.getValue();
        assertThat(salva.getItemEstoqueId()).isEqualTo(itemEstoqueId);
        assertThat(salva.getPecaEstoqueId()).isEqualTo(pecaId);
        assertThat(salva.getDescricaoPeca()).isEqualTo("Bateria BP-227");
        assertThat(salva.getItemEntradaOrigemId()).isEqualTo(itemEntradaId);
        assertThat(salva.getDataFim()).isEqualTo(salva.getDataInicio().plusDays(90));
    }

    @Test
    @DisplayName("registrarCobertura não deve criar nada quando o item não tem equipamento vinculado")
    void registrarCobertura_naoDeveCriarQuandoSemItemEstoque() {
        ItemEntrada item = ItemEntrada.builder()
                .osId(UUID.randomUUID()).tipoItem(TipoItem.EQUIPAMENTO).descricao("Rádio").build();
        item.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.PECA).itemEstoqueId(UUID.randomUUID()).descricao("Bateria")
                .quantidade(1).valorUnitario(BigDecimal.TEN).build());

        service.registrarCobertura(item);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("listarCoberturaAtiva deve delegar para o repositório com a data de hoje")
    void listarCoberturaAtiva_deveDelegar() {
        GarantiaPeca cobertura = GarantiaPeca.builder().build();
        when(repository.findByItemEstoqueIdAndDataFimGreaterThanEqual(any(), any()))
                .thenReturn(List.of(cobertura));

        List<GarantiaPeca> resultado = service.listarCoberturaAtiva(itemEstoqueId);

        assertThat(resultado).containsExactly(cobertura);
    }

    @Test
    @DisplayName("validarCoberturaAtiva deve lançar exceção quando a cobertura não está na lista ativa")
    void validarCoberturaAtiva_deveLancarExcecaoQuandoNaoEncontrada() {
        when(repository.findByItemEstoqueIdAndDataFimGreaterThanEqual(any(), any())).thenReturn(List.of());

        assertThatThrownBy(() -> service.validarCoberturaAtiva(itemEstoqueId, UUID.randomUUID()))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("validarCoberturaAtiva deve retornar a cobertura quando encontrada e ativa")
    void validarCoberturaAtiva_deveRetornarQuandoEncontrada() {
        UUID garantiaPecaId = UUID.randomUUID();
        GarantiaPeca cobertura = GarantiaPeca.builder()
                .itemEstoqueId(itemEstoqueId).dataInicio(LocalDate.now()).dataFim(LocalDate.now().plusDays(10))
                .build();
        ReflectionTestUtils.setField(cobertura, "id", garantiaPecaId);
        when(repository.findByItemEstoqueIdAndDataFimGreaterThanEqual(any(), any())).thenReturn(List.of(cobertura));

        GarantiaPeca resultado = service.validarCoberturaAtiva(itemEstoqueId, garantiaPecaId);

        assertThat(resultado.getId()).isEqualTo(garantiaPecaId);
    }
}
