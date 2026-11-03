package com.accesodatos.practicajdbc.repositorio;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Lee la config relativa (src/main/resources/database.properties) y abre conexiones JDBC. */
public final class ConexionBD {

    private static final String FICHERO_CONFIG = "/database.properties";
    private static String url;
    private static String usuario;
    private static String password;

    static {
        cargarConfiguracion();
    }

    private ConexionBD() {
    }

    private static void cargarConfiguracion() {
        try (InputStream in = ConexionBD.class.getResourceAsStream(FICHERO_CONFIG)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró " + FICHERO_CONFIG);
            }
            Properties props = new Properties();
            props.load(in);
            url = props.getProperty("db.url");
            usuario = props.getProperty("db.user");
            password = props.getProperty("db.password");
            if (url == null) {
                throw new IllegalStateException("Falta db.url en " + FICHERO_CONFIG);
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Error cargando la configuración de BD: " + e.getMessage());
        }
    }

    /** Nueva conexión; cerrarla con try-with-resources. */
    public static Connection getConnection() throws SQLException {
        if (usuario != null && !usuario.isBlank()) {
            return DriverManager.getConnection(url, usuario, password);
        }
        return DriverManager.getConnection(url);
    }

    public static String getUrl() {
        return url;
    }
}
