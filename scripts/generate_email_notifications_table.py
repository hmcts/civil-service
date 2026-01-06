#!/usr/bin/env python3
"""Generate docs/email-notifications.md from source code metadata."""
from __future__ import annotations

import argparse
import html
import os
import re
import sys
import textwrap
import xml.etree.ElementTree as ET
from collections import defaultdict, OrderedDict
from pathlib import Path
from typing import Dict, List, Optional, Set

REPO_ROOT = Path(__file__).resolve().parents[1]
JAVA_ROOT = REPO_ROOT / "src" / "main" / "java"
RESOURCE_ROOT = REPO_ROOT / "src" / "main" / "resources"
APPLICATION_YAML = RESOURCE_ROOT / "application.yaml"

# Regex helpers
CLASS_DEF_RE = re.compile(r"class\s+(?P<name>[A-Za-z0-9_]+)")
BASE_CLASS_RE = re.compile(r"class\s+(?P<name>[A-Za-z0-9_]+)\s+extends\s+(?P<base>[A-Za-z0-9_]+)")
NOTIFIER_EVENT_RE = re.compile(r"return\s+(?:[A-Za-z0-9_]+\.)?([A-Za-z0-9_]+)\.toString\s*\(", re.MULTILINE)
NOTIFIER_EVENT_NAME_RE = re.compile(r"return\s+(?:[A-Za-z0-9_]+\.)?([A-Za-z0-9_]+)\.name\s*\(", re.MULTILINE)
CONST_ASSIGN_RE = re.compile(
    r"private\s+static\s+final\s+String\s+(?P<const>[A-Za-z0-9_]+)\s*=\s*(?P<value>[A-Za-z0-9_.]+)\.toString\s*\(\s*\)\s*;"
)
RETURN_CONST_RE = re.compile(r"return\s+(?P<const>[A-Za-z0-9_]+)\s*;", re.MULTILINE)
CALL_HELPER_RE = re.compile(r"(\w+)\.(get[A-Za-z0-9_]+)\s*\(")
NOTIFICATION_GETTER_RE = re.compile(r"notificationsProperties\s*\.\s*get([A-Za-z0-9_]+)")
FIELD_DEF_RE = re.compile(r"(?:private|protected|public)\s+final\s+([A-Za-z0-9_<>]+)\s+(\w+)\s*;")

BASE_PARTY_DESC = {
    "AppSolOneEmailDTOGenerator": "Applicant solicitor (LR)",
    "RespSolOneEmailDTOGenerator": "Respondent 1 solicitor (LR)",
    "RespSolTwoEmailDTOGenerator": "Respondent 2 solicitor (LR)",
    "ClaimantEmailDTOGenerator": "Claimant (LiP)",
    "DefendantEmailDTOGenerator": "Defendant 1 (LiP)",
    "DefendantTwoEmailDTOGenerator": "Defendant 2 (LiP)",
}


class JavaClass:
    def __init__(self, path: Path):
        self.path = path
        self.text = path.read_text(encoding="utf-8")

    @property
    def name(self) -> str:
        match = CLASS_DEF_RE.search(self.text)
        return match.group("name") if match else self.path.stem

    def base_class(self) -> Optional[str]:
        match = BASE_CLASS_RE.search(self.text)
        return match.group("base") if match else None

    def constructor_params(self, class_name: str) -> List[str]:
        pattern = re.compile(rf"(?:public|protected)\s+{re.escape(class_name)}\s*\((.*?)\)\s*\{{", re.S)
        match = pattern.search(self.text)
        if not match:
            return []
        params_block = match.group(1).strip()
        if not params_block:
            return []
        params, depth, current = [], 0, []
        for ch in params_block:
            if ch == ',' and depth == 0:
                params.append(''.join(current).strip())
                current = []
                continue
            current.append(ch)
            if ch == '<':
                depth += 1
            elif ch == '>':
                depth = max(depth - 1, 0)
        if current:
            params.append(''.join(current).strip())
        return params


class SourceIndex:
    def __init__(self, java_root: Path):
        self.classes: Dict[str, JavaClass] = {}
        for path in java_root.rglob('*.java'):
            cls = JavaClass(path)
            self.classes[cls.name] = cls

    def get(self, name: Optional[str]) -> Optional[JavaClass]:
        if not name:
            return None
        return self.classes.get(name)


def parse_param_type(parameter: str) -> Optional[str]:
    parameter = re.sub(r"@\w+(?:\([^)]*\))?\s*", "", parameter)
    parameter = parameter.replace("final ", "").replace("var ", "").strip()
    tokens = parameter.split()
    if len(tokens) < 2:
        return None
    return tokens[-2]


