"""Reject packaged charts above 90 MiB, leaving headroom below Flux's limit."""

import argparse
import sys
import tarfile


MAX_BYTES = 90 * 1024 * 1024


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("chart", help="Packaged Helm chart (.tgz)")
    args = parser.parse_args()

    # Count file contents only: tar headers and padding are not chart contents.
    # Helm package expands dependency archives into the packaged chart.
    with tarfile.open(args.chart, "r:gz") as chart:
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
