# unified-report-scorer
Scoring service for Unified Report database


Get started
-----------

- Modify src/main/java/App.java

- Build

```
mvn install
```

- Download apache spark from [http://spark.apache.org/downloads.html](http://spark.apache.org/downloads.html), extract to `dev/`

- Run

```
./dev/spark-2.2.1-bin-hadoop2.7/bin/spark-submit --master local[*] --class App target/unified-report-scorer-1.0-SNAPSHOT.jar
```

References
----------

[https://spark.apache.org/docs/latest/index.html](https://spark.apache.org/docs/latest/index.html)

[https://www.datasciencebytes.com/bytes/2016/04/18/getting-started-with-spark-running-a-simple-spark-job-in-java/](https://www.datasciencebytes.com/bytes/2016/04/18/getting-started-with-spark-running-a-simple-spark-job-in-java/)
