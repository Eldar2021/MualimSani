#!/usr/bin/env bash
# Ders videolarini GitHub Release'e yukler ve uygulamanin okudugu manifest.json'u uretir.
#
# Kullanim:
#   scripts/publish-videos.sh <video-klasoru> [--dry-run]
#
# Gereksinimler: gh (GitHub CLI) kurulu ve `gh auth login` yapilmis olmali.
#
# Beklenen dosya listesi koddan okunur (HomeVM.kt icindeki Lesson(..., <dosya-no>)),
# boylece eksik ya da fazla video yuklenmeden once yakalanir. Script tekrar
# calistirilabilir: ayni isimli dosyalar uzerine yazilir.
set -euo pipefail

REPO="Eldar2021/MualimSani"
TAG="videos-v1"   # app/build.gradle.kts -> VIDEOS_BASE_URL ile ayni olmali
MAX_BYTES=$((2 * 1024 * 1024 * 1024))   # GitHub: dosya basina en fazla 2 GiB

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
HOME_VM="$ROOT/app/src/main/java/com/muallimsaniqr/flutter_app/ui/screens/home/HomeVM.kt"

DIR=""
DRY_RUN=""
ALLOW_MISSING=""
for arg in "$@"; do
  case "$arg" in
    --dry-run) DRY_RUN="--dry-run" ;;
    --allow-missing) ALLOW_MISSING=1 ;;  # eksik videolar atlanir; uygulama o dersi oynatamaz
    *) DIR="$arg" ;;
  esac
done
if [[ -z "$DIR" || ! -d "$DIR" ]]; then
  echo "Kullanim: $0 <video-klasoru> [--dry-run] [--allow-missing]" >&2
  exit 1
fi
DIR="$(cd "$DIR" && pwd)"

file_size() { stat -f%z "$1" 2>/dev/null || stat -c%s "$1"; }

# --- 1. Beklenen dosyalar -----------------------------------------------------
EXPECTED=$(grep -oE 'Lesson\([0-9]+, R\.string\.[a-z_0-9]+, [0-9]+\)' "$HOME_VM" \
  | sed -E 's/.*, ([0-9]+)\)$/\1/' | sort -n | uniq)
EXPECTED_COUNT=$(echo "$EXPECTED" | wc -l | tr -d ' ')

missing=()
for n in $EXPECTED; do
  [[ -s "$DIR/$n.mp4" ]] || missing+=("$n.mp4")
done
if (( ${#missing[@]} > 0 )); then
  if [[ -z "$ALLOW_MISSING" ]]; then
    echo "HATA: ${#missing[@]} video eksik veya bos: ${missing[*]}" >&2
    echo "Yine de yuklemek icin --allow-missing ekleyin." >&2
    exit 1
  fi
  echo "UYARI: ${#missing[@]} video eksik, atlaniyor: ${missing[*]}" >&2
  for m in "${missing[@]}"; do
    EXPECTED=$(echo "$EXPECTED" | grep -vx "${m%.mp4}")
  done
  EXPECTED_COUNT=$(echo "$EXPECTED" | wc -l | tr -d ' ')
fi

for f in "$DIR"/*; do
  name="$(basename "$f")"
  if ! echo "$EXPECTED" | grep -qx "${name%.mp4}" || [[ "$name" != *.mp4 ]]; then
    echo "UYARI: uygulamada karsiligi olmayan dosya atlaniyor: $name" >&2
  fi
done

# --- 2. manifest.json --------------------------------------------------------
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
MANIFEST="$OUT/manifest.json"
FILES=()
total=0
{
  echo '{'
  echo '  "version": 1,'
  echo '  "videos": ['
  first=1
  for n in $EXPECTED; do
    f="$DIR/$n.mp4"
    size=$(file_size "$f")
    if (( size > MAX_BYTES )); then
      echo "HATA: $n.mp4 2 GiB sinirini asiyor" >&2
      exit 1
    fi
    total=$((total + size))
    FILES+=("$f")
    [[ $first == 1 ]] || echo ','
    printf '    { "name": "%s", "size": %s }' "$n.mp4" "$size"
    first=0
  done
  echo
  echo '  ]'
  echo '}'
} > "$MANIFEST"

echo "$EXPECTED_COUNT video, toplam $((total / 1024 / 1024)) MB"

if [[ "$DRY_RUN" == "--dry-run" ]]; then
  cp "$MANIFEST" "$DIR/../manifest.preview.json"
  echo "Deneme modu: hicbir sey yuklenmedi. Manifest: $(cd "$DIR/.." && pwd)/manifest.preview.json"
  exit 0
fi

# --- 3. Yukleme --------------------------------------------------------------
if ! gh release view "$TAG" --repo "$REPO" >/dev/null 2>&1; then
  gh release create "$TAG" --repo "$REPO" \
    --title "Ders videolari ($TAG)" \
    --notes "Uygulamanin ilk acilista indirdigi ders videolari. scripts/publish-videos.sh ile yuklenir; elle duzenlemeyin." \
    --latest=false
fi

# Once videolar, en son manifest: uygulama hicbir zaman eksik dosyaya isaret eden
# bir manifest gormez.
gh release upload "$TAG" --repo "$REPO" --clobber "${FILES[@]}"
gh release upload "$TAG" --repo "$REPO" --clobber "$MANIFEST"

echo "Tamam: https://github.com/$REPO/releases/tag/$TAG"
