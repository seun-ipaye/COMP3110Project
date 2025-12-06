import os
from typing import List, Tuple, Optional


def prompt_for_file_path(label: str) -> Optional[str]:
    """
    Ask the user for a file path.
    Returns:
      - the file path string, or
      - None if the user enters 'q' to quit.
    """
    while True:
        raw = input(f"Enter the path of the {label} file (or 'q' to exit): ").strip()

        if raw.lower() == "q":
            return None

        # Expand ~ and remove quotes if user copies from file explorer
        path = os.path.expanduser(raw.strip('"'))

        if not path:
            print("  Error: empty path, please try again.\n")
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
    """
    Load a file and return a list of its lines (with newline stripped).
    """
    with open(path, "r", encoding="utf-8", errors="replace") as f:
        return [line.rstrip("\n") for line in f]


def get_two_files() -> Optional[Tuple[List[str], List[str]]]:
    """
    Ask for old and new file paths, validate them,
    and return their contents as lists of lines.
    Returns:
      - (old_lines, new_lines) on success
      - None if the user quits.
    """
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
    print(f"  Loaded NEW file: {new_path}  ({len(new_lines)} lines)")

    return old_lines, new_lines


def main():
    result = get_two_files()
    if result is None:
        return

    old_lines, new_lines = result

    # For now, just show a small preview so we know it worked
    print("\n=== Preview: first few lines of OLD file ===")
    for i, line in enumerate(old_lines[:5], start=1):
        print(f"{i:3}: {line}")

    print("\n=== Preview: first few lines of NEW file ===")
    for i, line in enumerate(new_lines[:5], start=1):
        print(f"{i:3}: {line}")

    print("\nStep 1 complete: files are loaded into memory as lists of lines.")


if __name__ == "__main__":
    main()
