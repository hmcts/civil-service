import { test as setup } from '../../../playwright-fixtures/index';
import { exuiAuthSetupUsers } from '../../../config/users/exui-users';
import config from '../../../config/config';

if (config.runSetup) {
  setup.describe('Authenticating exui user(s) and saving cookies', () => {
    setup.describe.configure({ mode: 'parallel' });

    for (const [index, exuiAuthSetupUser] of exuiAuthSetupUsers.entries()) {
      if (index === 0) {
        setup(
          exuiAuthSetupUser.name,
          { tag: '@verify-cookies-banner' },
          async ({ IdamSteps, ExuiDashboardSteps }) => {
            await IdamSteps.ExuiLogin(exuiAuthSetupUser);
            await ExuiDashboardSteps.AcceptCookies();
            await ExuiDashboardSteps.SaveCookies(exuiAuthSetupUser);
          },
        );
      } else
        setup(exuiAuthSetupUser.name, async ({ IdamSteps, ExuiDashboardSteps }) => {
          await IdamSteps.ExuiLogin(exuiAuthSetupUser);
          await ExuiDashboardSteps.SaveCookies(exuiAuthSetupUser);
        });
    }
  });
} else {
  console.log('Skipping authenticate exui users and save cookies setup');
  console.log('All exui users will be logged in via Idam when needed during each test execution');
}
