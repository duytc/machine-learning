#!/bin/bash
mvn compile
mvn exec:java -Dexec.mainClass="com.pubvantage.AppMain"  -Dexec.cleanupDaemonThreads=false