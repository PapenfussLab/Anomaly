#!/bin/bash
# build an anomaly jar and make a docker image out of it
# expect: java8 gradle4.6

# set version
version=1.1.6

# build jar
pushd ./../../ 
gradle clean bootjar
popd

# cp jar over
cp ../.././build/libs/AnomalyApi-${version}-SNAPSHOT.jar .

# build docker image
docker build -t my_anomaly -f Dockerfile-anomaly --build-arg version=$version .
