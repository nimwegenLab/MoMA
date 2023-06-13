#!/usr/bin/env bash

docker run --rm -it \
 --mount type=bind,src="/home/micha/Documents/LicenseFiles/gurobi_web_license.lic",target="/opt/gurobi/gurobi.lic" \
 --mount type=bind,src="$HOME/.moma",target="/root/.moma" \
 --mount type=bind,src="/home/micha/Documents/01_work/15_moma_notes/02_moma_development/feature/20230612-containerize-moma",target="/data" \
moma:v0.9.3 /bin/bash

./moma -p /data/mm.properties -i /data/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif