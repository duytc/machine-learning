package com.pubvantage.utils;

import com.pubvantage.AppMain;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.util.Properties;

public class HibernateUtil {
    private static SessionFactory sessionFactory;
    private static Logger logger = Logger.getLogger(AppMain.class.getName());
    private static Properties userConfig;
    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;
    private static String dbTimeZone;

    private static Session currentSession;

    static {
        AppResource appResource = new AppResource();
        userConfig = appResource.getUserConfiguration();
    }

    public HibernateUtil() {

    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
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
//        currentSession = sessionFactory.openSession();
    }

    /**
     * create hibernate session factory
     *
     * @return session factory
     */
    private static SessionFactory buildSessionFactory() {
        try {
            getDataBaseConfig();
            // Creating Configuration Instance & Passing Hibernate Configuration File
            Configuration configObj = new Configuration();
            configObj.configure("hibernate.cfg.xml");
            configObj.setProperty("hibernate.connection.url", dbUrl);
            configObj.setProperty("hibernate.connection.username", dbUser);
            configObj.setProperty("hibernate.connection.password", dbPassword);

            // Since Hibernate Version 4.x, ServiceRegistry Is Being Used
            ServiceRegistry serviceRegistryObj = new StandardServiceRegistryBuilder()
                    .applySettings(configObj.getProperties()).build();
            // Creating Hibernate SessionFactory Instance
            sessionFactory = configObj.buildSessionFactory(serviceRegistryObj);
        } catch (Exception e) {
            logger.error("DATABASE INFO WRONG: " + e.getMessage(), e);
        }
        return sessionFactory;
    }

    private static void getDataBaseConfig() {
        dbUrl = userConfig.getProperty("db.url");
        dbUser = userConfig.getProperty("db.user");
        dbPassword = userConfig.getProperty("db.password");
        dbTimeZone = userConfig.getProperty("db.serverTimezone");
    }

    public static Session getCurrentSession() {
        return currentSession;
    }
}