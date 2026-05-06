"""Unit tests for generate_email_notifications_table.py.

Run from repo root:
    python3 -m unittest scripts.test_generate_email_notifications_table
"""
from __future__ import annotations

import importlib.util
import textwrap
import unittest
from pathlib import Path
from unittest import mock

REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / 'scripts' / 'generate_email_notifications_table.py'

spec = importlib.util.spec_from_file_location('gen_table', SCRIPT_PATH)
gen_table = importlib.util.module_from_spec(spec)
spec.loader.exec_module(gen_table)


class SqlTupleParsingTests(unittest.TestCase):
    def test_iter_value_tuples_handles_nested_braces_and_quotes(self):
        body = textwrap.dedent(
            """
            ('Scenario.A', '{"Notice.X"}', '{"Notice.Y" : ["p1", "p2"]}'),
            ('Scenario.B', '{}', '{"Notice.Z" : []}')
            """
        )
        tuples = list(gen_table._iter_sql_value_tuples(body))
        self.assertEqual(len(tuples), 2)
        self.assertIn("'Scenario.A'", tuples[0])
        self.assertIn("'Scenario.B'", tuples[1])

    def test_iter_value_tuples_skips_escaped_quotes(self):
        body = "('Scenario.A', '{}', 'has ''escaped'' quote')"
        tuples = list(gen_table._iter_sql_value_tuples(body))
        self.assertEqual(len(tuples), 1)
        fields = gen_table._split_sql_tuple_fields(tuples[0])
        self.assertEqual(fields[0], "'Scenario.A'")
        self.assertEqual(fields[2], "'has ''escaped'' quote'")

    def test_split_fields_respects_brace_nesting(self):
        tuple_text = "'Scenario.A', '{}', '{\"Notice.X\", \"Notice.Y\"}'"
        fields = gen_table._split_sql_tuple_fields(tuple_text)
        self.assertEqual(len(fields), 3)
        self.assertEqual(fields[0], "'Scenario.A'")
        self.assertEqual(fields[2], "'{\"Notice.X\", \"Notice.Y\"}'")


class LoadScenarioTemplateMapTests(unittest.TestCase):
    def _run_with_sql(self, sql_files):
        """Patch MIGRATION_DIR to a temp dir holding the supplied SQL files."""
        import tempfile
        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = Path(tmp)
            for name, body in sql_files.items():
                (tmp_path / name).write_text(body, encoding='utf-8')
            with mock.patch.object(gen_table, 'MIGRATION_DIR', tmp_path):
                return gen_table.load_scenario_template_map()

    def test_insert_with_braced_list_no_quotes(self):
        sql = textwrap.dedent("""
            INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
            VALUES ('Scenario.AAA6.CP.HearingDocuments.Uploaded.Claimant',
                    '{Notice.AAA6.CP.HearingDocuments.Upload.Claimant}',
                    '{}');
        """)
        mapping = self._run_with_sql({'V2024_01_01_0001__a.sql': sql})
        self.assertIn('Scenario.AAA6.CP.HearingDocuments.Uploaded.Claimant', mapping)
        # notifications_to_create is empty — Uploaded scenario only deletes a notice.
        self.assertEqual(mapping['Scenario.AAA6.CP.HearingDocuments.Uploaded.Claimant'], [])

    def test_insert_with_jsonish_create_list(self):
        sql = textwrap.dedent("""
            INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
            VALUES ('Scenario.AAA6.CP.HearingDocuments.Upload.Claimant',
                    '{"Notice.AAA6.ClaimantIntent.GoToHearing.Claimant"}',
                    '{"Notice.AAA6.CP.HearingDocuments.Upload.Claimant" : ["sdoDocumentUploadRequestedDateEn"]}');
        """)
        mapping = self._run_with_sql({'V2024_01_01_0001__a.sql': sql})
        self.assertEqual(
            mapping['Scenario.AAA6.CP.HearingDocuments.Upload.Claimant'],
            ['Notice.AAA6.CP.HearingDocuments.Upload.Claimant'],
        )

    def test_multi_scenario_insert(self):
        sql = textwrap.dedent("""
            INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
            VALUES ('Scenario.A', '{}', '{"Notice.A" : []}'),
                   ('Scenario.B', '{}', '{"Notice.B1", "Notice.B2"}');
        """)
        mapping = self._run_with_sql({'V2024_01_01_0001__a.sql': sql})
        self.assertEqual(mapping['Scenario.A'], ['Notice.A'])
        self.assertEqual(mapping['Scenario.B'], ['Notice.B1', 'Notice.B2'])

    def test_later_migration_overrides_create_list_via_update(self):
        first = textwrap.dedent("""
            INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
            VALUES ('Scenario.X', '{}', '{"Notice.Original"}');
        """)
        second = textwrap.dedent("""
            UPDATE dbs.scenario SET notifications_to_create = '{"Notice.Replacement"}'
            WHERE name = 'Scenario.X';
        """)
        mapping = self._run_with_sql({
            'V2024_01_01_0001__a.sql': first,
            'V2024_02_01_0001__b.sql': second,
        })
        self.assertEqual(mapping['Scenario.X'], ['Notice.Replacement'])

    def test_update_to_delete_does_not_override_create_list(self):
        first = textwrap.dedent("""
            INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
            VALUES ('Scenario.X', '{}', '{"Notice.Original"}');
        """)
        second = textwrap.dedent("""
            UPDATE dbs.scenario SET notifications_to_delete = '{"Notice.Other"}'
            WHERE name = 'Scenario.X';
        """)
        mapping = self._run_with_sql({
            'V2024_01_01_0001__a.sql': first,
            'V2024_02_01_0001__b.sql': second,
        })
        self.assertEqual(mapping['Scenario.X'], ['Notice.Original'])


