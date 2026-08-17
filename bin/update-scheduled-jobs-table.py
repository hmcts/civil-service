#!/usr/bin/env python3
"""Generate the scheduled jobs table in the README from scheduler definitions."""

from __future__ import annotations

import json
import re
import sys
import xml.etree.ElementTree as ET
from collections import OrderedDict
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent
CAMUNDA_DIR = PROJECT_ROOT / "src" / "main" / "resources" / "camunda"
SPRING_SCHEDULER_DIR = PROJECT_ROOT / "src" / "main" / "java" / "uk" / "gov" / "hmcts" / "reform" / "civil" / "scheduler"
APPLICATION_YAML_PATH = PROJECT_ROOT / "src" / "main" / "resources" / "application.yaml"
METADATA_PATH = PROJECT_ROOT / "config" / "scheduled-jobs.json"
README_PATH = PROJECT_ROOT / "README.md"
TABLE_START = "<!-- SCHEDULED_JOBS_TABLE_START -->"
TABLE_END = "<!-- SCHEDULED_JOBS_TABLE_END -->"

NS = {
    "bpmn": "http://www.omg.org/spec/BPMN/20100524/MODEL",
    "camunda": "http://camunda.org/schema/1.0/bpmn",
}


def load_metadata() -> dict[str, dict[str, str]]:
    if not METADATA_PATH.exists():
        return {}
    return json.loads(METADATA_PATH.read_text())


def load_scheduler_cron_defaults() -> dict[str, str]:
    if not APPLICATION_YAML_PATH.exists():
        return {}

    defaults: dict[str, str] = {}
    in_scheduler_block = False
    current_scheduler: str | None = None
    for line in APPLICATION_YAML_PATH.read_text().splitlines():
        if line == "scheduler:":
            in_scheduler_block = True
            continue
        if in_scheduler_block and line and not line.startswith(" "):
            break
        if not in_scheduler_block:
            continue

        scheduler_match = re.fullmatch(r"  ([A-Za-z0-9-]+):", line)
        if scheduler_match:
            current_scheduler = scheduler_match.group(1)
            continue

        cron_match = re.fullmatch(r"    cronExpression: \$\{[^:}]+:(.+)}", line)
        if current_scheduler and cron_match:
            defaults[f"scheduler.{current_scheduler}.cronExpression"] = cron_match.group(1)

    return defaults


def format_topics(topics: list[str]) -> str:
    if not topics:
        return ""
    return "<br>".join(f"`{topic}`" for topic in topics)


def describe_cron(expr: str) -> str:
    repeat_match = re.fullmatch(r"R(\d*)/(\d{4})-\d{2}-\d{2}T(\d{2}):(\d{2}):\d{2}Z/P1M", expr)
    if repeat_match:
        _, year, hour, minute = repeat_match.groups()
        return f"Monthly during {year} at {int(hour):02d}:{int(minute):02d} UTC"

    parts = expr.split()
    if len(parts) not in (6, 7):
        return f"See cron `{expr}`"

    second, minute, hour, dom, month, dow, *_ = parts
    year = parts[6] if len(parts) == 7 else None

    def fmt_time(hour_str: str, minute_str: str) -> str:
        return f"{int(hour_str):02d}:{int(minute_str):02d}"

    def join_times(times: list[str]) -> str:
        if len(times) == 1:
            return times[0]
        if len(times) == 2:
            return f"{times[0]} and {times[1]}"
        return ", ".join(times[:-1]) + f", and {times[-1]}"

    def describe_daily(time_text: str) -> str:
        return f"Daily at {time_text}"

    if "-" in hour:
        start, end = hour.split("-")
        start_time = fmt_time(start, minute)
        end_time = fmt_time(end, minute)
        minute_text = "the top of the hour" if minute == "0" else f"minute {int(minute):02d} past the hour"
        return f"Hourly at {minute_text} from {start_time}–{end_time}"

    if "," in hour:
        times = [fmt_time(token, minute) for token in hour.split(",")]
        frequency = {2: "Twice daily", 3: "Three times daily"}.get(len(times), "Daily")
        return f"{frequency} at {join_times(times)}"

    base = None
    def append_year(text: str) -> str:
        if year not in (None, "*", "?"):
            return f"{text} until {year}"
        return text

    if dom == "1" and month == "*" and dow in {"?", "*"}:
        base = append_year("First day of each month")
    elif dom in {"*", "?"} and dow in {"*", "?"} and month == "*":
        base = append_year("Daily")

    time_text = fmt_time(hour, minute)
    if base:
        return f"{base} at {time_text}"
    return f"See cron `{expr}`"


