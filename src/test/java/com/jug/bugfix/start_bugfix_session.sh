#!/bin/bash

# IMPORTANT: Switch to the debug branch before running this script.

BUGFIX_DATA="/home/micha/Documents/01_work/15_moma_notes/02_moma_development/bugfix"
BUGFIX_CLASS_FOLDER="/home/micha/Documents/01_work/git/MoMA/src/test/java/com/jug/bugfix"

FULL_BRANCH_NAME=$(git branch --show-current)

if [[ ! $FULL_BRANCH_NAME == *"bugfix/"* ]]; then
  printf "ABORTING: You are not on a bugfix branch (i.e. a branch starting 'bugfix/').\n"
  exit
fi

printf "Starting debug session for branch:\n\t%s\n" "$FULL_BRANCH_NAME"

BUGFIX_BRANCH_NAME="${FULL_BRANCH_NAME/bugfix\//}"
DEBUG_CLASS_NAME="Bugfix__${BUGFIX_BRANCH_NAME//-/_}" # this replaces occurences of "-" with "_"
DEBUG_DATA_FOLDER="$BUGFIX_DATA/$BUGFIX_BRANCH_NAME"

cp "$BUGFIX_DATA/000__debug_template/"* "$DEBUG_DATA_FOLDER"
cp "$BUGFIX_CLASS_FOLDER/Bugfix__TEMPLATE.java" "$BUGFIX_CLASS_FOLDER/$DEBUG_CLASS_NAME.java"
sed -i "s/Bugfix__TEMPLATE/$DEBUG_CLASS_NAME/g" "$BUGFIX_CLASS_FOLDER/$DEBUG_CLASS_NAME.java"
sed -i "s/000__debug_template/$BUGFIX_BRANCH_NAME/g" "$BUGFIX_CLASS_FOLDER/$DEBUG_CLASS_NAME.java"

printf "The data folder for this debug session is:\n\t%s\n" "$DEBUG_DATA_FOLDER"
printf "The class for this debug session is:\n\t%s\n" "$DEBUG_CLASS_NAME"
