#!/bin/bash
#
# This script solves artifacts to be uploaded to GH release
set -eo pipefail

RET=$(find . -iname '*.jar' -and -not -iname '*-sources.jar' -and -not -iname '*-tests.jar' -and -not -iname 'original-*.jar' | awk '{printf $0","}')
echo "${RET%?}"
