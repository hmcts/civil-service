import BasePage from '../../../../../../../base/base-page.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import {
  paragraphs,
  radioButtons,
} from '../respondent-response-spec/respondent-response-spec-content.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import { getDQDocName } from '../../../../../exui-page/exui-content.ts';

@AllMethodsStep()
export default class RespondentResponse1v2DSSpecPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectText(paragraphs.responseForm, { count: 1 }),
      super.expectText(paragraphs.responseForm2, { count: 1 }),
      super.expectText(radioButtons.proceedWithClaim.label),
      super.expectLink(getDQDocName(ccdCaseData), { count: 2 }),
    ]);
  }

  async selectYes() {
    await super.clickBySelector(radioButtons.proceedWithClaim.yes.selector);
  }

  async selectNo() {
    await super.clickBySelector(radioButtons.proceedWithClaim.no.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
