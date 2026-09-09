package com.radiocom.ordemservico.application.service;

import com.radiocom.auth.domain.repository.UsuarioRepository;
import com.radiocom.cliente.domain.model.Cliente;
import com.radiocom.cliente.domain.model.Posto;
import com.radiocom.cliente.domain.service.ClienteDomainService;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.OrdemServico;
import com.radiocom.ordemservico.domain.service.ItemEntradaDomainService;
import com.radiocom.ordemservico.domain.service.OrdemServicoDomainService;
import com.radiocom.shared.pdf.PdfRenderer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrdemServicoPdfService {

    private final OrdemServicoDomainService osDomainService;
    private final ItemEntradaDomainService itemEntradaDomainService;
    private final ClienteDomainService clienteDomainService;
    private final UsuarioRepository usuarioRepository;
    private final PdfRenderer pdfRenderer;
    private final SpringTemplateEngine pdfTemplateEngine;

    public OrdemServicoPdfService(OrdemServicoDomainService osDomainService,
                                   ItemEntradaDomainService itemEntradaDomainService,
                                   ClienteDomainService clienteDomainService,
                                   UsuarioRepository usuarioRepository,
                                   PdfRenderer pdfRenderer,
                                   @Qualifier("pdfTemplateEngine") SpringTemplateEngine pdfTemplateEngine) {
        this.osDomainService = osDomainService;
        this.itemEntradaDomainService = itemEntradaDomainService;
        this.clienteDomainService = clienteDomainService;
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

        Context ctx = new Context();
        ctx.setVariable("os", os);
        ctx.setVariable("itens", itens);
        ctx.setVariable("cliente", cliente);
        ctx.setVariable("posto", posto);
        ctx.setVariable("tecnicoNome", tecnicoNome);
        ctx.setVariable("dataGeracao", LocalDateTime.now());

        String xhtml = pdfTemplateEngine.process("documentos/ordem-servico", ctx);
        return pdfRenderer.renderizar(xhtml);
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
