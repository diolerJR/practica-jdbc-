package com.accesodatos.practicajdbc.repositorio;

import com.accesodatos.practicajdbc.dominio.Categoria;
import com.accesodatos.practicajdbc.dominio.Precio;
import com.accesodatos.practicajdbc.dominio.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Repositorio JDBC de Producto (IRepositorio + IRepositorioExtend + propios). */
public class RepositorioProducto implements IRepositorioExtend<Producto, Long> {

    // SQL
    private static final String SQL_BASE =
            "SELECT p.id, p.nombre, p.precio, p.stock, p.formato, p.sabor_material, " +
            "       c.id AS categoria_id, c.nombre AS categoria_nombre, descripcion, es_suplemento " +
            "FROM producto p " +
            "INNER JOIN categoria c ON p.categoria_id = c.id";

    private static final String SQL_INSERT =
            "INSERT INTO producto (nombre, precio, stock, formato, sabor_material, categoria_id) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE =
            "UPDATE producto SET nombre = ?, precio = ?, stock = ?, formato = ?, " +
            "sabor_material = ?, categoria_id = ? WHERE id = ?";
    private static final String SQL_COUNT = "SELECT COUNT(*) FROM producto";
    private static final String SQL_EXISTS = "SELECT 1 FROM producto WHERE id = ?";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM producto WHERE id = ?";
    private static final String SQL_DELETE_ALL = "DELETE FROM producto";
    private static final String SQL_SELECT_BY_CATEGORIA =
            SQL_BASE + " WHERE p.categoria_id = ?";
    private static final String SQL_SELECT_SIN_STOCK =
            SQL_BASE + " WHERE p.stock = 0";
    private static final String SQL_REBAJA =
            "UPDATE producto SET precio = precio * (1 - ? / 100.0) WHERE categoria_id = ?";

    @Override
    public long count() {
        try (Connection c = ConexionBD.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(SQL_COUNT)) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error contando productos", e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_EXISTS)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error comprobando producto " + id, e);
        }
    }

    @Override
    public Producto findById(Long id) {
        return findByIdOptional(id).orElse(null);
    }

    @Override
    public Optional<Producto> findByIdOptional(Long id) {
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_BASE + " WHERE p.id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapearFila(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando producto " + id, e);
        }
    }

    @Override
    public Iterable<Producto> findAll() {
        return findAllToList();
    }

    @Override
    public List<Producto> findAllToList() {
        try (Connection c = ConexionBD.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(SQL_BASE)) {
            List<Producto> resultado = new ArrayList<>();
            while (rs.next()) resultado.add(mapearFila(rs));
            return resultado;
        } catch (SQLException e) {
            throw new RuntimeException("Error listando productos", e);
        }
    }

    @Override
    public <S extends Producto> S save(S producto) {
        try (Connection c = ConexionBD.getConnection()) {
            boolean esNuevo = producto.getId() == null || !existsById(producto.getId());
            if (esNuevo) {
                try (PreparedStatement ps = c.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
                    rellenarParametros(ps, producto);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) producto.setId(keys.getLong(1));
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
                    rellenarParametros(ps, producto);
                    ps.setLong(7, producto.getId());
                    ps.executeUpdate();
                }
            }
            return producto;
        } catch (SQLException e) {
            throw new RuntimeException("Error guardando producto " + producto.getNombre(), e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_DELETE_BY_ID)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error borrando producto " + id, e);
        }
    }

    @Override
    public long deleteAll() {
        try (Connection c = ConexionBD.getConnection();
             Statement st = c.createStatement()) {
            return st.executeUpdate(SQL_DELETE_ALL);
        } catch (SQLException e) {
            throw new RuntimeException("Error borrando todos los productos", e);
        }
    }

    @Override
    public Producto deleteAndGetById(Long id) {
        Producto producto = findById(id);
        if (producto != null) deleteById(id);
        return producto;
    }

    @Override
    public List<Producto> findLast(int n) {
        List<Producto> todos = findAllToList();
        return todos.subList(Math.max(0, todos.size() - n), todos.size());
    }

    @Override
    public boolean isEmpty() {
        return count() == 0;
    }

    // Métodos propios

    /** Productos de una categoría concreta. */
    public List<Producto> findByCategoria(Long categoriaId) {
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_BY_CATEGORIA)) {
            ps.setLong(1, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Producto> resultado = new ArrayList<>();
                while (rs.next()) resultado.add(mapearFila(rs));
                return resultado;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando productos de categoría " + categoriaId, e);
        }
    }

    /** Productos agotados (stock = 0). */
    public List<Producto> findSinStock() {
        try (Connection c = ConexionBD.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(SQL_SELECT_SIN_STOCK)) {
            List<Producto> resultado = new ArrayList<>();
            while (rs.next()) resultado.add(mapearFila(rs));
            return resultado;
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando productos sin stock", e);
        }
    }

    /** Rebaja (%) todos los productos de una categoría. Devuelve filas afectadas. */
    public int rebajarCategoria(Long categoriaId, double porcentaje) {
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_REBAJA)) {
            ps.setDouble(1, porcentaje);
            ps.setLong(2, categoriaId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error aplicando rebaja", e);
        }
    }

    // Mapeo tabla -> objeto de dominio

    private void rellenarParametros(PreparedStatement ps, Producto p) throws SQLException {
        ps.setString(1, p.getNombre());
        ps.setDouble(2, p.getPrecio().getValor());
        ps.setInt(3, p.getStock());
        ps.setString(4, p.getFormato());
        ps.setString(5, p.getSaborMaterial());
        ps.setLong(6, p.getCategoria().getId());
    }

    private Producto mapearFila(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria(
                rs.getLong("categoria_id"),
                rs.getString("categoria_nombre"),
                rs.getString("descripcion"),
                rs.getBoolean("es_suplemento"));
        return new Producto(
                rs.getLong("id"),
                rs.getString("nombre"),
                new Precio(rs.getDouble("precio")),
                rs.getInt("stock"),
                rs.getString("formato"),
                rs.getString("sabor_material"),
                categoria);
    }
}
