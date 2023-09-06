#!/bin/bash

version_tag=$1

version_regex="^v[0-9]*\.[0-9]*\.[0-9]*(\-(alpha[0-9]*|beta[0-9]*|[0-9a-f]*))*$"

if [[ ! "$version_tag" =~ $version_regex ]]; then
  printf "ERROR: Version sting is invalid.\n"
  exit 1
fi

# generate list of version tags sorted by version number and print it on a single line:
existing_versions_tags=$(git tag -l --sort=-v:refname | grep -E "^v[0-9]+\.[0-9]+\.[0-9]+$" | tr '\n' ' ')

# Abort if the version tag alread exists in existing_versions_tags:
if [[ "$existing_versions_tags" =~ (^|[[:space:]])"$version_tag"($|[[:space:]]) ]]; then
  printf "ERROR: Version tag already exists: %s\n" "${version_tag}"
  exit 1
fi

printf "\n"
printf "Please answer the following questions before tagging the new version:\n"
printf "\n"

printf "Here is a list of existing version tags:\n"

printf "%s\n" "$existing_versions_tags"
printf "\n"
printf "Do you want to continue with this version tag? (y/n)\n"
printf "   %s\n" "$version_tag"
read -r answer
if [[ "$answer" != "y" ]]; then
  printf "Aborting.\n"
  exit 1
fi

printf "Did you update CHANGELOG.md for this new version (%s)? (y/n)\n" "${version_tag}"
read -r answer
if [[ "$answer" != "y" ]]; then
  printf "Aborting.\n"
  exit 1
fi
printf "\n"

printf "\n"
printf "Tagging version: %s\n" "$version_tag"

git tag -a "$version_tag"
