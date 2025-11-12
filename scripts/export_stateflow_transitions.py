#!/usr/bin/env python3
"""Extract MAIN state-flow transitions and render analyst artefacts."""
from __future__ import annotations

import json
import re
from collections import defaultdict
from pathlib import Path
from typing import Dict, Iterable, List, Optional, Sequence, Set, Tuple

ROOT = Path(__file__).resolve().parents[1]
TRANSITIONS_DIR = ROOT / "src/main/java/uk/gov/hmcts/reform/civil/stateflow/transitions"
JSON_DIR = ROOT / "docs" / "stateflow"
PREDICATE_GLOSSARY = JSON_DIR / "predicate_descriptions.json"
CATALOGUE_JSON = JSON_DIR / "transition_catalogue.json"
TRANSITIONS_JSON = JSON_DIR / "stateflow_transitions.json"
CATALOGUE_MD = ROOT / "docs/stateflow_transition_catalogue.md"
MERMAID_DIR = ROOT / "docs"

PHASE_DEFINITIONS = {
    "draft_flow": {
        "title": "Draft to Submission",
        "states": {
            "DRAFT",
            "SPEC_DRAFT"
        },
        "bridge_in": {},
        "bridge_out": {
            "CLAIM_SUBMITTED": "CLAIM_SUBMITTED (continues in Issue flow)"
        }
    },
    "issue_flow": {
        "title": "Claim Issue & Notification (spec + unspec)",
        "states": {
            "CLAIM_SUBMITTED","CLAIM_ISSUED_PAYMENT_SUCCESSFUL","CLAIM_ISSUED_PAYMENT_FAILED",
            "PENDING_CLAIM_ISSUED","PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT",
            "PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC",
            "PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT","PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT",
            "CLAIM_ISSUED","CLAIM_NOTIFIED","CLAIM_DETAILS_NOTIFIED","CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION",
            "PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA",
            "PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA",
            "PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA",
            "CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE",
            "CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE",
            "CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE",
            "SPEC_DEFENDANT_NOC"
        },
        "bridge_in": {},
        "bridge_out": {
            "NOTIFICATION_ACKNOWLEDGED": "NOTIFICATION_ACKNOWLEDGED (continues in Defence flow)",
            "TAKEN_OFFLINE_SPEC_DEFENDANT_NOC": "TAKEN_OFFLINE_SPEC_DEFENDANT_NOC (continues in Post-response flow)",
            "TAKEN_OFFLINE_BY_STAFF": "TAKEN_OFFLINE_BY_STAFF (continues in Post-response flow)",
            "TAKEN_OFFLINE_BY_SYSTEM": "TAKEN_OFFLINE_BY_SYSTEM (continues in Post-response flow)",
            "TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED": "TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED (continues in Post-response flow)",
            "TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED": "TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED (continues in Post-response flow)",
            "TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT": "TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT (continues in Post-response flow)",
            "TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT": "TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT (continues in Post-response flow)",
            "TAKEN_OFFLINE_UNREGISTERED_DEFENDANT": "TAKEN_OFFLINE_UNREGISTERED_DEFENDANT (continues in Post-response flow)",
            "FULL_DEFENCE": "FULL_DEFENCE (continues in Defence flow)",
            "FULL_ADMISSION": "FULL_ADMISSION (continues in Defence flow)",
            "PART_ADMISSION": "PART_ADMISSION (continues in Defence flow)",
            "COUNTER_CLAIM": "COUNTER_CLAIM (continues in Defence flow)",
            "DIVERGENT_RESPOND_GO_OFFLINE": "DIVERGENT_RESPOND_GO_OFFLINE (continues in Defence flow)",
            "DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE": "DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE (continues in Defence flow)"
        }
    },
    "response_flow": {
        "title": "Defence Waiting & Divergence",
        "states": {
            "NOTIFICATION_ACKNOWLEDGED","NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION",
            "AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED","AWAITING_RESPONSES_FULL_ADMIT_RECEIVED",
            "AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED","ALL_RESPONSES_RECEIVED",
            "RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL","DIVERGENT_RESPOND_GO_OFFLINE",
            "DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE"
        },
        "bridge_in": {
            "CLAIM_DETAILS_NOTIFIED": "CLAIM_DETAILS_NOTIFIED (from Issue flow)",
            "CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION": "CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION (from Issue flow)"
        },
        "bridge_out": {
            "FULL_DEFENCE": "FULL_DEFENCE (continues in Post-response flow)",
            "FULL_ADMISSION": "FULL_ADMISSION (continues in Post-response flow)",
            "PART_ADMISSION": "PART_ADMISSION (continues in Post-response flow)",
            "COUNTER_CLAIM": "COUNTER_CLAIM (continues in Post-response flow)",
            "TAKEN_OFFLINE_BY_STAFF": "TAKEN_OFFLINE_BY_STAFF (continues in Post-response flow)",
            "TAKEN_OFFLINE_SDO_NOT_DRAWN": "TAKEN_OFFLINE_SDO_NOT_DRAWN (continues in Post-response flow)",
            "PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA": "PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA (continues in Post-response flow)"
        }
    },
    "post_response": {
        "title": "Post-Response Outcomes & Settlement",
        "states": {
            "FULL_DEFENCE","FULL_DEFENCE_PROCEED","FULL_DEFENCE_NOT_PROCEED","IN_MEDIATION",
            "MEDIATION_UNSUCCESSFUL_PROCEED","FULL_ADMISSION","FULL_ADMIT_PROCEED","FULL_ADMIT_NOT_PROCEED",
            "FULL_ADMIT_PAY_IMMEDIATELY","FULL_ADMIT_AGREE_REPAYMENT","FULL_ADMIT_REJECT_REPAYMENT",
            "FULL_ADMIT_JUDGMENT_ADMISSION","FULL_ADMIT_AGREE_SETTLE","PART_ADMISSION","PART_ADMIT_PROCEED",
            "PART_ADMIT_NOT_PROCEED","PART_ADMIT_PAY_IMMEDIATELY","PART_ADMIT_AGREE_SETTLE",
            "PART_ADMIT_AGREE_REPAYMENT","PART_ADMIT_REJECT_REPAYMENT","COUNTER_CLAIM",
            "SIGN_SETTLEMENT_AGREEMENT","CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE",
            "TAKEN_OFFLINE_AFTER_SDO","TAKEN_OFFLINE_SDO_NOT_DRAWN","TAKEN_OFFLINE_SPEC_DEFENDANT_NOC",
            "TAKEN_OFFLINE_SPEC_DEFENDANT_NOC_AFTER_JBA","PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA",
            "IN_HEARING_READINESS","TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED","TAKEN_OFFLINE_BY_STAFF",
            "TAKEN_OFFLINE_BY_SYSTEM","TAKEN_OFFLINE_SDO_NOT_DRAWN"
        },
        "bridge_in": {
            "NOTIFICATION_ACKNOWLEDGED": "NOTIFICATION_ACKNOWLEDGED (from Defence flow)",
            "NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION": "NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION (from Defence flow)"
        },
        "bridge_out": {}
    }
}



