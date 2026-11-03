package com.accesodatos.practicajdbc.dominio;

import java.util.Objects;

/** ENTIDAD de dominio del agregado Categoria, con tabla propia en BD. */
public class Producto {

    private Long id;
    private String nombre;
    private Precio precio;
    private int stock;
    private String formato;
    private String saborMaterial;

    private Categoria categoria;

    public Producto() {
    }

    public Producto(String nombre, Precio precio, int stock, String formato, String saborMaterial) {
        setNombre(nombre);
        setPrecio(precio);
        setStock(stock);
        this.formato = formato;
        this.saborMaterial = saborMaterial;
    }

    public Producto(Long id, String nombre, Precio precio, int stock,
                    String formato, String saborMaterial, Categoria categoria) {
        this(nombre, precio, stock, formato, saborMaterial);
        this.id = id;
        this.categoria = categoria;
    }

    // Comportamiento

    public boolean tieneMismoNombre(String nombre) {
        return this.nombre != null && this.nombre.equalsIgnoreCase(nombre);
    }

    public void reducirStock(int unidades) {
        if (unidades <= 0) {
            throw new IllegalArgumentException("Las unidades deben ser positivas");
        }
        if (unidades > stock) {
            throw new IllegalStateException("Stock insuficiente para '%s' (quedan %d)".formatted(nombre, stock));
        }
        stock -= unidades;
    }

    public void reponerStock(int unidades) {
        if (unidades <= 0) throw new IllegalArgumentException("Las unidades deben ser positivas");
        stock += unidades;
    }

    public boolean estaDisponible() {
        return stock > 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        this.nombre = nombre.trim();
    }

    public Precio getPrecio() { return precio; }
    public void setPrecio(Precio precio) {
        Objects.requireNonNull(precio, "El precio no puede ser nulo");
        this.precio = precio;
    }

    public int getStock() { return stock; }
    public void setStock(int stock) {
        if (stock < 0) throw new IllegalArgumentException("El stock no puede ser negativo");
        this.stock = stock;
    }

    public String getFormato() { return formato; }
    public void setFormato(String formato) { this.formato = formato; }

    public String getSaborMaterial() { return saborMaterial; }
    public void setSaborMaterial(String saborMaterial) { this.saborMaterial = saborMaterial; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Producto other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Producto{id=%d, nombre='%s', precio=%s, stock=%d, formato='%s', saborMaterial='%s', categoriaId=%s}"
                .formatted(id, nombre, precio, stock, formato, saborMaterial,
                        categoria != null ? categoria.getId() : null);
    }
}
