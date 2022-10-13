#!/bin/bash

session_type=$1
session_branch=$2

if [[ ! $session_type ]]; then
  printf "No session type proved.\n"
  exit 1
fi

if [[ ! $session_branch ]]; then
  printf "No session branch name provided.\n"
  exit 1
fi


allowed_session_types=("feature" "bugfix" "exploration")

if [[ ! " ${allowed_session_types[*]} " =~ " ${session_type} " ]]; then
  printf "The provided session type \"%s\" is invalid. Allowed values are: ${allowed_session_types[*]}\n" "${session_type}"
  exit 1
fi

#if exists_in_list "$allowed_session_types" " " "$session_type"; then
#fi

printf "Session type: %s\n" "$session_type"
printf "Session branch: %s\n" "$session_branch"