_MOVE_PATTERN = re.compile(r'\.moveTo\(([^,]+),\s*transitions\)')


def _load_text(path: Path) -> str:
    return path.read_text()


def _extract_from_state(text: str) -> Optional[str]:
    match = re.search(r'super\((?:FlowState\.Main\.)?([A-Z0-9_]+)', text)
    return match.group(1) if match else None


def _extract_transitions(text: str) -> List[Tuple[str, str]]:
    moves = list(_MOVE_PATTERN.finditer(text))
    results: List[Tuple[str, str]] = []
    for idx, match in enumerate(moves):
        target = match.group(1).strip()
        seg_start = match.end()
        seg_end = moves[idx + 1].start() if idx + 1 < len(moves) else len(text)
        segment = text[seg_start:seg_end]
        guard_match = re.search(r'\.onlyWhen\((.*?),\s*transitions\s*\)', segment, re.DOTALL)
        guard = guard_match.group(1).split('//')[0] if guard_match else 'ALWAYS'
        guard = ' '.join(guard.split())
        results.append((target, guard))
    return results


def harvest_transitions() -> List[Dict[str, str]]:
    harvested = []
    for file_path in sorted(TRANSITIONS_DIR.glob('*.java')):
        text = _load_text(file_path)
        if 'class TransitionBuilder' in text or 'class MidTransitionBuilder' in text:
            continue
        from_state = _extract_from_state(text)
        transitions = _extract_transitions(text)
        special_states = None
        if not from_state:
            if file_path.name == 'DraftTransitionBuilder.java':
                special_states = ['DRAFT', 'SPEC_DRAFT']
            else:
                continue
        if special_states:
            for state in special_states:
                for target, guard in transitions:
                    harvested.append({
                        'file': str(file_path.relative_to(ROOT)),
                        'from_state': state,
                        'to_state': target,
                        'guard': guard
                    })
        else:
            for target, guard in transitions:
                harvested.append({
                    'file': str(file_path.relative_to(ROOT)),
                    'from_state': from_state,
                    'to_state': target,
                    'guard': guard
                })
    return harvested