def find_notifiers(index: SourceIndex) -> List[Dict[str, Optional[str]]]:
    notifiers = []
    for class_name, java_class in index.classes.items():
        if "extends Notifier" not in java_class.text:
            continue
        event = resolve_notifier_event(java_class)
        aggregator = None
        for param in java_class.constructor_params(class_name):
            param_type = parse_param_type(param)
            agg_class = index.get(param_type)
            if agg_class and is_parties_generator(agg_class.text):
                aggregator = agg_class.name
                break
        notifiers.append({
            "class": class_name,
            "event": event,
            "aggregator": aggregator,
            "path": str(java_class.path.relative_to(REPO_ROOT))
        })
    return notifiers


def resolve_notifier_event(java_class: JavaClass) -> Optional[str]:
    direct_match = NOTIFIER_EVENT_RE.search(java_class.text)
    if direct_match:
        return direct_match.group(1)
    name_match = NOTIFIER_EVENT_NAME_RE.search(java_class.text)
    if name_match:
        return name_match.group(1)

    const_map = {
        match.group('const'): match.group('value').split('.')[-1]
        for match in CONST_ASSIGN_RE.finditer(java_class.text)
    }
    for return_match in RETURN_CONST_RE.finditer(java_class.text):
        const_name = return_match.group('const')
        if const_name in const_map:
            return const_map[const_name]
    return None


def is_parties_generator(text: str) -> bool:
    keywords = [
        "implements PartiesEmailGenerator",
        "extends AllPartiesEmailGenerator",
        "extends TrialReadyPartiesEmailGenerator",
        "extends AllLegalRepsEmailGenerator"
    ]
    return any(keyword in text for keyword in keywords)


def map_aggregators_to_generators(index: SourceIndex, aggregators: Set[str]) -> Dict[str, List[str]]:
    mapping: Dict[str, List[str]] = {}
    for agg_name in sorted(filter(None, aggregators)):
        java_class = index.get(agg_name)
        if not java_class:
            continue
        generators = []
        for param in java_class.constructor_params(agg_name):
            param_type = parse_param_type(param)
            if param_type and param_type.endswith('EmailDTOGenerator'):
                generators.append(param_type)
        mapping[agg_name] = generators
    return mapping


def parse_notifications_config(path: Path) -> Dict[str, str]:
    config: Dict[str, str] = {}
    inside = False
    with path.open(encoding="utf-8") as fh:
        for line in fh:
            if not inside:
                if line.startswith('notifications:'):
                    inside = True
                continue
            stripped_line = line.rstrip('\n')
            if not stripped_line.strip():
                continue
            if not stripped_line.startswith('  '):
                break
            stripped = stripped_line.strip()
            if ':' not in stripped:
                continue
            key, value = stripped.split(':', 1)
            key = key.strip()
            value = value.strip().strip('"')
            config[key] = value
    return config


def extract_block(text: str, start_idx: int) -> str:
    depth = 0
    body = []
    for idx in range(start_idx, len(text)):
        ch = text[idx]
        if ch == '{':
            depth += 1
        elif ch == '}':
            if depth == 0:
                break
            depth -= 1
            if depth == 0:
                return ''.join(body)
        if depth > 0:
            body.append(ch)
    return ''.join(body)


def load_helper_templates(index: SourceIndex, class_name: str, method: str, cache, seen) -> Set[str]:
    cache_key = (class_name, method)
    if cache_key in cache:
        return cache[cache_key]
    if cache_key in seen:
        return set()
    seen.add(cache_key)
    java_class = index.get(class_name)
    if not java_class:
        cache[cache_key] = set()
        return set()
    pattern = re.compile(rf"(?:public|protected|private)\s+[^{{]*{re.escape(method)}\s*\([^)]*\)\s*\{{", re.S)
    match = pattern.search(java_class.text)
    if not match:
        cache[cache_key] = set()
        return set()
    body = extract_block(java_class.text, match.end() - 1)
    templates = set(NOTIFICATION_GETTER_RE.findall(body))
    for inner in re.findall(r"(get[A-Za-z0-9_]+)\s*\(", body):
        if inner != method:
            templates.update(load_helper_templates(index, class_name, inner, cache, seen))
    cache[cache_key] = templates
    return templates


