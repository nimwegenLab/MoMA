#!/usr/bin/env bash

source helpers.sh

IMAGE_TAG=$(get_image_tag)

echo "$IMAGE_TAG"

docker build .. -t "${IMAGE_TAG}"

#docker run -it --mount type=bind,src="/home/micha/Documents/LicenseFiles/gurobi_web_license.lic",target="/opt/gurobi/gurobi.lic" moma:v0.9.3 /bin/bash