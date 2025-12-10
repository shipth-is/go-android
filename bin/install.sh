#!/bin/bash
set -euo pipefail

scriptDir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$scriptDir/.."

apksFile=""
bundletoolDefault="/opt/bundletool/bundletool.jar"
bundletoolPath="$bundletoolDefault"

for arg in "$@"; do
  case $arg in
    --apks=*) 
      apksFile="${arg#*=}" 
      ;;
    --bundletool=*) 
      bundletoolPath="${arg#*=}" 
      ;;
  esac
done

# Auto-detect APKS if not provided
if [ -z "$apksFile" ]; then
  apksFile=$(ls -t app/build/outputs/bundle/debug/*.apks 2>/dev/null | head -n 1 || true)
  if [ -z "$apksFile" ]; then
    echo "No .apks file found. Run build.sh first."
    exit 1
  fi
fi

if [ ! -f "$apksFile" ]; then
  echo "APKS file not found: $apksFile"
  exit 1
fi

# Validate bundletool
if [[ -f "$bundletoolPath" ]]; then
  bundletoolCmd=(java -jar "$bundletoolPath")
elif command -v "$bundletoolPath" >/dev/null 2>&1; then
  bundletoolCmd=("$bundletoolPath")
else
  echo "bundletool not found at: $bundletoolPath"
  exit 1
fi

echo "Uninstalling existing app (if present)"
adb uninstall com.shipthis.go || true

echo "Installing APKS:"
echo "$apksFile"

"${bundletoolCmd[@]}" install-apks --apks "$apksFile"

echo "Install complete"
