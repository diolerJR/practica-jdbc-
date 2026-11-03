package com.accesodatos.practicajdbc.dominio;

import java.util.Objects;

/** Objeto valor inmutable: precio validado y redondeado a 2 decimales. */
public final class Precio {

    private final double valor;

    public Precio(double valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo: " + valor);
        }
        this.valor = Math.round(valor * 100.0) / 100.0;
    }

    public double getValor() {
        return valor;
    }

    /** Devuelve un nuevo Precio con el descuento (%) aplicado. */
    public Precio aplicarDescuento(double porcentaje) {
        if (porcentaje < 0 || porcentaje > 100) {
            throw new IllegalArgumentException("Descuento fuera de rango (0-100): " + porcentaje);
        }
        return new Precio(valor * (1 - porcentaje / 100.0));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Precio precio)) return false;
        return Double.compare(valor, precio.valor) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return String.format("%.2f €", valor);
    }
}
