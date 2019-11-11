# Anomaly Docker Deployment

[Docker](http://www.docker.com) is a tool that allows complex software to be easily deployed and ran using lightweight containers linked together by configuration files.

Here we provide files to assist users in running Anomaly over Docker.


### Installing Docker

Unix users should install [Docker](https://docs.docker.com/install/#server) and [Docker Compose](https://docs.docker.com/compose/install/).

Windows users can use [Docker Desktop for Windows](https://docs.docker.com/docker-for-windows/install/).

Mac users can use [Docker Desktop for Mac](https://docs.docker.com/docker-for-mac/install/).


## Quick Start

To start Anomaly from the Docker image, once you've cloned the Anomaly repository, simply navigate to the docker directory and run docker-compose up.

```
cd <your anomaly dir>/docker
docker-compose up
```

This will spin up Anomaly (version 1.1.6 from Docker Hub at the time of writing) along with a local mongodb database, which will persist on the fileshare under docker/mongo-volume. 

## Build a custom anomaly image

If you've changed the Anomaly source code and now wish to build and run a new custom Anomaly docker image, we provide a script to assist you.

The script at docker/build/buildimage.sh will re-build the Anomaly JAR and subsequently make a new local Anomaly image called my_anomaly

```
cd <your anomaly diectory>
cd docker/build
sh buildimage.sh
```

Once you've got the custom image build, you can change the docker-compose.yaml in the docker directory to use your new image.
Simmple evil docker/docker-compose.yaml and change line 5 from image: 
```
  dockerpathos/anomaly:1.1.6 
```
to 
```
  image: my_anomaly
```
Subsequently, runnign docker-compose up from the docker directory will run your new custom Anomaly.


