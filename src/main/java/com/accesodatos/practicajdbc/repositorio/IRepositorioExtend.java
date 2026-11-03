package com.accesodatos.practicajdbc.repositorio;

import java.util.List;

/** Contrato extendido proporcionado en la práctica (borrado masivo y extras). */
public interface IRepositorioExtend<T, ID> extends IRepositorio<T, ID> {

    /** Borra todas las entidades. Devuelve cuántas. */
    long deleteAll();

    /** Borra y devuelve la entidad con ese ID (o null). */
    T deleteAndGetById(ID id);

    /** Últimas n entidades insertadas. */
    List<T> findLast(int n);

    /** true si no hay nada guardado. */
    boolean isEmpty();
}
