#!/usr/bin/env python3
"""Local input/provenance and verification helpers for the email docs workflows.

This module never fetches repositories or calls a remote API. Checkouts are
provided by the caller; generation uses only local files and the standard library.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import subprocess
import sys
import tempfile
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
MANIFEST = '.github/email-docs-sources.json'
REPOSITORIES = ('civil-ccd-definition', 'civil-general-apps-ccd-definition')
DOCUMENTS = ('docs/email-notifications.md', 'docs/email-notifications.html')
STRICT_FILES = {*DOCUMENTS, MANIFEST, 'scripts/generate_email_notifications_table.py', 'scripts/email_docs.py'}
STRICT_PREFIXES = ('.github/actions/email-docs/',)
WORKFLOWS = {f'.github/workflows/{name}.yml' for name in (
    'update-email-docs', 'email-notifications', 'deploy-email-docs')}


def revision(value: str) -> str:
    if not isinstance(value, str) or not re.fullmatch(r'[0-9a-f]{40}', value) or value == '0' * 40:
        raise ValueError(f'Expected a full, non-zero Git commit SHA: {value!r}')
    return value


def load_sources(root: Path = REPO_ROOT) -> dict:
    sources = json.loads((root / MANIFEST).read_text(encoding='utf-8'))
    if not isinstance(sources, dict) or set(sources) != set(REPOSITORIES):
        raise ValueError(f'{MANIFEST} must contain exactly {", ".join(REPOSITORIES)}')
    return {name: revision(sources[name]) for name in REPOSITORIES}


def git(*args: str, cwd: Path = REPO_ROOT) -> str:
    return subprocess.check_output(['git', *args], cwd=cwd, text=True).strip()


def classify(paths) -> str:
    paths = set(paths)
    if any(path in STRICT_FILES or path in WORKFLOWS or path.startswith(STRICT_PREFIXES)
           or (path.startswith('scripts/tests/') and 'email' in path)
           or path.startswith('.github/scripts/update-email-docs-pr') for path in paths):
        return 'sync'
    if any(path.startswith(('src/main/java/', 'src/main/resources/camunda/',
                            'src/main/resources/notification-templates/'))
           or path == 'src/main/resources/application.yaml' for path in paths):
        return 'report'
    return 'skip'


def classify_commits(base: str, head: str) -> str:
    revision(head)
    if base == '0' * 40:
        return 'sync'
    revision(base)
    # No fetch: full history is supplied by the workflow's checkout.
    paths = git('diff', '--no-renames', '--name-only', '-z', base, head, '--').split('\0')
    return classify(paths)


def record_sources(civil_checkout: Path, ga_checkout: Path, root: Path = REPO_ROOT) -> None:
    sources = {name: revision(git('rev-parse', 'HEAD', cwd=checkout))
               for name, checkout in zip(REPOSITORIES, (civil_checkout, ga_checkout))}
    (root / MANIFEST).write_text(json.dumps(sources, indent=2) + '\n', encoding='utf-8')


def generate(mode: str, civil_root: Path, ga_root: Path, root: Path = REPO_ROOT) -> int:
    sources = load_sources(root)
    with tempfile.TemporaryDirectory(prefix='email-docs-') as temporary:
        outputs = [Path(temporary) / Path(name).name for name in DOCUMENTS]
        command = [sys.executable, '-B', str(root / 'scripts/generate_email_notifications_table.py'),
                   '--bpmn-root', str(root / 'src/main/resources/camunda'),
                   '--ccd-definition-root', str(civil_root.expanduser().resolve()),
                   '--ga-ccd-definition-root', str(ga_root.expanduser().resolve()),
                   '--notify-service-id', 'a8b1617c-8e15-49aa-a8d3-a27a243f3c45',
                   '--output', str(outputs[0]), '--html-output', str(outputs[1])]
        subprocess.run(command, check=True, cwd=root)
        differences = []
        for name, output in zip(DOCUMENTS, outputs):
            target = root / name
            if not target.exists() or target.read_bytes() != output.read_bytes():
                differences.append(name)
            if mode == 'write':
                target.write_bytes(output.read_bytes())
        rows = sum(line.startswith('|') for line in outputs[0].read_text().splitlines()) - 2
        checksum = hashlib.sha256(outputs[1].read_bytes()).hexdigest()
        status = 'In sync' if not differences else (
            'Updated' if mode == 'write' else 'Documentation refresh needed')
        summary = '\n'.join([
            f'### Email notification documentation: {status}', '',
            f'- civil-service revision: `{git("rev-parse", "HEAD", cwd=root)}`',
            *[f'- {name}: `{sha}`' for name, sha in sources.items()],
            f'- Generated data rows: {rows}', f'- Generated HTML SHA-256: `{checksum}`',
            f'- Changed outputs: {", ".join(differences) or "none"}', '',
        ])
        print(summary)
        if os.environ.get('GITHUB_STEP_SUMMARY'):
            with open(os.environ['GITHUB_STEP_SUMMARY'], 'a', encoding='utf-8') as stream:
                stream.write(summary + '\n')
        if differences and mode == 'sync':
            print('Regenerate and include both documents before merging or publishing.', file=sys.stderr)
            return 1
        if differences and mode == 'report':
            print('::notice::Documentation drift detected. The updater will maintain a refresh PR after merge.')
        return 0


def main(argv=None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest='command', required=True)
    refs = commands.add_parser('refs', help='Emit validated checkout refs as GitHub step outputs')
    refs.add_argument('--refresh', action='store_true', help='Select repository default branches for the updater')
    record = commands.add_parser('record', help='Record local checkout revisions; does not fetch')
    record.add_argument('--civil-checkout', type=Path, default=REPO_ROOT / REPOSITORIES[0])
    record.add_argument('--ga-checkout', type=Path, default=REPO_ROOT / REPOSITORIES[1])
    changes = commands.add_parser('classify')
    changes.add_argument('--base', required=True)
    changes.add_argument('--head', required=True)
    generate_parser = commands.add_parser('generate')
    generate_parser.add_argument('--mode', choices=('write', 'sync', 'report'), default='sync')
    generate_parser.add_argument('--civil-root', type=Path,
                                 default=REPO_ROOT / REPOSITORIES[0] / 'ccd-definition')
    generate_parser.add_argument('--ga-root', type=Path,
                                 default=REPO_ROOT / REPOSITORIES[1] / 'ga-ccd-definition')
    args = parser.parse_args(argv)
    try:
        if args.command == 'refs':
            sources = load_sources()
            print(f'civil_ref={sources[REPOSITORIES[0]] if not args.refresh else ""}')
            print(f'ga_ref={sources[REPOSITORIES[1]] if not args.refresh else ""}')
        elif args.command == 'record':
            record_sources(args.civil_checkout, args.ga_checkout)
        elif args.command == 'classify':
            print(f'mode={classify_commits(args.base, args.head)}')
        else:
            return generate(args.mode, args.civil_root, args.ga_root)
    except (ValueError, TypeError, OSError, subprocess.CalledProcessError) as error:
        print(error, file=sys.stderr)
        return 1
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
