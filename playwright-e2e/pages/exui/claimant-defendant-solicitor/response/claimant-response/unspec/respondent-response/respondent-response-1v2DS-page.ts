import BasePage from '../../../../../../../base/base-page.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import { radioButtons, subheadings, paragraphs } from './respondent-response-content.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import partys from '../../../../../../../constants/partys.ts';
import { Page } from '@playwright/test';

@AllMethodsStep()
export default class RespondentResponse1v2DSPage extends ExuiPage(BasePage) {
  constructor(page: Page) {
    super(page);
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheadings.docUrl, { count: 2 }),
      // super.expectText(paragraphs.rejectAll, { count: 2 }),
      super.expectRadioYesLabel(radioButtons.proceedWithClaim.yes.selector1v2(partys.DEFENDANT_1)),
      super.expectRadioNoLabel(radioButtons.proceedWithClaim.no.selector1v2(partys.DEFENDANT_1)),
      super.expectRadioYesLabel(radioButtons.proceedWithClaim.yes.selector1v2(partys.DEFENDANT_2)),
      super.expectRadioNoLabel(radioButtons.proceedWithClaim.no.selector1v2(partys.DEFENDANT_2)),
    ]);
  }

  async selectYesBothDefendants() {
    await super.clickBySelector(radioButtons.proceedWithClaim.yes.selector1v2(partys.DEFENDANT_1));
    await super.clickBySelector(radioButtons.proceedWithClaim.yes.selector1v2(partys.DEFENDANT_2));
  }

  async selectNoBothDefendants() {
    await super.clickBySelector(radioButtons.proceedWithClaim.no.selector1v2(partys.DEFENDANT_1));
    await super.clickBySelector(radioButtons.proceedWithClaim.no.selector1v2(partys.DEFENDANT_2));
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
