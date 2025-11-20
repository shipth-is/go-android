#!/bin/bash
set -euo pipefail

scriptDir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$scriptDir/.."

# Flags
skipClean=false
copyLocalLibs=false
bundletoolDefault="/opt/bundletool/bundletool.jar"
bundletoolPath="$bundletoolDefault"

for arg in "$@"; do
  case $arg in
    --skipClean) 
      skipClean=true 
      ;;
    --copyLocalLibs) 
      copyLocalLibs=true 
      ;;
    --bundletool=*) 
      bundletoolPath="${arg#*=}" 
      ;;
  esac
done

# Validate bundletool
if [[ -f "$bundletoolPath" ]]; then
  bundletoolCmd=(java -jar "$bundletoolPath")
elif command -v "$bundletoolPath" >/dev/null 2>&1; then
  bundletoolCmd=("$bundletoolPath")
else
  echo "bundletool not found at: $bundletoolPath"
  echo "Specify a custom path using: --bundletool=/path/to/bundletool.jar"
  exit 1
fi

if [ "$copyLocalLibs" = true ]; then
  echo "Copying local .aar libraries"
  cp "$scriptDir/../godroid-builder/godot-4.5/bin/"*.aar app/libs/ || true
fi

if [ "$skipClean" = false ]; then
  ./gradlew clean
fi

./gradlew bundleDebug

aabPath="app/build/outputs/bundle/debug/app-debug.aab"
apksPath="app/build/outputs/bundle/debug/app-debug.apks"

rm -f "$apksPath"

"${bundletoolCmd[@]}" build-apks \
  --local-testing \
  --bundle="$aabPath" \
  --output="$apksPath"

echo "Built APKS:"
echo "$apksPath"
