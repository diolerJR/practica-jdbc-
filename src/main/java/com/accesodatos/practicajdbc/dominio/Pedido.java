package com.accesodatos.practicajdbc.dominio;

import java.time.LocalDate;
import java.util.Objects;

/** RAÍZ AGREGADA pedido: línea de pedido asociada a un producto y total calculado. */
public class Pedido {

    private Long id;
    private EmailCliente clienteEmail;
    private int cantidad;
    private LocalDate fechaPedido;
    private Precio total;

    private Producto producto;

    public Pedido() {
    }

    /** Fábrica de dominio: calcula el total = precio del producto × cantidad. */
    public static Pedido of(Producto producto, String clienteEmail, int cantidad) {
        Objects.requireNonNull(producto, "El pedido debe referirse a un producto");
        Pedido p = new Pedido();
        p.setClienteEmail(new EmailCliente(clienteEmail));
        p.setCantidad(cantidad);
        p.setProducto(producto);
        p.setFechaPedido(LocalDate.now());
        p.setTotal(new Precio(producto.getPrecio().getValor() * cantidad));
        return p;
    }

    public Pedido(Long id, String clienteEmail, int cantidad, LocalDate fechaPedido,
                  Precio total, Producto producto) {
        this.id = id;
        setClienteEmail(new EmailCliente(clienteEmail));
        setCantidad(cantidad);
        this.fechaPedido = fechaPedido;
        this.total = total;
        this.producto = producto;
    }

    public boolean esDeCliente(String email) {
        return clienteEmail.getValor().equalsIgnoreCase(email);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EmailCliente getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(EmailCliente clienteEmail) {
        Objects.requireNonNull(clienteEmail, "El email del cliente es obligatorio");
        this.clienteEmail = clienteEmail;
    }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        this.cantidad = cantidad;
    }

    public LocalDate getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(LocalDate fechaPedido) { this.fechaPedido = fechaPedido; }

    public Precio getTotal() { return total; }
    public void setTotal(Precio total) { this.total = total; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pedido other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Pedido{id=%d, cliente=%s, producto=%s, cantidad=%d, fecha=%s, total=%s}"
                .formatted(id, clienteEmail,
                        producto != null ? producto.getNombre() : "n/d",
                        cantidad, fechaPedido, total);
    }
}
