package com.radiocom.ordemservico.unit.application.service;

import com.radiocom.auth.domain.model.Usuario;
import com.radiocom.auth.domain.repository.UsuarioRepository;
import com.radiocom.cliente.domain.model.Cliente;
import com.radiocom.cliente.domain.model.Posto;
import com.radiocom.cliente.domain.model.enums.TipoPessoa;
import com.radiocom.cliente.domain.service.ClienteDomainService;
import com.radiocom.configuracao.domain.model.Configuracao;
import com.radiocom.configuracao.domain.service.ConfiguracaoDomainService;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.ordemservico.application.service.OrdemServicoPdfService;
import com.radiocom.ordemservico.domain.model.ItemConserto;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.OrdemServico;
import com.radiocom.ordemservico.domain.model.enums.TipoItemConserto;
import com.radiocom.ordemservico.domain.service.ItemEntradaDomainService;
import com.radiocom.ordemservico.domain.service.OrdemServicoDomainService;
import com.radiocom.shared.pdf.LogoEmpresa;
import com.radiocom.shared.pdf.PdfRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Usa o mesmo SpringTemplateEngine real (XML mode) configurado em
 * PdfTemplateConfig e o PdfRenderer real — só os serviços de domínio são
 * mockados. Isso exercita de verdade o template
 * templates/documentos/ordem-servico.html, pegando erros de variável/sintaxe
 * que um mock do template engine nunca pegaria.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrdemServicoPdfService - Teste de Integração do Template")
class OrdemServicoPdfServiceTest {

    @Mock private OrdemServicoDomainService osDomainService;
    @Mock private ItemEntradaDomainService itemEntradaDomainService;
    @Mock private ClienteDomainService clienteDomainService;
    @Mock private ConfiguracaoDomainService configuracaoDomainService;
    @Mock private UsuarioRepository usuarioRepository;

    private OrdemServicoPdfService service;

    private UUID osId;
    private UUID clienteId;
    private UUID postoId;
    private UUID tecnicoId;

