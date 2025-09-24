import BasePage from '../../../../../../../base/base-page';
import ExuiPage from '../../../../../exui-page/exui-page';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps';
import { radioButtons, subheadings, paragraphs } from './respondent-response-content.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';

@AllMethodsStep()
export default class RespondentResponsePage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheadings.docUrl, { count: 1 }),
      // super.expectText(paragraphs.rejectAll, { count: 1 }),
      super.expectLegend(radioButtons.proceedWithClaim.label, { count: 1 }),
      super.expectRadioYesLabel(radioButtons.proceedWithClaim.yes.selector),
      super.expectRadioNoLabel(radioButtons.proceedWithClaim.no.selector),
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
