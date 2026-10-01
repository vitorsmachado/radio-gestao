package com.radiocom.demo;

import com.radiocom.auth.application.dto.CriarUsuarioDTO;
import com.radiocom.auth.application.service.AuthApplicationService;
import com.radiocom.auth.domain.model.RoleUsuario;
import com.radiocom.auth.domain.repository.UsuarioRepository;
import com.radiocom.cliente.application.dto.ClienteCreateDTO;
import com.radiocom.cliente.application.dto.ClienteDTO;
import com.radiocom.cliente.application.dto.ContatoDTO;
import com.radiocom.cliente.application.dto.EnderecoDTO;
import com.radiocom.cliente.application.dto.PostoDTO;
import com.radiocom.cliente.application.service.ClienteApplicationService;
import com.radiocom.cliente.domain.model.enums.TipoContato;
import com.radiocom.configuracao.application.dto.AtualizarConfiguracaoDTO;
import com.radiocom.configuracao.application.dto.ConfiguracaoDTO;
import com.radiocom.configuracao.application.service.ConfiguracaoApplicationService;
import com.radiocom.estoque.application.dto.CatalogoModeloCreateDTO;
import com.radiocom.estoque.application.dto.CatalogoModeloDTO;
import com.radiocom.estoque.application.dto.PecaCreateDTO;
import com.radiocom.estoque.application.dto.PecaDTO;
import com.radiocom.estoque.application.dto.ResolverEquipamentoPorNSDTO;
import com.radiocom.estoque.application.service.CatalogoModeloService;
import com.radiocom.estoque.application.service.EstoqueApplicationService;
import com.radiocom.estoque.domain.model.enums.FaixaEquipamento;
import com.radiocom.estoque.domain.model.enums.TipoAcessorio;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.orcamento.application.dto.AtualizarOrcamentoDTO;
import com.radiocom.orcamento.application.dto.OrcamentoDTO;
import com.radiocom.orcamento.application.service.OrcamentoApplicationService;
import com.radiocom.ordemservico.application.dto.ConfirmarEntregaDTO;
import com.radiocom.ordemservico.application.dto.ItemConsertoCreateDTO;
import com.radiocom.ordemservico.application.dto.ItemEntradaCreateDTO;
import com.radiocom.ordemservico.application.dto.MotivoDTO;
import com.radiocom.ordemservico.application.dto.OrdemServicoCreateDTO;
import com.radiocom.ordemservico.application.dto.SalvarAvaliacaoTecnicaDTO;
import com.radiocom.ordemservico.application.service.ItemEntradaApplicationService;
import com.radiocom.ordemservico.application.service.OrdemServicoApplicationService;
import com.radiocom.ordemservico.domain.model.enums.ResultadoAvaliacao;
import com.radiocom.ordemservico.domain.model.enums.TipoItemConserto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Popula um banco vazio com uma empresa fictícia e OS em todas as etapas
 * do fluxo, pra quem for avaliar o sistema já encontrar algo pra ver.
 * Só existe no perfil {@code demo} — nunca roda numa instalação real.
 *
 * Tudo passa pelos application services (não SQL direto), pra que os
 * listeners de orçamento automático, garantia e histórico de status
 * rodem exatamente como no uso normal. Por isso esta classe não é
 * {@code @Transactional}: cada chamada precisa commitar sozinha pra
 * disparar os eventos AFTER_COMMIT.
 */