def collect_templates(index: SourceIndex, class_name: str, notifications: Dict[str, str], visited: Optional[Set[str]] = None) -> List[Dict[str, Optional[str]]]:
    visited = visited or set()
    if class_name in visited:
        return []
    visited.add(class_name)

    java_class = index.get(class_name)
    if not java_class:
        return []

    templates = set(NOTIFICATION_GETTER_RE.findall(java_class.text))
    helper_fields = {name: field_type.split('<')[-1].replace('>', '') for field_type, name in FIELD_DEF_RE.findall(java_class.text)}
    helper_cache = {}
    for var_name, method in re.findall(r"(\w+)\.(get[A-Za-z0-9_]+)\s*\(", java_class.text):
        helper_class = helper_fields.get(var_name)
        if helper_class:
            templates.update(load_helper_templates(index, helper_class, method, helper_cache, set()))

    if not templates:
        base = java_class.base_class()
        if base:
            return collect_templates(index, base, notifications, visited)

    result = []
    for getter in sorted(templates):
        prop = getter[0].lower() + getter[1:]
        result.append({
            "id": notifications.get(prop)
        })
    return result


def filter_rows_by_ccd_event(rows: List[Dict[str, str]], filters: Optional[List[str]]) -> List[Dict[str, str]]:
    if not filters:
        return rows
    lowered_filters = [flt.lower() for flt in filters if flt]
    if not lowered_filters:
        return rows
    filtered_rows = []
    for row in rows:
        ids = [event for event in row.get('ccd_event_ids', []) if event not in ('—', '')]
        if not ids:
            continue
        event_names = [event.lower() for event in ids]
        if any(any(flt in event for event in event_names) for flt in lowered_filters):
            filtered_rows.append(row)
    return filtered_rows


def describe_party(index: SourceIndex, class_name: str) -> str:
    java_class = index.get(class_name)
    if not java_class:
        return 'Custom'
    base = java_class.base_class()
    if base in BASE_PARTY_DESC:
        return BASE_PARTY_DESC[base]
    if base:
        return f"Custom (extends {base})"
    return 'Custom'


def index_bpmn(bpmn_root: Path):
    ns = {'bpmn': 'http://www.omg.org/spec/BPMN/20100524/MODEL'}
    service_map = defaultdict(list)
    ccd_events = {}
    ccd_event_labels = {}
    for bpmn_file in sorted(bpmn_root.glob('*.bpmn')):
        try:
            tree = ET.parse(bpmn_file)
        except ET.ParseError:
            continue
        root = tree.getroot()
        parent_map = {child: parent for parent in root.iter() for child in parent}
        messages = {}
        for msg in root.findall('.//bpmn:message', ns):
            messages[msg.attrib['id']] = msg.attrib.get('name')
        start_events = []
        start_labels = {}
        for start in root.findall('.//bpmn:startEvent', ns):
            for msg_def in start.findall('bpmn:messageEventDefinition', ns):
                ref = msg_def.attrib.get('messageRef')
                if ref and messages.get(ref):
                    event_name = messages[ref]
                    start_events.append(event_name)
                    parent = parent_map.get(start)
                    while parent is not None and not parent.tag.endswith('process'):
                        parent = parent_map.get(parent)
                    process_name = parent.attrib.get('name') if parent is not None else None
                    display = f"{process_name} ({event_name})" if process_name else event_name
                    start_labels[event_name] = display
        ccd_events[bpmn_file] = sorted(set(start_events))
        ccd_event_labels[bpmn_file] = start_labels
        for task in root.findall('.//bpmn:serviceTask', ns):
            service_map[task.attrib.get('id')].append(bpmn_file)
    return service_map, ccd_events, ccd_event_labels


def build_table_rows(index: SourceIndex, notifications: Dict[str, str], bpmn_root: Path):
    notifiers = find_notifiers(index)
    aggregator_map = map_aggregators_to_generators(index, {n['aggregator'] for n in notifiers})
    service_tasks, start_events, start_event_labels = index_bpmn(bpmn_root)
    rows = []
    for notifier in sorted(notifiers, key=lambda n: (n['event'] or n['class'])):
        event = notifier['event'] or 'UNKNOWN'
        generators = aggregator_map.get(notifier['aggregator'] or '', [])
        bpmn_files = service_tasks.get(event, [])
        event_label_map = OrderedDict()
        for file in bpmn_files:
            labels = start_event_labels.get(file, {})
            for event_name in start_events.get(file, []):
                event_label_map[event_name] = labels.get(event_name, event_name)
        ccd_ids = list(event_label_map.keys())
        ccd_display = list(event_label_map.values())
        for generator in generators:
            templates = collect_templates(index, generator, notifications)
            if not templates:
                templates = [{"id": None}]
            for tpl in templates:
                rows.append({
                    "event": event,
                    "handler": notifier['class'],
                    "aggregator": notifier['aggregator'] or '—',
                    "party": describe_party(index, generator),
                    "generator": generator,
                    "template_id": tpl['id'] or '—',
                    "bpmn_files": [os.path.relpath(path, REPO_ROOT) for path in bpmn_files] or ['—'],
                    "ccd_events": ccd_display or ['—'],
                    "ccd_event_ids": ccd_ids
                })
    return rows


