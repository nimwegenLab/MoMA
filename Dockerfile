FROM ubuntu:18.04 as buildoptimizer
ARG GRB_VERSION=9.5.2
ARG GRB_SHORT_VERSION=9.5

# install gurobi package and copy the files
WORKDIR /opt

RUN apt-get update \
    && apt-get install --no-install-recommends -y\
       ca-certificates  \
       wget \
    && update-ca-certificates \
    && wget -v https://packages.gurobi.com/${GRB_SHORT_VERSION}/gurobi${GRB_VERSION}_linux64.tar.gz \
    && tar -xvf gurobi${GRB_VERSION}_linux64.tar.gz  \
    && rm -f gurobi${GRB_VERSION}_linux64.tar.gz \
    && mv -f gurobi* gurobi \
    && rm -rf gurobi/linux64/docs


### Build image based on nvidia/cuda image
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

# copy gurobi files from buildoptimizer
WORKDIR /opt/gurobi
COPY --from=buildoptimizer /opt/gurobi .

ENV GUROBI_HOME /opt/gurobi/linux64
ENV PATH $PATH:$GUROBI_HOME/bin
ENV LD_LIBRARY_PATH $GUROBI_HOME/lib

### Setup MoMA
ARG moma_dir="/moma"
WORKDIR ${moma_dir}

RUN ln -s /build_dir/target/MotherMachine-v0.9.3.20230613-135612.d6e49c8.jar MoMA_fiji.jar

COPY docker/moma ${moma_dir}/moma