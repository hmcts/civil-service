import BasePage from '../../../../../base/base-page.ts';
import filePaths from '../../../../../config/file-paths.ts';
import { AllMethodsStep } from '../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../exui-page/exui-page.ts';
import { subheadings, inputs, dropdowns, buttons } from './manage-documents-content.ts';

@AllMethodsStep()
export default class ManageDocumentsPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheadings.bulkScanned),
    ]);
  }

  async addDocument1() {
    await super.clickBySelector(buttons.addNewTop.selector);
    await super.inputText('Document 1', inputs.document.name.selector(1));
    await super.selectFromDropdown(
      dropdowns.documentType.options[0],
      inputs.document.name.selector(1),
    );
    await super.retryUploadFile(filePaths.testPdfFile, inputs.document.name.selector(1));
  }

  async addDocument2() {
    await super.clickBySelector(buttons.addNewTop.selector);
    await super.inputText('Document 1', inputs.document.name.selector(2));
    await super.selectFromDropdown(
      dropdowns.documentType.options[3],
      inputs.document.name.selector(2),
    );
    await super.inputText('Other reason', inputs.document.other.selector(2));
    await super.retryUploadFile(filePaths.testPdfFile, inputs.document.name.selector(2));
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
