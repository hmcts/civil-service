import BasePage from '../../../../../base/base-page';
import ExuiHearingsPage from '../../../mixin-pages/exui-hearings-page/exui-hearings-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import { heading, inputs, radioButtons } from './hearing-timing-content';

@AllMethodsStep()
export default class HearingTimingPage extends ExuiHearingsPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyCaseName(ccdCaseData),
      super.expectHeading(heading),
    ]);
  }

  async enterHearingLength() {
    await super.inputText(
      inputs.lengthOfHearing.hours.value1,
      inputs.lengthOfHearing.hours.selector,
    );
  }

  async updateHearingLength() {
    await super.expectText(heading);
    await super.inputText(
      inputs.lengthOfHearing.hours.value2,
      inputs.lengthOfHearing.hours.selector,
    );
  }

  async selectHearingDate() {
    await super.expectText(radioButtons.specificDate.label);
    await super.clickBySelector(radioButtons.specificDate.no.selector);
  }

  async selectHearingPriority() {
    await super.expectText(radioButtons.priorityOfHearing.label);
    await super.clickBySelector(radioButtons.priorityOfHearing.standard.selector);
  }

  async continue() {
    await super.clickContinue();
  }
}
