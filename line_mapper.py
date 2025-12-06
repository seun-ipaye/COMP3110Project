import os
from typing import List, Tuple, Optional
import re

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


if __name__ == "__main__":
    main()
