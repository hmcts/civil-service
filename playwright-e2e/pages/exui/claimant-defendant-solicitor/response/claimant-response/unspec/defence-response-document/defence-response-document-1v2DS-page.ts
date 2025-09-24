import BasePage from '../../../../../../../base/base-page.ts';
import filePaths from '../../../../../../../config/file-paths.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import { inputs, subheadings } from './defence-response-document-content.ts';

@AllMethodsStep()
export default class DefenceResponseDocument1v2DSPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheadings.respondToDefence, { count: 2 }),
      super.expectLabel(inputs.uploadDoc.label, { count: 2 }),
    ]);
  }

  async uploadDocumentBothDefendants() {
    await super.retryUploadFile(filePaths.testPdfFile, inputs.uploadDoc.selector);
    await super.retryUploadFile(filePaths.testPdfFile, inputs.uploadDocDefendant2.selector);
  }
  async submit() {
    await super.retryClickSubmit();
  }
}
