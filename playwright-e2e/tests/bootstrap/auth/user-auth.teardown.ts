import { test as teardown } from '../../../playwright-fixtures/index';
import { exuiAuthSetupUsers } from '../../../config/users/exui-users';
import config from '../../../config/config';

if (config.runSetup) {
  teardown.describe('Signing out exui user(s)', () => {
    teardown.describe.configure({ mode: 'parallel' });

    teardown(exuiAuthSetupUsers[0].name, async ({ IdamSteps, ExuiDashboardSteps }) => {
      await IdamSteps.ExuiLogin(exuiAuthSetupUsers[0]);
      await ExuiDashboardSteps.GoToCaseList();
      await ExuiDashboardSteps.SignOut();
    });
  });
}
