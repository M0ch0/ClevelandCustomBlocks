#!/usr/bin/env python3
import argparse
import datetime as dt
import os
import re
import subprocess
import sys
from typing import List, Optional, Tuple

# Uses the "OpenAI-compatible" SDK pointed at NVIDIA (or any compatible endpoint)
from openai import OpenAI


def run(cmd: List[str], cwd: Optional[str] = None, check: bool = True) -> str:
    """Run a shell command and return stdout (stripped)."""
    res = subprocess.run(
        cmd, cwd=cwd, check=check, capture_output=True, text=True
    )
    return res.stdout.strip()


def get_all_tags() -> List[str]:
    # All tags (we rely on fetch-depth: 0 so tags are present)
    out = run(["git", "tag", "--list"])
    return [t for t in out.splitlines() if t]


SEMVER_RE = re.compile(
    r'^(?:v)?(?P<major>\d+)\.(?P<minor>\d+)\.(?P<patch>\d+)(?P<rest>.*)?$'
)

def parse_semver(tag: str) -> Optional[Tuple[int,int,int,str]]:
    m = SEMVER_RE.match(tag)
    if not m:
        return None
    return (
        int(m.group("major")),
        int(m.group("minor")),
        int(m.group("patch")),
        m.group("rest") or "",
    )


def previous_tag_for(new_tag: str, all_tags: List[str]) -> Optional[str]:
    """Try to find the previous tag by semver; fallback to creation date order."""
    parsed_new = parse_semver(new_tag)
    if parsed_new:
        # collect comparable semver tags
        candidates = []
        for t in all_tags:
            if t == new_tag:
                continue
            sv = parse_semver(t)
            if sv:
                candidates.append((sv, t))
        # pick the highest semver lower than new_tag
        lower = [t for (sv, t) in candidates if sv < parsed_new]
        if lower:
            # sort ascending then pick the max lower
            lower.sort()
            return lower[-1]

    # Fallback: creation date
    # List tags by creatordate descending; pick the first older than new_tag
    out = run([
        "git", "for-each-ref",
        "--sort=-creatordate",
        "--format=%(refname:short)",
        "refs/tags"
    ])
    ordered = [t for t in out.splitlines() if t]
    if new_tag in ordered:
        idx = ordered.index(new_tag)
        if idx + 1 < len(ordered):
            return ordered[idx + 1]
    # No previous tag found
    return None


def compare_url() -> Optional[str]:
    try:
        remote = run(["git", "config", "--get", "remote.origin.url"])
    except subprocess.CalledProcessError:
        return None
    remote = remote.strip()

    # Support SSH or HTTPS; normalize to https://github.com/owner/repo
    # SSH: git@github.com:owner/repo.git
    # HTTPS: https://github.com/owner/repo.git
    owner_repo = None
    if remote.startswith("git@github.com:"):
        owner_repo = remote.split("git@github.com:")[1]
    elif remote.startswith("https://github.com/"):
        owner_repo = remote.split("https://github.com/")[1]
    elif remote.startswith("http://github.com/"):
        owner_repo = remote.split("http://github.com/")[1]

    if not owner_repo:
        return None
    owner_repo = owner_repo[:-4] if owner_repo.endswith(".git") else owner_repo
    return f"https://github.com/{owner_repo}"


def git_range_or_initial(prev_tag: Optional[str], new_tag: str) -> str:
    if prev_tag:
        return f"{prev_tag}..{new_tag}"
    # No previous tag — diff from first commit to new_tag
    first = run(["git", "rev-list", "--max-parents=0", "HEAD"]).splitlines()[0]
    return f"{first}..{new_tag}"


def collect_commits(git_range: str, limit_chars: int = 120_000) -> str:
    """
    Collect commit subjects and bodies in a compact, parseable way.
    We keep it fairly long but cap character count to avoid blowing token limits.
    """
    fmt = r"%H|||%h|||%an|||%ad|||%s|||%bEND"
    log = run([
        "git", "log", "--date=short", f"--pretty=format:{fmt}", git_range
    ])
    text = []
    total = 0
    for line in log.split("END"):
        line = line.strip()
        if not line:
            continue
        if total + len(line) > limit_chars:
            text.append("\n[... truncated commit list ...]\n")
            break
        text.append(line)
        total += len(line)
    return "\n".join(text)


