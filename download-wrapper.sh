#!/usr/bin/env sh
set -eu

WRAPPER_DIR="$(dirname "$0")/gradle/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/gradle-wrapper.jar"

mkdir -p "$WRAPPER_DIR"

if [ -f "$WRAPPER_JAR" ]; then
  echo "gradle-wrapper.jar ja existe."
  exit 0
fi

URL="https://raw.githubusercontent.com/gradle/gradle/v8.8.0/gradle/wrapper/gradle-wrapper.jar"

if command -v curl >/dev/null 2>&1; then
  curl -L "$URL" -o "$WRAPPER_JAR"
elif command -v wget >/dev/null 2>&1; then
  wget -O "$WRAPPER_JAR" "$URL"
else
  echo "Precisa de curl ou wget para baixar o wrapper."
  exit 1
fi

echo "Wrapper baixado com sucesso."
