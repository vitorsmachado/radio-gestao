package com.radiocom.ordemservico.unit.domain.service;

import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.OrdemServico;
import com.radiocom.ordemservico.domain.repository.ItemEntradaRepository;
import com.radiocom.ordemservico.domain.repository.OrdemServicoRepository;
import com.radiocom.ordemservico.domain.service.NumeroOSGenerator;
import com.radiocom.ordemservico.domain.service.OrdemServicoDomainService;
import com.radiocom.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrdemServicoDomainService - Testes Unitários")
class OrdemServicoDomainServiceTest {

    @Mock private OrdemServicoRepository osRepository;
    @Mock private ItemEntradaRepository itemEntradaRepository;
    @Mock private NumeroOSGenerator numeroGenerator;

    @InjectMocks
    private OrdemServicoDomainService service;

    private UUID clienteId;
    private OrdemServico os;
    private UUID osId;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        osId = UUID.randomUUID();
        os = OrdemServico.builder()
                .numero("OS-2026-0001")
                .clienteId(clienteId)
                .build();
        ReflectionTestUtils.setField(os, "id", osId);
    }

    @Test
    @DisplayName("buscarPorId deve lançar exceção quando não existe")
    void buscarPorId_deveLancarExcecaoQuandoNaoExiste() {
        when(osRepository.findById(osId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(osId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("não encontrada");
    }

    @Test
    @DisplayName("criar deve gerar numero e salvar")
    void criar_deveGerarNumeroESalvar() {
        when(numeroGenerator.gerarNumero()).thenReturn("OS-2026-0042");
        when(osRepository.save(any(OrdemServico.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdemServico resultado = service.criar(clienteId, null, null, "João da Silva");

        assertThat(resultado.getNumero()).isEqualTo("OS-2026-0042");
        assertThat(resultado.getClienteId()).isEqualTo(clienteId);
        assertThat(resultado.getSolicitante()).isEqualTo("João da Silva");
    }

    @Test
    @DisplayName("listarPorCliente deve retornar as OS do cliente")
    void listarPorCliente_deveRetornarOSDoCliente() {
        when(osRepository.findByClienteId(clienteId)).thenReturn(List.of(os));

        List<OrdemServico> resultado = service.listarPorCliente(clienteId);

        assertThat(resultado).containsExactly(os);
    }

    @Test
    @DisplayName("buscar deve delegar pro repositório com os filtros informados")
    void buscar_deveDelegarParaORepositorio() {
        var pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        List<UUID> clienteIdsMatched = List.of(clienteId);
        var dataInicial = java.time.LocalDateTime.of(2026, 3, 1, 0, 0);
        var dataFinal = java.time.LocalDateTime.of(2026, 3, 31, 23, 59);
        when(osRepository.buscar("OS-2026", clienteIdsMatched, dataInicial, dataFinal, pageable))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(os), pageable, 1));

        var resultado = service.buscar("OS-2026", clienteIdsMatched, dataInicial, dataFinal, pageable);

        assertThat(resultado.getContent()).containsExactly(os);
    }

    @Test
    @DisplayName("iniciarAndamento deve mudar status e salvar")
    void iniciarAndamento_deveMudarStatusESalvar() {
        when(osRepository.findById(osId)).thenReturn(Optional.of(os));
        when(osRepository.save(any(OrdemServico.class))).thenAnswer(inv -> inv.getArgument(0));

        service.iniciarAndamento(osId);

        verify(osRepository).save(os);
    }

    @Test
    @DisplayName("confirmarEntrega deve delegar para o domínio e salvar")
    void confirmarEntrega_deveDelegarESalvar() {
        os.iniciarAndamento();
        when(osRepository.findById(osId)).thenReturn(Optional.of(os));
        when(osRepository.save(any(OrdemServico.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdemServico resultado = service.confirmarEntrega(osId, "Maria Souza");

        assertThat(resultado.getRecebedorNome()).isEqualTo("Maria Souza");
    }

    @Test
    @DisplayName("cancelar deve delegar para o domínio e salvar")
    void cancelar_deveDelegarESalvar() {
        when(osRepository.findById(osId)).thenReturn(Optional.of(os));
        when(osRepository.save(any(OrdemServico.class))).thenAnswer(inv -> inv.getArgument(0));

        service.cancelar(osId, "Cliente desistiu");

        verify(osRepository).save(os);
    }

    // ===== moverItem =====

    @Test
    @DisplayName("moverItem deve reatribuir a OS do item quando a OS destino existe")
    void moverItem_deveReatribuirOSQuandoDestinoExiste() {
        UUID novaOsId = UUID.randomUUID();
        OrdemServico novaOS = OrdemServico.builder().numero("OS-2026-0002").clienteId(clienteId).build();
        ItemEntrada item = ItemEntrada.builder()
                .osId(osId)
                .tipoItem(com.radiocom.estoque.domain.model.enums.TipoItem.EQUIPAMENTO)
                .descricao("Rádio").build();
        UUID itemId = UUID.randomUUID();

        when(osRepository.findById(novaOsId)).thenReturn(Optional.of(novaOS));
        when(itemEntradaRepository.findById(itemId)).thenReturn(Optional.of(item));

        service.moverItem(itemId, novaOsId);

        assertThat(item.getOsId()).isEqualTo(novaOsId);
        verify(itemEntradaRepository).save(item);
    }

    @Test
    @DisplayName("moverItem deve lançar exceção quando OS destino não existe")
    void moverItem_deveLancarExcecaoQuandoDestinoNaoExiste() {
        UUID novaOsId = UUID.randomUUID();
        when(osRepository.findById(novaOsId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.moverItem(UUID.randomUUID(), novaOsId))
                .isInstanceOf(DomainException.class);
    }

    // ===== dividir =====

    @Test
    @DisplayName("dividir deve criar nova OS e mover os itens escolhidos")
    void dividir_deveCriarNovaOSEMoverItens() {
        UUID itemId = UUID.randomUUID();
        ItemEntrada item = ItemEntrada.builder()
                .osId(osId)
                .tipoItem(com.radiocom.estoque.domain.model.enums.TipoItem.EQUIPAMENTO)
                .descricao("Rádio").build();

        // findById(any()) cobre tanto a busca da OS de origem quanto a checagem de
        // existência da OS nova dentro de moverItem (que não depende do objeto retornado)
        when(osRepository.findById(any())).thenReturn(Optional.of(os));
        when(numeroGenerator.gerarNumero()).thenReturn("OS-2026-0002");
        when(osRepository.save(any(OrdemServico.class))).thenAnswer(inv -> {
            OrdemServico novaOSEntidade = inv.getArgument(0);
            if (novaOSEntidade.getId() == null) {
                ReflectionTestUtils.setField(novaOSEntidade, "id", UUID.randomUUID());
            }
            return novaOSEntidade;
        });
        when(itemEntradaRepository.findById(itemId)).thenReturn(Optional.of(item));

        OrdemServico novaOS = service.dividir(osId, List.of(itemId), "Técnico João");

        assertThat(novaOS.getNumero()).isEqualTo("OS-2026-0002");
        assertThat(novaOS.getClienteId()).isEqualTo(clienteId);
        assertThat(item.getOsId()).isEqualTo(novaOS.getId());
    }

    @Test
    @DisplayName("dividir deve lançar exceção quando nenhum item é informado")
    void dividir_deveLancarExcecaoQuandoSemItens() {
        assertThatThrownBy(() -> service.dividir(osId, List.of(), "Técnico João"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Selecione ao menos um item");
    }

    // ===== unir =====

    @Test
    @DisplayName("unir deve mover itens das origens para o destino e cancelar as origens vazias")
    void unir_deveMoverItensECancelarOrigens() {
        UUID origemId = UUID.randomUUID();
        OrdemServico origem = OrdemServico.builder().numero("OS-2026-0003").clienteId(clienteId).build();
        ReflectionTestUtils.setField(origem, "id", origemId);

        UUID itemId = UUID.randomUUID();
        ItemEntrada item = ItemEntrada.builder()
                .osId(origemId)
                .tipoItem(com.radiocom.estoque.domain.model.enums.TipoItem.EQUIPAMENTO)
                .descricao("Rádio").build();
        ReflectionTestUtils.setField(item, "id", itemId);

        when(osRepository.findById(osId)).thenReturn(Optional.of(os));
        when(osRepository.findById(origemId)).thenReturn(Optional.of(origem));
        when(itemEntradaRepository.findByOsId(origemId)).thenReturn(List.of(item));
        when(itemEntradaRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(osRepository.save(any(OrdemServico.class))).thenAnswer(inv -> inv.getArgument(0));

        service.unir(osId, List.of(origemId));

        assertThat(item.getOsId()).isEqualTo(osId);
        assertThat(origem.isEncerrada()).isTrue();
    }
}
