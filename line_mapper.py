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
    """Normalize line exactly like professor’s slide:
       - lowercase
       - collapse multiple spaces
       - strip whitespace
    """
    line = line.lower()
    line = re.sub(r"\s+", " ", line)
    return line.strip()

def preprocess_lines(lines: List[str]) -> List[dict]:
    """Apply normalization and keep original line number + raw text."""
    processed = []

    for idx, raw_line in enumerate(lines, start=1):
        normalized = normalize_line(raw_line)

        processed.append({
            "line_num": idx,
            "raw": raw_line,
            "norm": normalized
        })

    return processed

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


if __name__ == "__main__":
    main()
