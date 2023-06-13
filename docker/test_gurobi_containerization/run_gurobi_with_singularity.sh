export GUROBI_HOME=/opt/gurobi/linux64
export LD_LIBRARY_PATH=$GUROBI_HOME/lib


rm -rf gurobi_image_v0.1.sif
singularity cache clean -f gurobi_image_v0.1.sif

singularity pull "docker://michaelmell/gurobi_image:v0.1"

# We use -e to not override container environment variable with host environment variables
singularity run --bind "/export/soft/source/g/Gurobi/gurobi.lic":"/opt/gurobi/gurobi.lic" -e ./gurobi_image_v0.1.sif
