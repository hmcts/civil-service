#!/usr/bin/env python3
"""Generate docs/email-notifications.md from source code metadata."""
from __future__ import annotations

import argparse
import html
import json
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
DASHBOARD_TASK_IDS_PATH = JAVA_ROOT / 'uk' / 'gov' / 'hmcts' / 'reform' / 'civil' / 'handler' / 'callback' / 'camunda' / 'dashboardnotifications' / 'DashboardTaskIds.java'
DASHBOARD_SCENARIOS_PATH = JAVA_ROOT / 'uk' / 'gov' / 'hmcts' / 'reform' / 'civil' / 'handler' / 'callback' / 'camunda' / 'dashboardnotifications' / 'DashboardScenarios.java'
TEMPLATE_DIR = REPO_ROOT / 'dashboard-notifications' / 'src' / 'main' / 'resources' / 'notification-templates'
RAW_GITHUB_BASE_URL = "https://raw.githubusercontent.com/hmcts/civil-service/master/"
TEMPLATE_VIEWER_PATH = "dashboard-template.html"
DIAGRAM_BASE_URL = "https://raw.githubusercontent.com/hmcts/civil-camunda-bpmn-definition/master/docs/bpmn-diagrams/"
DOCMOSIS_TEMPLATE_BASE_URL = "https://github.com/hmcts/rdo-docmosis/blob/HEAD/Templates/Base/"
DOCMOSIS_TEMPLATE_PROD_URL = "https://github.com/hmcts/rdo-docmosis/blob/HEAD/Templates/Prod/"

# Regex helpers
CLASS_DEF_RE = re.compile(r"class\s+(?P<name>[A-Za-z0-9_]+)")
BASE_CLASS_RE = re.compile(r"class\s+(?P<name>[A-Za-z0-9_]+)\s+extends\s+(?P<base>[A-Za-z0-9_]+)")
IMPLEMENTS_RE = re.compile(
    r"class\s+(?P<name>[A-Za-z0-9_]+)"
    r"(?:\s+extends\s+[A-Za-z0-9_<>,\s]+)?"
    r"\s+implements\s+(?P<ifs>[A-Za-z0-9_<>,\s]+)"
)
NOTIFIER_EVENT_RE = re.compile(r"return\s+(?:[A-Za-z0-9_]+\.)?([A-Za-z0-9_]+)\.toString\s*\(", re.MULTILINE)
NOTIFIER_EVENT_NAME_RE = re.compile(r"return\s+(?:[A-Za-z0-9_]+\.)?([A-Za-z0-9_]+)\.name\s*\(", re.MULTILINE)
CONST_ASSIGN_RE = re.compile(
    r"private\s+static\s+final\s+String\s+(?P<const>[A-Za-z0-9_]+)\s*=\s*(?P<value>[A-Za-z0-9_.]+)\.toString\s*\(\s*\)\s*;"
)
RETURN_CONST_RE = re.compile(r"return\s+(?P<const>[A-Za-z0-9_]+)\s*;", re.MULTILINE)
CALL_HELPER_RE = re.compile(r"(\w+)\.(get[A-Za-z0-9_]+)\s*\(")
NOTIFICATION_GETTER_RE = re.compile(r"notificationsProperties\s*\.\s*get([A-Za-z0-9_]+)")
FIELD_DEF_RE = re.compile(r"(?:private|protected|public)\s+(?:final\s+)?([A-Za-z0-9_<>]+)\s+(\w+)\s*;")
DASHBOARD_TASK_ID_CONST_RE = re.compile(r"public\s+static\s+final\s+String\s+(\w+)\s*=\s*\"([^\"]+)\";")
TASK_ID_RE = re.compile(r"(?:public|protected|private)\s+static\s+final\s+String\s+TASK_ID\s*=\s*\"([^\"]+)\"")
TASK_ID_CONST_RE = re.compile(
    r"(?:public|protected|private)\s+static\s+final\s+String\s+[A-Za-z0-9_]*TASK_ID[A-Za-z0-9_]*\s*=\s*\"([^\"]+)\""
)
EVENTS_DEF_RE = re.compile(
    r"EVENTS\s*=\s*(?:List\.of|Collections\.singletonList|Arrays\.asList)\s*\((?P<body>.*?)\)\s*;",
    re.S
)
SCENARIO_CONST_RE = re.compile(r"SCENARIO_[A-Z0-9_]+")
SCENARIO_LITERAL_RE = re.compile(r'"(Scenario\.[A-Za-z0-9_.]+)"')
DOCMOSIS_TEMPLATE_RE = re.compile(r"DocmosisTemplates\.([A-Z0-9_]+)")
DOCMOSIS_STATIC_IMPORT_RE = re.compile(r"import\s+static\s+[^;]*DocmosisTemplates\.([A-Z0-9_]+);")
DOCMOSIS_TITLE_PREFIX_RE = re.compile(r"^(?:%s[_-]*)+")
DOCMOSIS_TITLE_SUFFIX_RE = re.compile(r"([_-]*%s)+(?=\.pdf$|$)")

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


def extends_callback_handler(index: SourceIndex, class_name: str, cache: Dict[str, bool]) -> bool:
    if class_name in cache:
        return cache[class_name]
    java_class = index.get(class_name)
    if not java_class:
        cache[class_name] = False
        return False
    if "extends CallbackHandler" in java_class.text:
        cache[class_name] = True
        return True
    base = java_class.base_class()
    if base:
        cache[class_name] = extends_callback_handler(index, base, cache)
        return cache[class_name]
    cache[class_name] = False
    return False


def extends_notification_data(index: SourceIndex, class_name: str, cache: Dict[str, bool]) -> bool:
    if class_name in cache:
        return cache[class_name]
    java_class = index.get(class_name)
    if not java_class:
        cache[class_name] = False
        return False
    if "implements NotificationData" in java_class.text:
        cache[class_name] = True
        return True
    base = java_class.base_class()
    if base:
        cache[class_name] = extends_notification_data(index, base, cache)
        return cache[class_name]
    cache[class_name] = False
    return False


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
            "id": notifications.get(prop),
            "name": prop
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
    camunda_ns = {'camunda': 'http://camunda.org/schema/1.0/bpmn'}
    service_map = defaultdict(list)
    ccd_events = {}
    ccd_event_labels = {}
    case_event_map = defaultdict(list)
    start_event_file_map = defaultdict(list)
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
        for event_name in start_events:
            start_event_file_map[event_name].append(bpmn_file)
        for task in root.findall('.//bpmn:serviceTask', ns):
            service_map[task.attrib.get('id')].append(bpmn_file)
            for input_param in task.findall('.//camunda:inputParameter', camunda_ns):
                if input_param.attrib.get('name') == 'caseEvent' and input_param.text:
                    case_event_map[input_param.text.strip()].append(bpmn_file)
    return service_map, ccd_events, ccd_event_labels, case_event_map, start_event_file_map