    @BeforeEach
    void setUp() {
        SpringTemplateEngine templateEngine = criarTemplateEngineReal();
        service = new OrdemServicoPdfService(
                osDomainService, itemEntradaDomainService, clienteDomainService, configuracaoDomainService,
                usuarioRepository, new PdfRenderer(), templateEngine,
                new LogoEmpresa(new ClassPathResource("static/logo-teletrom.png")));

        osId = UUID.randomUUID();
        clienteId = UUID.randomUUID();
        postoId = UUID.randomUUID();
        tecnicoId = UUID.randomUUID();

        Configuracao empresa = Configuracao.builder()
                .valorMaoDeObraPadrao(BigDecimal.ZERO)
                .prazoGarantiaPecaDias(90)
                .prazoGarantiaEquipamentoDias(90)
                .prazoGarantiaAcessorioDias(90)
                .nomeEmpresa("Teletrom")
                .razaoSocialEmpresa("Teletrom Comércio e Serviços Ltda")
                .documentoEmpresa("59273032000103")
                .inscricaoEstadualEmpresa("0836629500144")
                .enderecoEmpresa("Av. Industrial, 500")
                .bairroEmpresa("Distrito Industrial")
                .cidadeEmpresa("São Paulo/SP")
                .telefoneEmpresa("(11) 4002-8922")
                .emailEmpresa("contato@teletrom.com.br")
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
        OrdemServico os = OrdemServico.builder()
                .numero("OS-2026-0001")
                .clienteId(clienteId)
                .postoId(postoId)
                .tecnicoId(tecnicoId)
                .solicitante("João da Silva")
                .build();
        ReflectionTestUtils.setField(os, "id", osId);

        Posto posto = Posto.builder().nome("Matriz").responsavel("Maria Souza").build();
        ReflectionTestUtils.setField(posto, "id", postoId);

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
                .postos(new java.util.LinkedHashSet<>(java.util.Set.of(posto)))
                .contatos(new java.util.LinkedHashSet<>(java.util.Set.of(contato)))
                .build();
        ReflectionTestUtils.setField(cliente, "id", clienteId);

        Usuario tecnico = Usuario.builder().nome("Técnico João").build();
        ReflectionTestUtils.setField(tecnico, "id", tecnicoId);

        ItemEntrada item = ItemEntrada.builder()
                .osId(osId)
                .tipoItem(TipoItem.EQUIPAMENTO)
                .descricao("Rádio Motorola EP450")
                .marca("Motorola")
                .modelo("EP450")
                .defeitoRelatado("Não liga")
                .build();
        item.avaliar("Capacitor queimado", false);
        item.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.PECA)
                .descricao("Capacitor")
                .quantidade(1)
                .valorUnitario(new BigDecimal("35.00"))
                .build());

        when(osDomainService.buscarPorId(osId)).thenReturn(os);
        when(itemEntradaDomainService.listarPorOS(osId)).thenReturn(List.of(item));
        when(clienteDomainService.buscarPorIdComRelacionamentos(clienteId)).thenReturn(cliente);
        when(usuarioRepository.findById(tecnicoId)).thenReturn(Optional.of(tecnico));

        byte[] pdf = service.gerarPdf(osId);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        // calcularTotalConserto() deve ter rodado antes do template, preenchendo
        // valorTotal de cada ItemConserto (senão a linha por peça sai "R$ null" —
        // só a linha de total do item calcularia certo).
        assertThat(item.getItensConserto().get(0).getValorTotal()).isEqualByComparingTo("35.00");
    }

    @Test
    @DisplayName("gerarPdf deve funcionar sem posto, técnico ou itens de conserto")
    void gerarPdf_deveFuncionarSemPostoTecnicoOuItensDeConserto() {
        OrdemServico os = OrdemServico.builder()
                .numero("OS-2026-0002")
                .clienteId(clienteId)
                .build();
        ReflectionTestUtils.setField(os, "id", osId);

        Cliente cliente = Cliente.builder()
                .tipo(TipoPessoa.PESSOA_FISICA)
                .documento("11144477735")
                .nomeRazaoSocial("Cliente Pessoa Física")
                .build();
        ReflectionTestUtils.setField(cliente, "id", clienteId);

        when(osDomainService.buscarPorId(osId)).thenReturn(os);
        when(itemEntradaDomainService.listarPorOS(osId)).thenReturn(List.of());
        when(clienteDomainService.buscarPorIdComRelacionamentos(clienteId)).thenReturn(cliente);

        byte[] pdf = service.gerarPdf(osId);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("template não deve exibir peças/valores de conserto de nenhum item, autorizado ou não")
    void template_naoDeveExibirItensDeConsertoDeNenhumItem() {
        SpringTemplateEngine templateEngine = criarTemplateEngineReal();

        OrdemServico os = OrdemServico.builder()
                .numero("OS-2026-0003")
                .clienteId(clienteId)
                .build();
        ReflectionTestUtils.setField(os, "id", osId);

        Cliente cliente = Cliente.builder()
                .tipo(TipoPessoa.PESSOA_FISICA)
                .documento("11144477735")
                .nomeRazaoSocial("Cliente Pessoa Física")
                .build();
        ReflectionTestUtils.setField(cliente, "id", clienteId);

        ItemEntrada itemAutorizado = ItemEntrada.builder()
                .osId(osId)
                .tipoItem(TipoItem.EQUIPAMENTO)
                .descricao("Rádio Motorola EP450")
                .defeitoRelatado("Não liga")
                .build();
        itemAutorizado.avaliar("Capacitor queimado", false);
        itemAutorizado.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.PECA)
                .descricao("Capacitor Trocado")
                .quantidade(1)
                .valorUnitario(new BigDecimal("35.00"))
                .build());

        ItemEntrada itemNaoAutorizado = ItemEntrada.builder()
                .osId(osId)
                .tipoItem(TipoItem.EQUIPAMENTO)
                .descricao("Rádio Motorola EP350")
                .defeitoRelatado("Tela quebrada")
                .build();
        itemNaoAutorizado.avaliar("Placa danificada", false);
        itemNaoAutorizado.enviarParaAutorizacao();
        itemNaoAutorizado.adicionarItemConserto(ItemConserto.builder()
                .tipo(TipoItemConserto.PECA)
                .descricao("Capacitor Recusado Pelo Cliente")
                .quantidade(1)
                .valorUnitario(new BigDecimal("35.00"))
                .build());
        itemNaoAutorizado.naoAutorizar("Cliente não quis pagar");
        itemAutorizado.calcularTotalConserto();
        itemNaoAutorizado.calcularTotalConserto();

        Context ctx = new Context();
        ctx.setVariable("os", os);
        ctx.setVariable("equipamentos", List.of(itemAutorizado, itemNaoAutorizado));
        ctx.setVariable("acessorios", List.of());
        ctx.setVariable("cliente", cliente);
        ctx.setVariable("contatoPrincipal", null);
        ctx.setVariable("posto", null);
        ctx.setVariable("tecnicoNome", null);
        ctx.setVariable("empresa", null);
        ctx.setVariable("logoBase64", null);
        ctx.setVariable("dataGeracao", java.time.LocalDateTime.now());

        String html = templateEngine.process("documentos/ordem-servico", ctx);

        assertThat(html).doesNotContain("Capacitor Trocado");
        assertThat(html).doesNotContain("Capacitor Recusado Pelo Cliente");
        assertThat(html).doesNotContain("R$ 35,00");
        assertThat(html).contains("Cliente não quis pagar");
        assertThat(html).contains("Capacitor queimado");
    }

    @Test
    @DisplayName("template deve agrupar cada item num bloco que não quebra entre páginas")
    void template_deveAgruparCadaItemNumBlocoQueNaoQuebraEntrePaginas() {
        SpringTemplateEngine templateEngine = criarTemplateEngineReal();

        OrdemServico os = OrdemServico.builder().numero("OS-2026-0004").clienteId(clienteId).build();
        ReflectionTestUtils.setField(os, "id", osId);

        Cliente cliente = Cliente.builder()
                .tipo(TipoPessoa.PESSOA_FISICA)
                .documento("11144477735")
                .nomeRazaoSocial("Cliente Pessoa Física")
                .build();
        ReflectionTestUtils.setField(cliente, "id", clienteId);

        ItemEntrada equipamento1 = ItemEntrada.builder().osId(osId).tipoItem(TipoItem.EQUIPAMENTO).descricao("Rádio 1").build();
        ItemEntrada equipamento2 = ItemEntrada.builder().osId(osId).tipoItem(TipoItem.EQUIPAMENTO).descricao("Rádio 2").build();
        ItemEntrada acessorio1 = ItemEntrada.builder().osId(osId).tipoItem(TipoItem.ACESSORIO).descricao("Bateria").build();

        Context ctx = new Context();
        ctx.setVariable("os", os);
        ctx.setVariable("equipamentos", List.of(equipamento1, equipamento2));
        ctx.setVariable("acessorios", List.of(acessorio1));
        ctx.setVariable("cliente", cliente);
        ctx.setVariable("contatoPrincipal", null);
        ctx.setVariable("posto", null);
        ctx.setVariable("tecnicoNome", null);
        ctx.setVariable("empresa", null);
        ctx.setVariable("logoBase64", null);
        ctx.setVariable("dataGeracao", java.time.LocalDateTime.now());

        String html = templateEngine.process("documentos/ordem-servico", ctx);

        // Um <tbody class="item-bloco"> por item (2 equipamentos + 1 acessório) —
        // é esse agrupamento que o CSS page-break-inside: avoid usa pra manter
        // cada item inteiro numa página só, em vez de cortar no meio.
        int ocorrencias = html.split("class=\"item-bloco\"", -1).length - 1;
        assertThat(ocorrencias).isEqualTo(3);
        assertThat(html).contains("page-break-inside: avoid");
        assertThat(html).contains("<thead>");
    }
}
