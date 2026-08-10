import BasePage from '../../../../../base/base-page';
import ExuiHearingsPage from '../../../mixin-pages/exui-hearings-page/exui-hearings-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import { heading, checkboxes } from './hearing-change-reason-content';

@AllMethodsStep()
export default class HearingChangeReasonPage extends ExuiHearingsPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyCaseName(ccdCaseData, { ignoreDuplicates: true }),
      super.expectText(heading),
    ]);
  }

  async selectReasonPartyRequestedChange() {
    await super.clickBySelector(checkboxes.partyRequestedChange.selector);
  }

  async continue() {
    await super.clickButtonByName('Submit change request');
  }
}
