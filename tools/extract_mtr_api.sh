#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
MTR_DIR="$REPO_ROOT/libs/mtr3"
TEMP_ROOT="$REPO_ROOT/build/mtr-api-temp"

rm -rf "$TEMP_ROOT"
mkdir -p "$TEMP_ROOT"

for jar in "$MTR_DIR"/MTR-*.jar; do
  base="$(basename "$jar" .jar)"
  if [[ "$base" == *-slim ]]; then
    echo "Skipping existing slim jar: $base"
    continue
  fi

  slim_jar="$MTR_DIR/${base}-slim.jar"
  temp_dir="$TEMP_ROOT/$base"
  rm -rf "$temp_dir"
  mkdir -p "$temp_dir"

  echo "Extracting API from $base"
  (cd "$temp_dir" && jar xf "$jar" mtr/data >/dev/null 2>&1)

  if [[ ! -d "$temp_dir/mtr/data" ]]; then
    echo "Warning: $jar contains no mtr/data, creating empty jar" >&2
    (cd "$temp_dir" && jar cf "$slim_jar" >/dev/null)
  else
    (cd "$temp_dir" && jar cf "$slim_jar" mtr/data >/dev/null)
  fi

  rm -f "$jar"
  echo "Created slim jar: $(basename "$slim_jar")"
done

rm -rf "$TEMP_ROOT"
echo "All slim jars generated in $MTR_DIR"
