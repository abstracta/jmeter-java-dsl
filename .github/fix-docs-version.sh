#!/bin/bash
#
# This script takes care of setting proper jmeter-java-dsl version in docs.
#
set -eo pipefail

VERSION=$1

update_file_versions() {
  local VERSION="$1"
  local FILE="$2"
  sed -i "s/jmeter-java-dsl\([^ :]*\):[0-9.]\+/jmeter-java-dsl\1:${VERSION}/g" "${FILE}"
  sed -i "/jmeter-java-dsl.*<\/artifactId>$/{N;s/jmeter-java-dsl\(.*\)<\/artifactId>\n\( *\)<version>.*<\/version>/jmeter-java-dsl\1<\/artifactId>\n\2<version>${VERSION}<\/version>/}" "${FILE}"
}

update_file_versions ${VERSION} README.md

find docs -name "*.md" -o -name "*.xml" -not -path "*/node_modules/*" | while read DOC_FILE; do
  update_file_versions ${VERSION} ${DOC_FILE}
done