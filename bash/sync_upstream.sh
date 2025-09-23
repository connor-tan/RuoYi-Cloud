#!/usr/bin/env bash
set -euo pipefail

# ==========================================
# RuoYi-Cloud 上游同步脚本
# - 流程：fetch upstream -> 重置镜像分支 -> 合并到工作分支 -> 推送 origin
# - 默认分支：
#     upstream remote   : upstream (https://github.com/yangzongzhuan/RuoYi-Cloud.git)
#     upstream branch   : springboot3
#     vendor mirror     : vendor/springboot3   (只追踪上游，永不直改)
#     work branch       : connor/springboot3   (你的开发分支)
# - 需要 Git 2.23+ (支持 git switch / restore)
#
# 用法（参数可选）:
#   ./sync_upstream.sh \
#     -U upstream           # 上游 remote 名称（默认 upstream）
#     -O origin             # 你的 fork remote 名称（默认 origin）
#     -b springboot3        # 上游分支名（默认 springboot3）
#     -v vendor/springboot3 # 镜像分支名（默认 vendor/springboot3）
#     -w connor/springboot3 # 工作分支名（默认 connor/springboot3）
#     -p                    # 合并成功后 push 到 origin
#
# 退出码：
#   0 正常；2 合并冲突(需手动解决)；>2 其它错误
# ==========================================

UPSTREAM_REMOTE="upstream"
ORIGIN_REMOTE="origin"
UPSTREAM_BRANCH="springboot3"
VENDOR_BRANCH="vendor/springboot3"
WORK_BRANCH="connor/springboot3"
DO_PUSH=false

usage() {
  grep '^# ' "$0" | sed 's/^# \{0,1\}//'
  exit 0
}

while getopts ":U:O:b:v:w:ph" opt; do
  case $opt in
    U) UPSTREAM_REMOTE="$OPTARG" ;;
    O) ORIGIN_REMOTE="$OPTARG" ;;
    b) UPSTREAM_BRANCH="$OPTARG" ;;
    v) VENDOR_BRANCH="$OPTARG" ;;
    w) WORK_BRANCH="$OPTARG" ;;
    p) DO_PUSH=true ;;
    h) usage ;;
    \?) echo "非法参数: -$OPTARG"; usage ;;
    :)  echo "缺少参数值: -$OPTARG"; usage ;;
  esac
done

log() { printf '\n[SYNC] %s\n' "$*"; }
fail() { echo "[ERROR] $*" >&2; exit 3; }

# --- 0) 基础检查 ---
git rev-parse --is-inside-work-tree >/dev/null 2>&1 || fail "当前目录不是 git 仓库"
command -v git >/dev/null || fail "未找到 git 命令"

# --- 1) 显示配置 ---
log "配置:
  upstream remote   : $UPSTREAM_REMOTE
  origin   remote   : $ORIGIN_REMOTE
  upstream branch   : $UPSTREAM_BRANCH
  vendor   branch   : $VENDOR_BRANCH
  work     branch   : $WORK_BRANCH
  push after merge  : $DO_PUSH"

# --- 2) 校验 remotes ---
git remote get-url "$UPSTREAM_REMOTE" >/dev/null 2>&1 || fail "缺少 upstream remote: $UPSTREAM_REMOTE"
git remote get-url "$ORIGIN_REMOTE"   >/dev/null 2>&1 || fail "缺少 origin remote: $ORIGIN_REMOTE"

# --- 3) 抓取上游 ---
log "fetch 上游：$UPSTREAM_REMOTE"
git fetch --prune "$UPSTREAM_REMOTE"

# 确认上游分支存在
git show-ref --verify --quiet "refs/remotes/$UPSTREAM_REMOTE/$UPSTREAM_BRANCH" \
  || fail "上游不存在分支：$UPSTREAM_REMOTE/$UPSTREAM_BRANCH"

# --- 4) 准备镜像分支（vendor）---
if git show-ref --verify --quiet "refs/heads/$VENDOR_BRANCH"; then
  log "切换镜像分支：$VENDOR_BRANCH"
  git switch "$VENDOR_BRANCH"
else
  log "创建镜像分支：$VENDOR_BRANCH <- $UPSTREAM_REMOTE/$UPSTREAM_BRANCH"
  git switch -c "$VENDOR_BRANCH" "$UPSTREAM_REMOTE/$UPSTREAM_BRANCH"
fi

# 强制对齐到上游（镜像分支不保留本地提交）
log "镜像分支硬重置到上游最新"
git reset --hard "$UPSTREAM_REMOTE/$UPSTREAM_BRANCH"

# --- 5) 准备工作分支（你的开发分支）---
if git show-ref --verify --quiet "refs/heads/$WORK_BRANCH"; then
  log "切换工作分支：$WORK_BRANCH"
  git switch "$WORK_BRANCH"
else
  log "创建工作分支：$WORK_BRANCH（首次初始化）"
  git switch -c "$WORK_BRANCH"
  # 若远端无跟踪，后续第一次推送会自动建立
fi

# 开启 rerere，减少重复冲突处理
git config rerere.enabled true

# --- 6) 合并 vendor -> work ---
log "合并上游：merge $VENDOR_BRANCH -> $WORK_BRANCH（保留你的改动，冲突需手动处理）"
set +e
git merge --no-ff "$VENDOR_BRANCH"
merge_rc=$?
set -e

if [ $merge_rc -ne 0 ]; then
  # 检查是否为冲突
  if git diff --name-only --diff-filter=U | grep -q .; then
    log "检测到冲突，列出冲突文件："
    git diff --name-only --diff-filter=U | sed 's/^/  - /'
    cat <<'HINT'

处理建议：
  # 保留“我的版本”（当前工作分支 = ours）
  git restore --ours   path/to/file && git add path/to/file

  # 使用“上游版本”（被合并进来的镜像分支 = theirs）
  git restore --theirs path/to/file && git add path/to/file

  # 解决全部文件后：
  git commit
  # 测试通过后如需推送：
  # git push

HINT
    exit 2
  else
    fail "合并失败（非冲突），退出码：$merge_rc"
  fi
fi

log "合并完成。"

# --- 7) 推送（可选）---
if $DO_PUSH; then
  log "推送到 $ORIGIN_REMOTE/$WORK_BRANCH"
  # 若未建立跟踪，添加 -u
  if git rev-parse --abbrev-ref --symbolic-full-name "@{u}" >/dev/null 2>&1; then
    git push "$ORIGIN_REMOTE" "$WORK_BRANCH"
  else
    git push -u "$ORIGIN_REMOTE" "$WORK_BRANCH"
  fi
  log "推送完成。"
else
  log "未开启自动推送。若需要： git push -u $ORIGIN_REMOTE $WORK_BRANCH"
fi

log "✅ 同步完成。建议现在运行编译/测试来验证。"
