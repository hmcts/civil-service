import BasePage from '../../../../../../../base/base-page';
import ExuiPage from '../../../../../mixin-pages/exui-page/exui-page';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps';
import { paragraphs, radioButtons } from './respondent-response-spec-content.ts';
import CCDCaseData from '../../../../../../../models/ccd-case-data.ts';
import { getResponseSealedFormDocName } from '../../../../../mixin-pages/exui-page/exui-content.ts';

@AllMethodsStep()
export default class RespondentResponseSpecPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectText(paragraphs.responseForm, { count: 1 }),
      super.expectLegend(radioButtons.proceedWithClaim.label),
      super.expectRadioYesLabel(radioButtons.proceedWithClaim.yes.selector),
      super.expectRadioNoLabel(radioButtons.proceedWithClaim.no.selector),
      super.expectButton(getResponseSealedFormDocName(ccdCaseData)),
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
