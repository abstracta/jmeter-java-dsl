#!/bin/bash
#
# This script solves artifacts to be uploaded to GH release
set -eo pipefail

RET=$(find . -iname '*.jar' -and -not -iname '*-sources.jar' -and -not -iname '*-javadoc.jar' -and -not -iname '*-tests.jar' -and -not -iname 'original-*.jar' -and -not -iname '*-jmdsl-*.jar' -or -type d -name dependency -prune -and -not -iname 'dependency' | awk '{printf $0","}')
echo "${RET%?},./jmeter-java-dsl-cli/src/main/json/jmdsl-config-schema.json"
