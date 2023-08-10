#!/bin/bash

# Create .moma directory in home-directory, if it does not exist
CONFIG_DIRECTORY=${HOME}/.moma
if [ ! -d "$CONFIG_DIRECTORY" ]; then
  mkdir $CONFIG_DIRECTORY
  printf "Config directory did not exist: $CONFIG_DIRECTORY\nCreated it.\n"
fi

# Setup temporary directories for MoMA
MOMA_TMP_WORK_DIR=$(mktemp -d -t moma.$(date +%Y%m%d-%H%M%S).XXXXX)
MOMA_TMP_WORK_DIR_TF="${MOMA_TMP_WORK_DIR}/tensorflow"
JAVA_TMPDIR="${MOMA_TMP_WORK_DIR}/java.io.tmpdir"
mkdir -p "${MOMA_TMP_WORK_DIR_TF}"
mkdir -p "${JAVA_TMPDIR}"
printf "MOMA_TMP_WORK_DIR: ${MOMA_TMP_WORK_DIR}\n"
printf "MOMA_TMP_WORK_DIR_TF: ${MOMA_TMP_WORK_DIR_TF}\n"
printf "JAVA_TMPDIR: ${JAVA_TMPDIR}\n"

# Get path to MoMA JAR file
MOMA_JAR_FILENAME=$(ls "${MOMA_JAR_PATH}"/*.jar | grep -P '^((?!original|tests|sources).)*.jar$' | grep -o '[^\/]*$')

# Run MoMA in headless mode using xvfb-run, if DISPLAY is not set. Otherwise run MoMA with X forwarding (MoMA will throw an exception, if X server/X forwarding is not available).
if [[ -z "${DISPLAY}" ]]; then
  xvfb-run java -Xmx24g \
       -Djava.io.tmpdir=$JAVA_TMPDIR \
       -Djava.library.path=$GUROBI_LIB_PATH:$TF_JAVA_LIB_PATH \
       -Dimagej.tensorflow.models.dir="${MOMA_TMP_WORK_DIR_TF}" \
       -Djava.util.prefs.systemRoot="${MOMA_TMP_WORK_DIR}" \
       -Djava.util.prefs.userRoot="${MOMA_TMP_WORK_DIR}" \
       -jar  "${MOMA_JAR_PATH}"/"${MOMA_JAR_FILENAME}" \
       "$@"
else
  java -Xmx24g \
       -Djava.io.tmpdir=$JAVA_TMPDIR \
       -Djava.library.path=$GUROBI_LIB_PATH:$TF_JAVA_LIB_PATH \
       -Dimagej.tensorflow.models.dir="${MOMA_TMP_WORK_DIR_TF}" \
       -Djava.util.prefs.systemRoot="${MOMA_TMP_WORK_DIR}" \
       -Djava.util.prefs.userRoot="${MOMA_TMP_WORK_DIR}" \
       -jar  "${MOMA_JAR_PATH}"/"${MOMA_JAR_FILENAME}" \
       "$@"
fi
