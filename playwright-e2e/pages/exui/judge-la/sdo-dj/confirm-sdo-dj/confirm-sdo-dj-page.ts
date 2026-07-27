import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { confirmationHeading, paragraphs, subheading } from './confirm-sdo-dj-content';

@AllMethodsStep()
export default class confirmSdoDJPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData): Promise<void> {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(confirmationHeading),
      super.expectSubheading(subheading),
      super.expectHeading(ccdCaseData.legacyCaseReference),
      super.expectText(paragraphs.paragraph1),
      super.expectText(paragraphs.paragraph2),
      super.expectText(paragraphs.paragraph3),
    ]);
  }
  async submit(...args: any[]): Promise<void> {
    await super.retryClickSubmit();
  }
}
