#!/usr/bin/env bash

function get_version() {
  # Check if the current commit is tagged with a version number. If not return "dev". If yes return the version number.
  # Fail, if it is tagged with an non-version tag.
  if [[ $(git describe) =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    # catch strings like e.g.: v0.9.7
    git describe
  elif [[ $(git describe) =~ ^v[0-9]+\.[0-9]+\.[0-9]+-[0-9]+-.*$ ]]; then
    # catch strings like e.g.: v0.9.7-5-g46d4d956
    echo "dev"
  else
    # fail for other values
    exit 1
  fi
}

version=$(get_version)

if [ $? -ne 0 ]; then
    echo "ERROR: Unknown tag: $(git describe)" >&2
    exit 1
fi

source ../env_vars.sh

IMAGE_TAG="${CONTAINER_NAMESPACE}/moma:${version}"

echo "Building image: ${IMAGE_TAG}"
docker build .. --build-arg="GRB_VERSION=${GRB_VERSION}" --build-arg="GRB_SHORT_VERSION=${GRB_SHORT_VERSION}" -t "${IMAGE_TAG}"