def collect_diff_summary(git_range: str, limit_chars: int = 80_000) -> str:
    """
    Collect a concise diff: name-status and shortstat.
    """
    try:
        name_status = run(["git", "diff", "--name-status", git_range])
    except subprocess.CalledProcessError:
        name_status = ""
    try:
        shortstat = run(["git", "diff", "--shortstat", git_range])
    except subprocess.CalledProcessError:
        shortstat = ""

    diff = f"""# File changes (git diff --name-status {git_range})
{name_status}

# Summary (git diff --shortstat {git_range})
{shortstat}
"""
    if len(diff) > limit_chars:
        diff = diff[:limit_chars] + "\n[... diff summary truncated ...]\n"
    return diff.strip() + "\n"


def ensure_changelog(path: str) -> None:
    if os.path.exists(path):
        return
    with open(path, "w", encoding="utf-8") as f:
        f.write(
            "# Changelog\n\n"
            "All notable changes to this project will be documented in this file.\n\n"
        )


def insert_or_replace_section(
        changelog_text: str, new_section_text: str, version_header: str
) -> str:
    """
    Insert the new section after the top header OR replace an existing section
    that begins with the given version header (e.g., '## [v1.2.3] - 2025-09-11').
    We match by version number regardless of date to allow idempotent updates.
    """
    # Extract the version token inside the bracket from the header
    m = re.match(r"^##\s*\[\s*(?P<ver>[^]]+)\s*]", version_header)
    if not m:
        # As a fallback, just prepend
        return prepend_after_top_header(changelog_text, new_section_text)

    version_token = re.escape(m.group("ver"))
    # Pattern to find any header that starts with "## [<ver>]" ignoring date
    section_start_re = re.compile(rf"(?m)^##\s*\[\s*{version_token}\s*].*$" )
    matches = list(section_start_re.finditer(changelog_text))
    if not matches:
        return prepend_after_top_header(changelog_text, new_section_text)

    start_idx = matches[0].start()

    # Find the start of the next section (or end of file)
    next_header = re.compile(r"(?m)^##\s*\[")
    next_match = next_header.search(changelog_text, matches[0].end())
    end_idx = next_match.start() if next_match else len(changelog_text)

    # Replace the existing section
    return changelog_text[:start_idx] + new_section_text + "\n" + changelog_text[end_idx:]


def prepend_after_top_header(changelog_text: str, new_section_text: str) -> str:
    # If file starts with a level-1 header, place after that; else prepend.
    m = re.match(r"^# .*\n(\n+)?", changelog_text)
    if m:
        insert_at = m.end()
        return changelog_text[:insert_at] + new_section_text + "\n" + changelog_text[insert_at:]
    else:
        return new_section_text + "\n" + changelog_text


def build_user_context(
        repo_url: Optional[str],
        prev_tag: Optional[str],
        new_tag: str,
        compare_url_base: Optional[str],
        commits: str,
        diff_summary: str,
) -> str:
    today = dt.date.today().isoformat()
    compare_url = None
    if compare_url_base and prev_tag:
        compare_url = f"{compare_url_base}/compare/{prev_tag}...{new_tag}"
    elif compare_url_base:
        compare_url = f"{compare_url_base}/tree/{new_tag}"

    header = [
        f"Repository: {repo_url or 'unknown'}",
        f"New tag: {new_tag}",
        f"Previous tag: {prev_tag or '(none — first release or tag)'}",
        f"Release date (YYYY-MM-DD): {today}",
    ]
    if compare_url:
        header.append(f"Compare URL: {compare_url}")
    header_text = "\n".join(header)

    body = f"""
{header_text}

=== COMMITS BEGIN ===
{commits}
=== COMMITS END ===

=== DIFF SUMMARY BEGIN ===
{diff_summary}
=== DIFF SUMMARY END ===
"""
    return body.strip()


