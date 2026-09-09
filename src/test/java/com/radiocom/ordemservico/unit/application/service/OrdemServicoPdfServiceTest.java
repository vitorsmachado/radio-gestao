package com.radiocom.ordemservico.unit.application.service;

import com.radiocom.auth.domain.model.Usuario;
import com.radiocom.auth.domain.repository.UsuarioRepository;
import com.radiocom.cliente.domain.model.Cliente;
import com.radiocom.cliente.domain.model.Posto;
import com.radiocom.cliente.domain.model.enums.TipoPessoa;
import com.radiocom.cliente.domain.service.ClienteDomainService;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.ordemservico.application.service.OrdemServicoPdfService;
import com.radiocom.ordemservico.domain.model.ItemConserto;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.OrdemServico;
import com.radiocom.ordemservico.domain.model.enums.TipoItemConserto;
import com.radiocom.ordemservico.domain.service.ItemEntradaDomainService;
import com.radiocom.ordemservico.domain.service.OrdemServicoDomainService;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
                osDomainService, itemEntradaDomainService, clienteDomainService,
                usuarioRepository, new PdfRenderer(), templateEngine);

        osId = UUID.randomUUID();
        clienteId = UUID.randomUUID();
        postoId = UUID.randomUUID();
        tecnicoId = UUID.randomUUID();
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

        Cliente cliente = Cliente.builder()
                .tipo(TipoPessoa.PESSOA_JURIDICA)
                .documento("11222333000181")
                .nomeRazaoSocial("Empresa Cliente Ltda")
                .postos(new java.util.LinkedHashSet<>(java.util.Set.of(posto)))
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
}