def render_markdown(rows: List[Dict[str, str]], notify_service_id: Optional[str]) -> str:
    header = [
        "Camunda task",
        "Handler",
        "Parties selector",
        "Party",
        "Email DTO generator",
        "Gov.Notify template ID",
        "BPMN file(s)",
        "CCD event(s)"
    ]
    lines = ["# Email notification matrix", "", textwrap.dedent("""
        The table below lists every Camunda notification task handled by `NotificationHandler`, the parties contacted, and the exact Gov.Notify templates configured in `src/main/resources/application.yaml`.
        It also links each task to the BPMN model (from `civil-camunda-bpmn-definition`) and shows the CCD events that start those BPMN flows, combining the process name with the CCD event ID.
    """).strip(), ""]
    lines.append('|' + '|'.join(header) + '|')
    lines.append('|' + '|'.join(['---'] * len(header)) + '|')
    for row in rows:
        template_id_cell = f"`{row['template_id']}`"
        if notify_service_id and row['template_id'] not in ('—', ''):
            template_id_cell = (
                f"[`{row['template_id']}`](https://www.notifications.service.gov.uk/"
                f"services/{notify_service_id}/templates/{row['template_id']})"
            )
        lines.append('|' + '|'.join([
            f"`{row['event']}`",
            f"`{row['handler']}`",
            f"`{row['aggregator']}`",
            row['party'],
            f"`{row['generator']}`",
            template_id_cell,
            '<br>'.join(row['bpmn_files']),
            '<br>'.join(row['ccd_events'])
        ]) + '|')
    lines.append('')
    return '\n'.join(lines)


def render_html(rows: List[Dict[str, str]], notify_service_id: Optional[str]) -> str:
    header = [
        "Camunda task",
        "Handler",
        "Parties selector",
        "Party",
        "Email DTO generator",
        "Gov.Notify template ID",
        "BPMN file(s)",
        "CCD event(s)"
    ]
    unique_events = sorted({event for row in rows for event in row.get('ccd_event_ids', []) if event not in ('—', '')})
    lines = [
        "<!DOCTYPE html>",
        "<html lang='en'>",
        "<head>",
        "  <meta charset='utf-8'>",
        "  <title>Email notification matrix</title>",
        "  <style>",
        "    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; margin: 1.5rem; }",
        "    table { border-collapse: collapse; width: 100%; font-size: 0.85rem; }",
        "    th, td { border: 1px solid #ddd; padding: 0.4rem; vertical-align: top; }",
        "    th { position: sticky; top: 0; background: #f5f5f5; text-align: left; }",
        "    tr:nth-child(even) { background: #fafafa; }",
        "    .filter-panel { margin-bottom: 1rem; display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; }",
        "    .filter-panel label { font-weight: 600; }",
        "    select { min-width: 18rem; padding: 0.2rem; }",
        "    button { padding: 0.2rem 0.6rem; }",
        "    .counts { font-size: 0.85rem; color: #555; }",
        "  </style>",
        "</head>",
        "<body>",
        "  <h1>Email notification matrix</h1>",
        "  <p>The table mirrors <code>docs/email-notifications.md</code> but adds an interactive filter on the CCD event column."
        " Use the dropdown below to focus on a single event. Each CCD entry shows the process name followed by the CCD event ID.</p>",
        "  <div class='filter-panel'>",
        "    <label for='ccd-filter'>CCD event:</label>",
        "    <select id='ccd-filter'>",
        "      <option value=''>All CCD events</option>",
    ]
    for event in unique_events:
        lines.append(f"      <option value='{html.escape(event.lower())}'>{html.escape(event)}</option>")
    lines.extend([
        "    </select>",
        "    <button type='button' id='reset-filter'>Reset</button>",
        "    <span class='counts'><span id='visible-count'>0</span> rows shown</span>",
        "  </div>",
        "  <table id='notifications-table'>",
        "    <thead>",
        "      <tr>",
    ])
    for col in header:
        lines.append(f"        <th>{html.escape(col)}</th>")
    lines.extend([
        "      </tr>",
        "    </thead>",
        "    <tbody>",
    ])
    for row in rows:
        ccd_attr = ' '.join(event.lower() for event in row.get('ccd_event_ids', []) if event not in ('—', ''))
        template_id_cell = html.escape(row['template_id'])
        if notify_service_id and row['template_id'] not in ('—', ''):
            template_id_cell = (
                f"<a href='https://www.notifications.service.gov.uk/services/{notify_service_id}/templates/"
                f"{html.escape(row['template_id'])}'>{html.escape(row['template_id'])}</a>"
            )
        lines.extend([
            f"      <tr data-ccd-events='{ccd_attr}'>",
            f"        <td><code>{html.escape(row['event'])}</code></td>",
            f"        <td><code>{html.escape(row['handler'])}</code></td>",
            f"        <td><code>{html.escape(row['aggregator'])}</code></td>",
            f"        <td>{html.escape(row['party'])}</td>",
            f"        <td><code>{html.escape(row['generator'])}</code></td>",
            f"        <td>{template_id_cell}</td>",
            f"        <td>{'<br>'.join(html.escape(path) for path in row['bpmn_files'])}</td>",
            f"        <td>{'<br>'.join(html.escape(event) for event in row['ccd_events'])}</td>",
            "      </tr>",
        ])
    lines.extend([
        "    </tbody>",
        "  </table>",
        "  <script>",
        "    (function() {",
        "      const select = document.getElementById('ccd-filter');",
        "      const reset = document.getElementById('reset-filter');",
        "      const rows = Array.from(document.querySelectorAll('#notifications-table tbody tr'));",
        "      const counter = document.getElementById('visible-count');",
        "      function applyFilter() {",
        "        const value = (select.value || '').trim();",
        "        let visible = 0;",
        "        rows.forEach(row => {",
        "          if (!value || (row.dataset.ccdEvents || '').includes(value)) {",
        "            row.style.display = '';",
        "            visible += 1;",
        "          } else {",
        "            row.style.display = 'none';",
        "          }",
        "        });",
        "        counter.textContent = visible;",
        "      }",
        "      select.addEventListener('change', applyFilter);",
        "      reset.addEventListener('click', () => { select.value = ''; applyFilter(); });",
        "      applyFilter();",
        "    })();",
        "  </script>",
        "</body>",
        "</html>",
    ])
    return '\n'.join(lines)