def build_email_rows(index: SourceIndex, notifications: Dict[str, str], bpmn_root: Path,
                     service_tasks, start_events, start_event_labels, case_event_map, start_event_file_map):
    notifiers = find_notifiers(index)
    aggregator_map = map_aggregators_to_generators(index, {n['aggregator'] for n in notifiers})
    rows = []
    for notifier in sorted(notifiers, key=lambda n: (n['event'] or n['class'])):
        event = notifier['event'] or 'UNKNOWN'
        generators = aggregator_map.get(notifier['aggregator'] or '', [])
        bpmn_files, ccd_display, ccd_ids = lookup_bpmn(event, service_tasks, start_events, start_event_labels)
        bpmn_files, ccd_display, ccd_ids = augment_with_case_events(
            [event] if event not in (None, 'UNKNOWN') else [],
            case_event_map, start_events, start_event_labels, start_event_file_map,
            bpmn_files, ccd_display, ccd_ids
        )
        for generator in generators:
            templates = collect_templates(index, generator, notifications)
            if not templates:
                templates = [{"id": None, "name": None}]
            for tpl in templates:
                rows.append({
                    "event": event,
                    "handler": notifier['class'],
                    "aggregator": notifier['aggregator'] or '—',
                    "party": describe_party(index, generator),
                    "generator": generator,
                    "template_id": tpl['id'] or '—',
                    "template_name": tpl.get('name'),
                    "bpmn_files": bpmn_files or ['—'],
                    "ccd_events": ccd_display or ['—'],
                    "ccd_event_ids": ccd_ids
                })
    notification_data_cache: Dict[str, bool] = {}
    callback_handler_cache: Dict[str, bool] = {}
    for class_name, java_class in index.classes.items():
        if '/handler/callback/camunda/notification/' not in str(java_class.path):
            continue
        if re.search(r'abstract\s+class\s+' + re.escape(class_name), java_class.text):
            continue
        if not extends_callback_handler(index, class_name, callback_handler_cache):
            continue
        if not extends_notification_data(index, class_name, notification_data_cache):
            continue
        events = extract_case_events_including_base(index, class_name)
        task_ids = extract_task_ids_including_base(index, class_name) or ['UNKNOWN']
        templates = collect_templates(index, class_name, notifications)
        if not templates:
            templates = [{"id": None, "name": None}]
        party = infer_party_from_class(java_class)
        path_lower = str(java_class.path).lower()
        if "/handler/callback/user/" in path_lower:
            channel = "Docmosis (User callback)"
        elif "/handler/callback/camunda/" in path_lower:
            channel = "Docmosis (Camunda callback)"
        else:
            channel = "Docmosis"
        for task_id in task_ids:
            bpmn_files, ccd_display, ccd_ids = lookup_bpmn(task_id, service_tasks, start_events, start_event_labels)
            bpmn_files, ccd_display, ccd_ids = augment_with_case_events(
                events, case_event_map, start_events, start_event_labels, start_event_file_map,
                bpmn_files, ccd_display, ccd_ids
            )
            for tpl in templates:
                rows.append({
                    "event": task_id,
                    "handler": class_name,
                    "aggregator": '—',
                    "party": party,
                    "generator": class_name,
                    "template_id": tpl['id'] or '—',
                    "template_name": tpl.get('name'),
                    "bpmn_files": bpmn_files or ['—'],
                    "ccd_events": ccd_display or ['—'],
                    "ccd_event_ids": ccd_ids
                })
    return rows


def lookup_bpmn(task_id: str, service_tasks, start_events, start_event_labels):
    files = service_tasks.get(task_id, [])
    rel_paths = [os.path.relpath(path, REPO_ROOT) for path in files]
    event_label_map = OrderedDict()
    for file in files:
        labels = start_event_labels.get(file, {})
        for event_name in start_events.get(file, []):
            event_label_map[event_name] = labels.get(event_name, event_name)
    ccd_ids = list(event_label_map.keys())
    ccd_display = list(event_label_map.values())
    return rel_paths, ccd_display, ccd_ids


def augment_with_case_events(events: List[str], case_event_map, start_events, start_event_labels,
                             start_event_file_map,
                             bpmn_files: List[str], ccd_display: List[str], ccd_ids: List[str],
                             event_label_map: Optional[Dict[str, str]] = None,
                             include_events: bool = False):
    files = list(bpmn_files)
    displays = list(ccd_display)
    ids = list(ccd_ids)
    seen_files = set(files)
    seen_ids = set(ids)
    seen_displays = set(displays)
    for event in events or []:
        matched_files = case_event_map.get(event, [])
        if not matched_files:
            matched_files = start_event_file_map.get(event, [])
        for file in matched_files:
            rel_path = os.path.relpath(file, REPO_ROOT)
            if rel_path not in seen_files:
                files.append(rel_path)
                seen_files.add(rel_path)
            labels = start_event_labels.get(file, {})
            for event_name in start_events.get(file, []):
                label = labels.get(event_name, event_name)
                if label not in seen_displays:
                    displays.append(label)
                    seen_displays.add(label)
                if event_name not in seen_ids:
                    ids.append(event_name)
                    seen_ids.add(event_name)
        if include_events and event and not matched_files:
            label = (event_label_map or {}).get(event, event)
            if label not in seen_displays:
                displays.append(label)
                seen_displays.add(label)
            if event not in seen_ids:
                ids.append(event)
                seen_ids.add(event)
    return files, displays, ids


def load_dashboard_task_ids() -> Dict[str, str]:
    mapping = {}
    if not DASHBOARD_TASK_IDS_PATH.exists():
        return mapping
    text = DASHBOARD_TASK_IDS_PATH.read_text(encoding='utf-8')
    for name, value in DASHBOARD_TASK_ID_CONST_RE.findall(text):
        mapping[name] = value
    return mapping


def load_dashboard_scenarios() -> Dict[str, str]:
    mapping = {}
    if not DASHBOARD_SCENARIOS_PATH.exists():
        return mapping
    text = DASHBOARD_SCENARIOS_PATH.read_text(encoding='utf-8')
    pattern = re.compile(r"(SCENARIO_[A-Z0-9_]+)\s*\(\"([^\"]+)\"\)")
    for const, value in pattern.findall(text):
        mapping[const] = value
    return mapping


