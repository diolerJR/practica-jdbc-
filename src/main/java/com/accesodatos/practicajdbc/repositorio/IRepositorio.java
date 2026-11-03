package com.accesodatos.practicajdbc.repositorio;

import java.util.List;
import java.util.Optional;

/** Contrato genérico de repositorio proporcionado en la práctica. */
public interface IRepositorio<T, ID> {

    /** Número total de elementos. */
    long count();

    /** ¿Existe una entidad con ese ID? */
    boolean existsById(ID id);

    /** Entidad con ese ID, o null si no existe. */
    T findById(ID id);

    /** Todas las entidades. */
    Iterable<T> findAll();

    /** Entidad con ese ID en un Optional. */
    Optional<T> findByIdOptional(ID id);

    /** Todas las entidades como List. */
    List<T> findAllToList();

    /** Inserta si es nueva, actualiza si ya existe. */
    <S extends T> S save(S entity);

    /** Borra la entidad con ese ID. */
    void deleteById(ID id);
}
