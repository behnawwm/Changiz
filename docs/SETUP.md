# Changiz — Setup Guide

## Integration Options

### Option 1: Gradle Plugin Portal (once published)

```kotlin
// build.gradle.kts (root)
plugins {
    id("ir.behnawwm.changiz") version "0.1.0"
}
```

### Option 2: Copy-paste as composite build

This is the simplest way to use Changiz in your project right now.

**Step 1**: Copy the Changiz repo into your project:

```bash
# From your project root:
mkdir -p build-logic
cp -r /path/to/changiz build-logic/changiz
```

Your structure should look like:
```
your-project/
├── build-logic/
│   └── changiz/
│       ├── build.gradle.kts
│       ├── settings.gradle.kts
│       ├── gradle.properties
│       └── src/main/kotlin/ir/behnawwm/changiz/...
├── app/
├── settings.gradle.kts
└── build.gradle.kts
```

**Step 2**: Include the composite build in your project's `settings.gradle.kts`:

```kotlin
// settings.gradle.kts (your project root)
pluginManagement {
    includeBuild("build-logic/changiz")
}

// ... rest of your settings
```

**Step 3**: Apply the plugin in your root `build.gradle.kts`:

```kotlin
// build.gradle.kts (your project root)
plugins {
    id("ir.behnawwm.changiz")
}
```

**Step 4**: Done. Run `./gradlew tasks --group=changiz` to verify.

### Option 3: Git submodule

```bash
git submodule add https://github.com/behnawwm/changiz.git build-logic/changiz
```

Then same `settings.gradle.kts` setup as Option 2.

---

## Initialize Changiz in your project

```bash
mkdir -p .changiz
mkdir -p changelogs/versions
echo "# Changelog" > changelogs/CHANGELOG.md
echo "# Changelog (Public)" > changelogs/CHANGELOG_PUBLIC.md
```

Create `.changiz/config.yaml`:

```yaml
languages:
  internal:
    - en
  public:
    - en
    - fa

changelog_types:
  public:
    max_length: 500
    required: false
  internal:
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
- name: Validate changiz
  run: ./gradlew validateChangiz
```

---

## Verify

```bash
./gradlew tasks --group=changiz
```

Expected:
```
Changiz tasks
-------------
checkChangizExists - Check that a changiz entry exists for source changes
consumeChangiz     - Consume changiz entries, bump version, and generate changelogs
createChangiz      - Create a new changiz entry (use --empty for no-op)
validateChangiz    - Validate all pending changiz entries
```
