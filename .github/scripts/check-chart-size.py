"""Reject packaged charts above 90 MiB, leaving headroom below Flux's limit."""

from pathlib import Path
import sys
import tarfile


MAX_BYTES = 90 * 1024 * 1024


def main():
    # Run from Helm's package output directory; do not accept arbitrary CLI paths.
    packages = list(Path.cwd().glob("civil-service-*.tgz"))
    if len(packages) != 1 or packages[0].is_symlink() or not packages[0].is_file():
        print("ERROR: Expected exactly one civil-service chart archive in the current directory.", file=sys.stderr)
        return 1

    # Count file contents only: tar headers and padding are not chart contents.
    # Helm package expands dependency archives into the packaged chart.
    with tarfile.open(packages[0], "r:gz") as chart:
        size = sum(member.size for member in chart if member.isfile())

    print(f"Unpacked chart size: {size / 1048576:.2f} MiB ({size} bytes); limit: 90 MiB")
    if size > MAX_BYTES:
        print(
            "ERROR: Packaged chart exceeds 90 MiB. Reduce chart contents or "
            "dependency sizes before merging to avoid blocking Flux deployments.",
            file=sys.stderr,
        )
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
