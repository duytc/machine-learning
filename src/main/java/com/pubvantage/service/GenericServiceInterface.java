package com.pubvantage.service;

import com.pubvantage.dao.GenericDaoInterface;

import java.io.Serializable;

/**
 * Created by quyendq on 06/04/2018.
 */
public interface GenericServiceInterface<E> {

    E findById(Serializable id, GenericDaoInterface dao);
}
