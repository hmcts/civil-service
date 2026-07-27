import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { paragraphs } from './manage-stay-request-update-content';

@AllMethodsStep()
export default class ManageStayRequestUpdatePage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectText(paragraphs.requestingUpdate),
      super.expectText(paragraphs.notifications),
      super.expectText(paragraphs.task),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
