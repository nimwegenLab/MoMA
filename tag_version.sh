#!/bin/bash

version_tag=$1
version_regex="^v[0-9]*\.[0-9]*\.[0-9]*(\-(alpha[0-9]*|beta[0-9]*|[0-9a-f]*))*$"

# Function to handle common input pattern and error checking
function prompt_and_check {
  prompt="$1"
  read -r -p "$prompt (y/n): " answer
  if [[ "$answer" != "y" ]]; then
    printf "Aborting.\n"
    exit 1
  fi
}

if [[ ! "$version_tag" =~ $version_regex ]]; then
  printf "ERROR: Version string is invalid.\n"
  exit 1
fi

existing_versions_tags=$(git tag -l --sort=-v:refname | grep -E "^v[0-9]+\.[0-9]+\.[0-9]+$" | tr '\n' ' ')

if [[ "$existing_versions_tags" =~ (^|[[:space:]])"$version_tag"($|[[:space:]]) ]]; then
  printf "ERROR: Version tag already exists: %s\n" "${version_tag}"
  exit 1
fi

printf "\nPlease answer the following questions before tagging the new version:\n\n"
printf "Here is a list of existing version tags:\n\n%s\n\n" "$existing_versions_tags"

prompt_and_check "Do you want to continue with this version tag?" "$version_tag"
prompt_and_check "If you changed/added/removed new settings: Did you update the default settings (in: default_moma_configuration/mm.properties) for this new version?"
prompt_and_check "Did you update CHANGELOG.md for this new version ($version_tag)?"
prompt_and_check "Did you update README.md (if needed) for this new version ($version_tag)?"

printf "\nTagging version: %s\n" "$version_tag"
git tag -a "$version_tag"