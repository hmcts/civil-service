import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import {
  paragraphs,
  subheadings,
  confirmationHeading1v2SS,
} from './confirm-defendant-response-spec-content.ts';
import DateHelper from '../../../../../../../helpers/date-helper.ts';

@AllMethodsStep()
export default class Confirm1v2SSDefendantResponseSpecPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    const claimantResponseDate = DateHelper.addToToday({
      days: 28,
      workingDay: true,
      addDayAfter4pm: true,
    });
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(confirmationHeading1v2SS),
      super.expectSubheading(subheadings.happensNext),
      super.expectText(ccdCaseData.legacyCaseReference, { exact: false }),
      super.expectText(
        paragraphs.claimantResponse(
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