# --- Guard parsing helpers -------------------------------------------------

def strip_outer_parens(text: str) -> str:
    text = text.strip()
    while text.startswith('(') and text.endswith(')'):
        depth = 0
        balanced = True
        for idx, ch in enumerate(text):
            if ch == '(':
                depth += 1
            elif ch == ')':
                depth -= 1
                if depth == 0 and idx != len(text) - 1:
                    balanced = False
                    break
        if balanced and depth == 0:
            text = text[1:-1].strip()
        else:
            break
    return text


def extract_argument(segment: str, offset: int) -> Tuple[str, str]:
    depth = 1
    for idx in range(offset, len(segment)):
        ch = segment[idx]
        if ch == '(':
            depth += 1
        elif ch == ')':
            depth -= 1
            if depth == 0:
                return segment[offset:idx], segment[idx + 1:]
    raise ValueError(f'Unmatched parentheses in: {segment!r}')


def split_first_call(text: str) -> Tuple[str, str]:
    depth = 0
    for idx, ch in enumerate(text):
        if ch == '(':
            depth += 1
        elif ch == ')':
            depth -= 1
        elif ch == '.' and depth == 0:
            return text[:idx], text[idx:]
    return text, ''


def parse_expr(text: str):
    text = strip_outer_parens(text)
    if not text:
        return None
    if text.endswith('.negate()'):
        return ('NOT', parse_expr(text[:-len('.negate()')]))
    if text.startswith('not('):
        inner, remainder = extract_argument(text, len('not('))
        if not remainder.strip():
            return ('NOT', parse_expr(inner))
    base, remainder = split_first_call(text)
    expr = parse_expr(base) if remainder else parse_atom(base)
    remainder = remainder.strip()
    while remainder:
        if remainder.startswith('.and('):
            inner, remainder = extract_argument(remainder, len('.and('))
            expr = ('AND', expr, parse_expr(inner))
            remainder = remainder.strip()
        elif remainder.startswith('.or('):
            inner, remainder = extract_argument(remainder, len('.or('))
            expr = ('OR', expr, parse_expr(inner))
            remainder = remainder.strip()
        elif remainder.startswith('.negate()'):
            expr = ('NOT', expr)
            remainder = remainder[len('.negate()'):].strip()
        else:
            raise ValueError(f'Unrecognised suffix: {remainder!r}')
    return expr


def parse_atom(text: str):
    text = strip_outer_parens(text)
    if not text:
        return None
    if text.startswith('not('):
        inner, remainder = extract_argument(text, len('not('))
        if remainder.strip():
            raise ValueError(f'Trailing text after not(): {text!r}')
        return ('NOT', parse_expr(inner))
    if text.endswith('.negate()'):
        return ('NOT', parse_expr(text[:-len('.negate()')]))
    if text.endswith('()'):
        text = text[:-2]
    if '::' in text:
        text = text.split('::')[-1]
    return ('PRED', text.strip())


def collect_predicates(expr, acc: Optional[Set[str]] = None) -> Set[str]:
    acc = acc or set()
    if expr is None:
        return acc
    kind = expr[0]
    if kind == 'PRED':
        acc.add(expr[1])
    else:
        for child in expr[1:]:
            collect_predicates(child, acc)
    return acc


def expr_to_text(expr, glossary: Dict[str, str]) -> str:
    if expr is None:
        return ''
    kind = expr[0]
    if kind == 'PRED':
        key = expr[1]
        return glossary.get(key, key)
    if kind == 'NOT':
        return f"NOT ({expr_to_text(expr[1], glossary)})"
    if kind == 'AND':
        left = expr_to_text(expr[1], glossary)
        right = expr_to_text(expr[2], glossary)
        return f"({left}) AND ({right})"
    if kind == 'OR':
        left = expr_to_text(expr[1], glossary)
        right = expr_to_text(expr[2], glossary)
        return f"({left}) OR ({right})"
    return ''


