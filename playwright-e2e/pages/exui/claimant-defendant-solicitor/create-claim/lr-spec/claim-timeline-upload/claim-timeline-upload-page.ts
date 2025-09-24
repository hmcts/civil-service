import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import { inputs, subheadings } from './claim-timeline-upload-content.ts';
import filePaths from '../../../../../../config/file-paths.ts';

@AllMethodsStep()
export default class ClaimTimelineUploadPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.verifyHeadings(),
      super.expectSubheading(subheadings.uploadClaimTimelineTemplate),
      super.expectLabel(inputs.uploadFiles.label),
    ]);
  }

  async uploadDocument() {
    await super.retryUploadFile(filePaths.testPdfFile, inputs.uploadFiles.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
