# Changiz — Developer Guide

## What is Changiz?

Changiz enforces changelog documentation at MR time. Every code change must include a **changiz entry** — a YAML file describing what changed. This prevents the "forgot to update changelog" problem.

---

## Workflow

```
1. Write code on your feature branch
2. Run: ./gradlew createChangiz
3. Commit the .changiz/*.yaml file with your code
4. Push → CI validates the entry exists and is valid
5. MR merges → entry accumulates in .changiz/
6. Release time → ./gradlew consumeChangiz → version bumps, changelogs generated
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

Prompts:
```
Bump type (major/minor/patch/empty) [patch]: minor
Affected modules (comma-separated) []: :app,:feature:auth
Ticket ID []: JIRA-1234
Internal changelog (en) [required]: Added biometric login support
Internal changelog (fa) [required]: اضافه شدن ورود بیومتریک
Public changelog (en) [optional]: Added fingerprint login
Public changelog (fa) [optional]: اضافه شدن ورود با اثر انگشت
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
  fa: "اضافه شدن ورود بیومتریک"
public:
  en: "Added fingerprint login"
  fa: "اضافه شدن ورود با اثر انگشت"
```

---

## Empty entries (skip changelog)

For MRs that don't add a user-visible or developer-notable change (refactoring, CI tweaks, dependency bumps):

```bash
./gradlew createChangiz --empty
```

This creates a minimal file:
```yaml
type: empty
```

This satisfies CI enforcement but produces no changelog entry and no version bump.

---

## Entry format

| Field | Required | Description |
|-------|----------|-------------|
| `type` | ✅ | `major`, `minor`, `patch`, or `empty` |
| `internal` | ✅ | Developer-facing notes per language |
| `public` | ❌ | User-facing notes for app stores (optional) |
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

What happens:
1. Reads all `.changiz/*.yaml` (ignores `config.yaml`)
2. Determines version bump (highest type among non-empty entries)
3. Bumps `version.properties`
4. Generates:
   - `changelogs/versions/{version}/public_en.txt` → Google Play
   - `changelogs/versions/{version}/public_fa.txt` → Cafe Bazaar / Myket
   - `changelogs/versions/{version}/internal_en.md`
   - `changelogs/versions/{version}/internal_fa.md`
   - `changelogs/versions/{version}/meta.yaml`
5. Prepends to `changelogs/CHANGELOG.md` and `changelogs/CHANGELOG_PUBLIC.md`
6. Deletes consumed `.changiz/*.yaml` files

---

## FAQ

**My MR doesn't change anything user-facing. What do I do?**
Run `./gradlew createChangiz --empty`. This satisfies CI without generating changelog.

**Can I have multiple entries per MR?**
Yes, but usually one is enough.

**Can I edit the YAML manually?**
Yes. It's just a file.

**What if CI fails saying "no changiz entry"?**
Run `./gradlew createChangiz` or `./gradlew createChangiz --empty`, commit, push.

**Who writes the public changelog?**
Developer writes it at MR time. It's optional — if omitted, only internal changelog is generated.

**What's the difference between internal and public?**
- **Internal**: goes into `CHANGELOG.md`, visible to the team
- **Public**: goes into market upload files (Google Play, Bazaar), visible to users
