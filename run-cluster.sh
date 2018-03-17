#!/bin/bash
for i in "$@"
do
case $i in
    -m=*|--masterUrl=*)
    masterUrl="${i#*=}"
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

if [[ ${masterUrl} == "" ]]
then
   echo "PLease enter master url"
   echo "Use option -m='your master url'"
   echo "Example: spark://dtag-litpu:7077"
else
  echo "Master url is ${masterUrl}"

  sudo apt-get install maven
  mvn clean compile assembly:single

   ./spark-2.2.0-bin-hadoop2.7/bin/spark-submit \
  --master ${masterUrl} \
  --class com.pubvantage.AppMain \
  --driver-class-path "lib/mysql-connector-java-5.1.21.jar" \
  target/unified-report-scorer-jar-with-dependencies.jar \
  ${masterUrl}

fi