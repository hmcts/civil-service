import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { confirmationHeadings, subheadings } from './confirm-manage-stay-content';

@AllMethodsStep()
export default class ConfirmManageStayRequestUpdatePage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(confirmationHeadings.requestedUpdate),
      super.expectHeading(confirmationHeadings.thisCase),
      super.expectSubheading(subheadings),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
