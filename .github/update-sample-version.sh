#!/bin/bash
#
# This script takes care of updating jmeter-java-dsl version in sample project.
#
set -eox pipefail

VERSION=$1

USER_EMAIL="$(git log --format='%ae' HEAD^!)"
USER_NAME="$(git log --format='%an' HEAD^!)"

cd jmeter-java-dsl-sample
sed -i "/jmeter-java-dsl<\/artifactId>$/{N;s/jmeter-java-dsl<\/artifactId>\n      <version>.*<\/version>/jmeter-java-dsl<\/artifactId>\n      <version>${VERSION}<\/version>/}" pom.xml
git add .
git config --local user.email "$USER_EMAIL"
git config --local user.name "$USER_NAME"
git commit -m "Updated jmeter-java-dsl version"
git push origin HEAD:master
cd ..
rm -rf jmeter-java-dsl-sample
