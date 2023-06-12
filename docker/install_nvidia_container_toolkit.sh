#!/usr/bin/env bash

###
# This script installs the nvidia container toolkit on the host machine.
# - Instructions found here: https://www.howtogeek.com/devops/how-to-use-an-nvidia-gpu-with-docker-containers/
# - For list of supported distributions, see: https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/install-guide.html
###

#distribution=$(. /etc/os-release;echo $ID$VERSION_ID) \
distribution="ubuntu18.04" \
   && curl -s -L https://nvidia.github.io/nvidia-docker/gpgkey | sudo apt-key add - \
   && curl -s -L https://nvidia.github.io/nvidia-docker/$distribution/nvidia-docker.list | sudo tee /etc/apt/sources.list.d/nvidia-docker.list

sudo apt-get update
sudo apt-get install -y nvidia-docker2
sudo systemctl restart docker