def main(argv: Optional[List[str]] = None) -> int:
    parser = argparse.ArgumentParser(description="Generate docs/email-notifications.md")
    parser.add_argument('--bpmn-root', default=str((REPO_ROOT / '..' / 'civil-camunda-bpmn-definition').resolve()),
                        help='Path to civil-camunda-bpmn-definition project (default: sibling directory).')
    parser.add_argument('--output', default=str(REPO_ROOT / 'docs' / 'email-notifications.md'),
                        help='Output markdown file path.')
    parser.add_argument('--notify-service-id', default=os.environ.get('NOTIFY_SERVICE_ID')
                        or os.environ.get('GOV_NOTIFY_SERVICE_ID')
                        or 'a8b1617c-8e15-49aa-a8d3-a27a243f3c45',
                        help='Gov.Notify service ID used to build template hyperlinks.')
    parser.add_argument('--ccd-event', dest='ccd_event_filters', action='append',
                        help='Optional case-insensitive substring filter applied to the CCD event column. '
                             'Repeat the flag to OR multiple filters.')
    parser.add_argument('--html-output', default=str(REPO_ROOT / 'docs' / 'email-notifications.html'),
                        help='Path for the interactive HTML export (leave blank to skip).')
    args = parser.parse_args(argv)

    bpmn_root = Path(args.bpmn_root)
    if not bpmn_root.exists():
        sys.stderr.write(f"BPMN directory not found: {bpmn_root}\n")
        return 1

    notifications = parse_notifications_config(APPLICATION_YAML)
    index = SourceIndex(JAVA_ROOT)
    rows = build_table_rows(index, notifications, bpmn_root / 'src' / 'main' / 'resources' / 'camunda' if (bpmn_root / 'src').exists() else bpmn_root)
    rows = filter_rows_by_ccd_event(rows, args.ccd_event_filters)
    markdown = render_markdown(rows, args.notify_service_id)
    output_path = Path(args.output)
    output_path.write_text(markdown + "\n", encoding="utf-8")
    print(f"Wrote {len(rows)} rows to {output_path}")
    html_output = (args.html_output or '').strip()
    if html_output:
        html_markup = render_html(rows, args.notify_service_id)
        html_path = Path(html_output)
        html_path.write_text(html_markup + "\n", encoding="utf-8")
        print(f"Wrote interactive HTML table to {html_path}")
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
