#!/bin/bash
sudo apt-get install maven
mvn compile
mvn exec:java -Dexec.mainClass="com.pubvantage.AppMain" -Dexec.args="--autoOptimizationId = 1,2 --identifier = allenwestrepublic.com,androidauthority.com" -Dexec.cleanupDaemonThreads=false
#-Dexec.args examples
#-Dexec.args="--autoOptimizationId = all --identifier = allenwestrepublic.com,androidauthority.com"
#-Dexec.args="--autoOptimizationId = all --identifier = all"
#-Dexec.args="--autoOptimizationId = all"
#-Dexec.args="--autoOptimizationId = 1,2"