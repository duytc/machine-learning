package com.pubvantage.service;

import com.pubvantage.entity.CoreLearningModel;

import java.util.List;

public interface DataTrainingService {

    boolean saveListModel(List<CoreLearningModel> modelList);
}
