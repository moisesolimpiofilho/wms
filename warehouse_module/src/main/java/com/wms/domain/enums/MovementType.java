package com.wms.domain.enums;

public enum MovementType {
    ENTRADA(1),
    SAIDA(2),
    TRANSFERENCIA(3),
    AJUSTE(4);

    private final int codigo;

    MovementType(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    // Método utilitário opcional caso precise converter um código int de volta para o Enum
    public static MovementType toEnum(Integer codigo) {
        if (codigo == null) {
            return null;
        }

        for (MovementType x : MovementType.values()) {
            if (codigo.equals(x.getCodigo())) {
                return x;
            }
        }

        throw new IllegalArgumentException("Código inválido: " + codigo);
    }
}