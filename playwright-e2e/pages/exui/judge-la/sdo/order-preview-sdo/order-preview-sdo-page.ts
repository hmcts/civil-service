import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import DateHelper from '../../../../../helpers/date-helper';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { paragraph, subheading } from './order-preview-sdo-content';

@AllMethodsStep()
export default class OrderPreviewSdoPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData): Promise<void> {
    const date = DateHelper.getToday();
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheading),
      super.expectText(paragraph),
      super.expectButton('.pdf', {exact: false})
    ]);
  }

  async submit(...args: any[]): Promise<void> {
    await super.retryClickSubmit();
  }
}
