import BasePage from '../../../../../../../base/base-page.ts';
import filePaths from '../../../../../../../config/file-paths.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import { inputs } from './defence-response-document-spec-content.ts';

@AllMethodsStep()
export default class DefenceResponseDocumentSpecPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLabel(inputs.uploadDoc.label, { count: 1 }),
    ]);
  }

  async uploadDoc() {
    await super.retryUploadFile(filePaths.testPdfFile, inputs.uploadDoc.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
