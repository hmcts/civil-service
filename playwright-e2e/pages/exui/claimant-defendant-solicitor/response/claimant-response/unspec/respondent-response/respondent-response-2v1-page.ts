import BasePage from '../../../../../../../base/base-page.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import {
  radioButtons,
  paragraphs,
  subheadings,
} from '../respondent-response/respondent-response-content.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import partys from '../../../../../../../constants/partys.ts';

@AllMethodsStep()
export default class RespondentResponse2v1Page extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheadings.docUrl, { count: 1 }),
      super.expectLegend(radioButtons.proceedWithClaim.label2v1(partys.CLAIMANT_1), {
        exact: false,
      }),
      super.expectLegend(radioButtons.proceedWithClaim.label2v1(partys.CLAIMANT_2), {
        exact: false,
      }),
      super.expectRadioYesLabel(radioButtons.proceedWithClaim.yes.selector2v1(partys.CLAIMANT_1)),
      super.expectRadioNoLabel(radioButtons.proceedWithClaim.no.selector2v1(partys.CLAIMANT_1)),
      super.expectRadioYesLabel(radioButtons.proceedWithClaim.yes.selector2v1(partys.CLAIMANT_2)),
      super.expectRadioNoLabel(radioButtons.proceedWithClaim.no.selector2v1(partys.CLAIMANT_2)),
    ]);
  }

  async selectYesBothClaimants() {
    await super.clickBySelector(radioButtons.proceedWithClaim.yes.selector2v1(partys.CLAIMANT_1));
    await super.clickBySelector(radioButtons.proceedWithClaim.yes.selector2v1(partys.CLAIMANT_2));
  }

  async selectNoBothClaimants() {
    await super.clickBySelector(radioButtons.proceedWithClaim.no.selector2v1(partys.CLAIMANT_1));
    await super.clickBySelector(radioButtons.proceedWithClaim.no.selector2v1(partys.CLAIMANT_2));
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
