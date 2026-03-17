#!/usr/bin/env bash

set -euo pipefail

MICROGO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WORKFLOW_SHA="${1:-$(git -C "$MICROGO_ROOT" rev-parse HEAD)}"
SERVICE_REPOS=(
  "centralized-config"
  "discovery"
  "gateway"
  "location-rider"
  "location-saver"
  "ride-request"
)

for repo in "${SERVICE_REPOS[@]}"; do
  workflow_dir="$MICROGO_ROOT/$repo/.github/workflows"
  if [[ ! -d "$workflow_dir" ]]; then
    echo "Skipping $repo: no workflow directory found"
    continue
  fi

  while IFS= read -r -d '' workflow_file; do
    perl -0pi -e \
      "s|(uses:\\s+hendafarhani/microGo/\\.github/workflows/[^@\\s]+)@[a-f0-9]{40}|\\1@${WORKFLOW_SHA}|g" \
      "$workflow_file"
    echo "Updated $workflow_file"
  done < <(find "$workflow_dir" -maxdepth 1 -type f \( -name '*.yml' -o -name '*.yaml' \) -print0)
done

echo "Pinned reusable workflow references to $WORKFLOW_SHA"
