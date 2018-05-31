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
     * Find by primary key
     *
     * @param id id column
     * @return unique entity
     */
    E findById(Serializable id, Session session);

    Object getCurrentChecksum(String tableName, Session session);
}