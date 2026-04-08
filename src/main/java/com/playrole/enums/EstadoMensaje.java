package com.playrole.enums;

public enum EstadoMensaje {
	NO_LEIDO(0),
    LEIDO(1),
    ARCHIVADO(2),
    EN_PAPELERA(3);

    private final int codigo;

    EstadoMensaje(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    //MEtodo para obtener enum desde un valor int
    public static EstadoMensaje fromCodigo(int codigo) {
        for (EstadoMensaje e : EstadoMensaje.values()) {
            if (e.getCodigo() == codigo) {
                return e;
            }
        }
        throw new IllegalArgumentException("Código de estado inválido: " + codigo);
    }
}
