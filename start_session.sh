#!/bin/bash

session_type=$1
session_name=$2


if [[ ! $session_type ]]; then
  printf "ERROR: No session type proved.\n"
  exit 1
fi

if [[ ! $session_name ]]; then
  printf "ERROR: No session branch name provided.\n"
  exit 1
fi

allowed_session_types=("feature" "bugfix" "exploration")

if [[ ! " ${allowed_session_types[*]} " =~ " ${session_type} " ]]; then
  printf "ERROR: The provided session type \"%s\" is invalid. Allowed values are: ${allowed_session_types[*]}\n" "${session_type}"
  exit 1
fi

if [[ "$session_name" =~ \/ ]]; then
  printf "ERROR: The provided session branch \"%s\" is invalid. The branch name should not contain a slash (i.e. '/') or sub-branch (I will add it for you).\n" "$session_name"
  exit 1
fi

full_topic_branch_name="$session_type"/"$session_name"

printf "Session type:\n\t%s\n" "$session_type"
printf "Session name:\n\t%s\n" "$session_name"

# TODO: Uncomment this, when development has finished!
#if [[ $(git diff --stat) != '' ]]; then
#  printf 'Cannot start a new session: The working tree is dirty. Please commit changes first.\n'
#  exit 1
#fi

test_folder_path="/home/micha/Documents/01_work/git/MoMA/src/test/java/com/jug"
#"${foo^}"
topic_class_name="${session_type^}"__"$session_name"
topic_class_name="${topic_class_name//-/_}" # this replaces occurences of "-" with "_"
topic_class_path="$test_folder_path"/"$session_type"/"$topic_class_name".java
devel_data_folder="/home/micha/Documents/01_work/15_moma_notes/02_moma_development"
topic_data_template_folder="$devel_data_folder/00_test_datasets/gl_data_1_template"
template_class_path="/home/micha/Documents/01_work/git/MoMA/src/test/java/com/jug/TEMPLATE_CLASS_FOR_INTERACTIVE_TESTING.java"

printf "Starting ${session_type} session on branch:\n\t%s\n" "$full_topic_branch_name"

echo git branch "$full_topic_branch_name"# TODO: Remove echo, when development has finished!
echo git checkout "$full_topic_branch_name"# TODO: Remove echo, when development has finished!

#BUGFIX_BRANCH_NAME="${FULL_BRANCH_NAME/bugfix\//}"
topic_branch_data_folder="$devel_data_folder"/"$session_type"/"$session_name"
image_file_name="20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif"
config_file_name="mm.properties"

mkdir -p "$topic_branch_data_folder"
cp -P "$topic_data_template_folder/$config_file_name" "$topic_branch_data_folder/$config_file_name"
ln -f -r -s "$topic_data_template_folder/$image_file_name" "$topic_branch_data_folder/$image_file_name"

mkdir -p "$topic_branch_data_folder"
#echo "$topic_class_path"
cp "$template_class_path" "$topic_class_path"
sed -i "s/TEMPLATE_CLASS_FOR_INTERACTIVE_TESTING/$topic_class_name/g" "$topic_class_path"

sed -i "s|TEMPLATE::BASE_PATH_TO_FOLDER_WITH_TEST_DATASETS|${devel_data_folder}|g" "$topic_class_path"
#git add "$class_folder/$topic_branch_test_class.java"

#echo "${session_type^}"

exit

printf "The data folder for this debug session is:\n\t%s\n" "$topic_branch_data_folder"
printf "The class for this debug session is:\n\t%s\n" "$topic_branch_test_class"

#git checkout "feature/20221013-add-script-to-generate-a-topic-session"  # TODO: Remove this, when development has finished!