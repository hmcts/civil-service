import BasePage from '../../../../../base/base-page';
import ExuiHearingsPage from '../../../mixin-pages/exui-hearings-page/exui-hearings-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import { heading, radioButtons, checkboxes, dropdowns, inputs } from './hearing-attendance-content';

@AllMethodsStep()
export default class HearingAttendancePage extends ExuiHearingsPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyCaseName(ccdCaseData),
      super.expectHeading(heading),
    ]);
  }

  async selectAttendance() {
    await super.clickBySelector(checkboxes.telephone.selector);

    const partyChannelCount = await super.countBySelector(dropdowns.selector);
    for (let index = 0; index < partyChannelCount; index++) {
      await super.selectFromDropdown(dropdowns.inPerson.label, `#partyChannel${index}`);
    }

    await super.inputText(inputs.numberOfAttendees, inputs.selector);
  }

  async updateAttendance() {
    await super.clickBySelector(checkboxes.inPerson.selector);
  }

  async continue() {
    await super.clickContinue();
  }
}
