import BasePage from '../../../../../../../base/base-page.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import { paragraphs, radioButtons } from './respondent-response-spec-content.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import { getDQDocName } from '../../../../../exui-page/exui-content.ts';

@AllMethodsStep()
export default class RespondentResponse2v1SpecPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectText(paragraphs.responseForm, { count: 1 }),
      super.expectLegend(radioButtons.proceedWithClaim.label2v1),
      super.expectLink(getDQDocName(ccdCaseData)),
      super.expectRadioYesLabel(radioButtons.proceedWithClaim.yes.selector2v1),
      super.expectRadioNoLabel(radioButtons.proceedWithClaim.no.selector2v1),
    ]);
  }

  async selectYes() {
    await super.clickBySelector(radioButtons.proceedWithClaim.yes.selector2v1);
  }

  async selectNo() {
    await super.clickBySelector(radioButtons.proceedWithClaim.no.selector2v1);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
