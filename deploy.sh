#!/usr/bin/env bash

BUILDLOG="$(pwd)/maven_logs/build_log__$(date +"%Y%m%d-%H%M%S").txt"
mkdir -p maven_logs
touch "$BUILDLOG"

mvn clean install:install-file -Dfile=lib/jmathplot.jar -DgroupId=jmathplot -DartifactId=jmathplot -Dversion=1.0 -Dpackaging=jar | tee "$BUILDLOG"

mvn clean install:install-file -Dfile=lib/gurobi.jar -DgroupId=gurobi -DartifactId=gurobi -Dversion=1.0 -Dpackaging=jar -DlocalRepositoryPath=$(pwd)/repo | tee -a "$BUILDLOG"

mvn -Dmaven.test.skip=true -Denforcer.skip package | tee "$BUILDLOG"