def gather_bpmn_jobs() -> list[dict[str, str]]:
    jobs: list[dict[str, str]] = []
    for path in sorted(CAMUNDA_DIR.rglob("*.bpmn")):
        text = path.read_text()
        if "timeCycle" not in text:
            continue
        tree = ET.fromstring(text)
        process = tree.find("bpmn:process", NS)
        if process is None:
            continue
        timers = [
            node.find("bpmn:timeCycle", NS).text.strip()
            for node in process.findall(".//bpmn:timerEventDefinition", NS)
            if node.find("bpmn:timeCycle", NS) is not None and node.find("bpmn:timeCycle", NS).text
        ]
        if not timers:
            continue
        topics_ordered: OrderedDict[str, None] = OrderedDict()
        for task in process.findall(".//bpmn:serviceTask", NS):
            topic = task.get("{http://camunda.org/schema/1.0/bpmn}topic")
            if topic:
                topics_ordered.setdefault(topic, None)
        jobs.append(
            {
                "name": process.get("name"),
                "cron": "\n".join(timers),
                "topics": list(topics_ordered.keys()),
                "springScheduler": "",
            }
        )
    return jobs


def gather_spring_jobs(cron_defaults: dict[str, str]) -> list[dict[str, str]]:
    jobs: list[dict[str, str]] = []
    if not SPRING_SCHEDULER_DIR.exists():
        return jobs

    for path in sorted(SPRING_SCHEDULER_DIR.rglob("*Scheduler.java")):
        text = path.read_text()
        if "implements CivilScheduler" not in text or "@Scheduled" not in text:
            continue

        name_match = re.search(r'SCHEDULER_NAME\s*=\s*"([^"]+)"', text)
        cron_property_match = re.search(r'@Scheduled\s*\(\s*cron\s*=\s*"\$\{([^}]+)}"', text)
        if not name_match or not cron_property_match:
            continue

        scheduler_name = name_match.group(1)
        cron_property = cron_property_match.group(1)
        cron_expr = cron_defaults.get(cron_property)
        if not cron_expr:
            continue

        jobs.append(
            {
                "name": path.stem,
                "cron": cron_expr,
                "topics": [scheduler_name],
                "springScheduler": scheduler_name,
            }
        )
    return jobs


def metadata_spring_name(metadata_entry: dict[str, str]) -> str | None:
    value = metadata_entry.get("springScheduler")
    if isinstance(value, str):
        return value
    return None


def metadata_topics(metadata_entry: dict[str, str]) -> list[str]:
    value = metadata_entry.get("topics")
    if isinstance(value, list):
        return value
    return []


def gather_jobs(metadata: dict[str, dict[str, str]]) -> list[dict[str, str]]:
    jobs_by_name = {job["name"]: job for job in gather_bpmn_jobs()}
    metadata_by_spring_name = {
        spring_name: name
        for name, entry in metadata.items()
        if (spring_name := metadata_spring_name(entry))
    }

    for spring_job in gather_spring_jobs(load_scheduler_cron_defaults()):
        metadata_name = metadata_by_spring_name.get(spring_job["springScheduler"])
        if metadata_name:
            job = jobs_by_name.get(metadata_name, {"name": metadata_name})
            job["cron"] = spring_job["cron"]
            job["springScheduler"] = spring_job["springScheduler"]
            job.setdefault("topics", metadata_topics(metadata[metadata_name]) or spring_job["topics"])
            if not job["topics"]:
                job["topics"] = metadata_topics(metadata[metadata_name]) or spring_job["topics"]
            jobs_by_name[metadata_name] = job
        else:
            jobs_by_name[spring_job["name"]] = spring_job

    jobs = list(jobs_by_name.values())
    jobs.sort(key=lambda item: item["name"].lower())
    return jobs


def build_table(jobs: list[dict[str, str]], metadata: dict[str, dict[str, str]]) -> str:
    lines = [
        "| Job | Purpose | Camunda topic(s) | Schedule (cron, UTC) | When it runs |",
        "| --- | --- | --- | --- | --- |",
    ]
    for job in jobs:
        name = job["name"]
        purpose = metadata.get(name, {}).get("purpose", "TODO: Describe this job.")
        topics = format_topics(job["topics"])
        cron_expr = job["cron"]
        when = describe_cron(cron_expr)
        lines.append(
            f"| {name} | {purpose} | {topics} | `{cron_expr}` | {when} |"
        )
    return "\n".join(lines)


def update_readme(table_markdown: str) -> None:
    content = README_PATH.read_text()
    if TABLE_START not in content or TABLE_END not in content:
        sys.exit("Unable to locate scheduled jobs markers in README.md")
    start_index = content.index(TABLE_START)
    end_index = content.index(TABLE_END, start_index) + len(TABLE_END)
    replacement = f"{TABLE_START}\n{table_markdown}\n{TABLE_END}"
    updated = content[:start_index] + replacement + content[end_index:]
    README_PATH.write_text(updated)


def main() -> None:
    metadata = load_metadata()
    jobs = gather_jobs(metadata)
    table = build_table(jobs, metadata)
    update_readme(table)
    print(f"Updated scheduled jobs table with {len(jobs)} entries.")


if __name__ == "__main__":
    main()
