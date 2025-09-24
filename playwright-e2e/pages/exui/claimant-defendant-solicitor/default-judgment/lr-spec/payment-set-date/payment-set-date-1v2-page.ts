import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import DateHelper from '../../../../../../helpers/date-helper.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import { subheadings, inputs } from '../payment-set-date/payment-set-date-content.ts';
import DateFragment from '../../../../fragments/date/date-fragment.ts';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data.ts';

@AllMethodsStep()
export default class PaymentSetDate1v2Page extends ExuiPage(BasePage) {
  private dateFragment: DateFragment;

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectText(subheadings.paymentSetDate1v2),
    ]);
  }

  async setPaymentDate() {
    const setDate = DateHelper.addToToday({ months: 1 });
    await this.dateFragment.enterDate(setDate, inputs.paymentSetDate.selectorKey);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