# --- Scenario tagging ------------------------------------------------------

def categorise(from_state: str, to_state: str, predicates: Iterable[str]) -> Set[str]:
    tags: Set[str] = set()
    state_text = f"{from_state} {to_state}".upper()
    predicate_text = ' '.join(predicates).lower()

    if 'SPEC' in state_text or 'spec' in predicate_text:
        tags.add('spec')
    else:
        tags.add('unspec')

    if any(key in state_text for key in ['LIP', 'UNREPRESENTED']) or 'lip' in predicate_text:
        tags.add('lip')

    if any(key in state_text for key in ['AWAITING_RESPONSES', 'ONE_V_TWO', 'TWO_V_ONE']) or any(
        token in predicate_text for token in ['respondent2', 'multipartycase', 'bothdefsamelegalrep']
    ):
        tags.add('multi-party')

    if any(key in state_text for key in ['TAKEN_OFFLINE', 'PAST_', 'CLAIM_DISMISSED']) or any(
        token in predicate_text for token in ['takenoffline', 'divergent', 'dismissed', 'camunda']
    ):
        tags.add('offline/timeout')

    if any(token in predicate_text for token in ['mediation']):
        tags.add('mediation')

    if any(key in to_state for key in ['SETTLEMENT', 'AGREE', 'REPAYMENT', 'JUDGMENT']):
        tags.add('settlement/Judgment')

    return tags


# --- Output helpers --------------------------------------------------------

def summarise_label(text: str) -> str:
    if text == 'Always':
        return text
    text = ' '.join(text.split())
    text = text.replace(':', ' -')
    text = text.replace('(', '').replace(')', '')
    text = text.replace(' OR ', '\\nOR ').replace(' AND ', ' / ').replace('NOT ', 'not ')
    text = text.replace(' | ', '\\nOR ')
    if '. ' in text:
        text = text.split('. ')[0]
    if len(text) > 70:
        text = text[:67] + '…'
    return text


def save_mermaid(
    transitions: Sequence[Dict],
    glossary: Dict[str, str],
    source_states: Set[str],
):
    MERMAID_DIR.mkdir(exist_ok=True)
    for slug, info in PHASE_DEFINITIONS.items():
        states = info['states']
        bridge_in = info.get('bridge_in', {})
        bridge_out = info.get('bridge_out', {})

        bridge_in_alias = {state: f"{slug.upper()}_IN_{idx}" for idx, state in enumerate(bridge_in)}
        bridge_out_alias = {state: f"{slug.upper()}_OUT_{idx}" for idx, state in enumerate(bridge_out)}
        bridge_out_nodes = set(bridge_out_alias.values())

        edges = []
        for item in transitions:
            src = item['from']
            dst = item['to']
            include = False
            src_alias = src
            dst_alias = dst

            if states is None:
                include = True
            else:
                if src in states and dst in states:
                    include = True
                elif src in states and dst in bridge_out:
                    include = True
                    dst_alias = bridge_out_alias[dst]
                elif src in bridge_in and dst in states:
                    include = True
                    src_alias = bridge_in_alias[src]
                elif src in bridge_in and dst in bridge_out:
                    include = True
                    src_alias = bridge_in_alias[src]
                    dst_alias = bridge_out_alias[dst]

            if not include:
                continue

            label = 'Always' if item['guard_raw'] == 'ALWAYS' else summarise_label(item['guard_text'])
            edges.append((src_alias, dst_alias, label))

        if not edges:
            continue

        branch_states = []
        if slug == "draft_flow":
            adjusted = []
            target_alias = bridge_out_alias.get("CLAIM_SUBMITTED")
            counter = 0
            for src_alias, dst_alias, label in edges:
                if dst_alias == target_alias and src_alias in states:
                    alias = f"{src_alias}_PATH_{counter}"
                    counter += 1
                    branch_label = label
                    if src_alias == 'SPEC_DRAFT' and label:
                        branch_label = f"SPEC: {label}"
                    branch_states.append((alias, branch_label))
                    adjusted.append((src_alias, alias, ""))
                    adjusted.append((alias, dst_alias, "Always"))
                else:
                    adjusted.append((src_alias, dst_alias, label))
            edges = adjusted

        state_aliases = set(states or [])
        edge_sources = {src for src, _, _ in edges}
        edge_nodes = edge_sources | {dst for _, dst, _ in edges}
        def mark_terminal(label: str, alias: str, state_name: Optional[str] = None) -> str:
            if alias in edge_sources:
                return label
            if state_name and state_name in source_states:
                return label
            text = label or ''
            lower = text.lower()
            if 'terminal' in lower:
                return f"{label} 🔚"
            return f"{label} 🔚" if label else '🔚'

        lines = ['stateDiagram-v2', f"  %% {info['title']}"]

        for state, label in bridge_in.items():
            alias = bridge_in_alias[state]
            display = mark_terminal(label, alias, state)
            lines.append(f'  state "{display}" as {alias}')

        for alias, label in branch_states:
            label_text = label if label else 'Condition'
            display = mark_terminal(label_text, alias).replace('"', '\"')
            lines.append(f'  state "{display}" as {alias}')

        for state, label in bridge_out.items():
            alias = bridge_out_alias[state]
            display = mark_terminal(label, alias, state)
            lines.append(f'  state "{display}" as {alias}')

        for src, dst, label in edges:
            line = f"  {src} --> {dst}"
            if label:
                line += f" : {label}"
            lines.append(line)

        if states:
            terminal_states = (state_aliases & edge_nodes) - edge_sources
            for alias in sorted(terminal_states):
                lines.append(f'  note right of {alias} : 🔚 Terminal state')

        (MERMAID_DIR / f"{slug}.mmd").write_text("\n".join(lines))


