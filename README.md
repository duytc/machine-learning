# unified-report-scorer
Scoring service for Unified Report database


Get started
-----------

####  1. Config parameters
- Open run.sh in unified-report-scorer folder and edit parameter in ```-Dexec.args```
    
```
#!/bin/bash
sudo apt-get install maven
mvn compile
mvn exec:java -Dexec.mainClass="com.pubvantage.AppMain" -Dexec.args="--autoOptimizationId = 1,2 --identifier = allenwestrepublic.com,androidauthority.com" -Dexec.cleanupDaemonThreads=false
```

- Bellow is som example of parameters

```
-Dexec.args examples
-Dexec.args="--autoOptimizationId = all --identifier = allenwestrepublic.com,androidauthority.com"
-Dexec.args="--autoOptimizationId = all --identifier = all"
-Dexec.args="--autoOptimizationId = all"
-Dexec.args="--autoOptimizationId = 1,2"
```

#### 2. Run

```
cd path/of/unified-report-scorer/
./run.sh
```

References
----------

[https://spark.apache.org/docs/latest/index.html](https://spark.apache.org/docs/latest/index.html)

[https://www.datasciencebytes.com/bytes/2016/04/18/getting-started-with-spark-running-a-simple-spark-job-in-java/](https://www.datasciencebytes.com/bytes/2016/04/18/getting-started-with-spark-running-a-simple-spark-job-in-java/)
