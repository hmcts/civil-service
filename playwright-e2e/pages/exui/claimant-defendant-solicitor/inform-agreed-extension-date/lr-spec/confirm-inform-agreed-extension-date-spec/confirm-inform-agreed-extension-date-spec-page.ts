import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import DateHelper from '../../../../../../helpers/date-helper';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data';
import ExuiPage from '../../../../exui-page/exui-page';
import {
  confirmationHeading,
  subheadings,
} from './confirm-inform-agreed-extension-date-spec-content';

@AllMethodsStep()
export default class ConfirmInformAgreedExtensionDateSpecPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    const date = DateHelper.addToDate(ccdCaseData.respondent1ResponseDeadline, {
      days: 28,
      workingDay: true,
      addDayAfter4pm: true,
    });
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(confirmationHeading),
      super.expectSubheading(subheadings.happensNext),
      super.expectText(DateHelper.formatDateToString(date)),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
