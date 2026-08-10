import { test as setup } from '../../../playwright-fixtures/index';
import config from '../../../config/config';

if (config.runSetup) {
  setup.describe('Setting up data', () => {
    setup.describe.configure({ mode: 'parallel' });

    setup('Upcoming bank holidays', async ({ DataApiSteps }) => {
      await DataApiSteps.SetupBankHolidaysData();
    });
  });
} else {
  console.log('Skipping setting up data');
  console.log('All data will be retrieved when needed during test execution');
}
