import { test as setup } from '../../../playwright-fixtures/index';
import { exuiUserDataSetupUsers } from '../../../config/users/exui-users';
import config from '../../../config/config';

if (config.runSetup) {
  setup.describe('Setting up user data', () => {
    setup.describe.configure({ mode: 'parallel' });

    exuiUserDataSetupUsers.forEach((exuiAuthSetupUser) => {
      setup(exuiAuthSetupUser.name, async ({ IdamApiSteps }) => {
        await IdamApiSteps.SetupUserData(exuiAuthSetupUser);
      });
    });
  });
} else {
  console.log('Skipping setting up user data');
  console.log('All users will get user data when needed during test execution');
}
