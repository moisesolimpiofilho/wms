package com.wms.api.operacoes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MotivoMovimentacaoControllerTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MotivoMovimentacaoController()).build();
    }

    @Test
    void listsTheFixedMovementReasonsIncludingInventoryAdjustment() throws Exception {
        mockMvc.perform(get("/api/motivos-movimentacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].codigo").value("RECEBIMENTO_PRODUCAO"))
                .andExpect(jsonPath("$[0].descricao").value("RECEBIMENTO - Produção"))
                .andExpect(jsonPath("$[1].codigo").value("RECEBIMENTO_AQUISICAO"))
                .andExpect(jsonPath("$[1].descricao").value("RECEBIMENTO - Aquisição"))
                .andExpect(jsonPath("$[2].codigo").value("EXPEDICAO_VENDA"))
                .andExpect(jsonPath("$[2].descricao").value("EXPEDIÇÃO - Venda"))
                .andExpect(jsonPath("$[3].codigo").value("EXPEDICAO_INVENTARIO"))
                .andExpect(jsonPath("$[3].descricao").value("EXPEDIÇÃO - Inventario"))
                .andExpect(jsonPath("$[4].codigo").value("AJUSTE_INVENTARIO"))
                .andExpect(jsonPath("$[4].descricao").value("AJUSTE - Inventário"));
    }

    @Test
    void doesNotAllowCreatingMovementReasons() throws Exception {
        mockMvc.perform(post("/api/motivos-movimentacao"))
                .andExpect(status().isMethodNotAllowed());
    }
}
