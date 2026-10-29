import contextlib
import importlib.util
import io
import json
import shutil
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

SCRIPT = Path(__file__).resolve().parents[1] / 'generate_email_notifications_table.py'
SPEC = importlib.util.spec_from_file_location('email_generator', SCRIPT)
generator = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(generator)

BPMN = '''<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">
  <message id="message" name="REQUEST"/>
  <process id="process" name="Request process">
    <startEvent id="start"><messageEventDefinition messageRef="message"/></startEvent>
    <serviceTask id="NotifyTask"/>
  </process>
</definitions>'''


class GeneratorTests(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name)
        self.repo = self.root / 'service'
        resources = self.repo / 'src/main/resources'
        self.bpmn = resources / 'camunda'
        self.bpmn.mkdir(parents=True)
        (self.bpmn / 'request.bpmn').write_text(BPMN)
        (resources / 'application.yaml').write_text(
            'notifications:\n  exampleTemplate: "11111111-1111-1111-1111-111111111111"\n')
        java = self.repo / 'src/main/java'
        java.mkdir(parents=True)
        # Exercise discovery from Java sources through BPMN/CCD enrichment and both renderers.
        for name, source in {
            'ExampleNotifier': '''public class ExampleNotifier extends Notifier {
                public ExampleNotifier(ExamplePartiesEmailGenerator generator) { super(generator); }
                public String getEvent() { return CaseEvent.NotifyTask.toString(); }
            }''',
            'ExamplePartiesEmailGenerator': '''public class ExamplePartiesEmailGenerator
                    implements PartiesEmailGenerator {
                public ExamplePartiesEmailGenerator(ExampleEmailDTOGenerator generator) {}
            }''',
            'ExampleEmailDTOGenerator': '''public class ExampleEmailDTOGenerator
                    extends ClaimantEmailDTOGenerator {
                public String getTemplate() { return notificationsProperties.getExampleTemplate(); }
            }''',
        }.items():
            (java / f'{name}.java').write_text(source)
        self.civil = self.definition('civil', 'REQUEST', 'Readable request')
        self.ga = self.definition('ga', 'GA_REQUEST', 'GA request')
        self.markdown = self.root / 'matrix.md'
        self.html = self.root / 'matrix.html'
        self.markdown.write_text('existing markdown')
        self.html.write_text('existing html')
        for name, value in {
            'REPO_ROOT': self.repo, 'RESOURCE_ROOT': resources,
            'APPLICATION_YAML': resources / 'application.yaml',
            'JAVA_ROOT': self.repo / 'src/main/java',
            'DASHBOARD_TASK_IDS_PATH': self.repo / 'TaskIds.java',
            'DASHBOARD_SCENARIOS_PATH': self.repo / 'Scenarios.java',
            'TEMPLATE_DIR': resources / 'notification-templates',
        }.items():
            patcher = patch.object(generator, name, value)
            patcher.start()
            self.addCleanup(patcher.stop)

    def definition(self, directory, event, label):
        root = self.root / directory
        events = root / 'CaseEvent/User'
        events.mkdir(parents=True)
        (events / 'events.json').write_text(json.dumps([{'ID': event, 'Name': label}]))
        return root

    def arguments(self):
        return ['--ccd-definition-root', str(self.civil), '--ga-ccd-definition-root', str(self.ga),
                '--output', str(self.markdown), '--html-output', str(self.html)]

    def run_generator(self, arguments):
        stderr = io.StringIO()
        with contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(stderr):
            code = generator.main(arguments)
        return code, stderr.getvalue()

    def assert_bpmn_rejected(self, xml, diagnostic):
        (self.bpmn / 'request.bpmn').write_text(xml)
        with patch.object(generator, 'SourceIndex') as index:
            code, error = self.run_generator(self.arguments())
            index.assert_not_called()
        self.assertEqual(1, code)
        self.assertIn('Invalid BPMN file', error)
        self.assertIn('request.bpmn', error)
        self.assertIn(diagnostic, error)
        self.assertEqual('existing markdown', self.markdown.read_text())
        self.assertEqual('existing html', self.html.read_text())

    def test_each_invalid_ccd_root_preserves_both_outputs(self):
        empty = self.root / 'empty'
        empty.mkdir()
        malformed = self.root / 'malformed/CaseEvent'
        malformed.mkdir(parents=True)
        (malformed / 'events.json').write_text('{invalid JSON')
        for flag in ('--ccd-definition-root', '--ga-ccd-definition-root'):
            for value in (str(self.root / 'missing'), str(self.markdown), str(empty),
                          str(malformed.parent), ''):
                with self.subTest(flag=flag, value=value):
                    args = self.arguments()
                    args[args.index(flag) + 1] = value
                    with patch.object(generator, 'SourceIndex') as index:
                        code, error = self.run_generator(args)
                        index.assert_not_called()
                    self.assertNotEqual(0, code)
                    self.assertIn(flag, error)
                    self.assertEqual('existing markdown', self.markdown.read_text())
                    self.assertEqual('existing html', self.html.read_text())

    def test_default_and_supported_bpmn_inputs_generate_identical_outputs(self):
        self.assertEqual((0, ''), self.run_generator(self.arguments()))
        expected = (self.markdown.read_bytes(), self.html.read_bytes())
        data_rows = [line for line in self.markdown.read_text().splitlines() if line.startswith('|')][2:]
        self.assertEqual(1, len(data_rows))
        for output in (data_rows[0], self.html.read_text()):
            for value in ('Readable request (REQUEST)', 'NotifyTask', 'request.png', 'Email',
                          'Claimant (LiP)', 'exampleTemplate', '11111111-1111-1111-1111-111111111111'):
                self.assertIn(value, output)
        for root in (self.repo, self.bpmn.parent, self.bpmn):
            with self.subTest(root=root):
                self.assertEqual((0, ''), self.run_generator(self.arguments() + ['--bpmn-root', str(root)]))
                self.assertEqual(expected, (self.markdown.read_bytes(), self.html.read_bytes()))

    def test_unusable_ccd_values_fail_before_either_output_changes(self):
        invalid_values = ('', '   ', '\t\n', None, False, True, 42, [], ['REQUEST'], {}, {'name': 'request'})
        for flag, root in (('--ccd-definition-root', self.civil), ('--ga-ccd-definition-root', self.ga)):
            events = root / 'CaseEvent/User/events.json'
            original = events.read_text()
            for field in ('ID', 'Name'):
                for value in invalid_values:
                    with self.subTest(flag=flag, field=field, value=value):
                        definition = {'ID': 'REQUEST', 'Name': 'Readable request', field: value}
                        events.write_text(json.dumps([definition]))
                        code, error = self.run_generator(self.arguments())
                        self.assertEqual(1, code)
                        self.assertIn(flag, error)
                        self.assertIn('no usable CaseEvent', error)
                        self.assertEqual('existing markdown', self.markdown.read_text())
                        self.assertEqual('existing html', self.html.read_text())
            events.write_text(original)

    def test_usable_ccd_strings_are_trimmed_and_invalid_entries_are_ignored(self):
        for root in (self.civil, self.ga):
            with self.subTest(root=root):
                (root / 'CaseEvent/User/events.json').write_text(json.dumps([
                    {'ID': 'REQUEST', 'Name': True}, {'ID': '  REQUEST\t', 'Name': '  Readable request\n'},
                    {'ID': ['REQUEST'], 'Name': 'Wrong type'}, {'ID': '   ', 'Name': 'Blank ID'},
                    {'ID': 'ANOTHER', 'Name': '   '},
                ]))
                self.assertEqual({'REQUEST': 'Readable request'}, generator.load_ccd_event_names(root))
        self.assertEqual((0, ''), self.run_generator(self.arguments()))
        self.assertIn('Readable request (REQUEST)', self.markdown.read_text())

    def test_omitted_optional_roots_warn_and_generate(self):
        args = ['--output', str(self.markdown), '--html-output', str(self.html)]
        code, error = self.run_generator(args)
        self.assertEqual(0, code)
        self.assertIn('--ccd-definition-root omitted', error)
        self.assertIn('--ga-ccd-definition-root omitted', error)

    def test_available_sibling_definitions_are_discovered(self):
        self.civil.rename(self.root / 'civil-ccd-definition')
        self.ga.rename(self.root / 'civil-general-apps-ccd-definition')
        for repository, child in (('civil-ccd-definition', 'ccd-definition'),
                                  ('civil-general-apps-ccd-definition', 'ga-ccd-definition')):
            checkout = self.root / repository
            (checkout / child).mkdir()
            (checkout / 'CaseEvent').rename(checkout / child / 'CaseEvent')
        self.assertEqual((0, ''), self.run_generator(
            ['--output', str(self.markdown), '--html-output', str(self.html)]))

    def test_unusable_optional_siblings_warn_and_continue(self):
        for repository, child, flag in (
                ('civil-ccd-definition', 'ccd-definition', '--ccd-definition-root'),
                ('civil-general-apps-ccd-definition', 'ga-ccd-definition', '--ga-ccd-definition-root')):
            for kind in ('file', 'empty', 'malformed', 'blank-values'):
                with self.subTest(repository=repository, kind=kind):
                    sibling = self.root / repository / child
                    sibling.parent.mkdir(parents=True)
                    if kind == 'file':
                        sibling.write_text('not a directory')
                    else:
                        sibling.mkdir()
                        if kind != 'empty':
                            (sibling / 'CaseEvent').mkdir()
                            (sibling / 'CaseEvent/events.json').write_text(
                                '{broken' if kind == 'malformed' else '[{"ID":" ","Name":" "}]')
                    args = self.arguments()
                    position = args.index(flag)
                    del args[position:position + 2]
                    code, error = self.run_generator(args)
                    shutil.rmtree(sibling.parent)
                    self.assertEqual(0, code)
                    self.assertIn(f'{flag} omitted', error)
                    self.assertIn('NotifyTask', self.markdown.read_text())
                    self.assertIn('request.png', self.html.read_text())

    def test_missing_empty_or_malformed_bpmn_fails_without_writes(self):
        empty = self.root / 'empty-bpmn'
        empty.mkdir()
        for root in (self.root / 'missing', empty, self.markdown):
            code, error = self.run_generator(self.arguments() + ['--bpmn-root', str(root)])
            self.assertNotEqual(0, code)
            self.assertIn('--bpmn-root', error)
        (self.bpmn / 'request.bpmn').write_text('<broken')
        code, error = self.run_generator(self.arguments())
        self.assertNotEqual(0, code)
        self.assertIn('Invalid BPMN file', error)
        self.assertEqual('existing markdown', self.markdown.read_text())
        self.assertEqual('existing html', self.html.read_text())

    def test_non_bpmn_xml_and_unusable_processes_fail_without_writes(self):
        invalid_inputs = (
            '<not-bpmn/>',
            '<definitions><process id="process"><startEvent id="start"/></process></definitions>',
            '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"/>',
            '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"><process id="process"/></definitions>',
            BPMN.replace('id="process"', 'id="  "'),
            BPMN.replace('id="start"', 'id="  "').replace('id="NotifyTask"', 'id="  "'),
            BPMN.replace('<startEvent', '<startEvent xmlns="wrong"').replace(
                '<serviceTask', '<serviceTask xmlns="wrong"'),
        )
        for xml in invalid_inputs:
            with self.subTest(xml=xml):
                (self.bpmn / 'request.bpmn').write_text(xml)
                code, error = self.run_generator(self.arguments())
                self.assertEqual(1, code)
                self.assertIn('Invalid BPMN file', error)
                self.assertEqual('existing markdown', self.markdown.read_text())
                self.assertEqual('existing html', self.html.read_text())

    def test_valid_timer_process_does_not_require_a_message_or_notification(self):
        (self.bpmn / 'timer.bpmn').write_text('''
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">
                <process id="timer"><startEvent id="start"><timerEventDefinition>
                    <timeCycle>R/PT1H</timeCycle>
                </timerEventDefinition></startEvent><endEvent id="end"/></process>
            </definitions>''')
        self.assertEqual((0, ''), self.run_generator(self.arguments()))
        self.assertIn('Readable request (REQUEST)', self.markdown.read_text())

    def test_each_indexed_id_is_required_alongside_valid_nodes_and_processes(self):
        for attribute in ('', ' id=""', ' id="   "'):
            cases = {
                'startEvent': BPMN.replace(' id="start"', attribute),
                'serviceTask': BPMN.replace(' id="NotifyTask"', attribute),
                'message': BPMN.replace(' id="message"', attribute),
                'process': BPMN.replace('</definitions>',
                    f'<process{attribute}><startEvent id="other-start"/></process></definitions>'),
                'nested serviceTask': BPMN.replace('<serviceTask id="NotifyTask"/>',
                    f'<subProcess id="nested"><serviceTask{attribute}/></subProcess>'),
                'subProcess': BPMN.replace('<serviceTask id="NotifyTask"/>',
                    f'<subProcess{attribute}><serviceTask id="NotifyTask"/></subProcess>'),
                'userTask': BPMN.replace('</process>', f'<userTask{attribute}/></process>'),
                'callActivity': BPMN.replace('</process>', f'<callActivity{attribute}/></process>'),
                'endEvent': BPMN.replace('</process>', f'<endEvent{attribute}/></process>'),
            }
            for kind, xml in cases.items():
                with self.subTest(kind=kind, attribute=attribute):
                    self.assert_bpmn_rejected(xml, kind.split()[-1] + ' requires a non-blank ID')

    def test_a_bad_node_in_a_later_file_preserves_both_outputs(self):
        (self.bpmn / 'z-invalid.bpmn').write_text(BPMN.replace(' id="NotifyTask"', ''))
        code, error = self.run_generator(self.arguments())
        self.assertEqual(1, code)
        self.assertIn('z-invalid.bpmn', error)
        self.assertIn('serviceTask requires a non-blank ID', error)
        self.assertEqual('existing markdown', self.markdown.read_text())
        self.assertEqual('existing html', self.html.read_text())

    def test_duplicate_indexed_ids_are_rejected(self):
        cases = (
            BPMN.replace('id="NotifyTask"', 'id="start"'),
            BPMN.replace('</definitions>', '<message id="message" name="OTHER"/></definitions>'),
            BPMN.replace('</definitions>', '<process id="process"><endEvent id="other-end"/></process></definitions>'),
        )
        for xml in cases:
            with self.subTest(xml=xml):
                self.assert_bpmn_rejected(xml, 'duplicate indexed ID')

    def test_start_message_references_must_resolve_locally(self):
        for attribute in ('', ' messageRef=""', ' messageRef="   "', ' messageRef="missing"',
                          ' messageRef="undeclared:message"'):
            with self.subTest(attribute=attribute):
                self.assert_bpmn_rejected(BPMN.replace(' messageRef="message"', attribute), 'messageRef')
        nested = BPMN.replace('<startEvent', '<subProcess id="nested"><startEvent').replace(
            '</startEvent>', '</startEvent></subProcess>').replace('messageRef="message"', 'messageRef="missing"')
        self.assert_bpmn_rejected(nested, 'messageRef')
        # A matching local ID cannot satisfy a reference to a different namespace.
        qualified = BPMN.replace('<definitions ', '<definitions targetNamespace="urn:local" xmlns:local="urn:local" ')
        qualified = qualified.replace('messageRef="message"', 'messageRef="local:message" xmlns:local="urn:other"')
        self.assert_bpmn_rejected(qualified, 'does not resolve to a local message')

    def test_referenced_messages_require_a_usable_name(self):
        for attribute in ('', ' name=""', ' name="   "'):
            with self.subTest(attribute=attribute):
                self.assert_bpmn_rejected(BPMN.replace(' name="REQUEST"', attribute),
                                          'requires a non-blank name for CCD event enrichment')

    def test_qualified_local_message_references_preserve_mapping_and_namespace_scopes(self):
        self.assertEqual((0, ''), self.run_generator(self.arguments()))
        expected = self.markdown.read_bytes(), self.html.read_bytes()
        qualified = BPMN.replace('<definitions ', '<definitions targetNamespace="urn:local" xmlns:local="urn:local" ')
        qualified = qualified.replace('messageRef="message"', 'messageRef="local:message"')
        scoped = qualified.replace('<process id="process"', '<process xmlns:local="urn:other" id="process"')
        scoped = scoped.replace('<messageEventDefinition ', '<messageEventDefinition xmlns:local="urn:local" ')
        # The alias used inside this sibling must not change the following start event's namespace scope.
        restored = qualified.replace('<startEvent', '<subProcess id="nested" xmlns:local="urn:other">'
                                     '<startEvent id="nested-start"/></subProcess><startEvent', 1)
        for xml in (qualified, scoped, restored):
            with self.subTest(xml=xml):
                (self.bpmn / 'request.bpmn').write_text(xml)
                self.assertEqual((0, ''), self.run_generator(self.arguments()))
                self.assertEqual(expected, (self.markdown.read_bytes(), self.html.read_bytes()))

    def test_valid_nested_nodes_and_unused_unnamed_messages_preserve_mapping(self):
        self.assertEqual((0, ''), self.run_generator(self.arguments()))
        expected = self.markdown.read_bytes(), self.html.read_bytes()
        nested = BPMN.replace('<startEvent', '<subProcess id="nested"><startEvent', 1).replace(
            '</process>', '</subProcess></process>')
        nested = nested.replace('</definitions>', '<message id="unused"/></definitions>')
        (self.bpmn / 'request.bpmn').write_text(nested)
        self.assertEqual((0, ''), self.run_generator(self.arguments()))
        self.assertEqual(expected, (self.markdown.read_bytes(), self.html.read_bytes()))

    def test_known_notification_retains_event_label_and_bpmn_link_in_both_formats(self):
        tasks, events, labels, _, _ = generator.index_bpmn(self.bpmn)
        files, displays, ids = generator.lookup_bpmn('NotifyTask', tasks, events, labels)
        rows = [{'ccd_events': displays, 'ccd_event_ids': ids, 'camunda_task': 'NotifyTask',
                 'bpmn_files': files, 'channel': 'Email', 'party': 'Claimant', 'templates': []}]
        names = generator.load_ccd_event_names(self.civil)
        generator.normalize_ccd_event_labels(rows, names)
        for output in (generator.render_combined_markdown(rows, None),
                       generator.render_combined_html(rows, None, names)):
            self.assertIn('Readable request (REQUEST)', output)
            self.assertIn('request.png', output)
            self.assertIn('NotifyTask', output)

    def test_user_event_name_takes_priority_over_camunda_name(self):
        camunda = self.civil / 'CaseEvent/Camunda'
        camunda.mkdir()
        (camunda / 'events.json').write_text(json.dumps([{'ID': 'REQUEST', 'Name': 'Internal name'}]))
        self.assertEqual('Readable request', generator.load_ccd_event_names(self.civil)['REQUEST'])


if __name__ == '__main__':
    unittest.main()
