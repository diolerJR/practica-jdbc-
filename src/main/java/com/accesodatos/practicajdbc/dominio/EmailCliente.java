package com.accesodatos.practicajdbc.dominio;

import java.util.Objects;

/** Objeto valor inmutable: email del cliente con validación de formato. */
public final class EmailCliente {

    private static final String REGEX = "^[\\w.+-]+@[\\w-]+\\.[\\w.]+$";

    private final String valor;

    public EmailCliente(String valor) {
        if (valor == null || !valor.matches(REGEX)) {
            throw new IllegalArgumentException("Email de cliente inválido: " + valor);
        }
        this.valor = valor.toLowerCase();
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailCliente that)) return false;
        return Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
