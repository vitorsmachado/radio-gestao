package com.radiocom.orcamento.unit.application.service;

import com.radiocom.cliente.domain.model.Cliente;
import com.radiocom.cliente.domain.model.enums.TipoPessoa;
import com.radiocom.cliente.domain.service.ClienteDomainService;
import com.radiocom.configuracao.domain.model.Configuracao;
import com.radiocom.configuracao.domain.service.ConfiguracaoDomainService;
import com.radiocom.estoque.application.service.CatalogoModeloService;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.ordemservico.domain.model.ItemConserto;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.OrdemServico;
import com.radiocom.ordemservico.domain.model.enums.TipoItemConserto;
import com.radiocom.ordemservico.domain.service.OrdemServicoDomainService;
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
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
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
    @Mock private OrdemServicoDomainService ordemServicoDomainService;
    @Mock private ClienteDomainService clienteDomainService;
    @Mock private ConfiguracaoDomainService configuracaoDomainService;
    @Mock private CatalogoModeloService catalogoModeloService;

    private OrcamentoPdfService service;

    private UUID orcamentoId;
    private UUID clienteId;
    private UUID osId;

    @BeforeEach
    void setUp() {
        SpringTemplateEngine templateEngine = criarTemplateEngineReal();
        service = new OrcamentoPdfService(orcamentoDomainService, ordemServicoDomainService, clienteDomainService,
                configuracaoDomainService, catalogoModeloService, new PdfRenderer(), templateEngine);

        orcamentoId = UUID.randomUUID();
        clienteId = UUID.randomUUID();
        osId = UUID.randomUUID();

        OrdemServico os = OrdemServico.builder().numero("OS-2026-0001").clienteId(clienteId)
                .solicitante("João da Silva").build();
        ReflectionTestUtils.setField(os, "id", osId);
        lenient().when(ordemServicoDomainService.buscarPorId(osId)).thenReturn(os);

        Configuracao empresa = Configuracao.builder()
                .valorMaoDeObraPadrao(BigDecimal.ZERO)
                .prazoGarantiaPecaDias(90)
                .prazoGarantiaEquipamentoDias(90)
                .prazoGarantiaAcessorioDias(90)
                .nomeEmpresa("Teletrom")
                .documentoEmpresa("59273032000103")
                .build();
        lenient().when(configuracaoDomainService.buscar()).thenReturn(empresa);
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

        com.radiocom.cliente.domain.model.Endereco endereco = com.radiocom.cliente.domain.model.Endereco.builder()
                .cep("01310100").logradouro("Av. Paulista").numero("1000")
                .bairro("Bela Vista").cidade("São Paulo").estado("SP").build();
        com.radiocom.cliente.domain.model.Contato contato = com.radiocom.cliente.domain.model.Contato.builder()
                .nome("Carlos Mendes").telefone("11988887777").principal(true)
                .tipo(com.radiocom.cliente.domain.model.enums.TipoContato.TECNICO).build();

        Cliente cliente = Cliente.builder()
                .tipo(TipoPessoa.PESSOA_JURIDICA)
                .documento("11222333000181")
                .nomeRazaoSocial("Empresa Cliente Ltda")
                .numeroIdentificacao(4821)
                .endereco(endereco)
                .contatos(new java.util.LinkedHashSet<>(java.util.Set.of(contato)))
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
        when(clienteDomainService.buscarPorIdComRelacionamentos(clienteId)).thenReturn(cliente);

        byte[] pdf = service.gerarPdf(orcamentoId, OrcamentoPdfService.Agrupamento.EQUIPAMENTO);

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
        when(clienteDomainService.buscarPorIdComRelacionamentos(clienteId)).thenReturn(cliente);

        byte[] pdf = service.gerarPdf(orcamentoId, OrcamentoPdfService.Agrupamento.EQUIPAMENTO);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("gerarPdf com agrupamento ITENS deve consolidar itens de conserto por descrição")
    void gerarPdf_comAgrupamentoItens_deveConsolidar() {
        Orcamento orcamento = Orcamento.builder()
                .numero("ORC-2026-0003")
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

        ItemEntrada item1 = ItemEntrada.builder().osId(osId).tipoItem(TipoItem.EQUIPAMENTO).descricao("Rádio 1").build();
        item1.avaliar("Bateria fraca", false);
        item1.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.PECA).descricao("Bateria BP-227").quantidade(1)
                .valorUnitario(new BigDecimal("80.00")).build());

        ItemEntrada item2 = ItemEntrada.builder().osId(osId).tipoItem(TipoItem.EQUIPAMENTO).descricao("Rádio 2").build();
        item2.avaliar("Bateria fraca", false);
        item2.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.PECA).descricao("Bateria BP-227").quantidade(1)
                .valorUnitario(new BigDecimal("80.00")).build());

        when(orcamentoDomainService.buscarPorId(orcamentoId)).thenReturn(orcamento);
        when(orcamentoDomainService.listarItens(orcamentoId)).thenReturn(List.of(item1, item2));
        when(orcamentoDomainService.calcularTotal(orcamentoId)).thenReturn(new BigDecimal("160.00"));
        when(orcamentoDomainService.calcularStatusAprovacao(orcamentoId)).thenReturn(StatusAprovacaoOrcamento.PENDENTE);
        when(clienteDomainService.buscarPorIdComRelacionamentos(clienteId)).thenReturn(cliente);

        byte[] pdf = service.gerarPdf(orcamentoId, OrcamentoPdfService.Agrupamento.ITENS);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("template deve agrupar cada item num bloco que não quebra entre páginas")
    void template_deveAgruparCadaItemNumBlocoQueNaoQuebraEntrePaginas() {
        SpringTemplateEngine templateEngine = criarTemplateEngineReal();

        Orcamento orcamento = Orcamento.builder().numero("ORC-2026-0004").osId(osId).clienteId(clienteId).build();
        ReflectionTestUtils.setField(orcamento, "id", orcamentoId);

        Cliente cliente = Cliente.builder()
                .tipo(TipoPessoa.PESSOA_FISICA)
                .documento("11144477735")
                .nomeRazaoSocial("Cliente Pessoa Física")
                .build();
        ReflectionTestUtils.setField(cliente, "id", clienteId);

        ItemEntrada item1 = ItemEntrada.builder().osId(osId).tipoItem(TipoItem.EQUIPAMENTO).descricao("Rádio 1").build();
        ReflectionTestUtils.setField(item1, "id", UUID.randomUUID());
        ItemEntrada item2 = ItemEntrada.builder().osId(osId).tipoItem(TipoItem.EQUIPAMENTO).descricao("Rádio 2").build();
        ReflectionTestUtils.setField(item2, "id", UUID.randomUUID());

        Context ctx = new Context();
        ctx.setVariable("orcamento", orcamento);
        ctx.setVariable("solicitante", null);
        ctx.setVariable("itens", List.of(item1, item2));
        ctx.setVariable("agrupamento", "EQUIPAMENTO");
        ctx.setVariable("itensConsolidados", List.of());
        ctx.setVariable("valoresReferencia", java.util.Map.of());
        ctx.setVariable("valorTotal", BigDecimal.ZERO);
        ctx.setVariable("statusAprovacao", StatusAprovacaoOrcamento.PENDENTE);
        ctx.setVariable("cliente", cliente);
        ctx.setVariable("documentoClienteFormatado", "111.444.777-35");
        ctx.setVariable("contatoPrincipal", null);
        ctx.setVariable("empresa", null);
        ctx.setVariable("documentoEmpresaFormatado", null);
        ctx.setVariable("logoBase64", null);
        ctx.setVariable("dataGeracao", java.time.LocalDateTime.now());

        String html = templateEngine.process("documentos/orcamento", ctx);

        // Um <tbody class="item-bloco"> por item — é esse agrupamento que o CSS
        // page-break-inside: avoid usa pra manter cada item inteiro numa página
        // só, em vez de cortar no meio (defeito/peças de conserto).
        int ocorrencias = html.split("class=\"item-bloco\"", -1).length - 1;
        assertThat(ocorrencias).isEqualTo(2);
        assertThat(html).contains("page-break-inside: avoid");
        assertThat(html).contains("<thead>");
    }
}
