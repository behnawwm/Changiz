# Changiz — Agent Context

## What is this project?

Changiz is a **Gradle plugin** that enforces changelog documentation at MR time for Android/Kotlin projects. Inspired by [changesets](https://github.com/changesets/changesets) from the JS ecosystem.

**Problem**: Developers forget to write changelogs when merging MRs. Changes lose track over time.

**Solution**: Each MR must include a `.changiz/*.yaml` file describing the change. CI blocks MRs without one. At release time, all entries are consumed to bump the version and generate changelogs.

## Repo structure

```
changiz/                                    ← Root-level Gradle plugin project
├── build.gradle.kts                        # kotlin-dsl, java-gradle-plugin, maven-publish, com.gradle.plugin-publish
├── settings.gradle.kts                     # rootProject.name = "changiz"
├── gradle.properties                       # GROUP=io.github.behnawwm.changiz, VERSION=0.1.0
├── .github/workflows/
│   ├── ci.yml                              # Build on push/PR to main
│   └── release.yml                         # Publish on tag push (v*)
├── docs/
│   ├── SETUP.md                            # Integration guide (Plugin Portal, copy-paste, submodule)
│   └── GUIDE.md                            # Developer workflow guide
├── README.md
├── LICENSE                                 # MIT
└── src/main/kotlin/io/github/behnawwm/changiz/
    ├── ChangizPlugin.kt                    # Registers 4 Gradle tasks
    ├── model/
    │   ├── BumpType.kt                     # enum: EMPTY, PATCH, MINOR, MAJOR
    │   ├── ChangizConfig.kt                # Config data class
    │   ├── ChangizEntry.kt                 # Single changiz entry data class
    │   └── Version.kt                      # Semver with bump() and code calculation
    ├── config/
    │   ├── ConfigParser.kt                 # Reads .changiz/config.yaml
    │   └── ChangizParser.kt                # Reads .changiz/*.yaml entry files
    ├── render/
    │   ├── ChangelogRenderer.kt            # Markdown output for CHANGELOG.md
    │   └── MarketRenderer.kt              # Plain text bullet points for app stores
    └── tasks/
        ├── CreateChangizTask.kt            # Interactive entry creation (--empty flag)
        ├── ValidateChangizTask.kt          # Schema/constraint validation
        ├── CheckChangizExistsTask.kt       # CI enforcement via git diff
        └── ConsumeChangizTask.kt           # Release: bump version + generate changelogs
```

## Key design decisions

1. **Internal changelog = English only** (`internalLanguages: [en]`). Required.
2. **Public changelog = multi-language** (`publicLanguages: [en, fa]`). Optional.
3. **`type: empty`** — satisfies CI enforcement without producing changelog or version bump. Used for MRs that don't need a changelog (refactoring, CI changes, etc.).
4. **Version source of truth** — `version.properties` file with `VERSION_MAJOR`, `VERSION_MINOR`, `VERSION_PATCH`, `VERSION_CODE`.
5. **Version code formula** — `major * 10000 + minor * 100 + patch`.
6. **Changiz entries directory** — `.changiz/` (not `.changesets/`).
7. **File naming** — branch name slugified: `feat-biometric-auth.yaml`.
8. **Consume deletes entries** — after `consumeChangiz`, all `.changiz/*.yaml` files are deleted.

## Config format (`.changiz/config.yaml`)

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
```

## Entry format (`.changiz/feat-xyz.yaml`)

```yaml
type: minor              # major | minor | patch | empty
modules:
  - :app
ticket: JIRA-1234
author: behnam
internal:
  en: "Added biometric login support"
public:                  # Optional
  en: "Added fingerprint login"
  fa: "اضافه شدن ورود با اثر انگشت"
```

## Gradle tasks

| Task | Purpose | When |
|------|---------|------|
| `createChangiz` | Interactive entry creation | Developer runs manually |
| `createChangiz --empty` | No-op entry for CI bypass | MRs without changelog |
| `validateChangiz` | Validate YAML, required fields, char limits | CI |
| `checkChangizExists` | Git diff → fail if source changed but no entry | CI |
| `consumeChangiz` | Bump version, generate changelogs, delete entries | Release |

## Output after `consumeChangiz`

```
changelogs/
├── CHANGELOG.md                # Full internal history (prepended)
├── CHANGELOG_PUBLIC.md         # Full public history (prepended)
└── versions/
    └── 2.3.0/
        ├── meta.yaml           # Version, date, consumed entries
        ├── internal_en.md      # Internal changelog
        ├── public_en.txt       # → Google Play
        └── public_fa.txt       # → Cafe Bazaar / Myket
```

## Publishing

- **Plugin ID**: `io.github.behnawwm.changiz`
- **Group**: `io.github.behnawwm.changiz`
- **Targets**: Gradle Plugin Portal + GitHub Packages
- **Trigger**: Push a git tag `v*` → GitHub Actions publishes
- **Secrets needed**: `GRADLE_PUBLISH_KEY`, `GRADLE_PUBLISH_SECRET`

## Integration (for consumers)

Three options:
1. **Gradle Plugin Portal**: `id("io.github.behnawwm.changiz") version "X.Y.Z"`
2. **Copy-paste**: Copy repo into `build-logic/changiz/`, add `includeBuild("build-logic/changiz")` to settings
3. **Git submodule**: `git submodule add` into `build-logic/changiz/`

## Tech stack

- Kotlin (Gradle plugin DSL)
- SnakeYAML 2.2 for YAML parsing
- Gradle 8.14
- JVM 17
- No Android dependencies (pure Gradle plugin, works with any Gradle project)

## Naming conventions

- The tool is called **Changiz** (not "changesets")
- Directory: `.changiz/` (not `.changesets/`)
- Tasks: `createChangiz`, `validateChangiz`, `checkChangizExists`, `consumeChangiz`
- Entry files: `{branch-slug}.yaml`
- The word "entry" is used instead of "changeset"
