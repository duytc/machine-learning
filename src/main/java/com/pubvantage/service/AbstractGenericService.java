package com.pubvantage.service;

import com.pubvantage.dao.GenericDaoInterface;
import com.pubvantage.entity.CoreReportView;
import com.pubvantage.utils.HibernateUtil;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.io.Serializable;

/**
 * Created by quyendq on 06/04/2018.
 */
public class AbstractGenericService<E> implements GenericServiceInterface<E> {
    private static Logger logger = Logger.getLogger(AbstractGenericService.class.getName());

    @Override
    public E findById(Serializable id, GenericDaoInterface dao) {
        Session session = null;
        Object object = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            object = dao.findById(id, session);
            session.clear();
            session.getTransaction().commit();
        } catch (Exception e) {
            if (null != session && null != session.getTransaction()) {
                session.getTransaction().rollback();
            }
            logger.error(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return (E) object;
    }
}
