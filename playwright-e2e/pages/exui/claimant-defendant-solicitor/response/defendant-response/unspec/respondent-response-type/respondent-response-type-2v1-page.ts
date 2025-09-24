import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { radioButtons } from './respondent-response-type-content.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import partys from '../../../../../../../constants/partys.ts';

@AllMethodsStep()
export default class RespondentResponseType2v1Page extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLabel(radioButtons.rejectAll.label, { count: 2 }),
      super.expectLabel(radioButtons.admitAll.label, { count: 2 }),
      super.expectLabel(radioButtons.partAdmit.label, { count: 2 }),
      super.expectLabel(radioButtons.counterClaim.label, { count: 2 }),
    ]);
  }

  async selectRejectAll() {
    await super.clickBySelector(
      radioButtons.rejectAll.selector(partys.DEFENDANT_1, partys.CLAIMANT_1),
    );
    await super.clickBySelector(
      radioButtons.rejectAll.selector(partys.DEFENDANT_1, partys.CLAIMANT_2),
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
