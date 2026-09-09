package com.radiocom.orcamento.unit.application.service;

import com.radiocom.cliente.domain.model.Cliente;
import com.radiocom.cliente.domain.model.enums.TipoPessoa;
import com.radiocom.cliente.domain.service.ClienteDomainService;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.ordemservico.domain.model.ItemConserto;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.enums.TipoItemConserto;
import com.radiocom.orcamento.application.service.OrcamentoPdfService;
import com.radiocom.orcamento.domain.model.Orcamento;
import com.radiocom.orcamento.domain.model.enums.StatusAprovacaoOrcamento;
import com.radiocom.orcamento.domain.service.OrcamentoDomainService;
import com.radiocom.shared.pdf.PdfRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Mesma abordagem de OrdemServicoPdfServiceTest: engine Thymeleaf real (XML
 * mode) + PdfRenderer real, só os serviços de domínio mockados — exercita de
 * verdade templates/documentos/orcamento.html.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrcamentoPdfService - Teste de Integração do Template")
class OrcamentoPdfServiceTest {

    @Mock private OrcamentoDomainService orcamentoDomainService;
    @Mock private ClienteDomainService clienteDomainService;

    private OrcamentoPdfService service;

    private UUID orcamentoId;
    private UUID clienteId;
    private UUID osId;

    @BeforeEach
    void setUp() {
        SpringTemplateEngine templateEngine = criarTemplateEngineReal();
        service = new OrcamentoPdfService(
                orcamentoDomainService, clienteDomainService, new PdfRenderer(), templateEngine);

        orcamentoId = UUID.randomUUID();
        clienteId = UUID.randomUUID();
        osId = UUID.randomUUID();
    }

    private SpringTemplateEngine criarTemplateEngineReal() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.XML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    @Test
    @DisplayName("gerarPdf deve renderizar o template real e retornar bytes de PDF válidos")
    void gerarPdf_deveRenderizarTemplateRealERetornarPdfValido() {
        Orcamento orcamento = Orcamento.builder()
                .numero("ORC-2026-0001")
                .osId(osId)
                .clienteId(clienteId)
                .validade(LocalDate.now().plusDays(15))
                .condicoesPagamento("50% na aprovação, 50% na entrega")
                .desconto(new BigDecimal("10.00"))
                .build();
        ReflectionTestUtils.setField(orcamento, "id", orcamentoId);

        Cliente cliente = Cliente.builder()
                .tipo(TipoPessoa.PESSOA_JURIDICA)
                .documento("11222333000181")
                .nomeRazaoSocial("Empresa Cliente Ltda")
                .build();
        ReflectionTestUtils.setField(cliente, "id", clienteId);

        ItemEntrada item = ItemEntrada.builder()
                .osId(osId)
                .tipoItem(TipoItem.EQUIPAMENTO)
                .descricao("Rádio Motorola EP450")
                .marca("Motorola")
                .modelo("EP450")
                .build();
        item.avaliar("Capacitor queimado", false);
        item.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.PECA)
                .descricao("Capacitor")
                .quantidade(1)
                .valorUnitario(new BigDecimal("35.00"))
                .build());

        when(orcamentoDomainService.buscarPorId(orcamentoId)).thenReturn(orcamento);
        when(orcamentoDomainService.listarItens(orcamentoId)).thenReturn(List.of(item));
        when(orcamentoDomainService.calcularTotal(orcamentoId)).thenReturn(new BigDecimal("25.00"));
        when(orcamentoDomainService.calcularStatusAprovacao(orcamentoId)).thenReturn(StatusAprovacaoOrcamento.PENDENTE);
        when(clienteDomainService.buscarPorId(clienteId)).thenReturn(cliente);

        byte[] pdf = service.gerarPdf(orcamentoId);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        // calcularTotalConserto() deve ter rodado antes do template, preenchendo
        // valorTotal de cada ItemConserto (senão a linha por peça sai "R$ null").
        assertThat(item.getItensConserto().get(0).getValorTotal()).isEqualByComparingTo("35.00");
    }

    @Test
    @DisplayName("gerarPdf deve funcionar sem validade, condições ou itens")
    void gerarPdf_deveFuncionarSemValidadeCondicoesOuItens() {
        Orcamento orcamento = Orcamento.builder()
                .numero("ORC-2026-0002")
                .osId(osId)
                .clienteId(clienteId)
                .build();
        ReflectionTestUtils.setField(orcamento, "id", orcamentoId);

        Cliente cliente = Cliente.builder()
                .tipo(TipoPessoa.PESSOA_FISICA)
                .documento("11144477735")
                .nomeRazaoSocial("Cliente Pessoa Física")
                .build();
        ReflectionTestUtils.setField(cliente, "id", clienteId);

        when(orcamentoDomainService.buscarPorId(orcamentoId)).thenReturn(orcamento);
        when(orcamentoDomainService.listarItens(orcamentoId)).thenReturn(List.of());
        when(orcamentoDomainService.calcularTotal(orcamentoId)).thenReturn(BigDecimal.ZERO);
        when(orcamentoDomainService.calcularStatusAprovacao(orcamentoId)).thenReturn(StatusAprovacaoOrcamento.PENDENTE);
        when(clienteDomainService.buscarPorId(clienteId)).thenReturn(cliente);

        byte[] pdf = service.gerarPdf(orcamentoId);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }
}
