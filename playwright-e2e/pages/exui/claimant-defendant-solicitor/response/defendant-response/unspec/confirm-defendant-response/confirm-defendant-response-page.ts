import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { confirmationHeading, paragraphs } from './confirm-defendant-response-content.ts';
import DateHelper from '../../../../../../../helpers/date-helper.ts';

@AllMethodsStep()
export default class ConfirmDefendantResponsePage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    const claimantResponseDate = DateHelper.addToToday({
      days: 28,
      workingDay: true,
      addDayAfter4pm: true,
    });
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(confirmationHeading),
      super.expectText(ccdCaseData.legacyCaseReference, { exact: false }),
      super.expectText(
        paragraphs.claimantsResponse(
          DateHelper.formatDateToString(claimantResponseDate, { outputFormat: 'DD Month YYYY' }),
        ),
        { exact: false },
      ),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
