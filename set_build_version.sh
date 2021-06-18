#!/bin/bash

set -e

GIT_SHA=${SOURCE_COMMIT:-'SNAPSHOT'}
GIT_SHA=${GIT_SHA:0:8}

PROJECT_VERSION=$(mvn -Dexec.executable='echo' -Dexec.args='${project.version}' --non-recursive exec:exec -q)

PROJECT_MAINTENANCE_VERSION=$(echo "$PROJECT_VERSION" | cut -f 1 -d '-')

NEW_PROJECT_VERSION="$PROJECT_MAINTENANCE_VERSION-$GIT_SHA"

echo "Setting project version to $NEW_PROJECT_VERSION"

mvn versions:set -DnewVersion="$NEW_PROJECT_VERSION" -q
