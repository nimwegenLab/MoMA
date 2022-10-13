#!/bin/bash

session_type=$1
session_branch=$2

if [[ ! $session_type ]]; then
  printf "ERROR: No session type proved.\n"
  exit 1
fi

if [[ ! $session_branch ]]; then
  printf "ERROR: No session branch name provided.\n"
  exit 1
fi

allowed_session_types=("feature" "bugfix" "exploration")

if [[ ! " ${allowed_session_types[*]} " =~ " ${session_type} " ]]; then
  printf "ERROR: The provided session type \"%s\" is invalid. Allowed values are: ${allowed_session_types[*]}\n" "${session_type}"
  exit 1
fi

if [[ "$session_branch" =~ \/ ]]; then
  printf "ERROR: The provided session branch \"%s\" is invalid. The branch name should not contain a slash (i.e. '/') or sub-branch (I will add it for you).\n" "$session_branch"
  exit 1
fi

session_branch="$session_type"/"$session_branch"

printf "Session type: %s\n" "$session_type"
printf "Session branch: %s\n" "$session_branch"

if [[ $(git diff --stat) != '' ]]; then
  printf 'Cannot start a new session: The working tree is dirty. Please commit changes first.\n'
  exit 1
fi


git branch "$session_branch"
git checkout "$session_branch"

git checkout "feature/20221013-add-script-to-generate-a-topic-session"