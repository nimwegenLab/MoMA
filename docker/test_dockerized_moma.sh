#!/bin/bash

export CONTAINER_TAG="michaelmell/moma:v0.9.6"
export GRB_LICENSE_FILE="/home/micha/Documents/LicenseFiles/gurobi_web_license.lic"
base_path="/home/micha/Documents/01_work/15_moma_notes/02_moma_development/feature/20230612-containerize-moma"

# corresponds to command inside container:
# /moma/moma_in_container.sh -p /home/micha/Documents/01_work/15_moma_notes/02_moma_development/feature/20230612-containerize-moma/mm.properties -tmax 10 -analysis slurm_test_3 -headless -trackonly -i /home/micha/Documents/01_work/15_moma_notes/02_moma_development/feature/20230612-containerize-moma/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif
# xvfb-run /moma/moma_in_container.sh -p /home/micha/Documents/01_work/15_moma_notes/02_moma_development/feature/20230612-containerize-moma/mm.properties -tmax 10 -analysis slurm_test_3 -headless -trackonly -i /home/micha/Documents/01_work/15_moma_notes/02_moma_development/feature/20230612-containerize-moma/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif

# run moma without arguments
#./moma

# run moma with help argument
#./moma -help

# command for testing the "TRACK" stage of the batch-run workflow:
export DISPLAY=""
#moma -p "${base_path}/mm.properties" -f -tmax 10 -analysis slurm_test_3 -headless -trackonly -i "${base_path}/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif"
moma -f -tmax 10 -analysis slurm_test_3 -headless -trackonly -i "${base_path}/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif"

# command for testing the "TRACK" stage of the batch-run workflow with long arguments:
#export DISPLAY=""
#./moma --props "${base_path}/mm.properties" -f -tmax 10 -analysis slurm_test_3 -headless -trackonly --infolder "${base_path}/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif"

# command for testing the "CURATE" stage of the batch-run workflow:
#./moma -p "${base_path}/mm.properties" -f -tmax 10 -analysis slurm_test_3 -rl "${base_path}"
