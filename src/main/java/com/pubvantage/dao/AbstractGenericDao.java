package com.pubvantage.dao;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

public abstract class AbstractGenericDao<E> implements GenericDaoInterface<E> {

    private final Class<E> entityClass;
    private static Logger logger = Logger.getLogger(AbstractGenericDao.class.getName());

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

    @Override
    public Object getCurrentChecksum(String tableName, Session session) {
        try {
            String stringBuilder = "CHECKSUM TABLE " + tableName;
            Query query = session.createSQLQuery(stringBuilder);
            List<Object> list = query.list();
            if (list != null && !list.isEmpty()) {
                Object[] checksumData = (Object[]) list.get(0);
                /*
                 * checksumData [2] contains
                 *  Table and Checksum
                 */
                if (checksumData.length >= 2) {
                    return checksumData[1];
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }
}