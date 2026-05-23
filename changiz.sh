#!/usr/bin/env bash
set -euo pipefail

CHANGIZ_DIR=".changiz"
CONFIG_FILE="$CHANGIZ_DIR/config.yaml"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

usage() {
    echo "Usage: ./changiz.sh <command>"
    echo ""
    echo "Commands:"
    echo "  init       Initialize .changiz/ directory and config"
    echo "  create     Create a new changiz entry (interactive)"
    echo "  validate   Validate all pending entries"
    echo "  check      Check that entries exist for source changes"
    echo "  consume    Release: bump version + generate changelogs"
    echo "  status     Show pending changiz entries"
    echo ""
}

cmd_init() {
    mkdir -p "$CHANGIZ_DIR"
    touch "$CHANGIZ_DIR/.gitkeep"

    if [ ! -f "$CONFIG_FILE" ]; then
        cat > "$CONFIG_FILE" << 'EOF'
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
EOF
        echo -e "${GREEN}✅ Created $CONFIG_FILE${NC}"
    fi

    mkdir -p changelogs/versions
    [ -f changelogs/CHANGELOG.md ] || echo "# Changelog" > changelogs/CHANGELOG.md
    [ -f changelogs/CHANGELOG_PUBLIC.md ] || echo "# Changelog (Public)" > changelogs/CHANGELOG_PUBLIC.md

    if [ ! -f version.properties ]; then
        cat > version.properties << 'EOF'
VERSION_MAJOR=0
VERSION_MINOR=1
VERSION_PATCH=0
VERSION_CODE=100
EOF
        echo -e "${GREEN}✅ Created version.properties${NC}"
    fi

    echo -e "${GREEN}✅ Changiz initialized.${NC}"
}

cmd_create() {
    ./gradlew createChangiz --console=plain -q
}

cmd_validate() {
    ./gradlew validateChangiz --console=plain
}

cmd_check() {
    local target="${1:-origin/develop}"
    ./gradlew checkChangizExists --targetBranch="$target" --console=plain
}

cmd_consume() {
    ./gradlew consumeChangiz --console=plain
}

cmd_status() {
    local entries
    entries=$(find "$CHANGIZ_DIR" -name "*.yaml" ! -name "config.yaml" 2>/dev/null)

    if [ -z "$entries" ]; then
        echo -e "${YELLOW}No pending changiz entries.${NC}"
        return
    fi

    echo -e "${GREEN}Pending changiz entries:${NC}"
    echo ""
    for f in $entries; do
        local type
        type=$(grep "^type:" "$f" | awk '{print $2}')
        local name
        name=$(basename "$f" .yaml)
        printf "  %-40s [%s]\n" "$name" "$type"
    done
    echo ""
    echo "Total: $(echo "$entries" | wc -l | tr -d ' ') entry(ies)"
}

# Main
case "${1:-}" in
    init)     cmd_init ;;
    create)   cmd_create ;;
    validate) cmd_validate ;;
    check)    cmd_check "${2:-}" ;;
    consume)  cmd_consume ;;
    status)   cmd_status ;;
    -h|--help|"") usage ;;
    *)
        echo -e "${RED}Unknown command: $1${NC}"
        usage
        exit 1
        ;;
esac
