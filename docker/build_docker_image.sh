#!/usr/bin/env bash

source helpers.sh

IMAGE_TAG=$(get_image_tag)

echo "$IMAGE_TAG"

#docker build --target moma_builder -t moma_builder_image  ..

#docker build -t "${IMAGE_TAG}" ..

docker build -t "moma:v0.9.3" --no-cache --progress=plain .. 2>&1 | tee build.log


#docker run -it --mount type=bind,src="/home/micha/Documents/LicenseFiles/gurobi_web_license.lic",target="/opt/gurobi/gurobi.lic" moma:v0.9.3 /bin/bash