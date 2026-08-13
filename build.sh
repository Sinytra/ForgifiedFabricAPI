#!/usr/bin/env bash
# ═══════════════════════════════════════════════════════════════════════
# build.sh — 统一构建契约（通用 Gradle 实现）
#
# 契约（所有 T0/T1 仓库一致，外部调用方式不得更改）：
#   用法:   ./build.sh [--clean] [--out <dir>]      # 默认 out=dist
#   退出码: 0=成功；非 0=失败（原因写 stderr）
#   产出:   <out>/*.jar  +  <out>/build-info.json
#           build-info.json = { repo, version, source_commit, jars:[{file,sha256}], built_at }
#
# 内部实现可自由改写（gradle 目标、模块过滤、重命名、多产物、外部前置任务）。
# 可选仓库级覆盖：
#   - build-tasks.txt  存在则其内容（一行一个任务）替代默认 `build` 任务
#   - build-jars.txt   存在则只收集其中列出的 jar 文件名模式（每行一个 glob）
# ═══════════════════════════════════════════════════════════════════════
set -euo pipefail
cd "$(dirname "$0")"

OUT=dist
CLEAN=0
while [ $# -gt 0 ]; do
  case "$1" in
    --clean) CLEAN=1 ;;
    --out) shift; OUT="$2" ;;
    *) echo "usage: $0 [--clean] [--out <dir>]" >&2; exit 2 ;;
  esac
  shift
done

if [ ! -x ./gradlew ]; then
  echo "build.sh: 本仓库无 ./gradlew，请改写本脚本适配（或检查目录）" >&2
  exit 1
fi

TASKS=build
[ -f build-tasks.txt ] && TASKS="$(tr '\n' ' ' < build-tasks.txt)"

if [ "$CLEAN" = 1 ]; then
  ./gradlew clean --console=plain
fi
./gradlew $TASKS --console=plain

rm -rf "$OUT"
mkdir -p "$OUT"

# 收集产物 jar：排除 sources/dev 件；其余（含 shadow/-all）都收
find . -path '*/build/libs/*.jar' ! -name '*-sources.jar' ! -name '*-dev.jar' -print0 \
  | while IFS= read -r -d '' j; do cp "$j" "$OUT/"; done

# 可选过滤：build-jars.txt 每行一个 glob，只保留匹配的 jar
if [ -f build-jars.txt ]; then
  python3 - "$OUT" <<'PYEOF'
import sys, glob, pathlib
out = pathlib.Path(sys.argv[1])
keep = [p.strip() for p in pathlib.Path("build-jars.txt").read_text().splitlines() if p.strip()]
for j in list(out.glob("*.jar")):
    if not any(j.match(k) for k in keep):
        j.unlink()
PYEOF
fi

if ! ls "$OUT"/*.jar >/dev/null 2>&1; then
  echo "build.sh: 构建完成但未收集到任何产物 jar（检查 build 输出或 build-jars.txt 过滤）" >&2
  exit 1
fi

python3 - "$OUT" <<'PYEOF'
import hashlib, json, subprocess, sys, datetime, pathlib
out = pathlib.Path(sys.argv[1])

def sha256(p):
    h = hashlib.sha256()
    with open(p, "rb") as f:
        for b in iter(lambda: f.read(1 << 20), b""):
            h.update(b)
    return h.hexdigest()

def git_head():
    r = subprocess.run(["git", "rev-parse", "HEAD"], capture_output=True, text=True)
    return r.stdout.strip() if r.returncode == 0 else None

def git_remote():
    r = subprocess.run(["git", "remote", "get-url", "origin"], capture_output=True, text=True)
    return r.stdout.strip() if r.returncode == 0 else None

# version：gradle.properties 的 mod_version/version，否则 git describe
version = None
gp = pathlib.Path("gradle.properties")
if gp.exists():
    for line in gp.read_text().splitlines():
        line = line.strip()
        if line.startswith("mod_version=") or line.startswith("version="):
            version = line.split("=", 1)[1].strip()
            break
if not version:
    r = subprocess.run(["git", "describe", "--tags", "--always"], capture_output=True, text=True)
    version = r.stdout.strip() or "unknown"

jars = [{"file": j.name, "sha256": sha256(j)} for j in sorted(out.glob("*.jar"))]
info = {
    "repo": git_remote(),
    "version": version,
    "source_commit": git_head(),
    "jars": jars,
    "built_at": datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
}
(out / "build-info.json").write_text(json.dumps(info, ensure_ascii=False, indent=2) + "\n")
print(f"build.sh: {len(jars)} jar(s) -> {out}/  version={version} commit={info['source_commit']}")
PYEOF