def load_docmosis_templates() -> Dict[str, Dict[str, str]]:
    path = JAVA_ROOT / 'uk' / 'gov' / 'hmcts' / 'reform' / 'civil' / 'service' / 'docmosis' / 'DocmosisTemplates.java'
    if not path.exists():
        return {}
    text = path.read_text(encoding='utf-8')
    mapping: Dict[str, Dict[str, str]] = {}
    raw_titles: Dict[str, str] = {}
    enum_start = text.find("public enum DocmosisTemplates")
    if enum_start == -1:
        return {}
    block_start = text.find("{", enum_start)
    block_end = text.find("private final String template", block_start)
    if block_start == -1 or block_end == -1:
        return {}
    block = text[block_start:block_end]

    def split_args(arg_text: str) -> List[str]:
        args = []
        current = []
        depth = 0
        in_quote = False
        escape = False
        for ch in arg_text:
            if in_quote:
                current.append(ch)
                if escape:
                    escape = False
                elif ch == '\\':
                    escape = True
                elif ch == '"':
                    in_quote = False
                continue
            if ch == '"':
                in_quote = True
                current.append(ch)
                continue
            if ch == '(':
                depth += 1
                current.append(ch)
                continue
            if ch == ')':
                if depth > 0:
                    depth -= 1
                current.append(ch)
                continue
            if ch == ',' and depth == 0:
                args.append(''.join(current).strip())
                current = []
                continue
            current.append(ch)
        if current:
            args.append(''.join(current).strip())
        return args

    pattern = re.compile(r"\b([A-Z0-9_]+)\s*\(")
    for match in pattern.finditer(block):
        const = match.group(1)
        idx = match.end()
        depth = 1
        in_quote = False
        escape = False
        buf = []
        while idx < len(block) and depth > 0:
            ch = block[idx]
            buf.append(ch)
            if in_quote:
                if escape:
                    escape = False
                elif ch == '\\':
                    escape = True
                elif ch == '"':
                    in_quote = False
            else:
                if ch == '"':
                    in_quote = True
                elif ch == '(':
                    depth += 1
                elif ch == ')':
                    depth -= 1
            idx += 1
        if depth != 0:
            continue
        arg_text = ''.join(buf[:-1]).strip()
        args = split_args(arg_text)
        if len(args) < 2:
            continue
        filename = args[0].strip()
        if filename.startswith('"') and filename.endswith('"'):
            filename = filename[1:-1]
        title_expr = args[1].strip()
        raw_titles[const] = title_expr
        mapping[const] = {"filename": filename, "title": ""}

    resolved = {}
    changed = True
    while changed:
        changed = False
        for const, title_expr in raw_titles.items():
            if const in resolved:
                continue
            if title_expr.startswith('"') and title_expr.endswith('"'):
                resolved[const] = title_expr[1:-1]
                changed = True
                continue
            ref_match = re.match(r"([A-Z0-9_]+)\.getDocumentTitle\(\)", title_expr)
            if ref_match:
                ref = ref_match.group(1)
                if ref in resolved:
                    resolved[const] = resolved[ref]
                    changed = True
    for const, meta in mapping.items():
        title = resolved.get(const)
        if title:
            meta["title"] = title
    return mapping


def normalize_docmosis_title(title: Optional[str]) -> Optional[str]:
    if not title:
        return title
    cleaned = DOCMOSIS_TITLE_PREFIX_RE.sub('', title)
    cleaned = DOCMOSIS_TITLE_SUFFIX_RE.sub('', cleaned)
    return cleaned


def load_ccd_event_names(definition_root: Path) -> Dict[str, str]:
    if not definition_root or not definition_root.exists():
        return {}
    case_event_root = definition_root / "CaseEvent"
    if not case_event_root.exists():
        return {}
    names: Dict[str, tuple[int, str]] = {}
    for path in sorted(case_event_root.rglob("*.json")):
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError:
            continue
        if not isinstance(data, list):
            continue
        priority = 2
        path_str = str(path).replace("\\", "/")
        if "/CaseEvent/User/" in path_str:
            priority = 0
        elif "/CaseEvent/Camunda/" in path_str:
            priority = 1
        for entry in data:
            if not isinstance(entry, dict):
                continue
            event_id = entry.get("ID")
            name = entry.get("Name")
            if not event_id or not name:
                continue
            existing = names.get(event_id)
            if existing and existing[0] <= priority:
                continue
            names[event_id] = (priority, str(name))
    return {event_id: name for event_id, (_, name) in names.items()}


def merge_ccd_event_names(*maps: Dict[str, str]) -> Dict[str, str]:
    merged: Dict[str, str] = {}
    for mapping in maps:
        for key, value in mapping.items():
            if key not in merged and value:
                merged[key] = value
    return merged


def load_template_paths() -> Dict[str, Dict[str, str]]:
    template_map = {}
    if not TEMPLATE_DIR.exists():
        return template_map
    for path in sorted(TEMPLATE_DIR.glob('*.json')):
        try:
            data = json.loads(path.read_text(encoding='utf-8'))
        except json.JSONDecodeError:
            continue
        name = data.get('name') or path.stem
        template_map[name] = {
            'path': os.path.relpath(path, REPO_ROOT),
            'content': json.dumps(data, indent=2, ensure_ascii=False)
        }
    return template_map


def constructor_param_map(java_class: JavaClass, class_name: str) -> Dict[str, str]:
    params = {}
    pattern = re.compile(rf"(?:public|protected)\s+{re.escape(class_name)}\s*\((.*?)\)\s*\{{", re.S)
    match = pattern.search(java_class.text)
    if not match:
        return params
    block = match.group(1).strip()
    if not block:
        return params
    depth = 0
    current = []
    entries = []
    for ch in block:
        if ch == ',' and depth == 0:
            entries.append(''.join(current).strip())
            current = []
            continue
        current.append(ch)
        if ch == '<':
            depth += 1
        elif ch == '>':
            depth = max(depth - 1, 0)
    if current:
        entries.append(''.join(current).strip())
    for entry in entries:
        entry = re.sub(r"@\w+(?:\([^)]*\))?\s*", "", entry)
        entry = entry.replace('final ', '').strip()
        tokens = entry.split()
        if len(tokens) < 2:
            continue
        var_name = tokens[-1]
        type_name = tokens[-2]
        params[var_name.strip()] = type_name.strip()
    return params


def split_arguments(arg_text: str) -> List[str]:
    args = []
    depth = 0
    current = []
    for ch in arg_text:
        if ch == ',' and depth == 0:
            token = ''.join(current).strip()
            if token:
                args.append(token)
            current = []
            continue
        current.append(ch)
        if ch in '([':
            depth += 1
        elif ch in ')]':
            depth = max(depth - 1, 0)
    if current:
        token = ''.join(current).strip()
        if token:
            args.append(token)
    return args


def field_types(java_class: JavaClass) -> Dict[str, str]:
    types = {}
    for field_type, field_name in FIELD_DEF_RE.findall(java_class.text):
        simple = field_type.split('.')[-1]
        simple = simple.split('<')[-1].replace('>', '')
        types[field_name] = simple
    return types


