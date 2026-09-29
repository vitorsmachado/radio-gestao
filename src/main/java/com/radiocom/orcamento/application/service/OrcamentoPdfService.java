package com.radiocom.orcamento.application.service;

import com.radiocom.cliente.domain.model.Cliente;
import com.radiocom.cliente.domain.model.Contato;
import com.radiocom.cliente.domain.service.ClienteDomainService;
import com.radiocom.configuracao.domain.model.Configuracao;
import com.radiocom.configuracao.domain.service.ConfiguracaoDomainService;
import com.radiocom.estoque.application.service.CatalogoModeloService;
import com.radiocom.ordemservico.domain.model.ItemConserto;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.enums.ResultadoAvaliacao;
import com.radiocom.orcamento.domain.model.Orcamento;
import com.radiocom.orcamento.domain.model.enums.StatusAprovacaoOrcamento;
import com.radiocom.orcamento.domain.service.OrcamentoDomainService;
import com.radiocom.shared.pdf.PdfRenderer;
import com.radiocom.shared.util.DocumentoFormatter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class OrcamentoPdfService {

    public enum Agrupamento { EQUIPAMENTO, ITENS }

    @Getter
    @AllArgsConstructor
    public static class ItemConsolidadoView {
        private final String tipoLabel;
        private final String descricao;
        private final int quantidade;
        private final BigDecimal valorUnitario;
        private final BigDecimal valorTotal;
    }

    private final OrcamentoDomainService orcamentoDomainService;
    private final ClienteDomainService clienteDomainService;
    private final ConfiguracaoDomainService configuracaoDomainService;
    private final CatalogoModeloService catalogoModeloService;
    private final PdfRenderer pdfRenderer;
    private final SpringTemplateEngine pdfTemplateEngine;

    private String logoBase64Cache;

    public OrcamentoPdfService(OrcamentoDomainService orcamentoDomainService,
                                ClienteDomainService clienteDomainService,
                                ConfiguracaoDomainService configuracaoDomainService,
                                CatalogoModeloService catalogoModeloService,
                                PdfRenderer pdfRenderer,
                                @Qualifier("pdfTemplateEngine") SpringTemplateEngine pdfTemplateEngine) {
        this.orcamentoDomainService = orcamentoDomainService;
        this.clienteDomainService = clienteDomainService;
        this.configuracaoDomainService = configuracaoDomainService;
        this.catalogoModeloService = catalogoModeloService;
        this.pdfRenderer = pdfRenderer;
        this.pdfTemplateEngine = pdfTemplateEngine;
    }

    @Transactional(readOnly = true)
    public byte[] gerarPdf(UUID orcamentoId, Agrupamento agrupamento) {
        Orcamento orcamento = orcamentoDomainService.buscarPorId(orcamentoId);
        List<ItemEntrada> itens = orcamentoDomainService.listarItens(orcamentoId);
        // calcularTotalConserto() recalcula valorTotal de cada ItemConserto como
        // efeito colateral (ver ItemEntrada) — chamado aqui antes do template pra
        // garantir que as linhas por peça/serviço já saem com o valor certo.
        itens.forEach(ItemEntrada::calcularTotalConserto);
        BigDecimal valorTotal = orcamentoDomainService.calcularTotal(orcamentoId);
        StatusAprovacaoOrcamento statusAprovacao = orcamentoDomainService.calcularStatusAprovacao(orcamentoId);
        Cliente cliente = clienteDomainService.buscarPorIdComRelacionamentos(orcamento.getClienteId());
        Contato contatoPrincipal = cliente.getContatoPrincipal();
        Configuracao empresa = configuracaoDomainService.buscar();

        Context ctx = new Context();
        ctx.setVariable("orcamento", orcamento);
        ctx.setVariable("itens", itens);
        ctx.setVariable("agrupamento", agrupamento.name());
        ctx.setVariable("itensConsolidados", agrupamento == Agrupamento.ITENS ? consolidar(itens) : List.of());
        ctx.setVariable("valoresReferencia", valoresReferenciaSemConserto(itens));
        ctx.setVariable("valorTotal", valorTotal);
        ctx.setVariable("statusAprovacao", statusAprovacao);
        ctx.setVariable("cliente", cliente);
        ctx.setVariable("documentoClienteFormatado", DocumentoFormatter.formatar(cliente.getDocumento()));
        ctx.setVariable("contatoPrincipal", contatoPrincipal);
        ctx.setVariable("empresa", empresa);
        ctx.setVariable("documentoEmpresaFormatado", DocumentoFormatter.formatar(empresa.getDocumentoEmpresa()));
        ctx.setVariable("logoBase64", carregarLogoBase64());
        ctx.setVariable("dataGeracao", LocalDateTime.now());

        String xhtml = pdfTemplateEngine.process("documentos/orcamento", ctx);
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

    /** Agrupa todos os itens de conserto de todos os equipamentos por tipo+descrição+valor unitário, somando quantidade. */
    private List<ItemConsolidadoView> consolidar(List<ItemEntrada> itens) {
        Map<String, ItemConsolidadoView> porChave = new LinkedHashMap<>();
        for (ItemEntrada item : itens) {
            for (ItemConserto conserto : item.getItensConserto()) {
                String chave = conserto.getTipo() + "|" + conserto.getDescricao() + "|" + conserto.getValorUnitario();
                ItemConsolidadoView atual = porChave.get(chave);
                int quantidade = (atual != null ? atual.getQuantidade() : 0) + conserto.getQuantidade();
                BigDecimal valorTotal = conserto.getValorUnitario().multiply(BigDecimal.valueOf(quantidade));
                porChave.put(chave, new ItemConsolidadoView(
                        conserto.getTipo().getLabel(), conserto.getDescricao(), quantidade,
                        conserto.getValorUnitario(), valorTotal));
            }
        }
        return new ArrayList<>(porChave.values());
    }

    /** Valor de referência de um item novo (catálogo), por item de entrada — só pra itens marcados "sem conserto". */
    private Map<UUID, BigDecimal> valoresReferenciaSemConserto(List<ItemEntrada> itens) {
        List<UUID> catalogoIds = itens.stream()
                .filter(i -> i.getResultadoAvaliacao() == ResultadoAvaliacao.SEM_CONSERTO)
                .map(ItemEntrada::getCatalogoModeloId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (catalogoIds.isEmpty()) return java.util.Collections.emptyMap();
        Map<UUID, BigDecimal> valorPorModelo = catalogoModeloService.buscarValoresReferenciaPorIds(catalogoIds);
        Map<UUID, BigDecimal> resultado = new LinkedHashMap<>();
        for (ItemEntrada item : itens) {
            if (item.getResultadoAvaliacao() == ResultadoAvaliacao.SEM_CONSERTO && item.getCatalogoModeloId() != null) {
                BigDecimal valor = valorPorModelo.get(item.getCatalogoModeloId());
                if (valor != null) resultado.put(item.getId(), valor);
            }
        }
        return resultado;
    }
}
