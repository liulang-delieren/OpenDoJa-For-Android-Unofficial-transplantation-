#!/usr/bin/env bash
set -euo pipefail

TARGET_DIR="${1:-target}"
ARTIFACT_PREFIX="${2:-opendoja-}"

matches=()
while IFS= read -r -d '' match; do
  matches+=("$match")
done < <(find "$TARGET_DIR" -maxdepth 1 -type f -name "${ARTIFACT_PREFIX}*.jar" ! -name 'original-*.jar' -print0 | sort -z)

if [ "${#matches[@]}" -ne 1 ]; then
  echo "expected exactly one release jar in $TARGET_DIR, found ${#matches[@]}" >&2
  printf '%s\n' "${matches[@]}" >&2
  exit 1
fi

printf '%s\n' "${matches[0]}"
