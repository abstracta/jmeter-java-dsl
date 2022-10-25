#!/bin/bash
#
# This script solves next minor version
set -eo pipefail

VERSION="$1"
MAJOR="${VERSION%%.*}"
VERSION="${VERSION#*.}"
MINOR="${VERSION%%.*}"
echo "${MAJOR}.$((MINOR + 1))-SNAPSHOT"
