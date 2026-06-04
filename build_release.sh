#!/bin/bash

# ==============================================================================
# Android Release APK Build Setup Script for Ubuntu/Debian
# This script installs JDK 17, downloads Android SDK, configures command-line tools,
# accepts licenses, and builds the signed release APK of the project automatically.
# ==============================================================================

set -e

echo "=========================================="
echo " Starting Android Build Environment Setup"
echo "=========================================="

# 1. Update system and install required system utilities
echo "--> Updating packages and installing prerequisites..."
sudo apt-get update -y
sudo apt-get install -y wget curl unzip zip git openjdk-17-jdk

# Define SDK installation paths
export ANDROID_HOME="$HOME/android-sdk"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

mkdir -p "$ANDROID_HOME/cmdline-tools"

# 2. Download Android Command Line Tools (latest stable)
if [ ! -d "$ANDROID_HOME/cmdline-tools/latest" ]; then
    echo "--> Downloading Android Command Line Tools..."
    # URL for CMD Line Tools (Linux format)
    CMD_LINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
    wget -q --show-progress "$CMD_LINE_TOOLS_URL" -O cmdline-tools.zip
    
    echo "--> Extracting tools..."
    unzip -q cmdline-tools.zip -d "$ANDROID_HOME/cmdline-tools"
    mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
    rm -f cmdline-tools.zip
fi

# 3. Accept Android SDK Licenses automatically
echo "--> Accepting Android Licenses..."
yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --licenses

# 4. Install required SDK components (Sdk Platforms & Build Tools)
echo "--> Installing Android SDK platform 36 and build tools..."
"$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" "platform-tools" "platforms;android-36" "build-tools;35.0.0"

# 5. Make gradlew executable
echo "--> Configuring project gradle wrapper..."
chmod +x gradlew

# 6. Build the Release APK
echo "--> Building the Signed Release APK..."
./gradlew :app:copyReleaseApk --no-configuration-cache

echo "=========================================="
echo " BUILD COMPLETE SUCCESSFUL!"
echo " Release APK location: release_apk/app-release.apk"
echo "=========================================="
