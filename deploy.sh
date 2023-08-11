#!/usr/bin/env bash

BUILDLOG="$(pwd)/maven_logs/build_log__$(date +"%Y%m%d-%H%M%S").txt"
mkdir -p maven_logs
touch "$BUILDLOG"

GRB_VERSION="10.0.2"

mvn clean install:install-file -Dfile=lib/jmathplot.jar -DgroupId=jmathplot -DartifactId=jmathplot -Dversion=1.0 -Dpackaging=jar | tee "$BUILDLOG"

mvn clean install:install-file -Dfile=lib/gurobi.jar -DgroupId=gurobi -DartifactId=gurobi-jar -Dversion="$GRB_VERSION" -Dpackaging=jar -DlocalRepositoryPath="$(pwd)/maven-local-repository" | tee -a "$BUILDLOG"

mvn -Dmaven.test.skip=true -Denforcer.skip package | tee "$BUILDLOG"
