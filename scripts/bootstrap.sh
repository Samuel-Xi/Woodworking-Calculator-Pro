#!/usr/bin/env bash
# One-time bootstrap: downloads a stand-alone Gradle distribution and uses it
# to generate `gradle/wrapper/gradle-wrapper.jar`. Run from project root:
#
#   bash scripts/bootstrap.sh
#
# Afterwards, you can build with `./gradlew assembleDebug` directly.
set -euo pipefail

GRADLE_VERSION="8.9"
DIST_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
CACHE_DIR=".gradle-bootstrap"
ZIP_PATH="${CACHE_DIR}/gradle.zip"
GRADLE_HOME="${CACHE_DIR}/gradle-${GRADLE_VERSION}"

if [ -f "gradle/wrapper/gradle-wrapper.jar" ]; then
  echo "✓ gradle-wrapper.jar already exists. Nothing to do."
  exit 0
fi

mkdir -p "$CACHE_DIR"

if [ ! -d "$GRADLE_HOME" ]; then
  echo "▸ Downloading Gradle ${GRADLE_VERSION}…"
  curl -fL --retry 3 -o "$ZIP_PATH" "$DIST_URL"
  echo "▸ Extracting…"
  unzip -q "$ZIP_PATH" -d "$CACHE_DIR"
fi

echo "▸ Generating wrapper jar via 'gradle wrapper'…"
"$GRADLE_HOME/bin/gradle" wrapper --gradle-version "$GRADLE_VERSION" --distribution-type bin --quiet

echo "✓ Wrapper jar ready. Try:  ./gradlew assembleDebug"