@Component
@Profile("demo")
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeeder implements ApplicationRunner {

    private static final BigDecimal MAO_DE_OBRA = new BigDecimal("120.00");

    private final ConfiguracaoApplicationService configuracaoService;
    private final AuthApplicationService authService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClienteApplicationService clienteService;
    private final CatalogoModeloService catalogoService;
    private final EstoqueApplicationService estoqueService;
    private final OrdemServicoApplicationService osService;
    private final ItemEntradaApplicationService itemService;
    private final OrcamentoApplicationService orcamentoService;

    @Value("${demo.usuario.senha}")
    private String senhaDemo;

    // Catálogo e estoque — usados pelos cenários de OS
    private CatalogoModeloDTO dep450, dep250, pd406, tk3402, bateriaDep;
    private PecaDTO altoFalante, botaoPtt, conectorAntena, displayPd406;
    private UUID tecnicoId;

    @Override
    public void run(ApplicationArguments args) {
        if (clienteService.listar(null, null, PageRequest.of(0, 1)).getTotalElements() > 0) {
            log.info("[demo] Banco já tem dados — seed ignorado.");
            return;
        }
        log.info("[demo] Populando banco com dados de demonstração...");
        configurarEmpresa();
        configurarUsuarios();
        criarCatalogoEEstoque();
        criarClientesEOrdens();
        log.info("[demo] Dados de demonstração criados.");
    }

    // ===== EMPRESA E USUÁRIOS =====

    private void configurarEmpresa() {
        ConfiguracaoDTO atual = configuracaoService.buscar();
        configuracaoService.atualizar(AtualizarConfiguracaoDTO.builder()
                .valorMaoDeObraPadrao(MAO_DE_OBRA)
                .prazoGarantiaPecaDias(atual.getPrazoGarantiaPecaDias())
                .prazoGarantiaEquipamentoDias(atual.getPrazoGarantiaEquipamentoDias())
                .prazoGarantiaAcessorioDias(atual.getPrazoGarantiaAcessorioDias())
                .nomeEmpresa("RadioNet Demo")
                .razaoSocialEmpresa("RadioNet Comunicações Demonstração Ltda")
                .documentoEmpresa("11122233000183")
                .enderecoEmpresa("Rua Fictícia, 100")
                .bairroEmpresa("Centro")
                .cidadeEmpresa("Brasília - DF")
                .telefoneEmpresa("(61) 3000-0000")
                .emailEmpresa("contato@radionet.example")
                .build());
    }

    /**
     * A senha do admin da migration V1 está no repositório — numa instância
     * aberta ela é trocada por uma aleatória que ninguém conhece. Quem avalia
     * entra com o usuário {@code demo}.
     */
    private void configurarUsuarios() {
        usuarioRepository.findByLoginAndAtivoTrue("admin").ifPresent(admin -> {
            admin.setSenhaHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            usuarioRepository.save(admin);
        });

        criarUsuario("Usuário Demonstração", "demo", "demo@radionet.example", senhaDemo, RoleUsuario.ADMIN);
        tecnicoId = criarUsuario("Carlos Andrade", "carlos", "carlos@radionet.example",
                UUID.randomUUID().toString(), RoleUsuario.TECNICO);
    }

    private UUID criarUsuario(String nome, String login, String email, String senha, RoleUsuario role) {
        CriarUsuarioDTO dto = new CriarUsuarioDTO();
        dto.setNome(nome);
        dto.setLogin(login);
        dto.setEmail(email);
        dto.setSenha(senha);
        dto.setRole(role);
        return authService.criar(dto).getId();
    }

    // ===== CATÁLOGO E ESTOQUE =====

    private void criarCatalogoEEstoque() {
        dep450 = modelo(TipoItem.EQUIPAMENTO, null, "Motorola", "DEP450", "Rádio portátil VHF/UHF", "1890.00");
        dep250 = modelo(TipoItem.EQUIPAMENTO, null, "Motorola", "DEP250", "Rádio portátil digital", "1450.00");
        pd406 = modelo(TipoItem.EQUIPAMENTO, null, "Hytera", "PD406", "Rádio portátil DMR", "1690.00");
        tk3402 = modelo(TipoItem.EQUIPAMENTO, null, "Kenwood", "TK-3402", "Rádio portátil UHF", "1200.00");
        bateriaDep = modelo(TipoItem.ACESSORIO, TipoAcessorio.BATERIA, "Motorola", "PMNN4251",
                "Bateria Li-Ion 1600 mAh", "280.00");

        altoFalante = peca("Alto-falante 4Ω", 10, 2, "45.00", dep450, dep250);
        botaoPtt = peca("Botão PTT", 6, 2, "38.00", dep450, dep250);
        conectorAntena = peca("Conector de antena SMA", 15, 3, "22.00", dep450, dep250, pd406, tk3402);
        // Sem estoque de propósito: o item autorizado com essa peça cai em "aguardando peça"
        displayPd406 = peca("Display LCD PD406", 0, 1, "180.00", pd406);
        peca("Chave seletora de canais", 4, 1, "65.00", tk3402);
    }

    private CatalogoModeloDTO modelo(TipoItem tipo, TipoAcessorio tipoAcessorio, String marca, String modelo,
                                     String descricao, String valor) {
        return catalogoService.criar(CatalogoModeloCreateDTO.builder()
                .tipoItem(tipo)
                .tipoAcessorio(tipoAcessorio)
                .marca(marca)
                .modelo(modelo)
                .descricao(descricao)
                .valorReferencia(new BigDecimal(valor))
                .controlePorSerie(tipo == TipoItem.EQUIPAMENTO)
                .build());
    }

    private PecaDTO peca(String descricao, int quantidade, int minima, String valor, CatalogoModeloDTO... compativeis) {
        return estoqueService.criarPeca(PecaCreateDTO.builder()
                .descricao(descricao)
                .quantidadeDisponivel(quantidade)
                .quantidadeMinima(minima)
                .valorUnitario(new BigDecimal(valor))
                .marca("Genérica")
                .modelo(descricao)
                .modelosCompativeisIds(List.of(compativeis).stream().map(CatalogoModeloDTO::getId).toList())
                .build());
    }

    // ===== CLIENTES E OS =====

    private void criarClientesEOrdens() {
        ClienteDTO rotaSul = cliente("12345678000195", "Transportadora Rota Sul Ltda", "Rota Sul",
                "Fernanda Lima", "Matriz Taguatinga");
        ClienteDTO parqueAguas = cliente("98765432000198", "Condomínio Residencial Parque das Águas", null,
                "Ricardo Souza", "Portaria principal");
        ClienteDTO alfa = cliente("45678912000155", "Alfa Segurança e Vigilância Ltda", "Alfa Segurança",
                "Marcos Oliveira", "Base operacional");
        ClienteDTO horizonte = cliente("32165498000139", "Construtora Horizonte Ltda", "Horizonte",
                "Patrícia Gomes", "Canteiro Águas Claras");
        ClienteDTO joao = cliente("12345678909", "João Pereira da Silva", null, "João Pereira da Silva", null);

        osEntregue(rotaSul);
        osEmManutencao(parqueAguas);
        osAguardandoPecaEAutorizacao(alfa);
        osEmAvaliacao(horizonte);
        osOrcamentoEmRascunho(rotaSul);
        osRecemAberta(joao);
    }

    /** Fluxo completo: avaliado → orçamento enviado → autorizado → reparado → entregue. */
    private void osEntregue(ClienteDTO cliente) {
        UUID os = abrirOS(cliente, "Fernanda Lima", 25, "Rádios da frota de caminhões.");
        UUID r1 = equipamento(os, cliente, dep450, "DEP450-778812", FaixaEquipamento.VHF, "Não transmite.");
        UUID r2 = equipamento(os, cliente, dep450, "DEP450-778830", FaixaEquipamento.VHF, "Áudio baixo e chiando.");

        consertar(r1, botaoPtt, 1);
        avaliar(r1, "Botão PTT sem contato", "Desgaste por uso", "Substituir botão PTT");
        consertar(r2, altoFalante, 1);
        avaliar(r2, "Alto-falante com cone rompido", "Entrada de umidade", "Substituir alto-falante");

        enviarOrcamento(os);
        for (UUID item : List.of(r1, r2)) {
            itemService.autorizar(item);
            itemService.iniciarManutencao(item);
            itemService.concluirManutencao(item);
            itemService.entregar(item);
        }
        osService.confirmarEntrega(os, ConfirmarEntregaDTO.builder().nomeRecebedor("Fernanda Lima").build());
    }

    /** Itens em ritmos diferentes na mesma OS: em manutenção, na fila, sem defeito e não autorizado. */
    private void osEmManutencao(ClienteDTO cliente) {
        UUID os = abrirOS(cliente, "Ricardo Souza", 10, "Rádios da equipe de portaria.");
        UUID r1 = equipamento(os, cliente, dep250, "DEP250-551201", FaixaEquipamento.UHF, "Não liga.");
        UUID r2 = equipamento(os, cliente, dep250, "DEP250-551219", FaixaEquipamento.UHF, "Sem recepção.");
        UUID r3 = equipamento(os, cliente, dep250, "DEP250-551240", FaixaEquipamento.UHF, "Bateria descarrega rápido.");
        UUID baterias = itemService.criar(ItemEntradaCreateDTO.builder()
                .osId(os)
                .tipoItem(TipoItem.ACESSORIO)
                .catalogoModeloId(bateriaDep.getId())
                .descricao("Bateria Motorola PMNN4251")
                .marca("Motorola").modelo("PMNN4251")
                .quantidade(2)
                .defeitoRelatado("Não seguram carga.")
                .build()).getId();

        consertar(r1, botaoPtt, 1);
        avaliar(r1, "Chave liga/desliga oxidada", "Umidade", "Limpeza e troca do botão");
        consertar(r2, conectorAntena, 1);
        avaliar(r2, "Conector de antena solto", "Queda", "Substituir conector SMA");
        itemService.iniciarAvaliacao(r3);
        itemService.salvarAvaliacaoTecnica(r3, SalvarAvaliacaoTecnicaDTO.builder()
                .resultado(ResultadoAvaliacao.SEM_DEFEITO)
                .observacoesTecnicas("Rádio testado por 4 horas sem falhas. Problema está na bateria do cliente.")
                .build());
        maoDeObra(baterias);
        avaliar(baterias, "Células com capacidade abaixo de 40%", "Fim da vida útil", "Recondicionar células");

        enviarOrcamento(os);
        itemService.autorizar(r1);
        itemService.iniciarManutencao(r1);
        itemService.autorizar(r2);
        itemService.naoAutorizar(baterias, MotivoDTO.builder().motivo("Cliente vai comprar baterias novas.").build());
        itemService.aguardarEntrega(baterias);
    }

    /** Orçamento enviado: um item autorizado esperando peça sem estoque e outro ainda sem resposta. */
    private void osAguardandoPecaEAutorizacao(ClienteDTO cliente) {
        UUID os = abrirOS(cliente, "Marcos Oliveira", 6, null);
        UUID r1 = equipamento(os, cliente, pd406, "PD406-90321", FaixaEquipamento.UHF, "Tela apagada.");
        UUID r2 = equipamento(os, cliente, pd406, "PD406-90357", FaixaEquipamento.UHF, "Tela com manchas.");

        consertar(r1, displayPd406, 1);
        avaliar(r1, "Display LCD queimado", "Queda", "Substituir display");
        consertar(r2, displayPd406, 1);
        avaliar(r2, "Display LCD trincado", "Impacto", "Substituir display");

        enviarOrcamento(os);
        itemService.autorizar(r1);
    }

    /** Recebida há poucos dias: um item com o técnico, outro esperando avaliação. */
    private void osEmAvaliacao(ClienteDTO cliente) {
        UUID os = abrirOS(cliente, "Patrícia Gomes", 3, "Urgente — obra parada sem comunicação.");
        UUID r1 = equipamento(os, cliente, tk3402, "TK3402-44120", FaixaEquipamento.UHF, "Não troca de canal.");
        equipamento(os, cliente, tk3402, "TK3402-44188", FaixaEquipamento.UHF, "Antena quebrada.");
        itemService.iniciarAvaliacao(r1);
    }

    /** Item avaliado agrupado num orçamento que ainda não foi enviado ao cliente. */
    private void osOrcamentoEmRascunho(ClienteDTO cliente) {
        UUID os = abrirOS(cliente, "Fernanda Lima", 2, null);
        UUID r1 = equipamento(os, cliente, dep450, "DEP450-778901", FaixaEquipamento.VHF, "Caiu na água.");
        consertar(r1, altoFalante, 1);
        consertar(r1, conectorAntena, 1);
        avaliar(r1, "Oxidação no alto-falante e no conector", "Contato com água", "Limpeza química e troca das peças");
    }

    private void osRecemAberta(ClienteDTO cliente) {
        UUID os = abrirOS(cliente, "João Pereira da Silva", 0, null);
        equipamento(os, cliente, dep450, "DEP450-801144", FaixaEquipamento.VHF, "Chiado constante.");
    }

    // ===== AUXILIARES =====

    private ClienteDTO cliente(String documento, String nome, String fantasia, String contato, String posto) {
        EnderecoDTO endereco = EnderecoDTO.builder()
                .cep("72000000").logradouro("Quadra Fictícia " + documento.substring(0, 2)).numero("10")
                .bairro("Setor Demonstração").cidade("Brasília").estado("DF")
                .build();
        return clienteService.criar(ClienteCreateDTO.builder()
                .documento(documento)
                .nomeRazaoSocial(nome)
                .nomeFantasia(fantasia)
                .endereco(endereco)
                .contatos(List.of(ContatoDTO.builder()
                        .nome(contato).tipo(TipoContato.COMERCIAL)
                        .telefone("61990000000").email("contato@cliente.example")
                        .principal(true).build()))
                .postos(posto == null ? List.of() : List.of(PostoDTO.builder()
                        .nome(posto).endereco(endereco).responsavel(contato).padrao(true).build()))
                .build());
    }

    private UUID abrirOS(ClienteDTO cliente, String solicitante, int diasAtras, String observacoes) {
        UUID postoId = cliente.getPostos() == null || cliente.getPostos().isEmpty()
                ? null : cliente.getPostos().get(0).getId();
        return osService.criar(OrdemServicoCreateDTO.builder()
                .clienteId(cliente.getId())
                .postoId(postoId)
                .tecnicoId(tecnicoId)
                .solicitante(solicitante)
                .dataAbertura(LocalDateTime.now().minusDays(diasAtras))
                .observacoes(observacoes)
                .build()).getId();
    }

    private UUID equipamento(UUID os, ClienteDTO cliente, CatalogoModeloDTO modelo, String numeroSerie,
                             FaixaEquipamento faixa, String defeito) {
        UUID equipamentoId = estoqueService.resolverEquipamentoPorNS(ResolverEquipamentoPorNSDTO.builder()
                .numeroSerie(numeroSerie)
                .clienteId(cliente.getId())
                .faixa(faixa)
                .catalogoModeloId(modelo.getId())
                .marca(modelo.getMarca())
                .modelo(modelo.getModelo())
                .descricao(modelo.getDescricao())
                .build()).getId();
        return itemService.criar(ItemEntradaCreateDTO.builder()
                .osId(os)
                .itemEstoqueId(equipamentoId)
                .catalogoModeloId(modelo.getId())
                .tipoItem(TipoItem.EQUIPAMENTO)
                .descricao(modelo.getDescricao())
                .numeroSerie(numeroSerie)
                .marca(modelo.getMarca())
                .modelo(modelo.getModelo())
                .faixa(faixa)
                .defeitoRelatado(defeito)
                .build()).getId();
    }

    /** Peça + mão de obra, como o técnico lança durante a avaliação. */
    private void consertar(UUID item, PecaDTO peca, int quantidade) {
        itemService.adicionarItemConserto(item, ItemConsertoCreateDTO.builder()
                .tipo(TipoItemConserto.PECA)
                .itemEstoqueId(peca.getId())
                .descricao(peca.getDescricao())
                .quantidade(quantidade)
                .valorUnitario(peca.getValorUnitario())
                .build());
        maoDeObra(item);
    }

    private void maoDeObra(UUID item) {
        itemService.adicionarItemConserto(item, ItemConsertoCreateDTO.builder()
                .tipo(TipoItemConserto.MAO_DE_OBRA)
                .descricao("Mão de obra")
                .quantidade(1)
                .valorUnitario(MAO_DE_OBRA)
                .build());
    }

    /** O listener de orçamento agrupa o item no rascunho da OS assim que esta chamada commita. */
    private void avaliar(UUID item, String defeito, String causa, String solucao) {
        itemService.iniciarAvaliacao(item);
        itemService.salvarAvaliacaoTecnica(item, SalvarAvaliacaoTecnicaDTO.builder()
                .resultado(ResultadoAvaliacao.ORCAMENTO)
                .defeitoEncontrado(defeito)
                .causaDefeito(causa)
                .solucaoRecomendada(solucao)
                .build());
    }

    private void enviarOrcamento(UUID os) {
        OrcamentoDTO orcamento = orcamentoService.listarPorOS(os).get(0);
        orcamentoService.atualizar(orcamento.getId(), AtualizarOrcamentoDTO.builder()
                .validade(LocalDate.now().plusDays(15))
                .condicoesPagamento("50% na aprovação e 50% na entrega")
                .build());
        orcamentoService.enviar(orcamento.getId());
    }
}
