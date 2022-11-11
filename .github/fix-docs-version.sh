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
update_file_versions ${VERSION} docs/index.md
update_file_versions ${VERSION} docs/guide/README.md
update_file_versions ${VERSION} docs/guide/distributed/test/pom.xml
