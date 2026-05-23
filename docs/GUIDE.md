# Changiz — Usage Guide

## Overview

Changiz enforces changelog documentation at MR time. Every code change that affects users must include a **changiz entry** — a small YAML file describing what changed, in both languages, for both public and internal audiences.

## Daily Workflow

### 1. Create a changiz entry when you're done with your feature/fix

```bash
./changiz.sh create
# or
./gradlew createChangiz
```

This will interactively ask:
- **Bump type**: `patch` (bug fix), `minor` (new feature), `major` (breaking change), `none` (no release impact)
- **Affected modules**: e.g. `:app`, `:feature:auth`
- **Ticket ID**: e.g. `JIRA-1234` (optional)
- **Public changelog** in each language (user-facing, goes to app stores)
- **Internal changelog** in each language (developer-facing, optional)

The file is created at `.changiz/{branch-slug}.yaml`.

### 2. Commit the entry with your code

```bash
git add .changiz/
git commit -m "Add changiz entry"
```

### 3. Push and create your MR

CI will automatically:
- ✅ Check that a changiz entry exists (if source files changed)
- ✅ Validate the entry format and content

---

## Changiz Entry Format

```yaml
type: patch
modules:
  - :app
  - :feature:auth
ticket: JIRA-1234
author: behnam
public:
  en: "Fixed a crash on the login screen"
  fa: "رفع کرش در صفحه ورود"
internal:
  en: "Fixed NPE in LoginViewModel when token is null"
  fa: "رفع NPE در LoginViewModel وقتی توکن null است"
```

### Fields

| Field | Required | Description |
|-------|----------|-------------|
| `type` | ✅ | `major`, `minor`, `patch`, or `none` |
| `modules` | ❌ | List of affected Gradle modules |
| `ticket` | ❌ | Jira/issue tracker ID |
| `author` | ❌ | Auto-filled from git config |
| `public` | ✅ | User-facing text per language (max 500 chars) |
| `internal` | ❌ | Developer-facing text per language |

### Bump type guide

| Type | When to use | Example |
|------|-------------|---------|
| `major` | Breaking changes, major redesigns | Migration to new auth system |
| `minor` | New features, significant improvements | Added dark mode |
| `patch` | Bug fixes, small improvements | Fixed crash on login |
| `none` | No user impact (refactoring, CI, tests) | Migrated to Kotlin DSL |

---

## Release Workflow

```bash
./changiz.sh consume
# or
./gradlew consumeChangiz
```

This will:
1. Read all pending `.changiz/*.yaml` files
2. Determine the version bump (highest type wins)
3. Bump `version.properties`
4. Generate per-language market files in `changelogs/versions/{version}/`
5. Update `changelogs/CHANGELOG.md` and `changelogs/CHANGELOG_PUBLIC.md`
6. Delete consumed entries

### Output after release

```
changelogs/
├── CHANGELOG.md
├── CHANGELOG_PUBLIC.md
└── versions/
    └── 2.3.0/
        ├── meta.yaml
        ├── public_en.txt       → Google Play
        ├── public_fa.txt       → Cafe Bazaar / Myket
        ├── internal_en.md
        └── internal_fa.md
```

---

## Shell Script Commands

```bash
./changiz.sh init       # Initialize .changiz/ directory and config
./changiz.sh create     # Create a new changiz entry (interactive)
./changiz.sh validate   # Validate all pending entries
./changiz.sh check      # Check that entries exist for changes
./changiz.sh consume    # Release: bump version + generate changelogs
./changiz.sh status     # Show pending entries
```

---

## Skipping the requirement

For changes that don't need a changelog entry:

**Option 1**: Create a `none` type entry:
```yaml
type: none
public:
  en: ""
  fa: ""
```

**Option 2**: Add `skip-changiz` label to MR (if configured in CI).

---

## Commands Reference

| Command | Purpose |
|---------|---------|
| `./gradlew createChangiz` | Create a new entry interactively |
| `./gradlew validateChangiz` | Validate all pending entries |
| `./gradlew checkChangizExists` | Verify entry exists for changes (CI) |
| `./gradlew consumeChangiz` | Release: bump + generate + archive |

---

## FAQ

**Q: One changiz entry per MR?**
Yes. One entry describing the overall change is sufficient.

**Q: Can I edit an entry after creating it?**
Yes — it's just a YAML file.

**Q: What if I forget?**
CI blocks your MR with a clear error message.

**Q: Who writes the public changelog?**
The developer at MR time. Product/QA can review before release.
