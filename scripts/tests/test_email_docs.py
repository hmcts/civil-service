import contextlib
import importlib.util
import io
import json
import os
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

SPEC = importlib.util.spec_from_file_location('email_docs', Path(__file__).resolve().parents[1] / 'email_docs.py')
docs = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(docs)


class VerificationTests(unittest.TestCase):
    def setUp(self):
        temporary = tempfile.TemporaryDirectory()
        self.addCleanup(temporary.cleanup)
        self.root = Path(temporary.name)
        (self.root / '.github').mkdir()
        (self.root / 'docs').mkdir()
        self.sources = {docs.REPOSITORIES[0]: 'a' * 40}
        (self.root / docs.MANIFEST).write_text(json.dumps(self.sources))
        self.markdown = '|head|\n|---|\n|row|\n'
        self.html = '<html>generated</html>\n'
        for name in docs.DOCUMENTS:
            (self.root / name).write_text('old content')

    def generation(self, command, **kwargs):
        self.command = command
        Path(command[command.index('--output') + 1]).write_text(self.markdown)
        Path(command[command.index('--html-output') + 1]).write_text(self.html)

    def run_mode(self, mode, roots=None):
        roots = roots if roots is not None else (self.root / 'civil', self.root / 'ga')
        self.stdout = io.StringIO()
        with patch.object(docs.subprocess, 'run', side_effect=self.generation), \
                patch.object(docs, 'git', return_value='c' * 40), \
                patch.dict(os.environ, {}, clear=True), \
                contextlib.redirect_stdout(self.stdout), contextlib.redirect_stderr(io.StringIO()):
            return docs.generate(mode, *roots, root=self.root)

    def generated_roots(self):
        return tuple(Path(self.command[self.command.index(flag) + 1])
                     for flag in ('--ccd-definition-root', '--ga-ccd-definition-root'))

    def add_toggle(self):
        (self.root / docs.TOGGLE).parent.mkdir(parents=True, exist_ok=True)
        (self.root / docs.TOGGLE).write_text('civil-service is the source of truth\n')

    def test_sync_rejects_stale_outputs_without_modifying_them(self):
        self.assertEqual(1, self.run_mode('sync'))
        for name in docs.DOCUMENTS:
            self.assertEqual('old content', (self.root / name).read_text())

    def test_source_only_drift_is_reported_without_modifying_outputs(self):
        self.assertEqual(0, self.run_mode('report'))
        for name in docs.DOCUMENTS:
            self.assertEqual('old content', (self.root / name).read_text())

    def test_write_then_sync_is_repeatable_and_checks_both_formats(self):
        self.assertEqual(0, self.run_mode('write'))
        self.assertEqual(0, self.run_mode('sync'))
        (self.root / docs.DOCUMENTS[1]).write_text('corrupted HTML')
        self.assertEqual(1, self.run_mode('sync'))
        self.assertEqual(self.markdown, (self.root / docs.DOCUMENTS[0]).read_text())

    def test_manifest_rejects_floating_invalid_and_missing_revisions(self):
        for invalid in ('master', 'a' * 39, '0' * 40, 'a' * 40 + '\nmode=skip', None, 123):
            sources = dict(self.sources)
            sources[docs.REPOSITORIES[0]] = invalid
            (self.root / docs.MANIFEST).write_text(json.dumps(sources))
            with self.subTest(invalid=invalid), self.assertRaises(ValueError):
                docs.load_sources(self.root)
        (self.root / docs.MANIFEST).write_text('{}')
        with self.assertRaises(ValueError):
            docs.load_sources(self.root)

    def test_default_roots_read_civil_and_ga_from_the_civil_ccd_definition_checkout(self):
        self.assertEqual(0, self.run_mode('report', roots=()))
        checkout = (self.root / 'civil-ccd-definition/ccd-definition').resolve()
        self.assertEqual((checkout, checkout / 'generalapplication'), self.generated_roots())
        self.assertIn(f'- civil-ccd-definition: `{"a" * 40}`', self.stdout.getvalue())

    def test_toggle_reads_in_repo_definitions_without_a_manifest(self):
        self.add_toggle()
        (self.root / docs.MANIFEST).write_text('{"stale": "pin"}')
        self.assertEqual(0, self.run_mode('report', roots=()))
        self.assertEqual(((self.root / 'ccd-definition/civil').resolve(),
                          (self.root / 'ccd-definition/generalapplication').resolve()), self.generated_roots())
        self.assertIn(f'- CCD definitions: in-repo `ccd-definition/` ({docs.TOGGLE} is present)',
                      self.stdout.getvalue())
        self.assertNotIn('civil-ccd-definition:', self.stdout.getvalue())
        (self.root / docs.TOGGLE).unlink()
        with self.assertRaises(ValueError):
            self.run_mode('report', roots=())

    def test_toggle_leaves_nothing_to_record(self):
        self.add_toggle()
        with self.assertRaises(ValueError):
            docs.record_sources(self.root / 'civil-ccd-definition', self.root)
        self.assertEqual(self.sources, json.loads((self.root / docs.MANIFEST).read_text()))

    def test_refs_select_the_pinned_checkout_or_none_with_the_toggle(self):
        def refs(*args, in_repo):
            output = io.StringIO()
            with patch.object(docs, 'in_repo_definitions', return_value=in_repo), \
                    patch.object(docs, 'load_sources', return_value=self.sources) as load, \
                    contextlib.redirect_stdout(output):
                self.assertEqual(0, docs.main(['refs', *args]))
            return output.getvalue(), load.called
        self.assertEqual((f'civil_ref={"a" * 40}\nin_repo=false\n', True), refs(in_repo=False))
        self.assertEqual(('civil_ref=\nin_repo=false\n', True), refs('--refresh', in_repo=False))
        self.assertEqual(('civil_ref=\nin_repo=true\n', False), refs(in_repo=True))
        self.assertEqual(('civil_ref=\nin_repo=true\n', False), refs('--refresh', in_repo=True))

    def test_in_repo_definitions_are_inputs_only_with_the_toggle(self):
        for path in ('ccd-definition/civil/CaseEvent/User/x.json',
                     'ccd-definition/generalapplication/CaseEvent/x.json'):
            with self.subTest(path=path):
                self.assertEqual('skip', docs.classify([path]))
                self.assertEqual('report', docs.classify([path], in_repo=True))
        self.assertEqual('skip', docs.classify(['ccd-definition/test/utils/x.js'], in_repo=True))
        self.assertEqual('sync', docs.classify([docs.TOGGLE]))
        with patch.object(docs, 'in_repo_definitions', return_value=True), \
                patch.object(docs, 'git', side_effect=['commit', 'ccd-definition/civil/CaseEvent/x.json\0']):
            self.assertEqual('report', docs.classify_commits('a' * 40, 'b' * 40))

    def test_change_policy_covers_templates_and_combined_source_document_changes(self):
        self.assertEqual('skip', docs.classify(['README.md', 'build.gradle']))
        self.assertEqual('report', docs.classify(['src/main/resources/notification-templates/new.json']))
        self.assertEqual('report', docs.classify(['src/main/java/SomeHandler.java']))
        self.assertEqual('sync', docs.classify(['src/main/java/SomeHandler.java', docs.DOCUMENTS[0]]))
        self.assertEqual('sync', docs.classify([docs.MANIFEST]))
        self.assertEqual('sync', docs.classify(['.github/actions/email-docs/action.yml']))
        self.assertEqual('sync', docs.classify(['.github/scripts/update-email-docs-pr.cjs']))
        self.assertEqual('sync', docs.classify(['.github/scripts/email-docs-artifact.cjs']))
        self.assertEqual('sync', docs.classify(['.github/scripts/email-docs-artifact.test.cjs']))

    def test_changed_commit_classification_accounts_for_removed_inputs_and_zero_base(self):
        with patch.object(docs, 'git', side_effect=[
                'commit', 'src/main/resources/camunda/removed.bpmn\0archive/removed.bpmn\0']) as git:
            self.assertEqual('report', docs.classify_commits('a' * 40, 'b' * 40))
            self.assertIn('--no-renames', git.call_args.args)
        with patch.object(docs, 'git') as git:
            self.assertEqual('sync', docs.classify_commits('0' * 40, 'b' * 40))
            git.assert_not_called()

    def test_missing_comparison_base_requires_sync_without_a_diff(self):
        base = 'a' * 40
        with patch.object(docs, 'git', return_value=f'{base}^{{commit}} missing') as git, \
                contextlib.redirect_stderr(io.StringIO()):
            self.assertEqual('sync', docs.classify_commits(base, 'b' * 40))
            git.assert_called_once_with('cat-file', '--batch-check=%(objecttype)', input=f'{base}^{{commit}}\n')

    def test_git_probe_and_diff_failures_are_not_treated_as_missing_bases(self):
        import subprocess
        failure = subprocess.CalledProcessError(128, 'git')
        for responses in ([failure], ['commit', failure], ['unexpected output']):
            with self.subTest(responses=responses), patch.object(docs, 'git', side_effect=responses):
                with self.assertRaises((subprocess.CalledProcessError, ValueError)):
                    docs.classify_commits('a' * 40, 'b' * 40)

    def test_generation_emits_comparison_result_for_actions(self):
        output = self.root / 'action-output'
        with patch.object(docs.subprocess, 'run', side_effect=self.generation), \
                patch.object(docs, 'git', return_value='c' * 40), \
                patch.dict(os.environ, {'GITHUB_OUTPUT': str(output)}, clear=True), \
                contextlib.redirect_stdout(io.StringIO()):
            self.assertEqual(0, docs.generate('report', self.root / 'civil', self.root / 'ga', self.root))
            for name, content in zip(docs.DOCUMENTS, (self.markdown, self.html)):
                (self.root / name).write_text(content)
            self.assertEqual(0, docs.generate('sync', self.root / 'civil', self.root / 'ga', self.root))
        self.assertEqual('in_sync=false\nin_sync=true\n', output.read_text())

    def test_generation_failure_preserves_outputs_even_in_write_mode(self):
        import subprocess
        with patch.object(docs.subprocess, 'run', side_effect=subprocess.CalledProcessError(1, 'generator')):
            with self.assertRaises(subprocess.CalledProcessError):
                docs.generate('write', self.root / 'civil', self.root / 'ga', self.root)
        for name in docs.DOCUMENTS:
            self.assertEqual('old content', (self.root / name).read_text())


if __name__ == '__main__':
    unittest.main()