def save_markdown(transitions: Sequence[Dict]):
    MERMAID_DIR.mkdir(exist_ok=True)
    grouped: Dict[str, List[Dict]] = defaultdict(list)
    for item in transitions:
        grouped[item['from']].append(item)
    lines = [
        '# State Flow Transition Catalogue',
        '',
        'Generated from builder definitions. Guards appear as business-facing conditions.',
        ''
    ]
    for state in sorted(grouped):
        lines.append(f'## {state}')
        lines.append('| To state | Business condition | Scenario tags |')
        lines.append('| --- | --- | --- |')
        for entry in sorted(grouped[state], key=lambda row: row['to']):
            condition = 'Always' if entry['guard_raw'] == 'ALWAYS' else entry['guard_text'].replace('\n', ' ')
            tags = ', '.join(entry['tags']) if entry['tags'] else '—'
            lines.append(f"| {entry['to']} | {condition} | {tags} |")
        lines.append('')
    CATALOGUE_MD.write_text('\n'.join(lines))


def main() -> None:
    JSON_DIR.mkdir(parents=True, exist_ok=True)
    transitions_raw = harvest_transitions()
    TRANSITIONS_JSON.write_text(json.dumps(transitions_raw, indent=2))

    if not PREDICATE_GLOSSARY.exists():
        raise FileNotFoundError(
            f"Missing predicate glossary at {PREDICATE_GLOSSARY}. Generate or curate descriptions before running." )
    glossary = json.loads(PREDICATE_GLOSSARY.read_text())

    catalogue = []
    for row in transitions_raw:
        guard_raw = row['guard']
        if guard_raw == 'ALWAYS':
            expr = None
            predicates = set()
            guard_text = 'Always'
        else:
            expr = parse_expr(guard_raw)
            predicates = collect_predicates(expr)
            guard_text = expr_to_text(expr, glossary)
        tags = sorted(categorise(row['from_state'], row['to_state'], predicates))
        catalogue.append({
            'from': row['from_state'],
            'to': row['to_state'],
            'guard_raw': guard_raw,
            'guard_text': guard_text,
            'tags': tags
        })

    CATALOGUE_JSON.write_text(json.dumps(catalogue, indent=2))
    save_markdown(catalogue)
    source_states = {row['from'] for row in catalogue}
    save_mermaid(catalogue, glossary, source_states)
    print(f"Harvested {len(catalogue)} transitions across {len({c['from'] for c in catalogue})} states.")
    tag_summary = defaultdict(int)
    for item in catalogue:
        for tag in item['tags']:
            tag_summary[tag] += 1
    print('Scenario counts:')
    for tag, count in sorted(tag_summary.items()):
        print(f"  {tag}: {count}")

if __name__ == '__main__':
    main()