def field_types_including_base(index: SourceIndex, class_name: str) -> Dict[str, str]:
    types: Dict[str, str] = {}
    current = class_name
    seen = set()
    while current and current not in seen:
        seen.add(current)
        java_class = index.get(current)
        if not java_class:
            break
        types.update(field_types(java_class))
        current = java_class.base_class()
    return types


def is_template_data_generator(java_class: Optional[JavaClass]) -> bool:
    if not java_class:
        return False
    return (
        "implements TemplateDataGenerator<" in java_class.text
        or "implements TemplateDataGeneratorWithAuth<" in java_class.text
        or "implements TemplateDataGenerator" in java_class.text
        or "implements TemplateDataGeneratorWithAuth" in java_class.text
    )


def collect_docmosis_templates_for_class(java_class: JavaClass) -> Set[str]:
    templates = set(DOCMOSIS_TEMPLATE_RE.findall(java_class.text))
    templates.update(DOCMOSIS_STATIC_IMPORT_RE.findall(java_class.text))
    return templates


def build_class_dependency_map(index: SourceIndex) -> Dict[str, Set[str]]:
    deps: Dict[str, Set[str]] = {}
    for class_name, java_class in index.classes.items():
        param_map = constructor_param_map(java_class, class_name)
        field_map = field_types_including_base(index, class_name)
        deps[class_name] = set(param_map.values()) | set(field_map.values())
    return deps


def build_interface_implementations(index: SourceIndex) -> Dict[str, Set[str]]:
    mapping: Dict[str, Set[str]] = {}
    for class_name, java_class in index.classes.items():
        match = IMPLEMENTS_RE.search(java_class.text)
        if not match:
            continue
        interfaces = [item.strip() for item in match.group('ifs').split(',') if item.strip()]
        for iface in interfaces:
            mapping.setdefault(iface, set()).add(class_name)
    return mapping


def resolve_docmosis_templates(class_name: str,
                               class_deps: Dict[str, Set[str]],
                               docmosis_templates_by_class: Dict[str, Set[str]],
                               interface_impls: Optional[Dict[str, Set[str]]] = None,
                               max_depth: int = 2) -> Set[str]:
    templates: Set[str] = set()
    queue = [(class_name, 0)]
    seen = set()
    while queue:
        current, depth = queue.pop(0)
        if current in seen:
            continue
        seen.add(current)
        templates.update(docmosis_templates_by_class.get(current, set()))
        if depth >= max_depth:
            continue
        for dep in class_deps.get(current, set()):
            if dep and dep not in seen:
                queue.append((dep, depth + 1))
            if interface_impls and dep in interface_impls:
                for impl in interface_impls[dep]:
                    if impl not in seen:
                        queue.append((impl, depth + 1))
    return templates


def scenario_names_for_class(index: SourceIndex,
                             class_name: Optional[str],
                             scenario_map: Dict[str, str],
                             cache: Dict[str, Set[str]],
                             visited: Optional[Set[str]] = None) -> Set[str]:
    if not class_name:
        return set()
    if class_name in cache:
        return cache[class_name]
    visited = visited or set()
    if class_name in visited:
        return set()
    visited.add(class_name)
    java_class = index.get(class_name)
    if not java_class:
        visited.remove(class_name)
        cache[class_name] = set()
        return set()
    scenarios = set()
    for const in SCENARIO_CONST_RE.findall(java_class.text):
        scenario = scenario_map.get(const)
        if scenario:
            scenarios.add(scenario)
    for literal in SCENARIO_LITERAL_RE.findall(java_class.text):
        scenarios.add(literal)
    dependencies = set()
    base = java_class.base_class()
    if base:
        dependencies.add(base)
    for simple_type in field_types(java_class).values():
        if any(keyword in simple_type for keyword in ('ScenarioService', 'DashboardService', 'ScenarioHelper')):
            dependencies.add(simple_type)
    for dep in dependencies:
        scenarios |= scenario_names_for_class(index, dep, scenario_map, cache, visited)
    visited.remove(class_name)
    cache[class_name] = scenarios
    return scenarios


def template_links_for_scenarios(scenarios: Set[str], template_map: Dict[str, Dict[str, str]]) -> List[Dict[str, str]]:
    if not scenarios:
        return []
    links = []
    for scenario in sorted(filter(None, scenarios)):
        if not scenario.startswith('Scenario.'):
            continue
        template_name = 'Notice.' + scenario.split('Scenario.', 1)[1]
        template_entry = template_map.get(template_name)
        if template_entry:
            links.append({
                'label': template_name,
                'path': template_entry['path'],
                'preview': template_entry['content']
            })
    if not links:
        links.append({'label': 'No template — task list only'})
    return links


def extends_dashboard_callback(index: SourceIndex, class_name: str,
                               cache: Dict[str, bool]) -> bool:
    if not class_name:
        return False
    if class_name in cache:
        return cache[class_name]
    java_class = index.get(class_name)
    if not java_class:
        cache[class_name] = False
        return False
    base = java_class.base_class()
    if not base:
        cache[class_name] = False
        return False
    dashboard_bases = {
        'DashboardCallbackHandler',
        'CaseProgressionDashboardCallbackHandler',
        'DashboardWithParamsCallbackHandler',
        'OrderCallbackHandler',
        'DashboardJudgementOnlineCallbackHandler'
    }
    if base == 'CallbackHandler' and 'dashboardnotifications' in str(java_class.path):
        cache[class_name] = True
        return True
    if base in dashboard_bases:
        cache[class_name] = True
        return True
    result = extends_dashboard_callback(index, base, cache)
    cache[class_name] = result
    return result


def split_arguments(arg_text: str) -> List[str]:
    args = []
    depth = 0
    current = []
    for ch in arg_text:
        if ch == ',' and depth == 0:
            token = ''.join(current).strip()
            if token:
                args.append(token)
            current = []
            continue
        current.append(ch)
        if ch in '([':
            depth += 1
        elif ch in ')]':
            depth = max(depth - 1, 0)
    if current:
        token = ''.join(current).strip()
        if token:
            args.append(token)
    return args


