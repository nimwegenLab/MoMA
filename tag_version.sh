#!/bin/bash

version_tag=$1

version_regex="^v[0-9]*\.[0-9]*\.[0-9]*(\-(alpha[0-9]*|beta[0-9]*|[0-9a-f]*))*$"

if [[ ! "$version_tag" =~ $version_regex ]]; then
  printf "ERROR: Version sting is invalid.\n"
  exit 1
fi

git tag -a "$version_tag"