class TemplateLinksForScenariosTests(unittest.TestCase):
    def test_uses_migration_map_when_template_name_differs_from_scenario(self):
        scenarios = {'Scenario.AAA6.CP.HearingDocuments.Upload.Claimant'}
        template_map = {
            'Notice.AAA6.CP.HearingDocuments.Upload.Claimant': {
                'path': 'src/main/resources/notification-templates/Notice.AAA6.CP.HearingDocuments.Upload.Claimant.json',
                'content': '{}',
            }
        }
        scenario_template_map = {
            'Scenario.AAA6.CP.HearingDocuments.Upload.Claimant': [
                'Notice.AAA6.CP.HearingDocuments.Upload.Claimant'
            ]
        }
        links = gen_table.template_links_for_scenarios(scenarios, template_map, scenario_template_map)
        self.assertEqual(len(links), 1)
        self.assertEqual(links[0]['label'], 'Notice.AAA6.CP.HearingDocuments.Upload.Claimant')
        self.assertIn('path', links[0])

    def test_returns_task_list_only_when_migration_map_says_no_create(self):
        scenarios = {'Scenario.AAA6.CP.HearingDocuments.Uploaded.Claimant'}
        template_map = {}
        scenario_template_map = {'Scenario.AAA6.CP.HearingDocuments.Uploaded.Claimant': []}
        links = gen_table.template_links_for_scenarios(scenarios, template_map, scenario_template_map)
        self.assertEqual(len(links), 1)
        self.assertEqual(links[0]['label'], 'No template — task list only')

    def test_falls_back_to_derived_name_when_scenario_absent_from_migration(self):
        scenarios = {'Scenario.AAA6.New.Scenario'}
        template_map = {
            'Notice.AAA6.New.Scenario': {'path': 'p', 'content': '{}'}
        }
        links = gen_table.template_links_for_scenarios(scenarios, template_map, {})
        self.assertEqual(len(links), 1)
        self.assertEqual(links[0]['label'], 'Notice.AAA6.New.Scenario')

    def test_handles_multiple_templates_per_scenario(self):
        scenarios = {'Scenario.X'}
        template_map = {
            'Notice.A': {'path': 'a.json', 'content': '{}'},
            'Notice.B': {'path': 'b.json', 'content': '{}'},
        }
        scenario_template_map = {'Scenario.X': ['Notice.A', 'Notice.B']}
        links = gen_table.template_links_for_scenarios(scenarios, template_map, scenario_template_map)
        labels = [link['label'] for link in links]
        self.assertEqual(labels, ['Notice.A', 'Notice.B'])


if __name__ == '__main__':
    unittest.main()
