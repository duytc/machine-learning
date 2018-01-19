package com.pubvantage.utils;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class HibernateUtil {
    private static SessionFactory sessionFactory;


    public static SessionFactory getSessionFactory() {
        if(sessionFactory == null){
            sessionFactory = buildSessionFactory();
        }
        return sessionFactory;
    }

    /**
     * close hibernate session
     */
    public static void shutdown() {
        // Close caches and connection pools
        getSessionFactory().close();
    }

    public static void startSession() {
        sessionFactory = buildSessionFactory();
    }

    /**
     * create hibernate session factory
     *
     * @return session factory
     */
    private static SessionFactory buildSessionFactory() {
        // Creating Configuration Instance & Passing Hibernate Configuration File
        Configuration configObj = new Configuration();
        configObj.configure("hibernate.cfg.xml");

        // Since Hibernate Version 4.x, ServiceRegistry Is Being Used
        ServiceRegistry serviceRegistryObj = new StandardServiceRegistryBuilder().applySettings(configObj.getProperties()).build();

        // Creating Hibernate SessionFactory Instance
        sessionFactory = configObj.buildSessionFactory(serviceRegistryObj);
        return sessionFactory;
    }

}