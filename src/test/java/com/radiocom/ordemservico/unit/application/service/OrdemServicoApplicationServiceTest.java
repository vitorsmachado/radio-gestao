package com.radiocom.ordemservico.unit.application.service;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrdemServicoApplicationService - Testes Unitários")
class OrdemServicoApplicationServiceTest {

    @Mock private OrdemServicoDomainService osDomainService;

    private OrdemServicoApplicationService service;

    private UUID osId;
    private UUID clienteId;
    private OrdemServico os;

    @BeforeEach
    void setUp() {
        service = new OrdemServicoApplicationService(osDomainService, new OrdemServicoMapper());
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
