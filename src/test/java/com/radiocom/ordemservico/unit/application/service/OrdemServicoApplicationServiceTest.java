package com.radiocom.ordemservico.unit.application.service;

import com.radiocom.cliente.application.dto.ClienteDTO;
import com.radiocom.cliente.application.service.ClienteApplicationService;
import com.radiocom.ordemservico.application.dto.*;
import com.radiocom.ordemservico.application.mapper.OrdemServicoMapper;
import com.radiocom.ordemservico.application.service.OrdemServicoApplicationService;
import com.radiocom.ordemservico.domain.model.OrdemServico;
import com.radiocom.ordemservico.domain.service.OrdemServicoDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrdemServicoApplicationService - Testes Unitários")
class OrdemServicoApplicationServiceTest {

    @Mock private OrdemServicoDomainService osDomainService;
    @Mock private ClienteApplicationService clienteApplicationService;

    private OrdemServicoApplicationService service;

    private UUID osId;
    private UUID clienteId;
    private OrdemServico os;

    @BeforeEach
    void setUp() {
        service = new OrdemServicoApplicationService(osDomainService, clienteApplicationService, new OrdemServicoMapper());
        osId = UUID.randomUUID();
        clienteId = UUID.randomUUID();
        os = OrdemServico.builder().numero("OS-2026-0001").clienteId(clienteId).build();
        ReflectionTestUtils.setField(os, "id", osId);
    }

    @Test
    @DisplayName("criar deve delegar para o domain service")
    void criar_deveDelegar() {
        OrdemServicoCreateDTO dto = OrdemServicoCreateDTO.builder()
                .clienteId(clienteId).solicitante("João da Silva").build();
        when(osDomainService.criar(clienteId, null, null, "João da Silva")).thenReturn(os);

        OrdemServicoDTO resultado = service.criar(dto);

        assertThat(resultado.getNumero()).isEqualTo("OS-2026-0001");
    }

    @Test
    @DisplayName("buscarPorId deve delegar para o domain service")
    void buscarPorId_deveDelegar() {
        when(osDomainService.buscarPorId(osId)).thenReturn(os);

        assertThat(service.buscarPorId(osId).getId()).isEqualTo(osId);
    }

    @Test
    @DisplayName("listarPorCliente deve delegar para o domain service")
    void listarPorCliente_deveDelegar() {
        when(osDomainService.listarPorCliente(clienteId)).thenReturn(List.of(os));

        List<OrdemServicoDTO> resultado = service.listarPorCliente(clienteId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNumero()).isEqualTo("OS-2026-0001");
    }

    @Test
    @DisplayName("listar sem busca deve passar lista sentinela de clienteId (evita IN vazio)")
    void listar_semBusca_devePassarListaSentinela() {
        var pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        when(osDomainService.buscar(eq(null), anyList(), eq(null), eq(null), eq(pageable)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0));

        service.listar(null, null, null, pageable);

        org.mockito.ArgumentCaptor<List<UUID>> captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(osDomainService).buscar(eq(null), captor.capture(), eq(null), eq(null), eq(pageable));
        assertThat(captor.getValue()).hasSize(1);
    }

    @Test
    @DisplayName("listar com busca sem cliente correspondente deve passar lista sentinela")
    void listar_comBuscaSemClienteEncontrado_devePassarListaSentinela() {
        var pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        when(clienteApplicationService.buscarPorNomeOuDocumento("OS-2026")).thenReturn(List.of());
        when(osDomainService.buscar(eq("OS-2026"), anyList(), any(), any(), eq(pageable)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0));

        service.listar("OS-2026", null, null, pageable);

        org.mockito.ArgumentCaptor<List<UUID>> captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(osDomainService).buscar(eq("OS-2026"), captor.capture(), any(), any(), eq(pageable));
        assertThat(captor.getValue()).hasSize(1);
    }

    @Test
    @DisplayName("listar com busca e cliente correspondente deve repassar os ids encontrados")
    void listar_comBuscaEClienteEncontrado_devePassarIdsEncontrados() {
        var pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        ClienteDTO clienteEncontrado = ClienteDTO.builder().id(clienteId).nomeRazaoSocial("Radio Comunicacao").build();
        when(clienteApplicationService.buscarPorNomeOuDocumento("Radio")).thenReturn(List.of(clienteEncontrado));
        when(osDomainService.buscar(eq("Radio"), anyList(), any(), any(), eq(pageable)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0));

        service.listar("Radio", null, null, pageable);

        org.mockito.ArgumentCaptor<List<UUID>> captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(osDomainService).buscar(eq("Radio"), captor.capture(), any(), any(), eq(pageable));
        assertThat(captor.getValue()).containsExactly(clienteId);
    }

