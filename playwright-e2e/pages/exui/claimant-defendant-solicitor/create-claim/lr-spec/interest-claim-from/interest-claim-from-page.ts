import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import { radioButtons, subheadings } from './interest-claim-from-content';

@AllMethodsStep()
export default class InterestClaimFromPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([super.expectText(subheadings.interestClaimFrom)]);
  }

  async selectFromClaimSubmitDate() {
    await super.clickBySelector(radioButtons.interestClaimFrom.submitDate.selector);
  }

  async selectfromASpecificDate() {
    await super.clickBySelector(radioButtons.interestClaimFrom.specificDate.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
