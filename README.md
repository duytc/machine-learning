# unified-report-scorer
Scoring service for Unified Report database


Get started
-----------

####  1. Requirements

- JDK 8.
- Maven:
```
sudo apt-get install maven
```

- curl or REST client tool (Postman, Advance rest client)


#### 2. Clone the project

- git clone git@github.com:tagcade/unified-report-scorer.git
- Branch : auto-optimization-rest

#### 3. Configuration
**Database** 
- Edit name of `user.config.properties.template` -> `user.config.properties`. Then update database info

```
#database
db.url=jdbc:mysql://localhost:3306/unified_reports_api
db.user=root
db.password=123456
```
- Change the __identifier correspond to your identifier column name in __training_data_x 

```
column.identifier=__identifier
```
**Port of Api**
- Open `user.config.properties` and change your port as bellow

```
#rest
api.port=8086
```

#### 4. Run project

##### 4.1 Run as Java application.


```
cd unified-report-scorer/
./run-local.sh
```


##### 4.2. Run on a local cluster

- Open `user.config.properties` and config as bellow

- run:

```
cd unified-report-scorer/

`./run-cluster.sh -m='local[*]'`
```

##### 4.2. Run on a remote cluster: 

- Download apache spark : http://spark.apache.org/downloads.html . Choose `version 2.2.0` and `Pre-built for Apache Hadoop 2.7 and later` 
- extract compressed file to project folder : `unified-report-scorer`. So we have folder `spark-2.2.0-bin-hadoop2.7` in `unified-report-scorer` 
- Start a Spark master node:
```
./spark-2.2.0-bin-hadoop2.7/sbin/start-master.sh
```
View your Spark master by going to localhost:8080 in your browser. take a look at `URL: spark://dtag-litpu:7077`

Example:
```
URL: spark://dtag-litpu:7077
REST URL: spark://dtag-litpu:6066 (cluster mode)
Alive Workers: 1
Cores in use: 4 Total, 4 Used
Memory in use: 6.8 GB Total, 1024.0 MB Used
Applications: 1 Running, 5 Completed
Drivers: 0 Running, 0 Completed
Status: ALIVE
```
- Start a worker with this command, filling in your URL  `spark://dtag-litpu:7077 ` 

```
./spark-2.2.0-bin-hadoop2.7/sbin/start-slave.sh spark://dtag-litpu:7077
```

- Run 
```
cd unified-report-scorer

./run-cluster.sh -m='spark://dtag-litpu:7077'
```
Because we run project on cluster, it will take about 1 minute to compile code and libraries to *.jar. 

After that  REST API is ready.

#### 5. Call API
**Api info** :

5.1. Do convert and learn

URL : `http://localhost:8086/api/learner`

Method : POST

Header : Content-Type: application/json

Parameter: `autoOptimizationConfigId`(a Auto optimization config id. Example: 1) and `token`
        
        
```
{
  "autoOptimizationConfigId": 1 ,
  "token" : "eyJhbGciOiJIUzI1NiJ9"
}
```

Return value: Example:
**1. Success: **

* Error code: 200

* Json data: 

  Example:

```
{
  "status": 200,
  "message": "Learn successfully",
  "data": [
            {
               "autoOptimizationConfigId": 1,
               "identifiers": [
                     "allenwestrepublic.com",
                     "androidauthority.com",
                     "dzunght163.com"
               ],
            }
  ],
}
```

**2. Fail**

* Error code: 401 | 400 (or other except 200)

* Json data for error code 401:

  Example:
```
{
  "status": 401,
  "message": "Fail authentication"
}
```
**Request Example**:
```
curl --header "Content-Type: application/json" --request POST --data '{"autoOptimizationConfigId":1,"token":"eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxIiwiaWF0IjoxNTE2Njg5ODMyLCJzdWIiOiJzdWJqZWN0IiwiaXNzIjoiaXNzdWVyIn0.aTkzk5DqatNh-fdE3b-dlMiXgHve1RoS7rJu4nwwkiw"}' http://localhost:8086/api/learner
```

References
----------

[https://spark.apache.org/docs/latest/index.html](https://spark.apache.org/docs/latest/index.html)

[https://www.datasciencebytes.com/bytes/2016/04/18/getting-started-with-spark-running-a-simple-spark-job-in-java/](https://www.datasciencebytes.com/bytes/2016/04/18/getting-started-with-spark-running-a-simple-spark-job-in-java/)
