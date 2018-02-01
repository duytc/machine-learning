package com.pubvantage.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pubvantage.dao.CoreAutoOptimizationConfigDao;
import com.pubvantage.entity.CoreAutoOptimizationConfig;
import com.pubvantage.utils.HibernateUtil;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

public class CoreAutoOptimizationConfigService implements CoreAutoOptimizationConfigServiceInterface {
    private CoreAutoOptimizationConfigDao coreAutoOptimizationConfigDao = new CoreAutoOptimizationConfigDao();
    private static Logger logger = Logger.getLogger(CoreAutoOptimizationConfigService.class.getName());


    @Override
    public String[] getObjectiveAndFactors(Long autoOptimizationId) {
        Session session = null;
        String[] objectiveAndFactors = new String[0];
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            CoreAutoOptimizationConfig config = findById(autoOptimizationId);
            session.clear();
            session.getTransaction().commit();

            String objective = config.getObjective();
            String factors = config.getFactors();
            JsonParser jsonParser = new JsonParser();
            JsonArray arrayFromString = jsonParser.parse(factors).getAsJsonArray();
            objectiveAndFactors = new String[1 + arrayFromString.size()];
            objectiveAndFactors[0] = objective;
            for (int i = 1; i < objectiveAndFactors.length; i++) {
                int factorIndex = i - 1;
                objectiveAndFactors[i] = arrayFromString.get(factorIndex).getAsString();
            }
        } catch (Exception e) {
            if (null != session && null != session.getTransaction()) {
                session.getTransaction().rollback();
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return objectiveAndFactors;
    }

    /**
     * @param autoOptimizationId auto optimization config id
     * @return get factors's data type
     */
    @Override
    public JsonObject getFieldType(Long autoOptimizationId) {
        Session session = null;
        String fieldTypesJSON = "{}";
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            CoreAutoOptimizationConfig config = findById(autoOptimizationId);
            session.clear();
            session.getTransaction().commit();

            fieldTypesJSON = config.getFieldTypes();
        } catch (Exception e) {
            if (null != session && null != session.getTransaction()) {
                session.getTransaction().rollback();
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return new Gson().fromJson(fieldTypesJSON, JsonObject.class);
    }

    /**
     * @param autoOptimizationId auto optimization config id
     * @return list of positive factors
     */
    @Override
    public List<String> getPositiveFactors(Long autoOptimizationId) {
        Session session = null;
        String jsonArrayString = "[]";
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            CoreAutoOptimizationConfig config = findById(autoOptimizationId);
            session.clear();
            session.getTransaction().commit();

            jsonArrayString = config.getPositiveFactors();
        } catch (Exception e) {
            if (null != session && null != session.getTransaction()) {
                session.getTransaction().rollback();
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return new Gson().fromJson(jsonArrayString, ArrayList.class);
    }

    /**
     * @param autoOptimizationId auto optimization config id
     * @return list of negative factors
     */
    @Override
    public List<String> getNegativeFactors(Long autoOptimizationId) {
        Session session = null;
        String jsonArrayString = "[]";

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            CoreAutoOptimizationConfig config = findById(autoOptimizationId);
            session.clear();
            session.getTransaction().commit();

            jsonArrayString = config.getNegativeFactors();
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
        return new Gson().fromJson(jsonArrayString, ArrayList.class);
    }

    /**
     * @param autoOptimizationConfigId auto optimization config id
     * @param token                    token
     * @return true if token is valid. false if token is invalid
     */
    @Override
    public boolean checkToken(Long autoOptimizationConfigId, String token) {
        Session session = null;
        boolean isValid = false;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            isValid = coreAutoOptimizationConfigDao.checkToken(session, autoOptimizationConfigId, token);
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
        return isValid;
    }

    /**
     * @param autoOptimizationId auto optimization config id
     * @return list of factors
     */
    @Override
    public List<String> getFactors(Long autoOptimizationId) {
        Session session = null;
        List<String> objectiveAndFactors = new ArrayList<>();
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            CoreAutoOptimizationConfig config = findById(autoOptimizationId);
            session.clear();
            session.getTransaction().commit();

            String factors = config.getFactors();
            JsonParser jsonParser = new JsonParser();
            JsonArray arrayFromString = jsonParser.parse(factors).getAsJsonArray();
            objectiveAndFactors = new ArrayList<>();
            for (int i = 0; i < arrayFromString.size(); i++) {
                objectiveAndFactors.add(arrayFromString.get(i).getAsString());
            }

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
        return objectiveAndFactors;
    }

    /**
     * @param autoOptimizationId auto optimization config id
     * @return data of core auto optimization config
     */
    @Override
    public CoreAutoOptimizationConfig findById(Long autoOptimizationId) {
        Session session = null;
        CoreAutoOptimizationConfig coreAutoOptimizationConfig = new CoreAutoOptimizationConfig();
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            coreAutoOptimizationConfig = coreAutoOptimizationConfigDao.findById(autoOptimizationId, session);
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
        return coreAutoOptimizationConfig;
    }
}
