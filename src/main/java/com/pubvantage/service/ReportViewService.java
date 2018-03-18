package com.pubvantage.service;

import com.pubvantage.dao.ReportViewDao;
import com.pubvantage.dao.ReportViewDaoInterface;
import com.pubvantage.entity.CoreReportView;
import com.pubvantage.utils.HibernateUtil;
import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 * Created by quyendq on 02/03/2018.
 */
public class ReportViewService implements ReportViewServiceInterface {
    private static Logger logger = Logger.getLogger(ReportViewService.class.getName());
    private ReportViewDaoInterface reportViewDao = new ReportViewDao();

    @Override
    public CoreReportView findById(Long reportViewId) {
        Session session = null;
        CoreReportView reportView = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            reportView = reportViewDao.findById(reportViewId, session);
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
        return reportView;
    }
}