def collect_dashboard_contributions(index: SourceIndex, service_tasks, start_events,
                                    start_event_labels, scenario_map: Dict[str, str],
                                    template_map: Dict[str, str], scenario_cache: Dict[str, Set[str]]) -> List[Dict[str, object]]:
    task_ids_map = load_dashboard_task_ids()
    rows = []
    for class_name, java_class in index.classes.items():
        if 'extends DashboardTaskContributor' not in java_class.text:
            continue
        param_map = constructor_param_map(java_class, class_name)
        super_match = re.search(r"super\s*\((.*?)\)\s*;", java_class.text, re.S)
        if not super_match:
            continue
        args = split_arguments(super_match.group(1))
        if not args:
            continue
        raw_task_id = args[0]
        task_id = raw_task_id
        literal = re.match(r'\"([^\"]+)\"', raw_task_id)
        if literal:
            task_id = literal.group(1)
        else:
            constant = re.match(r'.*\.([A-Za-z0-9_]+)', raw_task_id)
            if constant and constant.group(1) in task_ids_map:
                task_id = task_ids_map[constant.group(1)]
        for handler_var in args[1:]:
            handler_type = param_map.get(handler_var)
            if not handler_type:
                continue
            handler_class = index.get(handler_type)
            party = infer_party_from_class(handler_class)
            service = find_dashboard_service(handler_class)
            bpmn_files, ccd_display, ccd_ids = lookup_bpmn(task_id, service_tasks, start_events, start_event_labels)
            scenarios = scenario_names_for_class(index, handler_type, scenario_map, scenario_cache)
            templates = template_links_for_scenarios(scenarios, template_map)
            rows.append({
                'framework': 'DashboardNotificationHandler',
                'camunda_task': task_id,
                'handler': handler_type,
                'party': party,
                'bpmn_files': bpmn_files or ['—'],
                'ccd_events': ccd_display or ['—'],
                'ccd_event_ids': ccd_ids,
                'templates': templates,
            })
    return rows


def infer_party_from_class(java_class: Optional[JavaClass]) -> str:
    if not java_class:
        return '—'
    name = java_class.name.lower()
    path = str(java_class.path).lower()
    if 'claimant' in name or 'claimant' in path:
        return 'Claimant'
    if 'defendant' in name or 'defendant' in path:
        return 'Defendant'
    if 'applicant' in name or 'applicant' in path:
        return 'Applicant'
    if 'respondent' in name or 'respondent' in path:
        return 'Respondent'
    if 'ga' in path:
        return 'General application'
    return '—'


def find_dashboard_service(java_class: Optional[JavaClass]) -> Optional[str]:
    if not java_class:
        return None
    for field_type, _ in FIELD_DEF_RE.findall(java_class.text):
        simple = field_type.split('.')[-1]
        if 'DashboardService' in simple:
            return simple
    return None


def extract_case_events(java_class: JavaClass) -> List[str]:
    match = EVENTS_DEF_RE.search(java_class.text)
    if not match:
        return []
    body = match.group('body')
    events = []
    for token in split_arguments(body):
        token = token.strip()
        if not token:
            continue
        token = token.split('.')[-1]
        events.append(token)
    return events


def extract_case_events_including_base(index: SourceIndex, class_name: str) -> List[str]:
    current = class_name
    seen = set()
    while current and current not in seen:
        seen.add(current)
        java_class = index.get(current)
        if not java_class:
            break
        events = extract_case_events(java_class)
        if events:
            return events
        current = java_class.base_class()
    return []


def extract_task_ids_including_base(index: SourceIndex, class_name: str) -> List[str]:
    current = class_name
    seen = set()
    task_ids: List[str] = []
    while current and current not in seen:
        seen.add(current)
        java_class = index.get(current)
        if not java_class:
            break
        task_ids.extend(TASK_ID_CONST_RE.findall(java_class.text))
        if not task_ids:
            match = TASK_ID_RE.search(java_class.text)
            if match:
                task_ids.append(match.group(1))
        current = java_class.base_class()
    return list(dict.fromkeys(task_ids))


def collect_dashboard_callback_rows(index: SourceIndex, service_tasks, start_events,
                                    start_event_labels, case_event_map, start_event_file_map,
                                    scenario_map: Dict[str, str],
                                    template_map: Dict[str, Dict[str, str]],
                                    scenario_cache: Dict[str, Set[str]]) -> List[Dict[str, object]]:
    rows = []
    inheritance_cache: Dict[str, bool] = {}
    for class_name, java_class in index.classes.items():
        if not extends_dashboard_callback(index, class_name, inheritance_cache):
            continue
        if re.search(r'abstract\s+class\s+' + re.escape(class_name), java_class.text):
            continue
        task_id_match = TASK_ID_RE.search(java_class.text)
        if not task_id_match:
            continue
        task_id = task_id_match.group(1)
        events = extract_case_events(java_class)
        framework = 'DashboardCallbackHandler'
        if 'extends CaseProgressionDashboardCallbackHandler' in java_class.text:
            framework = 'CaseProgressionDashboardCallbackHandler'
        party = infer_party_from_class(java_class)
        bpmn_files, ccd_display, ccd_ids = lookup_bpmn(task_id, service_tasks, start_events, start_event_labels)
        bpmn_files, ccd_display, ccd_ids = augment_with_case_events(
            events, case_event_map, start_events, start_event_labels, start_event_file_map,
            bpmn_files, ccd_display, ccd_ids
        )
        scenarios = scenario_names_for_class(index, class_name, scenario_map, scenario_cache)
        templates = template_links_for_scenarios(scenarios, template_map)
        rows.append({
            'framework': framework,
            'camunda_task': task_id,
            'handler': class_name,
            'party': party,
            'bpmn_files': bpmn_files or ['—'],
            'ccd_events': ccd_display or ['—'],
            'ccd_event_ids': ccd_ids,
            'templates': templates,
        })
    return rows


def build_dashboard_rows(index: SourceIndex, service_tasks, start_events, start_event_labels,
                         case_event_map, start_event_file_map,
                         scenario_map: Dict[str, str], template_map: Dict[str, Dict[str, str]]):
    scenario_cache: Dict[str, Set[str]] = {}
    contribution_rows = collect_dashboard_contributions(
        index, service_tasks, start_events, start_event_labels, scenario_map, template_map, scenario_cache)
    callback_rows = collect_dashboard_callback_rows(
        index, service_tasks, start_events, start_event_labels, case_event_map, start_event_file_map,
        scenario_map, template_map, scenario_cache)
    all_rows = contribution_rows + callback_rows
    return sorted(all_rows, key=lambda row: (row['camunda_task'], row['framework'], row['handler']))


