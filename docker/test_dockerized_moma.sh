#!/bin/bash

base_path="/home/micha/Documents/01_work/15_moma_notes/02_moma_development/feature/20230612-containerize-moma"
./moma -p "${base_path}/mm.properties" -tmax 201 -analysis slurm_test_3 -headless -trackonly -i "${base_path}/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif"
