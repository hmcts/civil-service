import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd/ccd-case-data';
import ExuiPage from '../../../exui-page/exui-page';
import { subheading, table } from './submit-notify-claim-details-content';

@AllMethodsStep()
export default class SubmitNotifyClaimDetailsPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheading),
      super.expectText(table.uploadDocuments, { index: 1 }),
      super.expectText(table.particularsOfClaim, { first: true }),
      super.expectText(table.medicalReports, { first: true }),
      super.expectText(table.scheduleOfLoss, { first: true }),
      super.expectText(table.certificateOfSuitability, { first: true }),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
