import BasePage from '../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../models/ccd-case-data.ts';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page.ts';
import { Page } from '@playwright/test';
import { text } from './manage-contact-information-confirm-content.ts';

@AllMethodsStep()
export default class ManageContactInformationConfirmPage extends ExuiPage(BasePage) {

    constructor(page: Page) {
      super(page);
    }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectText(text.infoChanged),
    ]);
  }


  async submit() {
    await super.retryClickSubmit();
  }
}
