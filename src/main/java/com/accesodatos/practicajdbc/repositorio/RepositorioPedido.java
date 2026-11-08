package com.accesodatos.practicajdbc.repositorio;

import com.accesodatos.practicajdbc.dominio.Categoria;
import com.accesodatos.practicajdbc.dominio.Pedido;
import com.accesodatos.practicajdbc.dominio.Precio;
import com.accesodatos.practicajdbc.dominio.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Repositorio JDBC de Pedido (IRepositorio + IRepositorioExtend + propios). */
public class RepositorioPedido implements IRepositorioExtend<Pedido, Long> {

    // SQL
    private static final String SQL_BASE =
            "SELECT ped.id, ped.cliente_email, ped.cantidad, ped.fecha_pedido, ped.total, " +
                    "       p.id AS producto_id, p.nombre AS producto_nombre, p.precio, p.stock, p.formato, p.sabor_material, " +
                    "       c.id AS categoria_id, c.nombre AS categoria_nombre, c.descripcion, c.es_suplemento " +
                    "FROM pedido ped " +
                    "INNER JOIN producto p ON ped.producto_id = p.id " +
                    "LEFT JOIN categoria c ON p.categoria_id = c.id";

    private static final String SQL_INSERT =
            "INSERT INTO pedido (cliente_email, cantidad, fecha_pedido, total, producto_id) " +
                    "VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE =
            "UPDATE pedido SET cliente_email = ?, cantidad = ?, fecha_pedido = ?, " +
                    "total = ?, producto_id = ? WHERE id = ?";
    private static final String SQL_COUNT = "SELECT COUNT(*) FROM pedido";
    private static final String SQL_EXISTS = "SELECT 1 FROM pedido WHERE id = ?";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM pedido WHERE id = ?";
    private static final String SQL_DELETE_ALL = "DELETE FROM pedido";
    private static final String SQL_SELECT_BY_CLIENTE =
            SQL_BASE + " WHERE LOWER(ped.cliente_email) = LOWER(?)";
    private static final String SQL_TOTAL_POR_CLIENTE =
            "SELECT COALESCE(SUM(total), 0) FROM pedido WHERE LOWER(cliente_email) = LOWER(?)";

    @Override
    public long count() {
        try (Connection c = ConexionBD.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(SQL_COUNT)) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error contando pedidos", e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_EXISTS)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error comprobando pedido " + id, e);
        }
    }

    @Override
    public Pedido findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        return findByIdOptional(id).orElse(null);
    }

    @Override
    public Optional<Pedido> findByIdOptional(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_BASE + " WHERE ped.id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapearFila(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando pedido " + id, e);
        }
    }

    @Override
    public Iterable<Pedido> findAll() {
        return findAllToList();
    }

    @Override
    public List<Pedido> findAllToList() {
        try (Connection c = ConexionBD.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(SQL_BASE)) {
            List<Pedido> resultado = new ArrayList<>();
            while (rs.next()) {
                resultado.add(mapearFila(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw new RuntimeException("Error listando pedidos", e);
        }
    }

    @Override
    public <S extends Pedido> S save(S pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("El pedido no puede ser nulo");
        }
        try (Connection c = ConexionBD.getConnection()) {
            boolean esNuevo = pedido.getId() == null || !existsById(pedido.getId());
            if (esNuevo) {
                try (PreparedStatement ps = c.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
                    rellenarParametros(ps, pedido);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) pedido.setId(keys.getLong(1));
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
                    rellenarParametros(ps, pedido);
                    ps.setLong(6, pedido.getId());
                    ps.executeUpdate();
                }
            }
            return pedido;
        } catch (SQLException e) {
            throw new RuntimeException("Error guardando pedido", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_DELETE_BY_ID)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error borrando pedido " + id, e);
        }
    }

    @Override
    public void deleteAll() {
        try (Connection c = ConexionBD.getConnection();
             Statement st = c.createStatement()) {
            st.executeUpdate(SQL_DELETE_ALL);
        } catch (SQLException e) {
            throw new RuntimeException("Error borrando todos los pedidos", e);
        }
    }

    public Pedido deleteAndGetById(Long id) {
        Pedido pedido = findById(id);
        if (pedido != null) {
            deleteById(id);
        }
        return pedido;
    }

    public List<Pedido> findLast(int n) {
        List<Pedido> todos = findAllToList();
        return todos.subList(Math.max(0, todos.size() - n), todos.size());
    }

    public boolean isEmpty() {
        return count() == 0;
    }

    // Métodos propios

    /** Pedidos realizados por un cliente según su email. */
    public List<Pedido> findByCliente(String email) {
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_BY_CLIENTE)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                List<Pedido> resultado = new ArrayList<>();
                while (rs.next()) {
                    resultado.add(mapearFila(rs));
                }
                return resultado;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando pedidos del cliente " + email, e);
        }
    }

    /** Suma total gastada por un cliente en todos sus pedidos. */
    public double totalGastadoPorCliente(String email) {
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_TOTAL_POR_CLIENTE)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error calculando total gastado por " + email, e);
        }
    }

    // Mapeo tabla -> objeto de dominio

    private void rellenarParametros(PreparedStatement ps, Pedido p) throws SQLException {
        ps.setString(1, p.getClienteEmail().getValor());
        ps.setInt(2, p.getCantidad());
        ps.setDate(3, Date.valueOf(p.getFechaPedido()));
        ps.setDouble(4, p.getTotal().getValor());
        ps.setLong(5, p.getProducto().getId());
    }

    private Pedido mapearFila(ResultSet rs) throws SQLException {
        Categoria categoria = null;
        long categoriaId = rs.getLong("categoria_id");
        if (!rs.wasNull()) {
            categoria = new Categoria(
                    categoriaId,
                    rs.getString("categoria_nombre"),
                    rs.getString("descripcion"),
                    rs.getBoolean("es_suplemento"));
        }

        Producto producto = new Producto(
                rs.getLong("producto_id"),
                rs.getString("producto_nombre"),
                new Precio(rs.getDouble("precio")),
                rs.getInt("stock"),
                rs.getString("formato"),
                rs.getString("sabor_material"),
                categoria);

        return new Pedido(
                rs.getLong("id"),
                rs.getString("cliente_email"),
                rs.getInt("cantidad"),
                rs.getDate("fecha_pedido").toLocalDate(),
                new Precio(rs.getDouble("total")),
                producto);
    }
}