package com.wms.domain.enums;

public enum StatusOperacao {
    ABERTO(1),
    EM_CONFERENCIA(2),
    EM_SEPARACAO(3),
    EM_CONTAGEM(4),
    CONCLUIDO(5),
    CANCELADO(9);

    private final int codigo;

    StatusOperacao(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    // Método utilitário para converter código numérico -> Enum
    public static StatusOperacao fromCodigo(Integer codigo) {
        if (codigo == null) {
            return null;
        }

        for (StatusOperacao status : StatusOperacao.values()) {
            if (codigo.equals(status.getCodigo())) {
                return status;
            }
        }

        throw new IllegalArgumentException("Status inválido com código: " + codigo);
    }
}