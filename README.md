# Práctica JDBC — Acceso a Datos (2026-2027)

Aplicación Java con **DDD** y **Patrón Repositorio** sobre JDBC, usando las tablas
`CATEGORIA`, `PRODUCTO` y `PEDIDO`.

## Estructura

```
src/main/java/com/accesodatos/practicajdbc/
├── dominio/            # Modelo de dominio DDD
│   ├── Categoria.java     (raíz del agregado, 1 → N productos)
│   ├── Producto.java      (entidad del agregado)
│   ├── Pedido.java        (raíz del agregado pedido, N:1 producto)
│   ├── Precio.java        (objeto valor, inmutable)
│   └── EmailCliente.java  (objeto valor con validación)
├── repositorio/        # Patrón Repositorio sobre JDBC
│   ├── IRepositorio.java       (interfaz obligatoria)
│   ├── IRepositorioExtend.java (interfaz obligatoria)
│   ├── RepositorioCategoria.java
│   ├── RepositorioProducto.java
│   ├── RepositorioPedido.java
│   └── ConexionBD.java
└── Main.java           # Demostración de todos los métodos CRUD

src/main/resources/
├── database.properties  # Configuración de conexión (ruta relativa)
└── schema.sql           # Script de creación de la BD
```

## Modelo de dominio (UML)

- `Categoria` (raíz agregada) **1 → N** `Producto`
- `Producto` **1 → N** `Pedido` / `Pedido` **N → 1** `Producto`
- Objetos valor: `Precio`, `EmailCliente`

## Repositorios

Los tres repositorios implementan `IRepositorioExtend<T, ID>` (que hereda `IRepositorio<T, ID>`):
`count`, `existsById`, `findById`, `findByIdOptional`, `findAll`, `findAllToList`,
`save` (inserta o actualiza), `deleteById`, `deleteAll`, `deleteAndGetById`, `findLast`, `isEmpty`.

Métodos propios adicionales con valor semántico:

| Repositorio | Método propio |
|---|---|
| `RepositorioCategoria` | `findByNombre(String)`, `findSuplementos()` |
| `RepositorioProducto` | `findByCategoria(Long)`, `findSinStock()`, `rebajarCategoria(Long, %)` |
| `RepositorioPedido` | `findByCliente(String)`, `findByFecha(LocalDate)`, `totalGastadoPorCliente(String)` |

## Puesta en marcha

1. Ejecuta `src/main/resources/schema.sql` en tu MySQL/MariaDB.
2. Ajusta usuario y contraseña en `src/main/resources/database.properties`
   (si usas MySQL en vez de MariaDB cambia el driver en `pom.xml` por
   `mysql:mysql-connector-j` y la URL a `jdbc:mysql://localhost:3306/tienda`).
3. Compila y ejecuta:

```
mvn compile exec:java -Dexec.mainClass=com.accesodatos.practicajdbc.Main
```