def build_docmosis_rows(index: SourceIndex, service_tasks, start_events, start_event_labels,
                        case_event_map, start_event_file_map, docmosis_template_map: Dict[str, Dict[str, str]],
                        ccd_event_names: Optional[Dict[str, str]] = None) -> List[Dict[str, object]]:
    docmosis_templates_by_class: Dict[str, Set[str]] = {}
    for class_name, java_class in index.classes.items():
        templates = collect_docmosis_templates_for_class(java_class)
        if templates:
            docmosis_templates_by_class[class_name] = templates
    class_deps = build_class_dependency_map(index)
    interface_impls = build_interface_implementations(index)

    rows = []
    callback_handler_cache: Dict[str, bool] = {}
    for class_name, java_class in index.classes.items():
        if not extends_callback_handler(index, class_name, callback_handler_cache):
            continue
        if re.search(r'abstract\s+class\s+' + re.escape(class_name), java_class.text):
            continue
        templates_used: Set[str] = resolve_docmosis_templates(
            class_name, class_deps, docmosis_templates_by_class,
            interface_impls, max_depth=4)
        if not templates_used:
            continue
        events = extract_case_events_including_base(index, class_name)
        task_ids = extract_task_ids_including_base(index, class_name) or [class_name]
        party = infer_party_from_class(java_class)
        path_lower = str(java_class.path).lower()
        if "/handler/callback/user/" in path_lower:
            channel = "Docmosis (User callback)"
        elif "/handler/callback/camunda/" in path_lower:
            channel = "Docmosis (Camunda callback)"
        else:
            channel = "Docmosis"
        for task_id in task_ids:
            bpmn_files, ccd_display, ccd_ids = lookup_bpmn(task_id, service_tasks, start_events, start_event_labels)
            bpmn_files, ccd_display, ccd_ids = augment_with_case_events(
                events, case_event_map, start_events, start_event_labels, start_event_file_map,
                bpmn_files, ccd_display, ccd_ids,
                event_label_map=ccd_event_names,
                include_events=True
            )
            template_entries = []
            for tpl in sorted(templates_used):
                template_meta = docmosis_template_map.get(tpl) or {}
                filename = template_meta.get("filename")
                title = normalize_docmosis_title(template_meta.get("title"))
                if filename:
                    label = f"{tpl} ({filename})"
                    if title:
                        label = f"{label} - {title}"
                    link = f"{DOCMOSIS_TEMPLATE_BASE_URL}{filename}?raw=1"
                    alt_link = f"{DOCMOSIS_TEMPLATE_PROD_URL}{filename}?raw=1"
                else:
                    label = tpl
                    link = None
                    alt_link = None
                entry = {'label': label}
                if link:
                    entry['link'] = link
                if alt_link:
                    entry['alt_link'] = alt_link
                template_entries.append(entry)
            rows.append({
                'camunda_task': task_id,
                'handler': class_name,
                'channel': channel,
                'party': party,
                'bpmn_files': bpmn_files or ['—'],
                'ccd_events': ccd_display or ['—'],
                'ccd_event_ids': ccd_ids,
                'templates': template_entries,
            })
    return sorted(rows, key=lambda row: (row['camunda_task'], row['handler']))


def format_bpmn_markdown(paths: List[str]) -> str:
    formatted = []
    for path in paths:
        if path == '—':
            formatted.append('—')
        else:
            name = Path(path).name
            diagram = f"{DIAGRAM_BASE_URL}{Path(path).stem}.png"
            formatted.append(f"[{name}]({diagram})")
    return '<br>'.join(formatted)


def format_bpmn_html(paths: List[str]) -> str:
    formatted = []
    for path in paths:
        if path == '—':
            formatted.append('—')
        else:
            name = Path(path).name
            diagram = f"{DIAGRAM_BASE_URL}{Path(path).stem}.png"
            formatted.append(f"<a href='{diagram}'>{html.escape(name)}</a>")
    return '<br>'.join(formatted)


def format_templates_markdown(entries: List[Dict[str, str]], notify_service_id: Optional[str]) -> str:
    if not entries:
        return '—'
    parts = []
    for entry in entries:
        if entry.get('link'):
            link = f"[`{entry['label']}`]({entry['link']})"
        elif entry.get('gov_id'):
            label = entry['label']
            if notify_service_id:
                link = (
                    f"[`{label}`](https://www.notifications.service.gov.uk/services/"
                    f"{notify_service_id}/templates/{entry['gov_id']})"
                )
            else:
                link = f"`{label}`"
        elif entry.get('path'):
            viewer_url = f"{TEMPLATE_VIEWER_PATH}?path={entry['path']}"
            link = f"[`{entry['label']}`]({viewer_url})"
        else:
            link = f"`{entry['label']}`"
        preview = entry.get('content')
        if preview:
            link += (
                "\n\n<details><summary>Preview</summary>\n\n" \
                + "```json\n" + preview + "\n```" \
                + "\n</details>"
            )
        parts.append(link)
    return '<br>'.join(parts)


def format_templates_html(entries: List[Dict[str, str]], notify_service_id: Optional[str]) -> str:
    if not entries:
        return '—'
    parts = []
    for entry in entries:
        if entry.get('link'):
            link = f"<a class='template-link' href='{entry['link']}'><code>{html.escape(entry['label'])}</code></a>"
        elif entry.get('gov_id'):
            label = html.escape(entry['label'])
            if notify_service_id:
                link = (
                    f"<a class='template-link' href='https://www.notifications.service.gov.uk/services/{notify_service_id}/templates/"
                    f"{html.escape(entry['gov_id'])}'>{label}</a>"
                )
            else:
                link = f"<code>{label}</code>"
        elif entry.get('path'):
            viewer_url = f"{TEMPLATE_VIEWER_PATH}?path={html.escape(entry['path'])}"
            link = f"<a class='template-link' href='{viewer_url}'><code>{html.escape(entry['label'])}</code></a>"
        else:
            link = f"<code>{html.escape(entry['label'])}</code>"
        preview = entry.get('content')
        if preview:
            link += (
                f"<br><details><summary>Preview</summary><pre>{html.escape(preview)}</pre></details>"
            )
        parts.append(link)
    return '<br>'.join(parts)


def combine_rows(email_rows: List[Dict[str, object]],
                 dashboard_rows: List[Dict[str, object]],
                 docmosis_rows: List[Dict[str, object]]) -> List[Dict[str, object]]:
    combined = []
    for row in email_rows:
        templates = []
        template_id = row['template_id']
        if template_id not in ('—', ''):
            template_name = row.get('template_name')
            if template_name and template_name != template_id:
                label = f"{template_name} ({template_id})"
            else:
                label = template_id
            templates.append({'label': label, 'gov_id': template_id})
        combined.append({
            'ccd_events': row['ccd_events'],
            'ccd_event_ids': row['ccd_event_ids'],
            'camunda_task': row['event'],
            'channel': 'Email',
            'party': row['party'],
            'templates': templates,
            'bpmn_files': row['bpmn_files']
        })
    for row in dashboard_rows:
        templates = row.get('templates') or []
        combined.append({
            'ccd_events': row['ccd_events'],
            'ccd_event_ids': row['ccd_event_ids'],
            'camunda_task': row['camunda_task'],
            'channel': 'Dashboard',
            'party': row.get('party'),
            'templates': templates,
            'bpmn_files': row['bpmn_files']
        })
    for row in docmosis_rows:
        templates = row.get('templates') or []
        combined.append({
            'ccd_events': row['ccd_events'],
            'ccd_event_ids': row['ccd_event_ids'],
            'camunda_task': row['camunda_task'],
            'channel': row.get('channel', 'Docmosis'),
            'party': row.get('party'),
            'templates': templates,
            'bpmn_files': row['bpmn_files']
        })
    channel_priority = {'Email': 0, 'Dashboard': 1, 'Docmosis': 2}
    def channel_rank(value: str) -> int:
        base = (value or '').split(' ', 1)[0]
        return channel_priority.get(base, 99)
    combined.sort(key=lambda r: ((r['ccd_events'] or [''])[0].lower(), channel_rank(r['channel']), r['camunda_task']))
    return combined


