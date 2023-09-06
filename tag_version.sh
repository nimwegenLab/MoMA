#!/bin/bash

version_tag=$1

version_regex="^v[0-9]*\.[0-9]*\.[0-9]*(\-(alpha[0-9]*|beta[0-9]*|[0-9a-f]*))*$"

if [[ ! "$version_tag" =~ $version_regex ]]; then
  printf "ERROR: Version sting is invalid.\n"
  exit 1
fi

printf "\n"
printf "Please answer the following questions before tagging the new version:\n"
printf "\n"

printf "Did you update CHANGELOG.md for the new version? (y/n)\n"
read -r answer
if [[ "$answer" != "y" ]]; then
  printf "Aborting.\n"
  exit 1
fi
printf "\n"

printf "Here is a list of existing version tags:\n"

# generate list of version tags sorted by version number and print it on a single line:
version_tag_list=$(git tag -l --sort=-v:refname | grep -E "^v[0-9]+\.[0-9]+\.[0-9]+$" | tr '\n' ' ')

printf "%s\n" "$version_tag_list"
printf "\n"
printf "Is the version tag correct? (y/n)\n"
printf "   %s\n" "$version_tag"
read -r answer
if [[ "$answer" != "y" ]]; then
  printf "Aborting.\n"
  exit 1
fi

printf "\n"
printf "Tagging version: %s\n" "$version_tag"

git tag -a "$version_tag"
