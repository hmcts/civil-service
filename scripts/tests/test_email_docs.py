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
        self.sources = {name: digit * 40 for name, digit in zip(docs.REPOSITORIES, ('a', 'b'))}
        (self.root / docs.MANIFEST).write_text(json.dumps(self.sources))
        self.markdown = '|head|\n|---|\n|row|\n'
        self.html = '<html>generated</html>\n'
        for name in docs.DOCUMENTS:
            (self.root / name).write_text('old content')

    def generation(self, command, **kwargs):
        Path(command[command.index('--output') + 1]).write_text(self.markdown)
        Path(command[command.index('--html-output') + 1]).write_text(self.html)

    def run_mode(self, mode):
        with patch.object(docs.subprocess, 'run', side_effect=self.generation), \
                patch.object(docs, 'git', return_value='c' * 40), \
                patch.dict(os.environ, {}, clear=True), \
                contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(io.StringIO()):
            return docs.generate(mode, self.root / 'civil', self.root / 'ga', self.root)

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

    def test_change_policy_covers_templates_and_combined_source_document_changes(self):
        self.assertEqual('skip', docs.classify(['README.md', 'build.gradle']))
        self.assertEqual('report', docs.classify(['src/main/resources/notification-templates/new.json']))
        self.assertEqual('report', docs.classify(['src/main/java/SomeHandler.java']))
        self.assertEqual('sync', docs.classify(['src/main/java/SomeHandler.java', docs.DOCUMENTS[0]]))
        self.assertEqual('sync', docs.classify([docs.MANIFEST]))
        self.assertEqual('sync', docs.classify(['.github/actions/email-docs/action.yml']))
        self.assertEqual('sync', docs.classify(['.github/scripts/update-email-docs-pr.cjs']))

    def test_changed_commit_classification_accounts_for_removed_inputs_and_zero_base(self):
        with patch.object(docs, 'git', return_value='src/main/resources/camunda/removed.bpmn\0archive/removed.bpmn\0') as git:
            self.assertEqual('report', docs.classify_commits('a' * 40, 'b' * 40))
            self.assertIn('--no-renames', git.call_args.args)
        with patch.object(docs, 'git') as git:
            self.assertEqual('sync', docs.classify_commits('0' * 40, 'b' * 40))
            git.assert_not_called()

    def test_generation_failure_preserves_outputs_even_in_write_mode(self):
        import subprocess
        with patch.object(docs.subprocess, 'run', side_effect=subprocess.CalledProcessError(1, 'generator')):
            with self.assertRaises(subprocess.CalledProcessError):
                docs.generate('write', self.root / 'civil', self.root / 'ga', self.root)
        for name in docs.DOCUMENTS:
            self.assertEqual('old content', (self.root / name).read_text())


if __name__ == '__main__':
    unittest.main()
