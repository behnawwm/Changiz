# Changiz — Developer Guide

## What is Changiz?

Changiz enforces changelog documentation at MR time. Every code change must include a **changiz entry** — a YAML file describing what changed. CI blocks MRs without one.

- **Internal changelog** (required, English only) — developer-facing, goes into `CHANGELOG.md`
- **Public changelog** (optional, multi-language) — user-facing, goes to app stores

---

## Workflow

```
1. Write code on your feature branch
2. Run: ./gradlew createChangiz       (or --empty if no changelog needed)
3. Commit the .changiz/*.yaml file
4. Push → CI validates
5. MR merges → entries accumulate
6. Release time → ./gradlew consumeChangiz
```

---

## Commands

| Command | Purpose |
|---------|---------|
| `./gradlew createChangiz` | Create entry interactively |
| `./gradlew createChangiz --empty` | Create empty entry (no changelog needed) |
| `./gradlew validateChangiz` | Validate pending entries |
| `./gradlew checkChangizExists` | CI: verify entry exists |
| `./gradlew consumeChangiz` | Release: bump version + generate changelogs |

---

## Creating an entry

```bash
./gradlew createChangiz
```

```
Bump type (major/minor/patch/empty) [patch]: minor
Affected modules (comma-separated) []: :app,:feature:auth
Ticket ID []: JIRA-1234
Internal changelog (en) [required]: Added biometric login support
Public changelog (en) [optional, for app stores]: Added fingerprint login
Public changelog (fa) [optional, for app stores]: اضافه شدن ورود با اثر انگشت
```

Creates `.changiz/feat-biometric-auth.yaml`:

```yaml
type: minor
modules:
  - :app
  - :feature:auth
ticket: JIRA-1234
author: behnam
internal:
  en: "Added biometric login support"
public:
  en: "Added fingerprint login"
  fa: "اضافه شدن ورود با اثر انگشت"
```

---

## Empty entries

For MRs that don't need a changelog (refactoring, CI, dependency bumps):

```bash
./gradlew createChangiz --empty
```

Creates:
```yaml
type: empty
```

This satisfies CI but produces no changelog and no version bump.

---

## Entry format

| Field | Required | Description |
|-------|----------|-------------|
| `type` | ✅ | `major`, `minor`, `patch`, or `empty` |
| `internal` | ✅ | Developer-facing notes (English) |
| `public` | ❌ | User-facing notes for app stores (EN + FA) |
| `modules` | ❌ | Affected Gradle modules |
| `ticket` | ❌ | Issue tracker ID |
| `author` | ❌ | Auto-filled from git |

### Bump types

| Type | When | Example |
|------|------|---------|
| `major` | Breaking changes | New auth system |
| `minor` | New features | Dark mode |
| `patch` | Bug fixes | Crash fix |
| `empty` | No changelog needed | CI config change |

---

## Release flow

```bash
./gradlew consumeChangiz
```

1. Reads all `.changiz/*.yaml`
2. Determines version bump (highest type among non-empty entries)
3. Bumps `version.properties`
4. Generates:
   ```
   changelogs/versions/2.3.0/
   ├── internal_en.md          # Internal changelog (English)
   ├── public_en.txt           # → Google Play
   ├── public_fa.txt           # → Cafe Bazaar / Myket
   └── meta.yaml               # Release metadata
   ```
5. Prepends to `changelogs/CHANGELOG.md` and `changelogs/CHANGELOG_PUBLIC.md`
6. Deletes consumed `.changiz/*.yaml` files

---

## FAQ

**My MR doesn't change anything user-facing.**
→ `./gradlew createChangiz --empty`

**Do I need to write public changelog?**
→ No. Public is optional. Only internal (English) is required.

**Can I edit the YAML manually?**
→ Yes.

**What if CI fails?**
→ Run `./gradlew createChangiz` or `./gradlew createChangiz --empty`, commit, push.

**Who writes the public changelog?**
→ Developer at MR time, or product/QA can add it before release.
