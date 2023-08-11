#!/usr/bin/env bash

source helpers.sh

#IMAGE_TAG=$(get_image_tag)
IMAGE_TAG="michaelmell/moma:v0.9.6"

echo "$IMAGE_TAG"

docker build .. -t "${IMAGE_TAG}"

#docker tag 1b7127033a75 michaelmell/moma:v0.9.6
#docker push michaelmell/moma:v0.9.6

#docker run -it --mount type=bind,src="/home/micha/Documents/LicenseFiles/gurobi_web_license.lic",target="/opt/gurobi/gurobi.lic" moma:v0.9.3 /bin/bash