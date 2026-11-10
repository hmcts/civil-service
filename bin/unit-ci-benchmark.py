#!/usr/bin/env python3
"""Non-deploying unit-suite measurement. Exports no environment or credentials."""
import argparse
import csv
import hashlib
import json
import os
from pathlib import Path
import platform
import shutil
import signal
import subprocess
import sys
import time
import xml.etree.ElementTree as ET


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2, sort_keys=True) + '\n')


def process_snapshot(root):
    """Read only pid/parent/group, CPU ticks and RSS, never process arguments."""
    records = {}
    ticks = os.sysconf('SC_CLK_TCK')
    page = os.sysconf('SC_PAGE_SIZE')
    for entry in Path('/proc').iterdir():
        if not entry.name.isdigit():
            continue
        try:
            fields = (entry / 'stat').read_text().rsplit(')', 1)[1].split()
            records[int(entry.name)] = {
                'parent': int(fields[1]), 'group': int(fields[2]), 'start': fields[19],
                'cpu': (int(fields[11]) + int(fields[12])) / ticks,
                'rss': int(fields[21]) * page,
            }
        except (OSError, ValueError, IndexError):
            continue
    owned = {root}
    while True:
        children = {pid for pid, record in records.items()
                    if record['parent'] in owned or record['group'] == root}
        expanded = owned | children
        if expanded == owned:
            break
        owned = expanded
    return {(pid, record['start']): record for pid, record in records.items() if pid in owned}


def cgroup_snapshot():
    result = {}
    base = Path('/sys/fs/cgroup')
    try:
        relative = next(line[3:] for line in Path('/proc/self/cgroup').read_text().splitlines()
                        if line.startswith('0::'))
        candidate = base / relative.lstrip('/')
        if candidate.is_dir():
            base = candidate
    except (OSError, StopIteration):
        pass
    for name in ['cpu.max', 'cpu.stat', 'memory.max', 'memory.current', 'memory.peak', 'memory.events']:
        try:
            result[name] = (base / name).read_text().strip()
        except OSError:
            pass
    return result


def measure(command, directory, environment, timeout=1200):
    directory.mkdir(parents=True, exist_ok=True)
    before = cgroup_snapshot()
    start = time.monotonic()
    sampler_start = time.process_time()
    cpu = {}
    peak_tree = 0
    peak_single = 0
    timed_out = False
    with (directory / 'gradle.log').open('w') as log:
        process = subprocess.Popen(command, stdout=log, stderr=subprocess.STDOUT,
                                   env=environment, start_new_session=True)
        try:
            while process.poll() is None:
                snapshot = process_snapshot(process.pid)
                peak_tree = max(peak_tree, sum(record['rss'] for record in snapshot.values()))
                peak_single = max([peak_single] + [record['rss'] for record in snapshot.values()])
                for identity, record in snapshot.items():
                    cpu[identity] = max(cpu.get(identity, 0), record['cpu'])
                if time.monotonic() - start > timeout:
                    timed_out = True
                    os.killpg(process.pid, signal.SIGKILL)
                    break
                time.sleep(0.5)
            code = process.wait()
        finally:
            if process.poll() is None:
                os.killpg(process.pid, signal.SIGKILL)
                process.wait()
    result = {
        'exitCode': code, 'timedOut': timed_out, 'wallSeconds': time.monotonic() - start,
        'samplerCpuSeconds': time.process_time() - sampler_start,
        'sampledCpuSeconds': sum(cpu.values()), 'peakProcessTreeRssBytes': peak_tree,
        'peakSingleProcessRssBytes': peak_single, 'observedProcesses': len(cpu),
        'sampleIntervalSeconds': 0.5, 'cgroupBefore': before, 'cgroupAfter': cgroup_snapshot(),
    }
    write_json(directory / 'resources.json', result)
    return result


def test_results(root):
    cases = []
    for file in sorted(root.glob('**/build/test-results/test/TEST-*.xml')):
        if '.gradle' in file.parts:
            continue
        document = ET.parse(file).getroot()
        for case in document.iter('testcase'):
            state = 'passed'
            if case.find('skipped') is not None:
                state = 'skipped'
            if case.find('failure') is not None or case.find('error') is not None:
                state = 'failed'
            cases.append({'class': case.get('classname', document.get('name', '')),
                          'name': case.get('name', ''), 'state': state,
                          'seconds': float(case.get('time', 0))})
    cases.sort(key=lambda item: (item['class'], item['name'], item['state']))
    identities = '\n'.join(json.dumps([c['class'], c['name'], c['state']]) for c in cases)
    return cases, {'total': len(cases), 'passed': sum(c['state'] == 'passed' for c in cases),
                   'failed': sum(c['state'] == 'failed' for c in cases),
                   'skipped': sum(c['state'] == 'skipped' for c in cases),
                   'classes': len({c['class'] for c in cases}),
                   'identityAndStateSha256': hashlib.sha256(identities.encode()).hexdigest()}


