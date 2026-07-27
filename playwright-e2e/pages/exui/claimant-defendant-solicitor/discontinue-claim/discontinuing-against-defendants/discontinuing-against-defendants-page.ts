import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { getFormattedCaseId } from '../../../mixin-pages/exui-page/exui-content';
import { headings, radioButtons } from './discontinuing-against-defendants-content';

@AllMethodsStep()
export default class DiscontinuingAgainstDefendantsPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.expectText(headings.discontinueThisClaim),
      super.expectHeading(headings.discontinuingAgainstDefendants),
      super.expectHeading(getFormattedCaseId(ccdCaseData?.id!), { exact: false }),
      super.expectHeading(ccdCaseData?.caseNamePublic!, { exact: false }),
      super.expectLegend(radioButtons.label),
      super.expectRadioLabel(radioButtons.yes.label, radioButtons.yes.selector),
      super.expectRadioLabel(radioButtons.no.label, radioButtons.no.selector),
    ]);
  }

  async selectDiscontinuingAgainstBothDefendantsYes() {
    await super.clickBySelector(radioButtons.yes.selector);
  }

  async selectDiscontinuingAgainstBothDefendantsNo() {
    await super.clickBySelector(radioButtons.no.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
