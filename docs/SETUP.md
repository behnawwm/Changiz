# Changiz — Setup Guide

How to integrate Changiz into your Android/Gradle project.

## Option A: From Gradle Plugin Portal

```kotlin
// build.gradle.kts (root)
plugins {
    id("ir.behnawwm.changiz") version "0.1.0"
}
```

## Option B: Copy-paste (composite build)

1. Clone or download the Changiz repo
2. Copy the entire repo into your project as `build-logic/changiz/`
3. Wire it up:

```kotlin
// settings.gradle.kts
pluginManagement {
    includeBuild("build-logic/changiz")
}

// build.gradle.kts (root)
plugins {
    id("ir.behnawwm.changiz")
}
```

## Option C: Git submodule

```bash
git submodule add https://github.com/behnawwm/changiz.git build-logic/changiz
```

Then same `settings.gradle.kts` setup as Option B.

---

## Initialize

After applying the plugin, run:

```bash
mkdir -p .changiz
```

Create `.changiz/config.yaml`:

```yaml
languages:
  - en
  - fa

changelog_types:
  public:
    max_length: 500
    required: false
  internal:
    max_length: null
    required: true

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

Create `version.properties`:

```properties
VERSION_MAJOR=1
VERSION_MINOR=0
VERSION_PATCH=0
VERSION_CODE=10000
```

Create changelog directory:

```bash
mkdir -p changelogs/versions
echo "# Changelog" > changelogs/CHANGELOG.md
echo "# Changelog (Public)" > changelogs/CHANGELOG_PUBLIC.md
```

---

## Read version in your app

```kotlin
// app/build.gradle.kts
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

---

## CI Enforcement

### GitLab CI

```yaml
changiz-check:
  stage: validate
  rules:
    - if: '$CI_MERGE_REQUEST_IID'
  script:
    - ./gradlew checkChangizExists --targetBranch=origin/$CI_MERGE_REQUEST_TARGET_BRANCH_NAME

changiz-validate:
  stage: validate
  rules:
    - if: '$CI_MERGE_REQUEST_IID'
  script:
    - ./gradlew validateChangiz
```

### GitHub Actions

```yaml
- name: Check changiz
  run: ./gradlew checkChangizExists --targetBranch=origin/${{ github.base_ref }}
```

---

## Verify

```bash
./gradlew tasks --group=changiz
```
