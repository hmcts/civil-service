import BaseTestData from '../../../../../base/base-test-data';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import TestData from '../../../../../models/test-utils/test-data';
import InformAgreedExtensionDatePageFactory from '../../../../../pages/exui/claimant-defendant-solicitor/inform-agreed-extension-date/inform-agreed-extension-date-page-factory';

@AllMethodsStep()
export default class InformAgreedExtensionDateSpecActions extends BaseTestData {
  private informAgreedExtensionDatePageFactory: InformAgreedExtensionDatePageFactory;

  constructor(
    informAgreedExtensionDatePageFactory: InformAgreedExtensionDatePageFactory,
    testData: TestData,
  ) {
    super(testData);
    this.informAgreedExtensionDatePageFactory = informAgreedExtensionDatePageFactory;
  }

  async extensionDateSpec() {
    const { extensionDateSpecPage } = this.informAgreedExtensionDatePageFactory;
    await extensionDateSpecPage.verifyContent(this.ccdCaseData);
    await extensionDateSpecPage.enterDate(this.ccdCaseData);
    await extensionDateSpecPage.submit();
  }

  async confirmInformAgreedExtensionDateSpec() {
    const { confirmInformAgreedExtensionDateSpecPage } = this.informAgreedExtensionDatePageFactory;
    await confirmInformAgreedExtensionDateSpecPage.verifyContent(this.ccdCaseData);
    await confirmInformAgreedExtensionDateSpecPage.submit();
  }
}
