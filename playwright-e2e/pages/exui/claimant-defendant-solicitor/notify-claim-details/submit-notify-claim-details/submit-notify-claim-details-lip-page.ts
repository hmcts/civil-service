import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ExuiPage from '../../../exui-page/exui-page';
import CCDCaseData from '../../../../../models/ccd/ccd-case-data';
import { heading, subheading, COS_table } from './submit-notify-claim-details-content';

@AllMethodsStep()
export default class SubmitNotifyClaimDetailsLIPPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheading),
      super.expectHeading(heading.defendant1.label),
      super.expectText(COS_table.dateDeemedServed.label),
      super.expectText(COS_table.dateOfService.label),
      super.expectText(COS_table.documentsServed.label),
      super.expectText(COS_table.documentsServedLocation.label),
      super.expectText(COS_table.name.label),
      super.expectText(COS_table.firm.label),
      super.expectText(COS_table.locationType.label),
      super.expectText(COS_table.notifyClaimRecipient.label),
      super.expectText(COS_table.serveType.label),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
