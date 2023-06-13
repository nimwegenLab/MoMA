#!/usr/bin/env bash

docker run -it --mount type=bind,src="/home/micha/Documents/LicenseFiles/gurobi_web_license.lic",target="/opt/gurobi/gurobi.lic" moma:v0.9.3 /bin/bash