#!/bin/bash

set -e

MAVEN_BIN="$MAVEN_HOME/bin/mvn"
if [[ "$MAVEN_HOME" == "" ]]; then
  echo "[INFO] MAVEN_HOME variable is unset. Trying to use 'mvn' from the system path."
  MAVEN_BIN="mvn"
fi

GIT_SHA=${SOURCE_COMMIT:-'SNAPSHOT'}
GIT_SHA=${GIT_SHA:0:8}

GET_VERSION_CMD="$MAVEN_BIN -Dexec.executable='echo' -Dexec.args='\${project.version}' --non-recursive exec:exec -q"
PROJECT_VERSION=$(eval "$GET_VERSION_CMD")

PROJECT_MAINTENANCE_VERSION=$(echo "$PROJECT_VERSION" | cut -f 1 -d '-')

NEW_PROJECT_VERSION="$PROJECT_MAINTENANCE_VERSION-$GIT_SHA"

echo "Setting project version to $NEW_PROJECT_VERSION"

$MAVEN_BIN versions:set -DnewVersion="$NEW_PROJECT_VERSION" -q
