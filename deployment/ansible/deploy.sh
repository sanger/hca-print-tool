#!/bin/bash

ANSIBLE_DIR="$(dirname "$(realpath "$0")")"
cd $ANSIBLE_DIR

PLAYBOOK=hcaprint.yml
INVENTORY=hosts

command="ansible-playbook $PLAYBOOK -i $INVENTORY $@"
echo $command
$command
