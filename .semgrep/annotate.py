#!/usr/bin/env python3

import json
import sys
from pathlib import Path


def escape_message(value: str) -> str:
    return value.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")


def escape_property(value: str) -> str:
    return escape_message(value).replace(":", "%3A").replace(",", "%2C")


def main() -> int:
    if len(sys.argv) != 2:
        print("Usage: annotate.py <semgrep-results.json>", file=sys.stderr)
        return 2

    results_path = Path(sys.argv[1])
    report = json.loads(results_path.read_text(encoding="utf-8"))
    findings = sorted(
        report.get("results", []),
        key=lambda finding: (finding["path"], finding["start"]["line"]),
    )

    for finding in findings:
        start = finding["start"]
        end = finding["end"]
        check_id = finding["check_id"]
        message = finding["extra"]["message"]
        properties = ",".join([
            f"file={escape_property(finding['path'])}",
            f"line={start['line']}",
            f"col={start['col']}",
            f"endLine={end['line']}",
            f"endColumn={end['col']}",
            f"title={escape_property(f'PII logging check ({check_id})')}",
        ])
        print(f"::warning {properties}::{escape_message(message)}")

    print(f"PII logging check found {len(findings)} new issue(s); advisory mode is enabled.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
