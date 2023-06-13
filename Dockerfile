FROM nvidia/cuda:10.0-base-ubuntu18.04

RUN apt-get update && \
    apt-get install -y maven && \
    apt-get install -y openjdk-8-jre

ARG build_dir="/build_dir"

COPY .git ${build_dir}/.git
COPY deploy.sh ${build_dir}/deploy.sh
COPY src ${build_dir}/src
COPY lib ${build_dir}/lib
COPY pom.xml ${build_dir}/pom.xml
WORKDIR ${build_dir}

# this caches the maven dependencies to a separate layer so we do not have to download them every time
#RUN mvn verify --fail-never

RUN  --mount=type=cache,target=/root/.m2 chmod +x ${build_dir}/deploy.sh && \
    ${build_dir}/deploy.sh
