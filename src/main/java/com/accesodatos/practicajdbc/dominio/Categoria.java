package com.accesodatos.practicajdbc.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class Categoria {

    private Long id;
    private String nombre;
    private String descripcion;
    private boolean esSuplemento;

    private final List<Producto> productos = new ArrayList<>();

    public Categoria() {
    }

    public Categoria(String nombre, String descripcion, boolean esSuplemento) {
        setNombre(nombre);
        this.descripcion = descripcion;
        this.esSuplemento = esSuplemento;
    }

    public Categoria(Long id, String nombre, String descripcion, boolean esSuplemento) {
        this(nombre, descripcion, esSuplemento);
        this.id = id;
    }

    /** Añade un producto al agregado, sin nombres repetidos. */
    public void addProducto(Producto producto) {
        Objects.requireNonNull(producto, "El producto no puede ser nulo");
        if (productos.stream().anyMatch(p -> p.tieneMismoNombre(producto.getNombre()))) {
            throw new IllegalStateException(
                    "Ya existe un producto llamado '" + producto.getNombre() + "' en la categoría " + nombre);
        }
        producto.setCategoria(this);
        productos.add(producto);
    }

    public boolean removeProducto(Long productoId) {
        return productos.removeIf(p -> Objects.equals(p.getId(), productoId));
    }

    public List<Producto> getProductos() {
        return Collections.unmodifiableList(productos);
    }

    /** Para rehidratar el agregado desde JDBC. */
    public void loadProductos(List<Producto> productos) {
        this.productos.clear();
        if (productos != null) {
            for (Producto p : productos) {
                p.setCategoria(this);
                this.productos.add(p);
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
        this.nombre = nombre.trim();
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isEsSuplemento() {
        return esSuplemento;
    }

    public void setEsSuplemento(boolean esSuplemento) {
        this.esSuplemento = esSuplemento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Categoria other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Categoria{id=%d, nombre='%s', esSuplemento=%s, productos=%d}"
                .formatted(id, nombre, esSuplemento, productos.size());
    }
}