import BasePageFactory from '../../../../base/base-page-factory';
import ManageCaseFlagsChooseFlagPage from './manage-case-flags-choose-flag/manage-case-flags-choose-flag-page';
import ManageCaseFlagsUpdateFlagPage from './manage-case-flags-update-flag-page/manage-case-flags-update-flag-page';
import SubmitManageCaseFlags from './submit-manage-case-flags/submit-manage-case-flags-page';

export default class ManageCaseFlagsPageFactory extends BasePageFactory {
  get manageCaseFlagsChooseFlagPage() {
    return new ManageCaseFlagsChooseFlagPage(this.page);
  }

  get manageCaseFlagsUpdateFlagPage() {
    return new ManageCaseFlagsUpdateFlagPage(this.page);
  }

  get submitManageCaseFlagsPage() {
    return new SubmitManageCaseFlags(this.page);
  }
}
