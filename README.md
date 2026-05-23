# Changiz 🏛️

A changelog enforcer for Android/Gradle/Kotlin projects. Inspired by [changesets](https://github.com/changesets/changesets).

Changiz ensures every meaningful code change is documented at MR time — never lose track of what changed between versions again.

## Features

- **CI enforcement** — blocks MRs without changelog entries
- **Multi-language** — public changelogs in EN + FA (configurable)
- **Public + Internal** — separate user-facing and developer-facing notes
- **Semver versioning** — automatic version bumps based on change types
- **Market-ready** — generates plain text files for Google Play, Cafe Bazaar, Myket
- **Gradle-native** — runs as standard Gradle tasks

## Quick Start

```bash
# Create a changiz entry for your branch
./changiz.sh create

# Validate all pending entries
./changiz.sh validate

# Release: bump version + generate changelogs
./changiz.sh consume

# Or use Gradle directly:
./gradlew createChangiz
./gradlew validateChangiz
./gradlew consumeChangiz
```

## Installation

Add the plugin to your project:

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

## Documentation

- [Setup Guide](docs/SETUP.md) — Installation and CI integration
- [Usage Guide](docs/GUIDE.md) — Daily workflow and commands

## Project Structure

```
.changiz/              # Pending changiz YAML entries
  config.yaml          # Changiz configuration
changelogs/            # Generated output
  CHANGELOG.md         # Full internal changelog
  CHANGELOG_PUBLIC.md  # Full public changelog
  versions/            # Per-version archives with market files
build-logic/changiz/   # Gradle plugin source
```

## License

MIT