    @Test
    @DisplayName("listar deve resolver nome e documento do cliente na página de resultado")
    void listar_deveResolverNomeEDocumentoDoCliente() {
        var pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        when(osDomainService.buscar(eq(null), anyList(), eq(null), eq(null), eq(pageable)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(os), pageable, 1));
        ClienteDTO cliente = ClienteDTO.builder().id(clienteId).nomeRazaoSocial("Radio Comunicacao").documento("11222333000181").build();
        when(clienteApplicationService.buscarPorIds(List.of(clienteId))).thenReturn(List.of(cliente));

        var resultado = service.listar(null, null, null, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getClienteNome()).isEqualTo("Radio Comunicacao");
        assertThat(resultado.getContent().get(0).getClienteDocumento()).isEqualTo("11222333000181");
    }

    @Test
    @DisplayName("listar deve converter período para início e fim do dia")
    void listar_deveConverterPeriodoParaInicioEFimDoDia() {
        var pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        java.time.LocalDate dataInicial = java.time.LocalDate.of(2026, 3, 1);
        java.time.LocalDate dataFinal = java.time.LocalDate.of(2026, 3, 31);
        when(osDomainService.buscar(eq(null), anyList(), any(), any(), eq(pageable)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0));

        service.listar(null, dataInicial, dataFinal, pageable);

        org.mockito.ArgumentCaptor<java.time.LocalDateTime> inicioCaptor = org.mockito.ArgumentCaptor.forClass(java.time.LocalDateTime.class);
        org.mockito.ArgumentCaptor<java.time.LocalDateTime> fimCaptor = org.mockito.ArgumentCaptor.forClass(java.time.LocalDateTime.class);
        verify(osDomainService).buscar(eq(null), anyList(), inicioCaptor.capture(), fimCaptor.capture(), eq(pageable));
        assertThat(inicioCaptor.getValue()).isEqualTo(java.time.LocalDateTime.of(2026, 3, 1, 0, 0));
        assertThat(fimCaptor.getValue().toLocalDate()).isEqualTo(dataFinal);
        assertThat(fimCaptor.getValue().getHour()).isEqualTo(23);
    }

    @Test
    @DisplayName("confirmarEntrega deve delegar e retornar o recebedor")
    void confirmarEntrega_deveDelegar() {
        os.confirmarEntrega("Maria Souza");
        when(osDomainService.confirmarEntrega(osId, "Maria Souza")).thenReturn(os);

        OrdemServicoDTO resultado = service.confirmarEntrega(osId, ConfirmarEntregaDTO.builder()
                .nomeRecebedor("Maria Souza").build());

        assertThat(resultado.getRecebedorNome()).isEqualTo("Maria Souza");
    }

    @Test
    @DisplayName("moverItem deve delegar para o domain service")
    void moverItem_deveDelegar() {
        UUID itemId = UUID.randomUUID();
        UUID novaOsId = UUID.randomUUID();

        service.moverItem(itemId, MoverItemDTO.builder().novaOsId(novaOsId).build());

        verify(osDomainService).moverItem(itemId, novaOsId);
    }

    @Test
    @DisplayName("dividir deve delegar e retornar a nova OS")
    void dividir_deveDelegar() {
        UUID itemId = UUID.randomUUID();
        OrdemServico novaOS = OrdemServico.builder().numero("OS-2026-0002").clienteId(clienteId).build();
        when(osDomainService.dividir(eq(osId), eq(List.of(itemId)), eq("Técnico João"))).thenReturn(novaOS);

        OrdemServicoDTO resultado = service.dividir(osId,
                DividirOSDTO.builder().itemIds(List.of(itemId)).solicitante("Técnico João").build());

        assertThat(resultado.getNumero()).isEqualTo("OS-2026-0002");
    }

    @Test
    @DisplayName("unir deve delegar e retornar a OS destino")
    void unir_deveDelegar() {
        UUID origemId = UUID.randomUUID();
        when(osDomainService.unir(osId, List.of(origemId))).thenReturn(os);

        OrdemServicoDTO resultado = service.unir(osId, UnirOSDTO.builder().osOrigemIds(List.of(origemId)).build());

        assertThat(resultado.getId()).isEqualTo(osId);
    }
}
