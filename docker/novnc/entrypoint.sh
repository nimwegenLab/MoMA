#!/bin/bash
set -ex

RUN_FLUXBOX=${RUN_FLUXBOX:-yes}
RUN_XTERM=${RUN_XTERM:-yes}

case $RUN_FLUXBOX in
  false|no|n|0)
    rm -f /app/conf.d/fluxbox.conf
    ;;
esac

case $RUN_XTERM in
  false|no|n|0)
    rm -f /app/conf.d/xterm.conf
    ;;
esac

#exec supervisord -c /app/supervisord.conf
#supervisord -c /app/supervisord.conf &

Xvfb :0 -screen 0 "%(ENV_DISPLAY_WIDTH)s"x"%(ENV_DISPLAY_HEIGHT)s"x24 -listen tcp -ac &
websockify --web /usr/share/novnc 8080 localhost:5900 &
x11vnc -forever -shared &
fluxbox &

echo "Starting MoMA" > /data/moma.log

## Run MoMA
#exec /moma/moma -p /data/mm.properties -i /data/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif

MOMA_JAR_FILENAME=$(ls "${MOMA_JAR_PATH}"/*.jar | grep -P '^((?!original|tests|sources).)*.jar$' | grep -o '[^\/]*$')

#echo java -Xmx32g -Djava.library.path="${GUROBI_LIB_PATH}":"${TF_JAVA_LIB_PATH}" -jar "${MOMA_JAR_PATH}"/"${MOMA_JAR_FILENAME}" "$@"
java -Xmx32g -Djava.library.path="${GUROBI_LIB_PATH}":"${TF_JAVA_LIB_PATH}" -jar "${MOMA_JAR_PATH}"/"${MOMA_JAR_FILENAME}" "$@"
