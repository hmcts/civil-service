import BasePage from '../../../../../../base/base-page';
import filePaths from '../../../../../../config/file-paths';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import { inputs, paragraphs, subheadings } from './details-spec-content';

@AllMethodsStep()
export default class DetailsSpecPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectSubheading(subheadings.describe),
      super.expectSubheading(subheadings.uploadDocs),
      super.expectText(paragraphs.timeline),
      super.expectLabel(inputs.details.label),
      super.expectLabel(inputs.uploadFile.label),
      super.expectText(inputs.uploadFile.hintText),
    ]);
  }

  async enterDetails() {
    await super.inputText('This is the details of the claim', inputs.details.selector);
    await super.retryUploadFile(filePaths.testPdfFile, inputs.uploadFile.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
