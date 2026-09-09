package com.radiocom.orcamento.application.service;

import com.radiocom.cliente.domain.model.Cliente;
import com.radiocom.cliente.domain.service.ClienteDomainService;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.orcamento.domain.model.Orcamento;
import com.radiocom.orcamento.domain.model.enums.StatusAprovacaoOrcamento;
import com.radiocom.orcamento.domain.service.OrcamentoDomainService;
import com.radiocom.shared.pdf.PdfRenderer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrcamentoPdfService {

    private final OrcamentoDomainService orcamentoDomainService;
    private final ClienteDomainService clienteDomainService;
    private final PdfRenderer pdfRenderer;
    private final SpringTemplateEngine pdfTemplateEngine;

    public OrcamentoPdfService(OrcamentoDomainService orcamentoDomainService,
                                ClienteDomainService clienteDomainService,
                                PdfRenderer pdfRenderer,
                                @Qualifier("pdfTemplateEngine") SpringTemplateEngine pdfTemplateEngine) {
        this.orcamentoDomainService = orcamentoDomainService;
        this.clienteDomainService = clienteDomainService;
        this.pdfRenderer = pdfRenderer;
        this.pdfTemplateEngine = pdfTemplateEngine;
    }

    @Transactional(readOnly = true)
    public byte[] gerarPdf(UUID orcamentoId) {
        Orcamento orcamento = orcamentoDomainService.buscarPorId(orcamentoId);
        List<ItemEntrada> itens = orcamentoDomainService.listarItens(orcamentoId);
        // calcularTotalConserto() recalcula valorTotal de cada ItemConserto como
        // efeito colateral (ver ItemEntrada) — chamado aqui antes do template pra
        // garantir que as linhas por peça/serviço já saem com o valor certo.
        itens.forEach(ItemEntrada::calcularTotalConserto);
        BigDecimal valorTotal = orcamentoDomainService.calcularTotal(orcamentoId);
        StatusAprovacaoOrcamento statusAprovacao = orcamentoDomainService.calcularStatusAprovacao(orcamentoId);
        Cliente cliente = clienteDomainService.buscarPorId(orcamento.getClienteId());

        Context ctx = new Context();
        ctx.setVariable("orcamento", orcamento);
        ctx.setVariable("itens", itens);
        ctx.setVariable("valorTotal", valorTotal);
        ctx.setVariable("statusAprovacao", statusAprovacao);
        ctx.setVariable("cliente", cliente);
        ctx.setVariable("dataGeracao", LocalDateTime.now());

        String xhtml = pdfTemplateEngine.process("documentos/orcamento", ctx);
        return pdfRenderer.renderizar(xhtml);
    }
}
