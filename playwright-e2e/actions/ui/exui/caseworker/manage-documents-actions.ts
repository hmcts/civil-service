import BaseTestData from '../../../../base/base-test-data';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import TestData from '../../../../models/test-data';
import ManageDocumentsPageFactory from '../../../../pages/exui/caseworker/manage-documents/manage-documents-page-factory.ts';

@AllMethodsStep()
export default class ManageDocumentsActions extends BaseTestData {
  private manageDocumentsPageFactory: ManageDocumentsPageFactory;

  constructor(manageDocumentsPageFactory: ManageDocumentsPageFactory, testData: TestData) {
    super(testData);
    this.manageDocumentsPageFactory = manageDocumentsPageFactory;
  }

  async addDocuments() {
    const { manageDocumentsPage } = this.manageDocumentsPageFactory;
    await manageDocumentsPage.verifyContent(this.ccdCaseData);
    await manageDocumentsPage.addDocument1();
    await manageDocumentsPage.addDocument2();
    await manageDocumentsPage.submit();

    const { manageDocumentsSubmitPage } = this.manageDocumentsPageFactory;
    await manageDocumentsSubmitPage.verifyContent(this.ccdCaseData);
    await manageDocumentsSubmitPage.enterEventDetails();
    await manageDocumentsSubmitPage.submit();
  }
}