def coverage_counters(file):
    root = ET.parse(file).getroot()
    return {counter.get('type'): {'covered': int(counter.get('covered')),
                                 'missed': int(counter.get('missed'))}
            for counter in root.findall('counter')}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--provider', choices=['actions', 'jenkins'], required=True)
    parser.add_argument('--workers', type=int, required=True)
    parser.add_argument('--repository-init')
    args = parser.parse_args()
    if args.workers < 1:
        parser.error('workers must be positive')
    root = Path.cwd()
    output = root / 'benchmark-results' / args.provider
    if output.exists():
        shutil.rmtree(output)
    output.mkdir(parents=True, exist_ok=True)
    version = subprocess.run(['java', '-version'], capture_output=True, text=True, check=True)
    metadata = {
        'provider': args.provider, 'commit': subprocess.check_output(['git', 'rev-parse', 'HEAD'], text=True).strip(),
        'workers': args.workers, 'visibleCpuCount': os.cpu_count(), 'os': platform.platform(),
        'startedAtUtc': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime()),
        'dependencyCachePresent': (Path(os.environ.get('GRADLE_USER_HOME', str(Path.home() / '.gradle'))) / 'caches').is_dir(),
        'exactActionsCacheHit': os.environ.get('BENCHMARK_CACHE_HIT'),
        'java': (version.stdout + version.stderr).strip(), 'cgroup': cgroup_snapshot(),
        'memory': {line.split(':')[0]: line.split(':')[1].strip()
                   for line in Path('/proc/meminfo').read_text().splitlines()
                   if line.startswith(('MemTotal:', 'MemAvailable:'))},
        'notes': ['Two repetitions: fresh build outputs, then warm workspace; fresh Gradle/test JVMs each time.',
                  'Normal provider worker limits retained. Different host capacities are recorded, not assumed equal.',
                  'Test fail-fast disabled in both providers to collect complete outcomes. No integration or deployment tasks.',
                  'CPU and aggregate RSS are sampled for the owned process tree; brief processes can be missed.',
                  'Cgroup figures may include other activity; they are not attributed exclusively to this command.'],
    }
    write_json(output / 'environment.json', metadata)
    command = ['./gradlew', '--no-daemon', '--no-build-cache', '--no-configuration-cache',
               '--console=plain', '--max-workers=' + str(args.workers)]
    if args.repository_init:
        command += ['--init-script', args.repository_init]
    command += ['--init-script', 'bin/unit-ci-benchmark.init.gradle']
    summaries = []
    for number in [1, 2]:
        directory = output / ('run-' + str(number))
        directory.mkdir(parents=True, exist_ok=True)
        environment = dict(os.environ, UNIT_BENCHMARK_TASKS=str(directory / 'tasks.jsonl'))
        # Remove only this benchmark's previous unit outputs, forcing execution.
        for file in root.glob('**/build/test-results/test/TEST-*.xml'):
            file.unlink()
        coverage_file = root / 'build/reports/jacoco/test/jacocoTestReport.xml'
        if coverage_file.exists():
            coverage_file.unlink()
        prepare_tasks = ['clean', 'testClasses'] if number == 1 else ['testClasses']
        prepare = measure(command + prepare_tasks, directory / 'prepare', environment)
        unit = measure(command + ['test', 'jacocoTestReport'], directory / 'unit', environment) if prepare['exitCode'] == 0 else None
        cases, states = test_results(root)
        with (directory / 'test-states.csv').open('w', newline='') as file:
            writer = csv.DictWriter(file, fieldnames=['class', 'name', 'state', 'seconds'])
            writer.writeheader()
            writer.writerows(cases)
        coverage = coverage_counters(coverage_file) if unit and unit['exitCode'] == 0 and coverage_file.exists() else None
        summary = {'run': number, 'workspace': 'fresh outputs' if number == 1 else 'warm outputs',
                   'prepare': prepare, 'unit': unit, 'states': states, 'coverage': coverage}
        write_json(directory / 'summary.json', summary)
        summaries.append(summary)
        print(json.dumps({'provider': args.provider, 'run': number, 'states': states,
                          'unitWallSeconds': unit['wallSeconds'] if unit else None}), flush=True)
    write_json(output / 'summary.json', summaries)
    metadata['finishedAtUtc'] = time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())
    write_json(output / 'environment.json', metadata)
    return 0 if all(s['unit'] and s['unit']['exitCode'] == 0 and s['states']['total'] > 0
                    and s['states']['failed'] == 0 for s in summaries) else 1


if __name__ == '__main__':
    sys.exit(main())
