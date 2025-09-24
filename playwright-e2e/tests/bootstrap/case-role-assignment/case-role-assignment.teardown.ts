import { test as teardown } from '../../../playwright-fixtures/index';
import { solicitorUsers } from '../../../config/users/exui-users';
import config from '../../../config/config';

if (config.unassignCases) {
  teardown.describe('Unassigning case roles for solicitor users', () => {
    teardown.describe.configure({ mode: 'parallel' });

    for (const solicitorUser of solicitorUsers) {
      teardown(solicitorUser.name, async ({ CaseRoleAssignmentApiSteps }) => {
        await CaseRoleAssignmentApiSteps.UnassignCasesForUser(solicitorUser);
      });
    }
  });
} else {
  console.log('Skipping case role unassignment for users');
}
