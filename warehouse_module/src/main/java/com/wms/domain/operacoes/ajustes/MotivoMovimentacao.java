package com.wms.domain.operacoes.ajustes;

public enum MotivoMovimentacao {
    RECEBIMENTO_PRODUCAO("RECEBIMENTO - Produção"),
    RECEBIMENTO_AQUISICAO("RECEBIMENTO - Aquisição"),
    EXPEDICAO_VENDA("EXPEDIÇÃO - Venda"),
    EXPEDICAO_INVENTARIO("EXPEDIÇÃO - Inventario"),
    AJUSTE_INVENTARIO("AJUSTE - Inventário");

    private final String descricao;

    MotivoMovimentacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
