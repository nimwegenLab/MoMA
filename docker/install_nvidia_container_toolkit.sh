#!/usr/bin/env bash

###
# This script installs the nvidia container toolkit on the host machine.
# - Code is based on instructions found here: https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/install-guide.html#installation-guide
# - For list of supported distributions, see: https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/install-guide.html
# - Note: For unsupported distributions, you can try setting the distribution variable to an earlier version and see if it works (e.g. setting "ubuntu18.04" worked for Ubuntu 22.10). Note: "ubuntu20.04" and "ubuntu22.04" are symbolic links to the "ubuntu18.04" according to: https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/install-guide.html
###

#distribution="ubuntu22.04" \
distribution=$(. /etc/os-release;echo $ID$VERSION_ID) \
      && curl -fsSL https://nvidia.github.io/libnvidia-container/gpgkey | sudo gpg --dearmor -o /usr/share/keyrings/nvidia-container-toolkit-keyring.gpg \
      && curl -s -L https://nvidia.github.io/libnvidia-container/$distribution/libnvidia-container.list | \
            sed 's#deb https://#deb [signed-by=/usr/share/keyrings/nvidia-container-toolkit-keyring.gpg] https://#g' | \
            sudo tee /etc/apt/sources.list.d/nvidia-container-toolkit.list

sudo apt-get update
sudo apt-get install -y nvidia-container-toolkit
sudo nvidia-ctk runtime configure --runtime=docker
sudo systemctl restart docker
