import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import { heading, paragraphs, lists, checkboxes } from './show-certify-statement-both-content.ts';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data.ts';
import { getFormattedCaseId } from '../../../../exui-page/exui-content.ts';

@AllMethodsStep()
export default class ShowCertifyStatmentBothPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.expectText(heading),
      super.expectHeading(getFormattedCaseId(ccdCaseData.id)),
      super.expectHeading(ccdCaseData.caseNamePublic),
      super.expectText(lists.timeExpired),
      super.expectText(lists.notResponded),
      super.expectText(lists.noOutstandingApp),
      super.expectText(lists.notSatisfiedClaim),
      super.expectText(lists.notFiledAdmission),
      super.expectText(paragraphs.descriptionText),
    ]);
  }

  async acceptCPR() {
    await super.clickBySelector(checkboxes.certifyStatement.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
