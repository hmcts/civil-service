import BasePage from '../../../../../../../base/base-page.ts';
import partys from '../../../../../../../constants/partys.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { radioButtons } from './respondent-response-type-spec-content.ts';

@AllMethodsStep()
export default class RespondentResponseType1v2SpecPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLabel(radioButtons.fullDefence.label, { count: 2 }),
      super.expectLabel(radioButtons.fullAdmit.label, { count: 2 }),
      super.expectLabel(radioButtons.partAdmit.label, { count: 2 }),
      super.expectLabel(radioButtons.counterClaim.label, { count: 2 }),
    ]);
  }

  async selectFullDefenceBothDefendants() {
    await super.clickBySelector(radioButtons.fullDefence.selector(partys.DEFENDANT_1));
    await super.clickBySelector(radioButtons.fullDefence.selector(partys.DEFENDANT_2));
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
