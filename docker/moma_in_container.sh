#!/bin/bash
#[ -z "$MOMA_HOME" ] && { echo "Environment variable MOMA_HOME need to be set!"; echo; exit 1; }

# store script arguments to pass it to the executable
#ARGS=$@

# setup .moma directory in home-directory, if it does not exist
CONFIG_DIRECTORY=$HOME/.moma
if [ ! -d "$CONFIG_DIRECTORY" ]; then
  mkdir $CONFIG_DIRECTORY
  printf "Config directory did not exist: $CONFIG_DIRECTORY\nCreated it.\n"
fi

# copy default config file to home-directory, if it does not exist
CONFIG_FILE=$CONFIG_DIRECTORY/mm.properties
if [ ! -f "$CONFIG_FILE" ]; then
  cp "$MOMA_HOME/default_moma_configuration/mm.properties" $CONFIG_FILE
  printf "Config file did not exist: $CONFIG_FILE\nCreated it.\n"
fi

MOMA_TMP_WORK_DIR=$(mktemp -d -t moma.$(date +%Y%m%d-%H%M%S).XXXXX)
MOMA_TMP_WORK_DIR_TF="${MOMA_TMP_WORK_DIR}/tensorflow"
JAVA_TMPDIR="${MOMA_TMP_WORK_DIR}/java.io.tmpdir"
mkdir -p "${MOMA_TMP_WORK_DIR_TF}"
mkdir -p "${JAVA_TMPDIR}"
printf "MOMA_TMP_WORK_DIR: ${MOMA_TMP_WORK_DIR}\n"
printf "MOMA_TMP_WORK_DIR_TF: ${MOMA_TMP_WORK_DIR_TF}\n"
printf "JAVA_TMPDIR: ${JAVA_TMPDIR}\n"

#cd $MOMA_HOME
# Notes:
# 1. CLASSPATH is loaded by module load myGurobi
# 2. This contains the paths to the CUDA enabled TensorFlow Java libraries: /scicore/home/nimwegen/GROUP/Moma/MM_Analysis/unstable/Tools/Fiji.app/lib/linux64
#GUROBI_LIB_PATH=$GUROBI_HOME/lib/
#TF_JAVA_LIB_PATH=$MOMA_HOME/../Tools/Fiji.app/lib/linux64

#echo "CONFIG_DIRECTORY: $CONFIG_DIRECTORY"
#echo "TF_JAVA_LIB_PATH: $TF_JAVA_LIB_PATH"

#echo "java -Xmx32g -Djava.io.tmpdir=$TMPDIR -Djava.library.path=$GUROBI_LIB_PATH:$TF_JAVA_LIB_PATH -jar MoMA_fiji.jar $ARGS 2>moma_track.log"
#java -Xmx32g -Djava.io.tmpdir=$TMPDIR -Djava.library.path=$GUROBI_LIB_PATH:$TF_JAVA_LIB_PATH -jar MoMA_fiji.jar $ARGS 2>moma_track.log
#java -Xmx32g -Djava.library.path=$GUROBI_LIB_PATH:$TF_JAVA_LIB_PATH -jar MoMA_fiji.jar $ARGS 2>moma_track.log

MOMA_JAR_FILENAME=$(ls "${MOMA_JAR_PATH}"/*.jar | grep -P '^((?!original|tests|sources).)*.jar$' | grep -o '[^\/]*$')

#echo java -Xmx32g -Djava.library.path="${GUROBI_LIB_PATH}":"${TF_JAVA_LIB_PATH}" -jar "${MOMA_JAR_PATH}"/"${MOMA_JAR_FILENAME}" "$@"
#xvfb-run java -Xmx32g -Djava.library.path="${GUROBI_LIB_PATH}":"${TF_JAVA_LIB_PATH}" -jar "${MOMA_JAR_PATH}"/"${MOMA_JAR_FILENAME}" "$@"
if [[ -z "${DISPLAY}" ]]; then
#  xvfb-run java -Xmx32g -Djava.library.path="${GUROBI_LIB_PATH}":"${TF_JAVA_LIB_PATH}" -jar "${MOMA_JAR_PATH}"/"${MOMA_JAR_FILENAME}" "$@"
xvfb-run java -Xmx24g \
     -Djava.io.tmpdir=$JAVA_TMPDIR \
     -Djava.library.path=$GUROBI_LIB_PATH:$TF_JAVA_LIB_PATH \
     -Dimagej.tensorflow.models.dir="${MOMA_TMP_WORK_DIR_TF}" \
     -Djava.util.prefs.systemRoot="${MOMA_TMP_WORK_DIR}" \
     -Djava.util.prefs.userRoot="${MOMA_TMP_WORK_DIR}" \
     -jar  "${MOMA_JAR_PATH}"/"${MOMA_JAR_FILENAME}" \
     "$@"
else
#  java -Xmx32g -Djava.library.path="${GUROBI_LIB_PATH}":"${TF_JAVA_LIB_PATH}" -jar "${MOMA_JAR_PATH}"/"${MOMA_JAR_FILENAME}" "$@"
java -Xmx24g \
     -Djava.io.tmpdir=$JAVA_TMPDIR \
     -Djava.library.path=$GUROBI_LIB_PATH:$TF_JAVA_LIB_PATH \
     -Dimagej.tensorflow.models.dir="${MOMA_TMP_WORK_DIR_TF}" \
     -Djava.util.prefs.systemRoot="${MOMA_TMP_WORK_DIR}" \
     -Djava.util.prefs.userRoot="${MOMA_TMP_WORK_DIR}" \
     -jar  "${MOMA_JAR_PATH}"/"${MOMA_JAR_FILENAME}" \
     "$@"
fi

#cd -
