import BasePageActionsFactory from '../../../../base/base-page-actions-factory';
import CreateCaseFlagsPageFactory from '../../../../pages/exui/hearing-center-admin/create-case-flags/create-case-flags-page-factory';
import ManageCaseFlagsPageFactory from '../../../../pages/exui/hearing-center-admin/manage-case-flags/manage-case-flags-page-factory';
import CreateCaseFlagsActions from './create-case-flags/create-case-flags-actions';
import CreateCaseFlagsSpecActions from './create-case-flags/create-case-flags-spec-actions';
import ManageCaseFlagsActions from './manage-case-flags-actions';

export default class HearingCenterAdminActionsFactory extends BasePageActionsFactory {
  get createCaseFlagsActions() {
    return new CreateCaseFlagsActions(new CreateCaseFlagsPageFactory(this.page), this.testData);
  }

  get createCaseFlagsSpecActions() {
    return new CreateCaseFlagsSpecActions(new CreateCaseFlagsPageFactory(this.page), this.testData);
  }

  get manageCaseFlagsActions() {
    return new ManageCaseFlagsActions(new ManageCaseFlagsPageFactory(this.page), this.testData);
  }
}
