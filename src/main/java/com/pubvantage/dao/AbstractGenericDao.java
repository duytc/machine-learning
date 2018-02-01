package com.pubvantage.dao;

import org.hibernate.Session;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

public abstract class AbstractGenericDao<E> implements GenericDaoInterface<E> {

    private final Class<E> entityClass;

    public AbstractGenericDao() {
        this.entityClass = (Class<E>) ((ParameterizedType) this.getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }


    @Override
    public E findById(final Serializable id, Session session) {
        return (E) session.get(this.entityClass, id);
    }

    @Override
    public Serializable save(E entity, Session session) {
        return session.save(entity);
    }
}