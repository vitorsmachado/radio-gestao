package com.radiocom.configuracao.unit.domain.service;

import com.radiocom.configuracao.domain.model.Configuracao;
import com.radiocom.configuracao.domain.repository.ConfiguracaoRepository;
import com.radiocom.configuracao.domain.service.ConfiguracaoDomainService;
import com.radiocom.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfiguracaoDomainService - Testes Unitários")
class ConfiguracaoDomainServiceTest {

    @Mock private ConfiguracaoRepository repository;

    @InjectMocks
    private ConfiguracaoDomainService service;

    private Configuracao configuracao;

    @BeforeEach
    void setUp() {
        configuracao = Configuracao.builder().valorMaoDeObraPadrao(new BigDecimal("50.00")).build();
    }

    @Test
    @DisplayName("buscar deve retornar a configuração existente")
    void buscar_deveRetornarConfiguracao() {
        when(repository.findFirstByOrderByDataCriacaoAsc()).thenReturn(Optional.of(configuracao));

        assertThat(service.buscar().getValorMaoDeObraPadrao()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("buscar deve lançar exceção quando não existe nenhuma configuração")
    void buscar_deveLancarExcecaoQuandoNaoExiste() {
        when(repository.findFirstByOrderByDataCriacaoAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar()).isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("atualizarValorMaoDeObraPadrao deve definir o novo valor e salvar")
    void atualizarValorMaoDeObraPadrao_deveDefinirESalvar() {
        when(repository.findFirstByOrderByDataCriacaoAsc()).thenReturn(Optional.of(configuracao));
        when(repository.save(any(Configuracao.class))).thenAnswer(inv -> inv.getArgument(0));

        Configuracao resultado = service.atualizarValorMaoDeObraPadrao(new BigDecimal("75.00"));

        assertThat(resultado.getValorMaoDeObraPadrao()).isEqualByComparingTo("75.00");
    }
}
