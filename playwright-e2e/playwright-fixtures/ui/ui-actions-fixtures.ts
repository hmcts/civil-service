import { test as requestFactories } from '../api/request-factory-fixtures';
import { test as testUtils } from '../utils/test-utils-fixtures';
import { mergeTests } from 'playwright/test';
import CaseworkerActionsFactory from '../../actions/ui/exui/caseworker/caseworker-actions-factory';
import ClaimantSolicitorActionsFactory from '../../actions/ui/exui/claimant-solicitor/claimant-solcitor-actions-factory';
import ExuiDashboardActions from '../../actions/ui/exui/common/exui-dashboard-actions';
import DefendantActionsFactory from '../../actions/ui/exui/defendant-solicitor/defendant-actions-factory';
import JudgeActionsFactory from '../../actions/ui/exui/judge/judge-actions-factory';
import IdamActions from '../../actions/ui/idam/idam-actions';
import PageUtilsFactory from '../../pages/utils/page-utils-factory';
import IdamPageFactory from '../../pages/idam/idam-page-factory';
import ExuiDashboardPageFactory from '../../pages/exui/exui-dashboard/exui-dashboard-page-factory';
import RequestsFactory from '../../requests/requests-factory';
import HearingCenterAdminActionsFactory from '../../actions/ui/exui/hearing-center-admin/hearing-center-admin-actions-factory';

type UiActionsFixtures = {
  _idamActions: IdamActions;
  _exuiDashboardActions: ExuiDashboardActions;
  _claimantSolicitor1ActionsFactory: ClaimantSolicitorActionsFactory;
  _defendantActionsFactory: DefendantActionsFactory;
  _caseworkerActionsFactory: CaseworkerActionsFactory;
  _hearingCenterAdminActionsFactory: HearingCenterAdminActionsFactory;
  _judgeActionsFactory: JudgeActionsFactory;
};

export const test = mergeTests(testUtils, requestFactories).extend<UiActionsFixtures>({
  _idamActions: async ({ page, request, _testData, _isSetupTest, _isTeardownTest, _verifyCookiesBanner }, use) => {
    await use(new IdamActions(new PageUtilsFactory(page), new IdamPageFactory(page), new RequestsFactory(request), _isSetupTest, _isTeardownTest, _verifyCookiesBanner, _testData));
  },
  _exuiDashboardActions: async ({ page, request, _testData }, use) => {
    await use(new ExuiDashboardActions(new PageUtilsFactory(page), new ExuiDashboardPageFactory(page), new RequestsFactory(request), _testData));
  },
  _claimantSolicitor1ActionsFactory: async ({ page, _testData }, use) => {
    await use(new ClaimantSolicitorActionsFactory(page, _testData));
  },
  _defendantActionsFactory: async ({ page, _testData }, use) => {
    await use(new DefendantActionsFactory(page, _testData));
  },
  _caseworkerActionsFactory: async ({ page, _testData }, use) => {
    await use(new CaseworkerActionsFactory(page, _testData));
  },
  _hearingCenterAdminActionsFactory: async ({ page, _testData }, use) => {
    await use(new HearingCenterAdminActionsFactory(page, _testData));
  },
  _judgeActionsFactory: async ({ page, _testData }, use) => {
    await use(new JudgeActionsFactory(page, _testData));
  }
});
