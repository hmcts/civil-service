import BaseTestData from '../../../../../base/base-test-data';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import TestData from '../../../../../models/test-utils/test-data';
import InformAgreedExtensionDatePageFactory from '../../../../../pages/exui/claimant-defendant-solicitor/inform-agreed-extension-date/inform-agreed-extension-date-page-factory';

@AllMethodsStep()
export default class InformAgreedExtensionDateActions extends BaseTestData {
  private informAgreedExtensionDatePageFactory: InformAgreedExtensionDatePageFactory;

  constructor(
    informAgreedExtensionDatePageFactory: InformAgreedExtensionDatePageFactory,
    testData: TestData,
  ) {
    super(testData);
    this.informAgreedExtensionDatePageFactory = informAgreedExtensionDatePageFactory;
  }

  async extensionDate() {
    const { extensionDatePage } = this.informAgreedExtensionDatePageFactory;
    await extensionDatePage.verifyContent(this.ccdCaseData);
    await extensionDatePage.submit();
  }

  async confirmInformAgreedExtensionDate() {
    const { confirmInformAgreedExtensionDatePage } = this.informAgreedExtensionDatePageFactory;
    await confirmInformAgreedExtensionDatePage.verifyContent(this.ccdCaseData);
    await confirmInformAgreedExtensionDatePage.submit();
  }
}
