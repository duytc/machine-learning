#!/bin/bash
sudo apt-get install maven
mvn compile

for i in "$@"
do
case $i in
    -a=*|--autoOptimizationId=*)
    autoOptimizationId="${i#*=}"
    shift # past argument=value
    ;;
    -i=*|--identifier=*)
    identifier="${i#*=}"
    shift # past argument=value
    ;;
    --default)
    DEFAULT=YES
    shift # past argument with no value
    ;;
    *)
          # unknown option
    ;;
esac
done
#echo "FILE autoOptimizationId  = ${autoOptimizationId}"
#echo "SEARCH PATH     = ${identifier}"

mvn exec:java -Dexec.mainClass="com.pubvantage.AppMain" -Dexec.args="--autoOptimizationId = ${autoOptimizationId} --identifier = ${identifier}" -Dexec.cleanupDaemonThreads=false