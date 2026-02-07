#!/bin/bash
set -euo pipefail

PROPS=$(./gradlew properties --quiet --console=plain)
NAME=$(echo "$PROPS" | grep '^pluginName: ' | cut -f2- -d ' ')
VERSION=$(echo "$PROPS" | grep '^version: ' | cut -f2- -d ' ')

# Assumes buildPlugin produces ${pluginName}-${version}.zip (the default archive name).
echo "filename=${NAME}-${VERSION}.zip"
echo "version=${VERSION}"
