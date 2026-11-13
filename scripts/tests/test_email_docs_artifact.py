import io
import json
import os
from pathlib import Path
import subprocess
import sys
import tarfile
import tempfile
import textwrap
import unittest
import zipfile


WORKFLOW = Path(__file__).resolve().parents[2] / '.github/workflows/deploy-email-docs.yml'
# Test the exact inline validator: publication must not download or check out code.
CONTENTS = WORKFLOW.read_text(encoding='utf-8')
START = "python3 - <<'PY'\n"
END = '\n          PY'
if CONTENTS.count(START) != 1 or CONTENTS.count(END) != 1:
    raise ValueError('Expected one inline publication validator')
VALIDATOR = textwrap.dedent(CONTENTS.split(START, 1)[1].split(END, 1)[0])


def member(name, content=b'', *, kind=tarfile.REGTYPE, mode=0o644, link=''):
    entry = tarfile.TarInfo(name)
    entry.type = kind
    entry.mode = mode
    entry.linkname = link
    entry.size = len(content) if kind == tarfile.REGTYPE else 0
    return entry, content


class PublicationArchiveTests(unittest.TestCase):
    def setUp(self):
        temporary = tempfile.TemporaryDirectory()
        self.addCleanup(temporary.cleanup)
        self.root = Path(temporary.name)
        self.files = {'index.html': b'<h1>Verified</h1>', 'nested/diagram.svg': b'<svg/>'}
        self.entries = []
        for name, content in self.files.items():
            # hash-object without -w only computes an identity: no repository mutations or network.
            identity = subprocess.check_output(['git', 'hash-object', '--stdin'], input=content, cwd=self.root)
            self.entries.append({'path': name, 'type': 'blob', 'mode': '100644',
                                 'size': len(content), 'sha': identity.decode().strip()})
        self.write_tree()

    def write_tree(self):
        (self.root / 'email-docs-tree.json').write_text(json.dumps(self.entries))

    def members(self):
        return [member('./' + name, content) for name, content in self.files.items()]

    def package(self, members):
        packed = io.BytesIO()
        with tarfile.open(fileobj=packed, mode='w') as bundle:
            for entry, content in members:
                bundle.addfile(entry, io.BytesIO(content) if entry.isfile() else None)
        with zipfile.ZipFile(self.root / 'email-docs.zip', 'w') as archive:
            archive.writestr('email-docs.tar', packed.getvalue())

    def validate(self):
        return subprocess.run([sys.executable, '-B', '-c', VALIDATOR], cwd=self.root,
                              env={**os.environ, 'RUNNER_TEMP': str(self.root)},
                              capture_output=True, text=True)

    def test_complete_artefact_is_verified_and_written_as_data(self):
        self.package([member('.', kind=tarfile.DIRTYPE), *self.members()])
        result = self.validate()
        self.assertEqual(0, result.returncode, result.stderr)
        destination = self.root / 'verified-email-docs'
        self.assertEqual(self.files, {str(path.relative_to(destination)): path.read_bytes()
                                     for path in destination.rglob('*') if path.is_file()})

    def test_changed_content_fails_even_with_the_expected_size(self):
        members = self.members()
        members[0] = member('index.html', self.files['index.html'].swapcase())
        self.package(members)
        result = self.validate()
        self.assertNotEqual(0, result.returncode)
        self.assertIn('content mismatch', result.stderr)

    def test_missing_file_fails(self):
        self.package(self.members()[:1])
        result = self.validate()
        self.assertNotEqual(0, result.returncode)
        self.assertIn('incomplete', result.stderr)

    def test_extra_file_fails(self):
        self.package([*self.members(), member('extra.txt', b'extra')])
        result = self.validate()
        self.assertNotEqual(0, result.returncode)
        self.assertIn('Unexpected or duplicate', result.stderr)

    def test_duplicate_file_fails(self):
        self.package([*self.members(), self.members()[0]])
        result = self.validate()
        self.assertNotEqual(0, result.returncode)
        self.assertIn('Unexpected or duplicate', result.stderr)

    def test_executable_bit_mismatch_fails(self):
        members = self.members()
        members[0][0].mode = 0o755
        self.package(members)
        result = self.validate()
        self.assertNotEqual(0, result.returncode)
        self.assertIn('metadata mismatch', result.stderr)

    def test_traversal_and_absolute_paths_fail_before_writing_files(self):
        for name in ('../outside', '/outside', 'nested/../../outside'):
            with self.subTest(name=name), tempfile.TemporaryDirectory() as temporary:
                self.root = Path(temporary)
                self.write_tree()
                self.package([member(name, b'untrusted')])
                result = self.validate()
                self.assertNotEqual(0, result.returncode)
                self.assertIn('Unsafe documentation archive path', result.stderr)
                self.assertEqual([], list((self.root / 'verified-email-docs').rglob('*')))

    def test_links_are_rejected_without_dereferencing_them(self):
        for kind in (tarfile.SYMTYPE, tarfile.LNKTYPE):
            with self.subTest(kind=kind), tempfile.TemporaryDirectory() as temporary:
                self.root = Path(temporary)
                self.write_tree()
                self.package([member('index.html', kind=kind, link='../outside')])
                result = self.validate()
                self.assertNotEqual(0, result.returncode)
                self.assertIn('Unexpected or duplicate', result.stderr)

    def test_links_in_the_git_tree_are_unsupported(self):
        self.entries[0]['mode'] = '120000'
        self.write_tree()
        self.package(self.members())
        result = self.validate()
        self.assertNotEqual(0, result.returncode)
        self.assertIn('only regular files', result.stderr)

    def test_extra_zip_entries_fail(self):
        self.package(self.members())
        with zipfile.ZipFile(self.root / 'email-docs.zip', 'a') as archive:
            archive.writestr('unexpected.txt', 'extra')
        result = self.validate()
        self.assertNotEqual(0, result.returncode)
        self.assertIn('Unexpected documentation archive contents', result.stderr)


if __name__ == '__main__':
    unittest.main()
