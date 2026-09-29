package com.radiocom.ordemservico.application.service;

import com.radiocom.auth.domain.repository.UsuarioRepository;
import com.radiocom.cliente.domain.model.Cliente;
import com.radiocom.cliente.domain.model.Contato;
import com.radiocom.cliente.domain.model.Posto;
import com.radiocom.cliente.domain.service.ClienteDomainService;
import com.radiocom.configuracao.domain.service.ConfiguracaoDomainService;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.OrdemServico;
import com.radiocom.ordemservico.domain.service.ItemEntradaDomainService;
import com.radiocom.ordemservico.domain.service.OrdemServicoDomainService;
import com.radiocom.configuracao.domain.model.Configuracao;
import com.radiocom.shared.pdf.PdfRenderer;
import com.radiocom.shared.util.DocumentoFormatter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class OrdemServicoPdfService {

    private final OrdemServicoDomainService osDomainService;
    private final ItemEntradaDomainService itemEntradaDomainService;
    private final ClienteDomainService clienteDomainService;
    private final ConfiguracaoDomainService configuracaoDomainService;
    private final UsuarioRepository usuarioRepository;
    private final PdfRenderer pdfRenderer;
    private final SpringTemplateEngine pdfTemplateEngine;

    private String logoBase64Cache;

    public OrdemServicoPdfService(OrdemServicoDomainService osDomainService,
                                   ItemEntradaDomainService itemEntradaDomainService,
                                   ClienteDomainService clienteDomainService,
                                   ConfiguracaoDomainService configuracaoDomainService,
                                   UsuarioRepository usuarioRepository,
                                   PdfRenderer pdfRenderer,
                                   @Qualifier("pdfTemplateEngine") SpringTemplateEngine pdfTemplateEngine) {
        this.osDomainService = osDomainService;
        this.itemEntradaDomainService = itemEntradaDomainService;
        this.clienteDomainService = clienteDomainService;
        this.configuracaoDomainService = configuracaoDomainService;
        this.usuarioRepository = usuarioRepository;
        this.pdfRenderer = pdfRenderer;
        this.pdfTemplateEngine = pdfTemplateEngine;
    }

    @Transactional(readOnly = true)
    public byte[] gerarPdf(UUID osId) {
        OrdemServico os = osDomainService.buscarPorId(osId);
        List<ItemEntrada> itens = itemEntradaDomainService.listarPorOS(osId);
        // calcularTotalConserto() recalcula valorTotal de cada ItemConserto como
        // efeito colateral (ver ItemEntrada) — chamado aqui antes do template pra
        // garantir que as linhas por peça/serviço já saem com o valor certo.
        itens.forEach(ItemEntrada::calcularTotalConserto);
        Cliente cliente = clienteDomainService.buscarPorIdComRelacionamentos(os.getClienteId());
        Posto posto = buscarPosto(cliente, os.getPostoId());
        String tecnicoNome = buscarNomeTecnico(os.getTecnicoId());
        Contato contatoPrincipal = cliente.getContatoPrincipal();
        Configuracao empresa = configuracaoDomainService.buscar();

        Context ctx = new Context();
        ctx.setVariable("os", os);
        ctx.setVariable("equipamentos", itens.stream().filter(i -> i.getTipoItem() == TipoItem.EQUIPAMENTO).toList());
        ctx.setVariable("acessorios", itens.stream().filter(i -> i.getTipoItem() == TipoItem.ACESSORIO).toList());
        ctx.setVariable("cliente", cliente);
        ctx.setVariable("documentoClienteFormatado", DocumentoFormatter.formatar(cliente.getDocumento()));
        ctx.setVariable("contatoPrincipal", contatoPrincipal);
        ctx.setVariable("posto", posto);
        ctx.setVariable("tecnicoNome", tecnicoNome);
        ctx.setVariable("empresa", empresa);
        ctx.setVariable("documentoEmpresaFormatado", DocumentoFormatter.formatar(empresa.getDocumentoEmpresa()));
        ctx.setVariable("logoBase64", carregarLogoBase64());
        ctx.setVariable("dataGeracao", LocalDateTime.now());

        String xhtml = pdfTemplateEngine.process("documentos/ordem-servico", ctx);
        return pdfRenderer.renderizar(xhtml);
    }

    private String carregarLogoBase64() {
        if (logoBase64Cache != null) return logoBase64Cache;
        try (InputStream in = new ClassPathResource("static/logo-teletrom.png").getInputStream()) {
            logoBase64Cache = Base64.getEncoder().encodeToString(in.readAllBytes());
            return logoBase64Cache;
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao carregar a logo da empresa", e);
        }
    }

    private Posto buscarPosto(Cliente cliente, UUID postoId) {
        if (postoId == null) return null;
        return cliente.getPostos().stream()
                .filter(p -> p.getId().equals(postoId))
                .findFirst()
                .orElse(null);
    }

    private String buscarNomeTecnico(UUID tecnicoId) {
        if (tecnicoId == null) return null;
        return usuarioRepository.findById(tecnicoId).map(u -> u.getNome()).orElse(null);
    }
}
