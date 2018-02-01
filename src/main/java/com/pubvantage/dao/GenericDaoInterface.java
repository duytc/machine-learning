package com.pubvantage.dao;

import org.hibernate.Session;

import java.io.Serializable;

public interface GenericDaoInterface<E> {

    /**
     * @param entity: entity to save
     * @return Identifier of saved entity
     */
    Serializable save(E entity, Session session);

    /**
     * @param entity:entity to save or update
     */

    /**
     * Find by primary key
     *
     * @param id
     * @return unique entity
     */
    E findById(Serializable id, Session session);

}