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
DIAGRAM_BASE_URL = "https://raw.githubusercontent.com/hmcts/civil-camunda-bpmn-definition/master/docs/bpmn-diagrams/"

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
DASHBOARD_TASK_ID_CONST_RE = re.compile(r"public\s+static\s+final\s+String\s+(\w+)\s*=\s*\"([^\"]+)\";")
TASK_ID_RE = re.compile(r"(?:public|protected|private)\s+static\s+final\s+String\s+TASK_ID\s*=\s*\"([^\"]+)\"")
EVENTS_DEF_RE = re.compile(
    r"EVENTS\s*=\s*(?:List\\.of|Collections\\.singletonList|Arrays\\.asList)\s*\((?P<body>.*?)\)\s*;",
    re.S
)
SCENARIO_CONST_RE = re.compile(r"SCENARIO_[A-Z0-9_]+")
SCENARIO_LITERAL_RE = re.compile(r'"(Scenario\.[A-Za-z0-9_.]+)"')

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


def build_email_rows(index: SourceIndex, notifications: Dict[str, str], bpmn_root: Path,
                     service_tasks, start_events, start_event_labels):
    notifiers = find_notifiers(index)
    aggregator_map = map_aggregators_to_generators(index, {n['aggregator'] for n in notifiers})
    rows = []
    for notifier in sorted(notifiers, key=lambda n: (n['event'] or n['class'])):
        event = notifier['event'] or 'UNKNOWN'
        generators = aggregator_map.get(notifier['aggregator'] or '', [])
        bpmn_files, ccd_display, ccd_ids = lookup_bpmn(event, service_tasks, start_events, start_event_labels)
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


def load_template_paths() -> Dict[str, str]:
    template_map = {}
    if not TEMPLATE_DIR.exists():
        return template_map
    for path in sorted(TEMPLATE_DIR.glob('*.json')):
        try:
            data = json.loads(path.read_text(encoding='utf-8'))
        except json.JSONDecodeError:
            continue
        name = data.get('name') or path.stem
        template_map[name] = os.path.relpath(path, REPO_ROOT)
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


