import os
from typing import List, Tuple, Optional
import re
from Levenshtein import ratio as levenshtein_ratio

#Input Handling
def prompt_for_file_path(label: str) -> Optional[str]:
    while True:
        raw = input(f"Enter the path of the {label} file (or 'q' to exit): ").strip()

        if raw.lower() == "q":
            return None

        path = os.path.expanduser(raw.strip('"'))

        if not path:
            print("  Error: empty path.\n")
            continue

        if not os.path.exists(path):
            print(f"  Error: file '{path}' does not exist.\n")
            continue

        if not os.path.isfile(path):
            print(f"  Error: '{path}' is not a regular file.\n")
            continue

        if not os.access(path, os.R_OK):
            print(f"  Error: file '{path}' is not readable.\n")
            continue

        return path


def load_file_lines(path: str) -> List[str]:
    with open(path, "r", encoding="utf-8", errors="replace") as f:
        return [line.rstrip("\n") for line in f]


def get_two_files() -> Optional[Tuple[List[str], List[str]]]:
    print("=== Line Mapper (Step 1: file input) ===")

    old_path = prompt_for_file_path("OLD")
    if old_path is None:
        print("Exiting.")
        return None

    new_path = prompt_for_file_path("NEW")
    if new_path is None:
        print("Exiting.")
        return None

    print("\nLoading files...")
    old_lines = load_file_lines(old_path)
    new_lines = load_file_lines(new_path)

    print(f"  Loaded OLD file: {old_path}  ({len(old_lines)} lines)")
    print(f"  Loaded NEW file: {new_path}  ({len(new_lines)} lines)\n")

    return old_lines, new_lines

#Preprocessing
def normalize_line(line: str) -> str:
    line = line.lower()
    line = re.sub(r"\s+", " ", line)
    return line.strip()

def preprocess_lines(lines: List[str]) -> List[dict]:
    processed = []

    for idx, raw_line in enumerate(lines, start=1):
        normalized = normalize_line(raw_line)

        processed.append({
            "line_num": idx,
            "raw": raw_line,
            "norm": normalized
        })

    return processed

#3

def lcs_table(a: List[str], b: List[str]) -> List[List[int]]:
    n, m = len(a), len(b)
    dp = [[0]*(m+1) for _ in range(n+1)]

    for i in range(n):
        for j in range(m):
            if a[i] == b[j]:
                dp[i+1][j+1] = dp[i][j] + 1
            else:
                dp[i+1][j+1] = max(dp[i][j+1], dp[i+1][j])
    return dp


def recover_lcs_pairs(a: List[str], b: List[str], dp: List[List[int]]):
    i, j = len(a), len(b)
    pairs = []

    while i > 0 and j > 0:
        if a[i-1] == b[j-1]:
            pairs.append((i-1, j-1))
            i -= 1
            j -= 1
        else:
            if dp[i-1][j] >= dp[i][j-1]:
                i -= 1
            else:
                j -= 1

    pairs.reverse()
    return pairs


def classify_unmatched(old_len: int, new_len: int, unchanged_pairs):
    unchanged_old = {i for (i, j) in unchanged_pairs}
    unchanged_new = {j for (i, j) in unchanged_pairs}

    left_list = [i for i in range(old_len) if i not in unchanged_old]
    right_list = [j for j in range(new_len) if j not in unchanged_new]

    return left_list, right_list


def detect_unchanged(old_processed, new_processed):
    old_norm = [entry["norm"] for entry in old_processed]
    new_norm = [entry["norm"] for entry in new_processed]

    dp = lcs_table(old_norm, new_norm)
    unchanged_pairs = recover_lcs_pairs(old_norm, new_norm, dp)

    left_list, right_list = classify_unmatched(
        len(old_norm),
        len(new_norm),
        unchanged_pairs
    )

    return unchanged_pairs, left_list, right_list

# -----------------------------
# Step 4: Candidate Generation
# -----------------------------



def get_context(norm_lines: List[str], index: int, window: int = 4) -> str:
    """Return normalized context window: 4 lines above + 4 lines below."""
    start = max(0, index - window)
    end = min(len(norm_lines), index + window + 1)
    return " ".join(norm_lines[start:end])


