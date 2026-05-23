# Changiz — Setup Guide

## Prerequisites

- Gradle 8.0+
- Kotlin 1.9+
- Git

## 1. Add the plugin

### Option A: From Maven Central (recommended for consumers)

```kotlin
// settings.gradle.kts
pluginManagement {
    plugins {
        id("ir.behnawwm.changiz") version "0.1.0"
    }
}

// build.gradle.kts (root)
plugins {
    id("ir.behnawwm.changiz")
}
```

### Option B: Composite build (for development / customization)

Copy `build-logic/changiz/` into your project.

```kotlin
// settings.gradle.kts
pluginManagement {
    includeBuild("build-logic")
}

// build.gradle.kts (root)
plugins {
    id("ir.behnawwm.changiz")
}
```

## 2. Initialize

```bash
mkdir -p .changiz
touch .changiz/.gitkeep
```

Or use the shell script:

```bash
curl -sL https://raw.githubusercontent.com/behnawwm/changiz/main/changiz.sh -o changiz.sh
chmod +x changiz.sh
./changiz.sh init
```

## 3. Create config file

Create `.changiz/config.yaml`:

```yaml
languages:
  - en
  - fa

changelog_types:
  public:
    max_length: 500
    required: true
  internal:
    max_length: null
    required: false

version_file: version.properties

paths_requiring_changiz:
  - "*/src/**"
  - "*.gradle.kts"

paths_excluded:
  - ".changiz/**"
  - "*.md"
  - ".gitlab-ci.yml"
  - "buildSrc/**"
  - "build-logic/**"
```

## 4. Create version file

Create `version.properties`:

```properties
VERSION_MAJOR=1
VERSION_MINOR=0
VERSION_PATCH=0
VERSION_CODE=10000
```

Read it in `app/build.gradle.kts`:

```kotlin
val versionProps = java.util.Properties().apply {
    load(rootProject.file("version.properties").inputStream())
}

android {
    defaultConfig {
        versionName = "${versionProps["VERSION_MAJOR"]}.${versionProps["VERSION_MINOR"]}.${versionProps["VERSION_PATCH"]}"
        versionCode = (versionProps["VERSION_CODE"] as String).toInt()
    }
}
```

## 5. CI enforcement (GitLab CI)

```yaml
changiz-check:
  stage: validate
  rules:
    - if: '$CI_MERGE_REQUEST_IID'
  script:
    - ./gradlew checkChangizExists --targetBranch=origin/$CI_MERGE_REQUEST_TARGET_BRANCH_NAME
  allow_failure: false

changiz-validate:
  stage: validate
  rules:
    - if: '$CI_MERGE_REQUEST_IID'
  script:
    - ./gradlew validateChangiz
```

## 6. Verify

```bash
./gradlew tasks --group=changiz
```

Expected output:

```
Changiz tasks
-------------
checkChangizExists - Check that a changiz entry exists for source changes
consumeChangiz - Consume changiz entries, bump version, and generate changelogs
createChangiz - Create a new changiz entry
validateChangiz - Validate all pending changiz entries
```
