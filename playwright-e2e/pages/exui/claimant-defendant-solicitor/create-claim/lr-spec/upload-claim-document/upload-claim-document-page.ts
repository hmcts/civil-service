import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import { radioButtons, subheadings } from './upload-claim-document-content';

@AllMethodsStep()
export default class UploadClaimDocumentPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectSubheading(subheadings.addClaimTimeline),
      super.expectLabel(radioButtons.timeline.upload.label),
      super.expectLabel(radioButtons.timeline.manual.label),
    ]);
  }

  async selectUpload() {
    await super.clickBySelector(radioButtons.timeline.upload.selector);
  }

  async selectManualOption() {
    await super.clickBySelector(radioButtons.timeline.manual.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
