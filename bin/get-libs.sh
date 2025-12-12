#!/bin/bash

set -euo pipefail

scriptDir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

versions="3.x 4.0 4.1 4.2 4.3 4.4 4.5"

# loop through versions and copy .aar files
for version in $versions; do
  builtDir="$scriptDir/../../godroid-builder/godot-$version/bin"

  if [ ! -d "$builtDir" ]; then
    echo "WARNING: Built directory not found: $builtDir"
    continue
  fi

  destDir="$scriptDir/../app/libs"

  cp "$builtDir/godot-lib.template_release.aar" "$destDir/godot-lib-v$version.template_release.aar"
  cp "$builtDir/godot-lib.template_debug.aar" "$destDir/godot-lib-v$version.template_debug.aar"
  echo "Copied $version .aar libraries to $destDir"

done