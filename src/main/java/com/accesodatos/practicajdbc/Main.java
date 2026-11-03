package com.accesodatos.practicajdbc;

import com.accesodatos.practicajdbc.dominio.Categoria;
import com.accesodatos.practicajdbc.dominio.Pedido;
import com.accesodatos.practicajdbc.dominio.Precio;
import com.accesodatos.practicajdbc.dominio.Producto;
import com.accesodatos.practicajdbc.repositorio.ConexionBD;
import com.accesodatos.practicajdbc.repositorio.RepositorioCategoria;
import com.accesodatos.practicajdbc.repositorio.RepositorioPedido;
import com.accesodatos.practicajdbc.repositorio.RepositorioProducto;

import java.util.List;

/** Demostración del CRUD completo sobre la BD tienda. */
public class Main {

    public static void main(String[] args) {
        var repoCategoria = new RepositorioCategoria();
        var repoProducto = new RepositorioProducto();
        var repoPedido = new RepositorioPedido();

        System.out.println("Conectando a: " + ConexionBD.getUrl());

        // 1. Insertar categoría con dos productos
        Categoria proteinas = new Categoria("Proteínas", "Suplementos proteicos", false);
        proteinas.addProducto(new Producto("Whey Isolate", new Precio(39.90), 25, "1 kg", "Chocolate"));
        proteinas.addProducto(new Producto("Caseína Micelar", new Precio(34.50), 15, "1 kg", "Vainilla"));
        repoCategoria.save(proteinas);
        proteinas.getProductos().forEach(repoProducto::save);
        System.out.println("Guardado: " + proteinas);

        // 2. Leer por ID
        Categoria recuperada = repoCategoria.findById(proteinas.getId());
        System.out.println("Leída por ID: " + recuperada);
        System.out.println("Total categorías: " + repoCategoria.count());

        // 3. save sobre entidad existente actualiza
        recuperada.setDescripcion("Proteínas de alta calidad");
        repoCategoria.save(recuperada);
        System.out.println("Actualizada: " + repoCategoria.findById(recuperada.getId()));

        // 4. Método propio: productos por categoría
        List<Producto> productosCategoria = repoProducto.findByCategoria(recuperada.getId());
        System.out.println("Productos de '" + recuperada.getNombre() + "': " + productosCategoria.size());

        // 5. Pedido con total calculado por la fábrica de dominio
        Producto whey = repoProducto.findAllToList().get(0);
        Pedido pedido = Pedido.of(whey, "ana@example.com", 2);
        repoPedido.save(pedido);
        System.out.println("Pedido guardado: " + pedido);

        // 6. Métodos propios de pedido
        System.out.println("Pedidos de ana@example.com: " + repoPedido.findByCliente("ana@example.com"));
        System.out.printf("Total gastado por ana: %.2f €%n",
                repoPedido.totalGastadoPorCliente("ana@example.com"));

        // 7. existsById + findByIdOptional
        System.out.println("Existe pedido " + pedido.getId() + "? " + repoPedido.existsById(pedido.getId()));
        repoPedido.findByIdOptional(pedido.getId())
                .ifPresent(p -> System.out.println("findByIdOptional → " + p));

        // 8. Borrar
        repoPedido.deleteById(pedido.getId());
        System.out.println("Pedido borrado. ¿Existe? " + repoPedido.existsById(pedido.getId()));
    }
}