def normalize_ccd_event_labels(rows: List[Dict[str, object]], ccd_event_names: Dict[str, str]) -> List[Dict[str, object]]:
    if not ccd_event_names:
        return rows
    for row in rows:
        ids = row.get('ccd_event_ids') or []
        labels = row.get('ccd_events') or []
        if not ids or not labels:
            continue
        normalized = []
        for event_id, label in zip(ids, labels):
            if event_id in ('—', ''):
                normalized.append(label)
                continue
            normalized.append(build_filter_label(event_id, label, ccd_event_names.get(event_id)))
        row['ccd_events'] = normalized
    return rows


def render_combined_markdown(rows: List[Dict[str, object]], notify_service_id: Optional[str]) -> str:
    header = [
        "CCD event(s)",
        "Camunda task",
        "BPMN file(s)",
        "Channel",
        "Party",
        "Template(s)"
    ]
    intro = textwrap.dedent("""
        The table below lists every citizen-facing notification triggered from CCD events. It combines the
        Gov.Notify emails and dashboard notices, grouping them by the CCD events that kick off the relevant
        Camunda flow. Use the template column to jump straight to the Gov.Notify template or the dashboard
        JSON housed in `dashboard-notifications`.
        \n**Tip:** To focus on a single CCD event, re-run the generator locally with `--ccd-event EVENT_ID`.
    """).strip()
    lines = [intro, ""]
    lines.append('|' + '|'.join(header) + '|')
    lines.append('|' + '|'.join(['---'] * len(header)) + '|')
    for row in rows:
        lines.append('|' + '|'.join([
            '<br>'.join(row['ccd_events'] or ['—']),
            f"`{row['camunda_task']}`",
            format_bpmn_markdown(row['bpmn_files']),
            row['channel'],
            row.get('party') or '—',
            format_templates_markdown(row.get('templates') or [], notify_service_id)
        ]) + '|')
    lines.append('')
    return '\n'.join(lines)


def build_filter_label(event_id: str, bpmn_label: Optional[str], ccd_label: Optional[str]) -> str:
    base = ccd_label or bpmn_label or event_id
    if base == event_id:
        return event_id
    normalized = base.strip()
    suffix = f"({event_id})"
    if normalized.endswith(suffix):
        return base
    return f"{base} ({event_id})"


