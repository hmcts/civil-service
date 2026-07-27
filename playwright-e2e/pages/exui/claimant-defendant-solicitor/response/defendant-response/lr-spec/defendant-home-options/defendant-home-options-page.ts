import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd-case-data.ts';
import ExuiPage from '../../../../../mixin-pages/exui-page/exui-page.ts';
import { radioButtons } from './defendant-home-options-content.ts';

@AllMethodsStep()
export default class DefendantHomeOptionsPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLabel(radioButtons.homeType.ownedHome.label),
      super.expectLabel(radioButtons.homeType.jointlyOwned.label),
      super.expectLabel(radioButtons.homeType.privateRental.label),
      super.expectLabel(radioButtons.homeType.associationHome.label),
      super.expectLabel(radioButtons.homeType.other.label),
    ]);
  }

  async selectOwnedHome() {
    await super.clickBySelector(radioButtons.homeType.ownedHome.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
