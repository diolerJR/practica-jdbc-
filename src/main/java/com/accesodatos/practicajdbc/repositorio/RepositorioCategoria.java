package com.accesodatos.practicajdbc.repositorio;

import com.accesodatos.practicajdbc.dominio.Categoria;
import com.accesodatos.practicajdbc.dominio.Precio;
import com.accesodatos.practicajdbc.dominio.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Repositorio JDBC de Categoria (IRepositorio + IRepositorioExtend + propios). */
public class RepositorioCategoria implements IRepositorioExtend<Categoria, Long> {

    // SQL
    private static final String SQL_INSERT =
            "INSERT INTO categoria (nombre, descripcion, es_suplemento) VALUES (?, ?, ?)";
    private static final String SQL_UPDATE =
            "UPDATE categoria SET nombre = ?, descripcion = ?, es_suplemento = ? WHERE id = ?";
    private static final String SQL_SELECT_ALL =
            "SELECT id, nombre, descripcion, es_suplemento FROM categoria";
    private static final String SQL_SELECT_BY_ID =
            SQL_SELECT_ALL + " WHERE id = ?";
    private static final String SQL_COUNT = "SELECT COUNT(*) FROM categoria";
    private static final String SQL_EXISTS =
            "SELECT 1 FROM categoria WHERE id = ?";
    private static final String SQL_DELETE_BY_ID =
            "DELETE FROM categoria WHERE id = ?";
    private static final String SQL_DELETE_ALL = "DELETE FROM categoria";
    private static final String SQL_SELECT_BY_NOMBRE =
            SQL_SELECT_ALL + " WHERE nombre LIKE ?";
    private static final String SQL_SELECT_SUPLEMENTOS =
            SQL_SELECT_ALL + " WHERE es_suplemento = TRUE";
    private static final String SQL_SELECT_PRODUCTOS_DE_CATEGORIA =
            "SELECT id, nombre, precio, stock, formato, sabor_material FROM producto WHERE categoria_id = ?";

    @Override
    public long count() {
        try (Connection c = ConexionBD.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(SQL_COUNT)) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error contando categorías", e);
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
            throw new RuntimeException("Error comprobando existencia de categoría " + id, e);
        }
    }

    @Override
    public Categoria findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        return findByIdOptional(id).orElse(null);
    }

    @Override
    public Optional<Categoria> findByIdOptional(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapearFila(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando categoría " + id, e);
        }
    }

    @Override
    public Iterable<Categoria> findAll() {
        return findAllToList();
    }

    @Override
    public List<Categoria> findAllToList() {
        try (Connection c = ConexionBD.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(SQL_SELECT_ALL)) {
            List<Categoria> resultado = new ArrayList<>();
            while (rs.next()) {
                resultado.add(mapearFila(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw new RuntimeException("Error listando categorías", e);
        }
    }

    @Override
    public <S extends Categoria> S save(S categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría no puede ser nula");
        }
        try (Connection c = ConexionBD.getConnection()) {
            boolean esNueva = categoria.getId() == null || !existsById(categoria.getId());
            if (esNueva) {
                try (PreparedStatement ps = c.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
                    rellenarParametros(ps, categoria);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) categoria.setId(keys.getLong(1));
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
                    rellenarParametros(ps, categoria);
                    ps.setLong(4, categoria.getId());
                    ps.executeUpdate();
                }
            }
            return categoria;
        } catch (SQLException e) {
            throw new RuntimeException("Error guardando categoría " + categoria.getNombre(), e);
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
            throw new RuntimeException("Error borrando categoría " + id, e);
        }
    }

    @Override
    public void deleteAll() {
        try (Connection c = ConexionBD.getConnection();
             Statement st = c.createStatement()) {
            st.executeUpdate(SQL_DELETE_ALL);
        } catch (SQLException e) {
            throw new RuntimeException("Error borrando todas las categorías", e);
        }
    }

    public Categoria deleteAndGetById(Long id) {
        Categoria categoria = findById(id);
        if (categoria != null) {
            deleteById(id);
        }
        return categoria;
    }

    public List<Categoria> findLast(int n) {
        List<Categoria> todas = findAllToList();
        return todas.subList(Math.max(0, todas.size() - n), todas.size());
    }

    public boolean isEmpty() {
        return count() == 0;
    }

    // Métodos propios

    /** Categorías cuyo nombre contiene el texto (LIKE, insensible al caso del LIKE). */
    public List<Categoria> findByNombre(String texto) {
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_BY_NOMBRE)) {
            ps.setString(1, "%" + texto + "%");
            try (ResultSet rs = ps.executeQuery()) {
                List<Categoria> resultado = new ArrayList<>();
                while (rs.next()) resultado.add(mapearFila(rs));
                return resultado;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando categorías por nombre", e);
        }
    }

    /** Solo categorías marcadas como suplemento. */
    public List<Categoria> findSuplementos() {
        try (Connection c = ConexionBD.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(SQL_SELECT_SUPLEMENTOS)) {
            List<Categoria> resultado = new ArrayList<>();
            while (rs.next()) resultado.add(mapearFila(rs));
            return resultado;
        } catch (SQLException e) {
            throw new RuntimeException("Error listando suplementos", e);
        }
    }

    // Mapeo tabla -> objeto de dominio

    private void rellenarParametros(PreparedStatement ps, Categoria cat) throws SQLException {
        ps.setString(1, cat.getNombre());
        ps.setString(2, cat.getDescripcion());
        ps.setBoolean(3, cat.isEsSuplemento());
    }

    private Categoria mapearFila(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getBoolean("es_suplemento"));
        categoria.loadProductos(cargarProductosDeCategoria(categoria));
        return categoria;
    }

    private List<Producto> cargarProductosDeCategoria(Categoria categoria) throws SQLException {
        try (Connection c = ConexionBD.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_PRODUCTOS_DE_CATEGORIA)) {
            ps.setLong(1, categoria.getId());
            try (ResultSet rs = ps.executeQuery()) {
                List<Producto> productos = new ArrayList<>();
                while (rs.next()) {
                    productos.add(new Producto(
                            rs.getLong("id"),
                            rs.getString("nombre"),
                            new Precio(rs.getDouble("precio")),
                            rs.getInt("stock"),
                            rs.getString("formato"),
                            rs.getString("sabor_material"),
                            categoria));
                }
                return productos;
            }
        }
    }
}