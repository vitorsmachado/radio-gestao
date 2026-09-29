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
        configuracao = Configuracao.builder()
                .valorMaoDeObraPadrao(new BigDecimal("50.00"))
                .prazoGarantiaPecaDias(90)
                .prazoGarantiaEquipamentoDias(90)
                .prazoGarantiaAcessorioDias(90)
                .nomeEmpresa("Teletrom")
                .documentoEmpresa("59273032000103")
                .build();
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
    @DisplayName("atualizar deve definir os novos valores e salvar")
    void atualizar_deveDefinirESalvar() {
        when(repository.findFirstByOrderByDataCriacaoAsc()).thenReturn(Optional.of(configuracao));
        when(repository.save(any(Configuracao.class))).thenAnswer(inv -> inv.getArgument(0));

        Configuracao dados = Configuracao.builder()
                .valorMaoDeObraPadrao(new BigDecimal("75.00"))
                .prazoGarantiaPecaDias(30)
                .prazoGarantiaEquipamentoDias(60)
                .prazoGarantiaAcessorioDias(45)
                .nomeEmpresa("Teletrom")
                .razaoSocialEmpresa("Teletrom Comércio e Serviços Ltda")
                .documentoEmpresa("59273032000103")
                .inscricaoEstadualEmpresa("0836629500144")
                .enderecoEmpresa("Rua Exemplo, 100")
                .bairroEmpresa("Centro")
                .cidadeEmpresa("São Paulo")
                .telefoneEmpresa("(11) 99999-9999")
                .emailEmpresa("contato@teletrom.com.br")
                .build();

        Configuracao resultado = service.atualizar(dados);

        assertThat(resultado.getValorMaoDeObraPadrao()).isEqualByComparingTo("75.00");
        assertThat(resultado.getPrazoGarantiaPecaDias()).isEqualTo(30);
        assertThat(resultado.getPrazoGarantiaEquipamentoDias()).isEqualTo(60);
        assertThat(resultado.getPrazoGarantiaAcessorioDias()).isEqualTo(45);
        assertThat(resultado.getNomeEmpresa()).isEqualTo("Teletrom");
        assertThat(resultado.getRazaoSocialEmpresa()).isEqualTo("Teletrom Comércio e Serviços Ltda");
        assertThat(resultado.getDocumentoEmpresa()).isEqualTo("59273032000103");
        assertThat(resultado.getInscricaoEstadualEmpresa()).isEqualTo("0836629500144");
        assertThat(resultado.getEnderecoEmpresa()).isEqualTo("Rua Exemplo, 100");
        assertThat(resultado.getBairroEmpresa()).isEqualTo("Centro");
        assertThat(resultado.getCidadeEmpresa()).isEqualTo("São Paulo");
        assertThat(resultado.getTelefoneEmpresa()).isEqualTo("(11) 99999-9999");
        assertThat(resultado.getEmailEmpresa()).isEqualTo("contato@teletrom.com.br");
    }
}
