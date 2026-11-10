import importlib.util
from pathlib import Path
import sys
import tempfile
import unittest

spec = importlib.util.spec_from_file_location('unit_ci_benchmark', Path(__file__).with_name('unit-ci-benchmark.py'))
benchmark = importlib.util.module_from_spec(spec)
spec.loader.exec_module(benchmark)


class BenchmarkTests(unittest.TestCase):
    def test_junit_states_are_counted_without_trusting_suite_totals(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            target = root / 'build/test-results/test'
            target.mkdir(parents=True)
            (target / 'TEST-example.xml').write_text('''<testsuite name="Suite" tests="999">
              <testcase classname="Example" name="passes" time="0.25"/>
              <testcase classname="Example" name="skips"><skipped/></testcase>
              <testcase classname="Example" name="fails"><failure>detail</failure></testcase>
              <testcase classname="Example" name="errors"><error>detail</error></testcase>
            </testsuite>''')
            cases, states = benchmark.test_results(root)
            self.assertEqual((states['total'], states['passed'], states['failed'], states['skipped']), (4, 1, 2, 1))
            self.assertEqual(len(cases), 4)
            self.assertEqual(states['identityAndStateSha256'], benchmark.test_results(root)[1]['identityAndStateSha256'])

    def test_coverage_uses_root_counters_without_double_counting_classes(self):
        with tempfile.TemporaryDirectory() as temporary:
            file = Path(temporary) / 'coverage.xml'
            file.write_text('''<report><package><class><counter type="LINE" covered="1" missed="2"/></class></package>
              <counter type="LINE" covered="10" missed="5"/>
              <counter type="BRANCH" covered="6" missed="2"/></report>''')
            self.assertEqual(benchmark.coverage_counters(file), {
                'LINE': {'covered': 10, 'missed': 5}, 'BRANCH': {'covered': 6, 'missed': 2}})

    @unittest.skipUnless(sys.platform.startswith('linux'), 'Resource sampling uses Linux /proc')
    def test_measure_records_owned_process_memory_and_exit_state(self):
        with tempfile.TemporaryDirectory() as temporary:
            result = benchmark.measure([sys.executable, '-c', 'import time; data=bytearray(12*1024*1024); time.sleep(1.2)'],
                                       Path(temporary), dict(benchmark.os.environ), timeout=5)
            self.assertEqual(result['exitCode'], 0)
            self.assertFalse(result['timedOut'])
            self.assertGreater(result['peakProcessTreeRssBytes'], 8 * 1024 * 1024)
            self.assertGreater(result['observedProcesses'], 0)

    @unittest.skipUnless(sys.platform.startswith('linux'), 'Resource sampling uses Linux /proc')
    def test_timeout_fails_and_terminates_the_owned_process(self):
        with tempfile.TemporaryDirectory() as temporary:
            result = benchmark.measure([sys.executable, '-c', 'import time; time.sleep(20)'],
                                       Path(temporary), dict(benchmark.os.environ), timeout=0.1)
            self.assertNotEqual(result['exitCode'], 0)
            self.assertTrue(result['timedOut'])


if __name__ == '__main__':
    unittest.main()
