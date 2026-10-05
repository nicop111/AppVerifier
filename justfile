# Build and sign AppVerifier release APKs.
# Override any variable on the command line, e.g. `just keystore=/path/to/key.jks sign`.

java_home := env("JAVA_HOME_21", "/usr/lib/jvm/java-21-openjdk-amd64")
sdk := env("ANDROID_HOME", home_directory() / "Android/Sdk")
build_tools := sdk / "build-tools/36.0.0"
keystore := env("APPVERIFIER_KEYSTORE", home_directory() / "keystores/appverifier-release.jks")
key_alias := "appverifier"

set shell := ["bash", "-uc"]

version := `grep -oP 'versionCode = \K[0-9]+' app/build.gradle.kts`
# Gradle wipes its output folder on every build, so finished APKs go to dist/
unsigned := "app/build/outputs/apk/release/app-release-unsigned.apk"
dist := "dist"
aligned := dist / "AppVerifier-nicop111-" + version + "-aligned.apk"
signed := dist / "AppVerifier-nicop111-" + version + ".apk"

# Build, align and sign the release APK
default: release

# Build, align and sign the release APK
release: build align sign verify

# Build the unsigned release APK (Gradle 8.12 needs JDK 21, not the default JDK 25)
build:
    JAVA_HOME={{java_home}} bash ./gradlew assembleRelease

# Zipalign the unsigned APK
align:
    mkdir -p {{dist}}
    {{build_tools}}/zipalign -P 16 -f 4 {{unsigned}} {{aligned}}

# Sign the aligned APK with the release keystore (prompts for the password)
sign:
    {{build_tools}}/apksigner sign --ks {{keystore}} --ks-key-alias {{key_alias}} --out {{signed}} {{aligned}}

# Check the signature and print the certificate SHA-256
verify:
    {{build_tools}}/apksigner verify --print-certs {{signed}} 2>&1 | grep -v '^WARNING'; exit ${PIPESTATUS[0]}

# Install the signed APK on a connected device
install:
    {{sdk}}/platform-tools/adb install -r {{signed}}

# Create the release keystore (one-time; back it up together with its password)
keystore:
    test ! -e {{keystore}} || { echo "{{keystore}} already exists"; exit 1; }
    mkdir -p "$(dirname {{keystore}})"
    keytool -genkeypair -v -keystore {{keystore}} -alias {{key_alias}} -keyalg RSA -keysize 4096 -validity 10000 -dname "CN=nicop111"

# Remove build outputs (keeps dist/)
clean:
    JAVA_HOME={{java_home}} bash ./gradlew clean