def template_links_for_scenarios(scenarios: Set[str], template_map: Dict[str, str]) -> List[Dict[str, str]]:
    links = []
    for scenario in sorted(filter(None, scenarios)):
        if not scenario.startswith('Scenario.'):
            continue
        template_name = 'Notice.' + scenario.split('Scenario.', 1)[1]
        path = template_map.get(template_name)
        if path:
            links.append({'name': template_name, 'path': path})
    return links


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
            details = [f"Contributor: `{class_name}`"]
            if service:
                details.append(f"Service: `{service}`")
            bpmn_files, ccd_display, ccd_ids = lookup_bpmn(task_id, service_tasks, start_events, start_event_labels)
            scenarios = scenario_names_for_class(index, handler_type, scenario_map, scenario_cache)
            templates = template_links_for_scenarios(scenarios, template_map)
            rows.append({
                'framework': 'DashboardNotificationHandler',
                'camunda_task': task_id,
                'handler': handler_type,
                'party': party,
                'details': details,
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


def collect_dashboard_callback_rows(index: SourceIndex, service_tasks, start_events,
                                    start_event_labels, scenario_map: Dict[str, str],
                                    template_map: Dict[str, str], scenario_cache: Dict[str, Set[str]]) -> List[Dict[str, object]]:
    rows = []
    for class_name, java_class in index.classes.items():
        if ('extends DashboardCallbackHandler' not in java_class.text
                and 'extends CaseProgressionDashboardCallbackHandler' not in java_class.text):
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
        details = []
        if events:
            details.append('Case events: ' + ', '.join(f'`{evt}`' for evt in events))
        bpmn_files, ccd_display, ccd_ids = lookup_bpmn(task_id, service_tasks, start_events, start_event_labels)
        scenarios = scenario_names_for_class(index, class_name, scenario_map, scenario_cache)
        templates = template_links_for_scenarios(scenarios, template_map)
        rows.append({
            'framework': framework,
            'camunda_task': task_id,
            'handler': class_name,
            'party': party,
            'details': details,
            'bpmn_files': bpmn_files or ['—'],
            'ccd_events': ccd_display or ['—'],
            'ccd_event_ids': ccd_ids,
            'templates': templates,
        })
    return rows


def build_dashboard_rows(index: SourceIndex, service_tasks, start_events, start_event_labels,
                         scenario_map: Dict[str, str], template_map: Dict[str, str]):
    scenario_cache: Dict[str, Set[str]] = {}
    contribution_rows = collect_dashboard_contributions(
        index, service_tasks, start_events, start_event_labels, scenario_map, template_map, scenario_cache)
    callback_rows = collect_dashboard_callback_rows(
        index, service_tasks, start_events, start_event_labels, scenario_map, template_map, scenario_cache)
    all_rows = contribution_rows + callback_rows
    return sorted(all_rows, key=lambda row: (row['camunda_task'], row['framework'], row['handler']))


def render_email_markdown(rows: List[Dict[str, str]], notify_service_id: Optional[str]) -> str:
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
    lines = ["## Email notification matrix", "", textwrap.dedent("""
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
        formatted_bpmn = []
        for path in row['bpmn_files']:
            if path == '—':
                formatted_bpmn.append('—')
            else:
                name = Path(path).stem
                formatted_bpmn.append(f"{path} ([diagram]({DIAGRAM_BASE_URL}{name}.png))")
        lines.append('|' + '|'.join([
            f"`{row['event']}`",
            f"`{row['handler']}`",
            f"`{row['aggregator']}`",
            row['party'],
            f"`{row['generator']}`",
            template_id_cell,
            '<br>'.join(formatted_bpmn),
            '<br>'.join(row['ccd_events'])
        ]) + '|')
    lines.append('')
    return '\n'.join(lines)


def render_dashboard_markdown(rows: List[Dict[str, object]]) -> str:
    header = [
        "Framework",
        "Camunda task",
        "Handler / task",
        "Party",
        "Details",
        "Template JSON",
        "BPMN file(s)",
        "CCD event(s)"
    ]
    intro = textwrap.dedent("""
        Dashboard notifications are produced by two frameworks: service task contributions dispatched via
        `DashboardNotificationHandler`, and per-event callbacks implemented through `DashboardCallbackHandler`
        (including the case progression specialisation). Each row links the Camunda service task ID to the
        dashboard task or handler class that records citizen scenarios and surfaces the underlying
        `dashboard-notifications` JSON template(s).
    """).strip()
    lines = ["## Dashboard notification matrix", "", intro, ""]
    lines.append('|' + '|'.join(header) + '|')
    lines.append('|' + '|'.join(['---'] * len(header)) + '|')
    for row in rows:
        details = '<br>'.join(row.get('details') or ['—'])
        template_links = row.get('templates') or []
        template_cell = '<br>'.join(
            f"[`{tpl['name']}`]({tpl['path']})" for tpl in template_links
        ) or '—'
        formatted_bpmn = []
        for path in row['bpmn_files']:
            if path == '—':
                formatted_bpmn.append('—')
            else:
                name = Path(path).stem
                formatted_bpmn.append(f"{path} ([diagram]({DIAGRAM_BASE_URL}{name}.png))")
        lines.append('|' + '|'.join([
            row['framework'],
            f"`{row['camunda_task']}`",
            f"`{row['handler']}`",
            row.get('party') or '—',
            details or '—',
            template_cell,
            '<br>'.join(formatted_bpmn),
            '<br>'.join(row['ccd_events'] or ['—'])
        ]) + '|')
    lines.append('')
    return '\n'.join(lines)


def render_html(email_rows: List[Dict[str, str]], dashboard_rows: List[Dict[str, object]],
                notify_service_id: Optional[str]) -> str:
    email_header = [
        "Camunda task",
        "Handler",
        "Parties selector",
        "Party",
        "Email DTO generator",
        "Gov.Notify template ID",
        "BPMN file(s)",
        "CCD event(s)"
    ]
    dashboard_header = [
        "Framework",
        "Camunda task",
        "Handler / task",
        "Party",
        "Details",
        "Template JSON",
        "BPMN file(s)",
        "CCD event(s)"
    ]
    email_events = sorted({event for row in email_rows for event in row.get('ccd_event_ids', []) if event not in ('—', '')})
    dashboard_events = sorted({event for row in dashboard_rows for event in row.get('ccd_event_ids', []) if event not in ('—', '')})
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
        "    .filter-panel { margin: 0.75rem 0; display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; }",
        "    .filter-panel label { font-weight: 600; }",
        "    select { min-width: 18rem; padding: 0.2rem; }",
        "    button { padding: 0.2rem 0.6rem; }",
        "    .counts { font-size: 0.85rem; color: #555; }",
        "    h2 { margin-top: 2.5rem; }",
        "  </style>",
        "</head>",
        "<body>",
        "  <h1>Notification matrix</h1>",
        "  <p>The tables mirror <code>docs/email-notifications.md</code> and add CCD event filters for both email and dashboard notifications." 
        " Each CCD entry shows the process name followed by the CCD event ID.</p>",
        "  <h2>Email notification matrix</h2>",
        "  <div class='filter-panel'>",
        "    <label for='email-ccd-filter'>CCD event:</label>",
        "    <select id='email-ccd-filter'>",
        "      <option value=''>All CCD events</option>",
    ]
    for event in email_events:
        lines.append(f"      <option value='{html.escape(event.lower())}'>{html.escape(event)}</option>")
    lines.extend([
        "    </select>",
        "    <button type='button' id='email-reset-filter'>Reset</button>",
        "    <span class='counts'><span id='email-visible-count'>0</span> rows shown</span>",
        "  </div>",
        "  <table id='email-notifications-table'>",
        "    <thead>",
        "      <tr>",
    ])
    for col in email_header:
        lines.append(f"        <th>{html.escape(col)}</th>")
    lines.extend([
        "      </tr>",
        "    </thead>",
        "    <tbody>",
    ])
    for row in email_rows:
        ccd_attr = ' '.join(event.lower() for event in row.get('ccd_event_ids', []) if event not in ('—', ''))
        template_id_cell = html.escape(row['template_id'])
        if notify_service_id and row['template_id'] not in ('—', ''):
            template_id_cell = (
                f"<a href='https://www.notifications.service.gov.uk/services/{notify_service_id}/templates/"
                f"{html.escape(row['template_id'])}'>{html.escape(row['template_id'])}</a>"
            )
        formatted_bpmn = []
        for path in row['bpmn_files']:
            if path == '—':
                formatted_bpmn.append('—')
            else:
                name = Path(path).stem
                formatted_bpmn.append(f"{html.escape(path)}<br><a href='{DIAGRAM_BASE_URL}{name}.png'>diagram</a>")
        lines.extend([
            f"      <tr data-ccd-events='{ccd_attr}'>",
            f"        <td><code>{html.escape(row['event'])}</code></td>",
            f"        <td><code>{html.escape(row['handler'])}</code></td>",
            f"        <td><code>{html.escape(row['aggregator'])}</code></td>",
            f"        <td>{html.escape(row['party'])}</td>",
            f"        <td><code>{html.escape(row['generator'])}</code></td>",
            f"        <td>{template_id_cell}</td>",
            f"        <td>{'<br>'.join(formatted_bpmn)}</td>",
            f"        <td>{'<br>'.join(html.escape(event) for event in row['ccd_events'])}</td>",
            "      </tr>",
        ])
    lines.extend([
        "    </tbody>",
        "  </table>",
        "  <h2>Dashboard notification matrix</h2>",
        "  <div class='filter-panel'>",
        "    <label for='dashboard-ccd-filter'>CCD event:</label>",
        "    <select id='dashboard-ccd-filter'>",
        "      <option value=''>All CCD events</option>",
    ])
    for event in dashboard_events:
        lines.append(f"      <option value='{html.escape(event.lower())}'>{html.escape(event)}</option>")
    lines.extend([
        "    </select>",
        "    <button type='button' id='dashboard-reset-filter'>Reset</button>",
        "    <span class='counts'><span id='dashboard-visible-count'>0</span> rows shown</span>",
        "  </div>",
        "  <table id='dashboard-notifications-table'>",
        "    <thead>",
        "      <tr>",
    ])
    for col in dashboard_header:
        lines.append(f"        <th>{html.escape(col)}</th>")
    lines.extend([
        "      </tr>",
        "    </thead>",
        "    <tbody>",
    ])
    for row in dashboard_rows:
        ccd_attr = ' '.join(event.lower() for event in row.get('ccd_event_ids', []) if event not in ('—', ''))
        formatted_bpmn = []
        for path in row['bpmn_files']:
            if path == '—':
                formatted_bpmn.append('—')
            else:
                name = Path(path).stem
                formatted_bpmn.append(f"{html.escape(path)}<br><a href='{DIAGRAM_BASE_URL}{name}.png'>diagram</a>")
        details = '<br>'.join(html.escape(detail) for detail in (row.get('details') or ['—']))
        template_links = row.get('templates') or []
        template_cell = '<br>'.join(
            f"<a href='{html.escape(link['path'])}'>{html.escape(link['name'])}</a>" for link in template_links
        ) or '—'
        lines.extend([
            f"      <tr data-ccd-events='{ccd_attr}'>",
            f"        <td>{html.escape(row['framework'])}</td>",
            f"        <td><code>{html.escape(row['camunda_task'])}</code></td>",
            f"        <td><code>{html.escape(row['handler'])}</code></td>",
            f"        <td>{html.escape(row.get('party') or '—')}</td>",
            f"        <td>{details}</td>",
            f"        <td>{template_cell}</td>",
            f"        <td>{'<br>'.join(formatted_bpmn)}</td>",
            f"        <td>{'<br>'.join(html.escape(event) for event in row['ccd_events'])}</td>",
            "      </tr>",
        ])
    lines.extend([
        "    </tbody>",
        "  </table>",
        "  <script>",
        "    (function() {",
        "      function wireFilter(selectId, resetId, tableId, counterId) {",
        "        const select = document.getElementById(selectId);",
        "        const reset = document.getElementById(resetId);",
        "        const rows = Array.from(document.querySelectorAll(`#${tableId} tbody tr`));",
        "        const counter = document.getElementById(counterId);",
        "        function applyFilter() {",
        "          const value = (select.value || '').trim();",
        "          let visible = 0;",
        "          rows.forEach(row => {",
        "            if (!value || (row.dataset.ccdEvents || '').includes(value)) {",
        "              row.style.display = '';",
        "              visible += 1;",
        "            } else {",
        "              row.style.display = 'none';",
        "            }",
        "          });",
        "          counter.textContent = visible;",
        "        }",
        "        select.addEventListener('change', applyFilter);",
        "        reset.addEventListener('click', () => { select.value = ''; applyFilter(); });",
        "        applyFilter();",
        "      }",
        "      wireFilter('email-ccd-filter', 'email-reset-filter', 'email-notifications-table', 'email-visible-count');",
        "      wireFilter('dashboard-ccd-filter', 'dashboard-reset-filter', 'dashboard-notifications-table', 'dashboard-visible-count');",
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
    camunda_root = bpmn_root / 'src' / 'main' / 'resources' / 'camunda' if (bpmn_root / 'src').exists() else bpmn_root
    service_tasks, start_events, start_event_labels = index_bpmn(camunda_root)
    scenario_map = load_dashboard_scenarios()
    template_map = load_template_paths()
    email_rows = build_email_rows(index, notifications, camunda_root, service_tasks, start_events, start_event_labels)
    filtered_email_rows = filter_rows_by_ccd_event(email_rows, args.ccd_event_filters)
    dashboard_rows = build_dashboard_rows(index, service_tasks, start_events, start_event_labels, scenario_map, template_map)
    filtered_dashboard_rows = filter_rows_by_ccd_event(dashboard_rows, args.ccd_event_filters)
    markdown_sections = [
        "# Notification matrix",
        render_email_markdown(filtered_email_rows, args.notify_service_id),
        render_dashboard_markdown(filtered_dashboard_rows)
    ]
    markdown = '\n\n'.join(markdown_sections)
    output_path = Path(args.output)
    output_path.write_text(markdown + "\n", encoding="utf-8")
    print(f"Wrote {len(filtered_email_rows)} email rows and {len(filtered_dashboard_rows)} dashboard rows to {output_path}")
    html_output = (args.html_output or '').strip()
    if html_output:
        html_markup = render_html(filtered_email_rows, filtered_dashboard_rows, args.notify_service_id)
        html_path = Path(html_output)
        html_path.write_text(html_markup + "\n", encoding="utf-8")
        print(f"Wrote interactive HTML table to {html_path}")
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
