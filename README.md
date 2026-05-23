# Changiz 🏛️

[![CI](https://github.com/behnawwm/changiz/actions/workflows/ci.yml/badge.svg)](https://github.com/behnawwm/changiz/actions/workflows/ci.yml)

A changelog enforcer for Gradle projects. Inspired by [changesets](https://github.com/changesets/changesets).

**Problem**: Changelog is forgotten when merging MRs. Changes lose track over time.

**Solution**: Changiz requires a small YAML file per MR describing the change. CI blocks MRs without one. At release time, all entries are consumed to bump the version and generate changelogs.

## Features

- 🚫 **CI enforcement** — blocks MRs without a changiz entry
- 🌍 **Multi-language** — internal + public changelogs in multiple languages
- 📱 **Market-ready** — generates plain text for Google Play, Cafe Bazaar, Myket
- 🔢 **Semver** — automatic version bumps (major/minor/patch)
- ⏭️ **Empty entries** — `--empty` flag for MRs that don't need a changelog
- 🔌 **Gradle-native** — standard Gradle tasks, works with any Gradle project

## Quick Start

```kotlin
// build.gradle.kts
plugins {
    id("ir.behnawwm.changiz") version "0.1.0"
}
```

```bash
# Create a changiz entry
./gradlew createChangiz

# For MRs with no changelog needed
./gradlew createChangiz --empty

# Validate entries
./gradlew validateChangiz

# Release
./gradlew consumeChangiz
```

## How it works

```
Developer creates MR
        │
        ▼
./gradlew createChangiz  →  .changiz/feat-xyz.yaml
        │
        ▼
CI runs checkChangizExists + validateChangiz
        │
        ▼
MR merges → entries accumulate in .changiz/
        │
        ▼ (release time)
./gradlew consumeChangiz
        │
        ├── Bumps version.properties
        ├── Generates changelogs/versions/X.Y.Z/public_en.txt
        ├── Generates changelogs/versions/X.Y.Z/public_fa.txt
        ├── Updates changelogs/CHANGELOG.md
        └── Deletes consumed .changiz/*.yaml
```

## Entry format

```yaml
type: minor                    # major | minor | patch | empty
modules:
  - :app
ticket: JIRA-1234
internal:                      # Required
  en: "Added biometric login"
  fa: "اضافه شدن ورود بیومتریک"
public:                        # Optional (for app stores)
  en: "Added fingerprint login"
  fa: "اضافه شدن ورود با اثر انگشت"
```

## Documentation

- **[Setup Guide](docs/SETUP.md)** — Installation, integration options, CI setup
- **[Developer Guide](docs/GUIDE.md)** — Daily workflow, commands, FAQ

## Integration Options

1. **Gradle Plugin Portal** — `id("ir.behnawwm.changiz") version "X.Y.Z"`
2. **Copy-paste composite build** — Copy this repo into `build-logic/changiz/`
3. **Git submodule** — `git submodule add` this repo

See [SETUP.md](docs/SETUP.md) for details.

## Releasing (for maintainers)

```bash
git tag v0.1.0
git push origin v0.1.0
# GitHub Actions publishes to Gradle Plugin Portal + GitHub Packages
```

Requires `GRADLE_PUBLISH_KEY` and `GRADLE_PUBLISH_SECRET` secrets.

## License

MIT
