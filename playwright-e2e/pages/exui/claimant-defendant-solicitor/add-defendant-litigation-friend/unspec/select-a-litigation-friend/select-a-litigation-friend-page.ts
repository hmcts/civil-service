import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../../models/ccd-case-data';
import ExuiPage from '../../../../mixin-pages/exui-page/exui-page';
import { dropdowns } from './select-a-litigation-friend-content';

@AllMethodsStep()
export default class SelectALitigationFriendPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLabel(dropdowns.selectLitigationFriend.label),
    ]);
  }

  async selectBoth() {
    await super.selectFromDropdown(
      dropdowns.selectLitigationFriend.options.both,
      dropdowns.selectLitigationFriend.selector,
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