def main():
    parser = argparse.ArgumentParser(description="Generate and prepend a changelog entry using an LLM.")
    parser.add_argument("--new-tag", dest="new_tag", default=os.getenv("NEW_TAG"))
    parser.add_argument("--prev-tag", dest="prev_tag", default=os.getenv("PREV_TAG"))
    parser.add_argument("--instruct", dest="instruct_path", default=os.getenv("INSTRUCT_PATH", "changelog_instruct.txt"))
    parser.add_argument("--changelog", dest="changelog_path", default=os.getenv("CHANGELOG_PATH", "CHANGELOG.md"))
    parser.add_argument("--model", dest="model", default=os.getenv("LLM_MODEL", "qwen/qwen3-coder-480b-a35b-instruct"))
    parser.add_argument("--base-url", dest="base_url", default=os.getenv("LLM_BASE_URL", "https://integrate.api.nvidia.com/v1"))
    parser.add_argument("--api-key", dest="api_key", default=os.getenv("LLM_API_KEY"))
    parser.add_argument("--temperature", type=float, default=float(os.getenv("LLM_TEMPERATURE", "0.2")))
    parser.add_argument("--max-tokens", type=int, default=int(os.getenv("LLM_MAX_TOKENS", "8192")))
    args = parser.parse_args()

    if not args.new_tag:
        print("ERROR: --new-tag or NEW_TAG env var is required (e.g., v1.2.3).", file=sys.stderr)
        sys.exit(2)

    # Resolve prev tag if not provided
    all_tags = get_all_tags()
    prev_tag = args.prev_tag or previous_tag_for(args.new_tag, all_tags)

    # Build git range
    rng = git_range_or_initial(prev_tag, args.new_tag)

    # Collect context
    commits = collect_commits(rng)
    diff_summary = collect_diff_summary(rng)
    repo_url = compare_url()
    user_context = build_user_context(repo_url, prev_tag, args.new_tag, repo_url, commits, diff_summary)

    # Load instructions (system prompt)
    if not os.path.exists(args.instruct_path):
        print(f"ERROR: instructions file not found: {args.instruct_path}", file=sys.stderr)
        sys.exit(2)
    with open(args.instruct_path, "r", encoding="utf-8") as f:
        system_instructions = f.read().strip()

    # Call LLM
    if not args.api_key:
        print("ERROR: LLM_API_KEY not provided (set env secret).", file=sys.stderr)
        sys.exit(2)

    client = OpenAI(base_url=args.base_url, api_key=args.api_key)

    messages = [
        {"role": "system", "content": system_instructions},
        {"role": "user", "content": user_context},
    ]

    completion = client.chat.completions.create(
        model=args.model,
        messages=messages,
        temperature=args.temperature,
        top_p=0.9,
        max_tokens=args.max_tokens,
    )
    content = completion.choices[0].message.content.strip()

    # Ensure we got a section header for this version
    # First line should look like: ## [vX.Y.Z] - YYYY-MM-DD
    header_line = f"## [{args.new_tag}] - {dt.date.today().isoformat()}"
    if not content.lstrip().startswith("## ["):
        # If model returned just the body, add header
        content = f"{header_line}\n\n{content}"
    else:
        # Keep for section replacement logic
        header_line = content.splitlines()[0].strip()

    ensure_changelog(args.changelog_path)
    with open(args.changelog_path, "r", encoding="utf-8") as f:
        old = f.read()

    new_text = insert_or_replace_section(old, content.strip() + "\n", header_line)

    if new_text != old:
        with open(args.changelog_path, "w", encoding="utf-8") as f:
            f.write(new_text)
        print(f"CHANGELOG updated for {args.new_tag}.")
    else:
        print("CHANGELOG already up to date for this tag.")


if __name__ == "__main__":
    main()
