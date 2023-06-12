#!/usr/bin/env bash

source helpers.sh

IMAGE_TAG=$(get_image_tag)

echo "$IMAGE_TAG"

docker build .. -t "${IMAGE_TAG}"
