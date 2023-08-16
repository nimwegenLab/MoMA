FROM continuumio/miniconda3:22.11.1 as moma_batch_run_builder
# build container for building stand-alone Python package of moma_batch_run

RUN apt-get update && \
    apt-get install -y git binutils

WORKDIR /build_dir

# REFs:
# https://anaconda.org/conda-forge/pyinstaller
# https://anaconda.org/anaconda/pyyaml
RUN git clone https://github.com/michaelmell/moma-batch-run.git && \
    conda create -y -n moma-batch-run python=3.10

WORKDIR /build_dir/moma-batch-run
# Use `conda run`, because `conda activate` fails in containers; see here: https://pythonspeed.com/articles/activate-conda-dockerfile/#working
SHELL ["conda", "run", "--no-capture-output", "-n", "moma-batch-run", "/bin/bash", "-c"]
# The following RUN commands are now executed with the `conda run` command using `/bin/bash` as the shell.
# Output path of the stand-alone `moma_batch_run` build is: /build_dir/moma-batch-run/dist/moma_batch_run
RUN conda install -y pyyaml && \
    conda install -y -c conda-forge pyinstaller && \
    pyinstaller --onefile --name moma_batch_run moma_batch_run.py

# Build stand-alone `moma` executable of `moma` Python script
# Output path is: /build_dir/moma/dist/moma
WORKDIR /build_dir/moma
COPY docker/moma.py moma.py
RUN pyinstaller --onefile --name moma moma.py


FROM ubuntu:18.04 as gurobi_builder

ARG GRB_VERSION
ARG GRB_SHORT_VERSION

# install gurobi package and copy the files
WORKDIR /opt

RUN --mount=type=cache,target=/var/cache/apt/ apt-get update \
    && apt-get install --no-install-recommends -y\
       ca-certificates  \
       wget \
    && update-ca-certificates \
    && wget -v https://packages.gurobi.com/${GRB_SHORT_VERSION}/gurobi${GRB_VERSION}_linux64.tar.gz \
    && tar -xvf gurobi${GRB_VERSION}_linux64.tar.gz  \
    && rm -f gurobi${GRB_VERSION}_linux64.tar.gz \
    && mv -f gurobi* gurobi \
    && rm -rf gurobi/linux64/docs

### Build container with MoMA
#FROM ubuntu:18.04 as moma_builder
FROM ubuntu:18.04

RUN apt-get update && \
    apt-get install -y maven openjdk-8-jre

ARG build_dir="/build_dir"

COPY .git ${build_dir}/.git
COPY deploy.sh ${build_dir}/deploy.sh
COPY env_vars.sh ${build_dir}/env_vars.sh
COPY src ${build_dir}/src
COPY lib ${build_dir}/lib
#COPY --from=buildoptimizer /opt/gurobi/linux64/lib/* ${build_dir}/lib
COPY pom.xml ${build_dir}/pom.xml
WORKDIR ${build_dir}

# this caches the maven dependencies to a separate layer so we do not have to download them every time
#RUN mvn verify --fail-never

#RUN chmod +x ${build_dir}/deploy.sh
RUN ${build_dir}/deploy.sh
#RUN --mount=type=cache,target=/root/.m2/ chmod +x ${build_dir}/deploy.sh && \
#    ${build_dir}/deploy.sh
#RUN chmod +x ${build_dir}/deploy.sh && \
#    ${build_dir}/deploy.sh

### Build image based on nvidia/cuda image
#FROM ubuntu:18.04

RUN --mount=type=cache,target=/var/cache/apt/ apt-get update && \
    apt-get install -y vim tmux xvfb openjdk-11-jdk

#ARG build_dir="/build_dir"

# setup Gurobi
COPY --from=gurobi_builder /opt/gurobi /opt/gurobi
ENV GUROBI_HOME /opt/gurobi/linux64
ENV PATH $PATH:$GUROBI_HOME/bin
ENV LD_LIBRARY_PATH $GUROBI_HOME:$GUROBI_HOME/lib
ENV GUROBI_LIB_PATH $GUROBI_HOME/lib/

### Setup MoMA
#COPY --from=moma_builder ${build_dir}/target ${build_dir}/target
ENV MOMA_JAR_PATH ${build_dir}/target/

### Setup environment variables
ARG moma_dir="/moma"
ENV TF_JAVA_LIB_PATH ${moma_dir}/tensorflow
#ENV MOMA_JAR_PATH ${moma_dir}/MoMA_fiji.jar

#COPY /opt/gurobi811/linux64/lib/libGurobiJni81.so /opt/gurobi/linux64/lib/libGurobiJni81.so

WORKDIR ${moma_dir}

#RUN ln -s /build_dir/target/MotherMachine-v0.9.3.20230613-135612.d6e49c8.jar ${moma_dir}/MoMA_fiji.jar

COPY docker/tensorflow ${moma_dir}/tensorflow
COPY docker/moma_in_container ${moma_dir}/moma

ARG host_scripts="/host_scripts"
RUN mkdir $host_scripts
COPY docker/moma.py $host_scripts/moma.py
COPY --from=moma_batch_run_builder /build_dir/moma/dist/moma $host_scripts/moma
COPY --from=moma_batch_run_builder /build_dir/moma-batch-run/dist/moma_batch_run $host_scripts/moma_batch_run

WORKDIR /

ENTRYPOINT ["/moma/moma"]