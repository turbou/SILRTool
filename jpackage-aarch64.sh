#!/bin/sh

JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"
APP_NAME="SILRTool"
APP_VERSION="1.0.3"
SM_KEY_PAIR="key_455812447"

jpackage \
  --name ${APP_NAME}-${APP_VERSION}-aarch64 \
  --mac-package-name ${APP_NAME} \
  --type "app-image" \
  --input build/libs \
  --dest build/ \
  --main-jar ${APP_NAME}-${APP_VERSION}.jar \
  --main-class com.contrastsecurity.silrtool.Main \
  --java-options -XstartOnFirstThread \
  --java-options -Xms256m \
  --java-options -Xmx512m \
  --java-options "--add-opens java.base/java.time.format=ALL-UNNAMED" \
  --java-options "--add-opens java.base/java.util.regex=ALL-UNNAMED" \
  --icon src/main/resources/silrtool.icns \
  --vendor "Contrast Security Japan G.K." \
  --copyright "Copyright (c) 2023 Contrast Security Japan G.K."

smctl-mac-x64 sign \
  --keypair-alias ${SM_KEY_PAIR} \
  --config-file /Users/turbou/digicert/pkcs11properties.cfg \
  --input build/${APP_NAME}-${APP_VERSION}-aarch64.app/

cd build && 7z a ${APP_NAME}-${APP_VERSION}-aarch64.cli7z ${APP_NAME}-${APP_VERSION}-aarch64.app/

exit 0
