#! /usr/bin/env python3

import os
import subprocess
import sys
from shutil import which
import configparser
from pathlib import Path

def is_tool(name):
    """Check whether `name` is on PATH and marked as executable."""
    return which(name) is not None


def get_bind_mount_arg(path, container_engine):
    if container_engine == "singularity":
        return f'--bind {get_directory_path(path)}:{get_directory_path(path)}'
    elif container_engine == "docker":
        return f'--mount type=bind,src={get_directory_path(path)},target={get_directory_path(path)}'
    else:
        raise ValueError(f"ERROR: Invalid containerization value: {container_engine}")


def get_directory_path(target_path):
    """This function takes the path of a file or directory. If file-path is passed it returns the path to the parent
    directory. If a directory is passed, it returns the path to the directory itself."""
    if not os.path.exists(target_path):
        print(f"ERROR: Path does not exist: {target_path}", file=sys.stderr)
        sys.exit(1)
    if os.path.isdir(target_path):
        return Path(target_path)
    elif os.path.isfile(target_path):
        return Path(os.path.dirname(target_path))


def parse_segmentation_model_path(file_path):
    with open(file_path, 'r') as file:
        lines = file.readlines()

    for line in lines:
        if line.strip().startswith('SEGMENTATION_MODEL_PATH='):
            return line.strip().split('=')[1]

    return None


def get_mount_paths_from_args(args):
    path_args = ["-i", "--infolder", "-infolder", "-o", "--outfolder", "-outfolder", "-p", "--props", "-props", "-rl", "--reload", "-reload"]

    mount_paths = []
    for ind, arg in enumerate(args):
        if arg in path_args:
            mount_paths += [get_directory_path(args[ind+1])]
    return mount_paths


def process_args(args, container_engine):
    path_args = ["-i", "--infolder", "-infolder", "-o", "--outfolder", "-outfolder", "-p", "--props", "-props", "-rl", "--reload", "-reload"]
    mount_string = ""

    for ind, arg in enumerate(args):
        if arg in path_args:
            path = args[ind+1]
            current_path = get_directory_path(path)
            if current_path not in mount_string:
                mount_string += f" {get_bind_mount_arg(current_path, container_engine)}"

            if arg in ["-p", "--props"]:
                properties_path = args[ind+1]
                segmentation_model_path = parse_segmentation_model_path(properties_path)
                if segmentation_model_path and segmentation_model_path not in mount_string:
                    mount_string += f" {get_bind_mount_arg(segmentation_model_path, container_engine)}"

    return mount_string


def is_parent_path(parent_path, child_path):
    parent = Path(parent_path)
    child = Path(child_path)

    return child.is_relative_to(parent) and not child == parent


def get_top_level_paths(paths: list[Path]):
    top_level_paths = set(paths.copy())
    for path1 in paths:
        for path2 in paths:
            if is_parent_path(path1, path2) and path2 in top_level_paths:
                top_level_paths.remove(path2)
    return list(top_level_paths)


def build_mount_args(mount_paths: list[Path], container_engine: str):
    mount_args = []
    for path in mount_paths:
        mount_args += [get_bind_mount_arg(path, container_engine)]
    return mount_args


if __name__ == "__main__":
    args = sys.argv[1:]

    headless_option = False
    if "-headless" in args:
        headless_option = True

    if not os.environ.get('DISPLAY') and not headless_option:
        print("ERROR: Running non-headless (i.e. without option '-headless'), but no display is available (i.e. DISPLAY is not set).\n")
        sys.exit(1)

    if is_tool("singularity"):
        container_engine = "singularity"
    elif is_tool("docker"):
        container_engine = "docker"
    else:
        print("ERROR: No supported containerization tool found. Please install Docker or Singularity.\n")
        sys.exit(1)

    # mount_string = process_args(args, container_engine)
    mount_paths = get_mount_paths_from_args(args)

    # Add path to license file to mount options.
    grb_license_file = os.environ.get("GRB_LICENSE_FILE")
    if grb_license_file:
        # mount_string += f" {get_bind_mount_arg(grb_license_file, container_engine)}"
        mount_paths += [get_directory_path(grb_license_file)]
    else:
        print("ERROR: Could not determine path to Gurobi license file. Variable not set: GRB_LICENSE_FILE")
        exit(1)

    # Add home-directory path to mount options to access ~/.moma directory.
    home_directory = os.environ.get("HOME")
    if home_directory:
        # mount_string += f" {get_bind_mount_arg(home_directory, container_engine)}"
        mount_paths += [get_directory_path(home_directory)]
    else:
        print("ERROR: Could not determine home-directory path. Variable not set: HOME")
        exit(1)

    print(mount_paths)
    mount_paths = get_top_level_paths(mount_paths)
    mount_args = build_mount_args(mount_paths, container_engine)

    if container_engine == "singularity":
        print("Using Singularity.")
        singularity_container_file_path = os.environ.get("SINGULARITY_CONTAINER_FILE_PATH")
        subprocess.run(["singularity", "run"] + mount_args + [singularity_container_file_path] + args)
    elif container_engine == "docker":
        print("Using Docker.")
        headless_option = ""  # Replace with the actual headless_option value
        if not headless_option:  # option '-headless' not provided; running with GUI
            x_forwarding_options = ["--net=host", "--env=DISPLAY", f"--volume={os.path.expanduser('~')}/.Xauthority:/root/.Xauthority:rw"]
        else:
            x_forwarding_options = []
        container_tag = os.environ.get("CONTAINER_TAG")
        print(f"CONTAINER_TAG: {container_tag}")
        gurobi_license_file = os.environ.get('GRB_LICENSE_FILE')
        arr = ["docker", "run", "-it", "--rm", f"--user={os.getuid()}:{os.getgid()}", "--env=HOME", f"--env=GRB_LICENSE_FILE={gurobi_license_file}", *x_forwarding_options, *mount_args, container_tag, *args]
        print(" ".join(arr))
        subprocess.run(arr)