def render_combined_html(rows: List[Dict[str, object]], notify_service_id: Optional[str],
                         ccd_event_names: Optional[Dict[str, str]] = None,
                         docmosis_template_map: Optional[Dict[str, str]] = None,
                         docmosis_rows: Optional[List[Dict[str, object]]] = None) -> str:
    header = [
        "CCD event(s)",
        "Camunda task",
        "BPMN file(s)",
        "Channel",
        "Party",
        "Template(s)"
    ]
    event_map = OrderedDict()
    for row in rows:
        ids = row.get('ccd_event_ids', []) or []
        labels = row.get('ccd_events', []) or []
        for event_id, label in zip(ids, labels):
            if event_id in ('—', ''):
                continue
            event_map.setdefault(event_id, label)
    ccd_event_names = ccd_event_names or {}
    unique_events = []
    for event_id, label in event_map.items():
        display_label = build_filter_label(event_id, label, ccd_event_names.get(event_id))
        unique_events.append((event_id, display_label))
    unique_events = sorted(unique_events, key=lambda item: (item[1] or item[0]).lower())
    lines = [
        "<!DOCTYPE html>",
        "<html lang='en'>",
        "<head>",
        "  <meta charset='utf-8'>",
        "  <title>Notification matrix</title>",
        "  <style>",
        "    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; margin: 1.5rem; }",
        "    table { border-collapse: collapse; width: 100%; font-size: 0.85rem; margin-bottom: 2rem; }",
        "    th, td { border: 1px solid #ddd; padding: 0.4rem; vertical-align: top; }",
        "    th { position: sticky; top: 0; background: #f5f5f5; text-align: left; }",
        "    tr:nth-child(even) { background: #fafafa; }",
        "    th:nth-child(6), td:nth-child(6) { min-width: 28rem; width: 40%; }",
        "    .filter-panel { margin: 0.75rem 0; display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; }",
        "    .filter-panel label { font-weight: 600; }",
        "    select { min-width: 18rem; padding: 0.2rem; }",
        "    #channel-filter { min-width: 12rem; }",
        "    button { padding: 0.2rem 0.6rem; }",
        "    .counts { font-size: 0.85rem; color: #555; }",
        "    h1 { margin-bottom: 0.25rem; }",
        "    details { margin-top: 0.25rem; }",
        "    details pre { background: #f8f8f8; padding: 0.5rem; overflow-x: auto; }",
        "    .template-link { display: inline-block; border-radius: 4px; padding: 0 0.15rem; transition: background-color 120ms ease-in-out, box-shadow 120ms ease-in-out; }",
        "    .template-link:hover, .template-link:focus-visible { background: #fff3bf; box-shadow: 0 0 0 2px #ffe066; text-decoration: none; outline: none; }",
        "  </style>",
        "</head>",
        "<body>",
        "  <h1>Notification matrix</h1>",
        "  <p>This single table combines the email and dashboard notifications for every CCD event trigger."
        " Use the CCD event filter to focus on one workflow.</p>",
        "  <div class='filter-panel'>",
        "    <label for='ccd-filter'>CCD event:</label>",
        "    <select id='ccd-filter'>",
        "      <option value=''>All CCD events</option>",
    ]
    for event_id, label in unique_events:
        lines.append(f"      <option value='{html.escape(event_id.lower())}'>{html.escape(label or event_id)}</option>")
    lines.extend([
        "    </select>",
        "    <label for='channel-filter'>Channel:</label>",
        "    <select id='channel-filter'>",
        "      <option value=''>All channels</option>",
        "      <option value='email'>Email</option>",
        "      <option value='dashboard'>Dashboard</option>",
        "      <option value='docmosis'>Docmosis</option>",
        "    </select>",
        "    <button type='button' id='reset-filter'>Reset</button>",
        "    <span class='counts'><span id='visible-count'>0</span> rows shown</span>",
        "  </div>",
    ])
    lines.extend([
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
        lines.extend([
            f"      <tr data-ccd-events='{ccd_attr}' data-channel='{html.escape(row['channel'].split(' ', 1)[0].lower())}'>",
            f"        <td>{'<br>'.join(html.escape(event) for event in row['ccd_events'])}</td>",
            f"        <td><code>{html.escape(row['camunda_task'])}</code></td>",
            f"        <td>{format_bpmn_html(row['bpmn_files'])}</td>",
            f"        <td>{html.escape(row['channel'])}</td>",
            f"        <td>{html.escape(row.get('party') or '—')}</td>",
            f"        <td>{format_templates_html(row.get('templates') or [], notify_service_id)}</td>",
            "      </tr>",
        ])
    lines.extend([
        "    </tbody>",
        "  </table>",
        "  <script>",
        "    (function() {",
        "      const select = document.getElementById('ccd-filter');",
        "      const channel = document.getElementById('channel-filter');",
        "      const reset = document.getElementById('reset-filter');",
        "      const rows = Array.from(document.querySelectorAll('#notifications-table tbody tr'));",
        "      const counter = document.getElementById('visible-count');",
        "      function applyFilter() {",
        "        const value = (select.value || '').trim();",
        "        const channelValue = (channel.value || '').trim();",
        "        let visible = 0;",
        "        rows.forEach(row => {",
        "          const tokens = (row.dataset.ccdEvents || '').split(/\s+/).filter(Boolean);",
        "          const channelToken = row.dataset.channel || '';",
        "          const matchesCcd = !value || tokens.some(token => token === value);",
        "          const matchesChannel = !channelValue || channelToken === channelValue;",
        "          if (matchesCcd && matchesChannel) {",
        "            row.style.display = '';",
        "            visible += 1;",
        "          } else {",
        "            row.style.display = 'none';",
        "          }",
        "        });",
        "        counter.textContent = visible;",
        "        window.sessionStorage.setItem('ccdFilter', value);",
        "        window.sessionStorage.setItem('channelFilter', channelValue);",
        "        if (value) {",
        "          window.location.hash = encodeURIComponent(value);",
        "        } else {",
        "          history.replaceState(null, document.title, window.location.pathname + window.location.search);",
        "        }",
        "      }",
        "      const saved = window.sessionStorage.getItem('ccdFilter') || (window.location.hash ? decodeURIComponent(window.location.hash.substring(1)) : '');",
        "      const savedChannel = window.sessionStorage.getItem('channelFilter') || '';",
        "      if (saved) {",
        "        select.value = saved;",
        "      }",
        "      if (savedChannel) {",
        "        channel.value = savedChannel;",
        "      }",
        "      select.addEventListener('change', applyFilter);",
        "      channel.addEventListener('change', applyFilter);",
        "      reset.addEventListener('click', () => { select.value = ''; channel.value = ''; applyFilter(); });",
        "      applyFilter();",
        "    })();",
        "  </script>",
        "</body>",
        "</html>",
    ])
    return '\n'.join(lines)


def main(argv: Optional[List[str]] = None) -> int:
    default_ccd_root = (REPO_ROOT / '..' / 'civil-ccd-definition' / 'ccd-definition').resolve()
    default_ga_root = (REPO_ROOT / '..' / 'civil-general-apps-ccd-definition' / 'ga-ccd-definition').resolve()
    parser = argparse.ArgumentParser(description="Generate docs/email-notifications.md")
    parser.add_argument('--bpmn-root', default=str((REPO_ROOT / '..' / 'civil-camunda-bpmn-definition').resolve()),
                        help='Path to civil-camunda-bpmn-definition project (default: sibling directory).')
    parser.add_argument('--ccd-definition-root',
                        default=str(default_ccd_root) if default_ccd_root.exists() else '',
                        help='Path to civil-ccd-definition/ccd-definition (default: sibling directory if present).')
    parser.add_argument('--ga-ccd-definition-root',
                        default=str(default_ga_root) if default_ga_root.exists() else '',
                        help='Path to civil-general-apps-ccd-definition/ga-ccd-definition (default: sibling directory if present).')
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
    camunda_root = bpmn_root / 'src' / 'main' / 'resources' / 'camunda' if (bpmn_root / 'src').exists() else bpmn_root
    service_tasks, start_events, start_event_labels, case_event_map, start_event_file_map = index_bpmn(camunda_root)
    scenario_map = load_dashboard_scenarios()
    template_map = load_template_paths()
    ccd_root = Path(args.ccd_definition_root) if args.ccd_definition_root else None
    ga_root = Path(args.ga_ccd_definition_root) if args.ga_ccd_definition_root else None
    civil_event_names = load_ccd_event_names(ccd_root) if ccd_root else {}
    ga_event_names = load_ccd_event_names(ga_root) if ga_root else {}
    ccd_event_names = merge_ccd_event_names(civil_event_names, ga_event_names)
    email_rows = build_email_rows(index, notifications, camunda_root, service_tasks, start_events, start_event_labels,
                                  case_event_map, start_event_file_map)
    dashboard_rows = build_dashboard_rows(index, service_tasks, start_events, start_event_labels, case_event_map,
                                          start_event_file_map, scenario_map, template_map)
    docmosis_template_map = load_docmosis_templates()
    docmosis_rows = build_docmosis_rows(index, service_tasks, start_events, start_event_labels, case_event_map,
                                        start_event_file_map, docmosis_template_map, ccd_event_names)
    combined_rows = combine_rows(email_rows, dashboard_rows, docmosis_rows)
    combined_rows = normalize_ccd_event_labels(combined_rows, ccd_event_names)
    filtered_rows = filter_rows_by_ccd_event(combined_rows, args.ccd_event_filters)
    markdown_parts = ["# Notification matrix", render_combined_markdown(filtered_rows, args.notify_service_id)]
    markdown = '\n\n'.join(markdown_parts)
    output_path = Path(args.output)
    output_path.write_text(markdown + "\n", encoding="utf-8")
    print(f"Wrote {len(filtered_rows)} combined rows to {output_path}")
    html_output = (args.html_output or '').strip()
    if html_output:
        html_markup = render_combined_html(
            filtered_rows,
            args.notify_service_id,
            ccd_event_names,
            docmosis_template_map,
            docmosis_rows
        )
        html_path = Path(html_output)
        html_path.write_text(html_markup + "\n", encoding="utf-8")
        print(f"Wrote interactive HTML table to {html_path}")
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