def simhash(text: str, bits: int = 64) -> int:
    """Compute a simple simhash for a string."""
    if not text:
        return 0

    v = [0] * bits

    for token in text.split():
        h = hash(token)  # built-in hash
        for i in range(bits):
            bitmask = 1 << i
            if h & bitmask:
                v[i] += 1
            else:
                v[i] -= 1

    # Build final hash
    fingerprint = 0
    for i in range(bits):
        if v[i] >= 0:
            fingerprint |= (1 << i)

    return fingerprint


def hamming_distance(x: int, y: int) -> int:
    return bin(x ^ y).count("1")


def compute_similarity(old_line: str, new_line: str,
                       old_context: str, new_context: str) -> float:
    """Compute combined content + context similarity."""
    content_sim = levenshtein_ratio(old_line, new_line)
    context_sim = levenshtein_ratio(old_context, new_context)

    score = 0.6 * content_sim + 0.4 * context_sim
    return score


def generate_candidates(old_processed, new_processed,
                        left_list, right_list, k: int = 5):
    """Generate top-k candidate matches for each deleted line."""

    # Pre-extract normalized lines
    old_norm = [x["norm"] for x in old_processed]
    new_norm = [x["norm"] for x in new_processed]

    # Precompute contexts
    old_contexts = [get_context(old_norm, i) for i in range(len(old_norm))]
    new_contexts = [get_context(new_norm, j) for j in range(len(new_norm))]

    # Precompute simhash for bonus marks
    old_hashes = [simhash(old_norm[i] + " " + old_contexts[i]) for i in range(len(old_norm))]
    new_hashes = [simhash(new_norm[j] + " " + new_contexts[j]) for j in range(len(new_norm))]

    candidate_map = {}

    for oi in left_list:
        scores = []

        for nj in right_list:
            # Optional: quick reject using simhash distance
            ham = hamming_distance(old_hashes[oi], new_hashes[nj])
            if ham > 20:  # threshold; adjust as needed
                continue

            score = compute_similarity(
                old_norm[oi], new_norm[nj],
                old_contexts[oi], new_contexts[nj]
            )

            scores.append((score, oi, nj))

        # Sort highest score first
        scores.sort(reverse=True, key=lambda x: x[0])

        # Take top-k
        candidate_map[oi] = scores[:k]

    return candidate_map



#main

def main():
    result = get_two_files()
    if result is None:
        return

    old_lines, new_lines = result

    print("\n=== Step 2: Preprocessing ===")
    old_processed = preprocess_lines(old_lines)
    new_processed = preprocess_lines(new_lines)

    print("\nOLD FILE (first 5 preprocessed lines):")
    for entry in old_processed[:5]:
        print(f"Line {entry['line_num']:3}: norm = {entry['norm']}")

    print("\nNEW FILE (first 5 preprocessed lines):")
    for entry in new_processed[:5]:
        print(f"Line {entry['line_num']:3}: norm = {entry['norm']}")

    print("\nStep 2 complete.\n")

    print("\n=== Step 3: LCS UNCHANGED DETECTION ===")
    unchanged, left_list, right_list = detect_unchanged(old_processed, new_processed)

    print("\nUNCHANGED PAIRS:")
    for (oi, nj) in unchanged:
        print(f"  old {oi+1} ↔ new {nj+1}")

    print("\nLEFT LIST (deleted candidates):", [i+1 for i in left_list])
    print("RIGHT LIST (added candidates):", [j+1 for j in right_list])

    print("\nStep 3 complete.\n")
    print("\n=== Step 4: Candidate Matching ===")
    candidates = generate_candidates(old_processed, new_processed, left_list, right_list)

    for oi, cand_list in candidates.items():
        print(f"\nOld line {oi+1} candidates:")
        for score, old_i, new_j in cand_list:
            print(f"   → new {new_j+1}  (score={score:.3f})")

    print("\nStep 4 complete.\n")



if __name__ == "__main__":
    main()
