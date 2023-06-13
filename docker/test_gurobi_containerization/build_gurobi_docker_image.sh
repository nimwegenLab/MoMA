docker build . -t michaelmell/gurobi_image:v0.1 -f ./Dockerfile.gurobi

#docker run -it --mount type=bind,src=/home/micha/Documents/LicenseFiles/gurobi9.lic,target=/opt/gurobi/gurobi.lic michaelmell/gurobi_image:v0.1

docker push michaelmell/gurobi_image:v0.1