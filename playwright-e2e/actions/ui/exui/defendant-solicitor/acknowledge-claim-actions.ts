import BaseTestData from '../../../../base/base-test-data';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import TestData from '../../../../models/test-data';
import AcknowledgeClaimPageFactory from '../../../../pages/exui/claimant-defendant-solicitor/acknowledge-claim/acknowledge-claim-page-factory';

@AllMethodsStep()
export default class AcknowledgeClaimActions extends BaseTestData {
  private acknowledgeClaimPageFactory: AcknowledgeClaimPageFactory;

  constructor(acknowledgeClaimPageFactory: AcknowledgeClaimPageFactory, testData: TestData) {
    super(testData);
    this.acknowledgeClaimPageFactory = acknowledgeClaimPageFactory;
  }

  async confirmNameAndAddress() {
    const { confirmNameAndAddressPage } = this.acknowledgeClaimPageFactory;
    await confirmNameAndAddressPage.verifyContent();
    await confirmNameAndAddressPage.submit();
  }

  async responseIntentionDS1() {
    const { responseIntentionDS1Page } = this.acknowledgeClaimPageFactory;
    await responseIntentionDS1Page.verifyContent(this.ccdCaseData);
    await responseIntentionDS1Page.selectRejectAll();
    await responseIntentionDS1Page.submit();
  }

  async responseIntentionDS2() {
    const { responseIntentionDS2Page } = this.acknowledgeClaimPageFactory;
    await responseIntentionDS2Page.verifyContent(this.ccdCaseData);
    await responseIntentionDS2Page.selectRejectAll();
    await responseIntentionDS2Page.submit();
  }

  async solicitorReferencesAcknowledgeClaimDS1() {
    const { solicitorReferencesAcknowledgeClaimDS1Page } = this.acknowledgeClaimPageFactory;
    await solicitorReferencesAcknowledgeClaimDS1Page.verifyContent(this.ccdCaseData);
    await solicitorReferencesAcknowledgeClaimDS1Page.submit();
  }

  async submitAcknowledgeClaim() {
    const { submitAcknowledgeClaimPage } = this.acknowledgeClaimPageFactory;
    await submitAcknowledgeClaimPage.verifyContent(this.ccdCaseData);
    await submitAcknowledgeClaimPage.submit();
  }

  async confirmAcknowledgeClaimDS1() {
    const { confirmAcknowledgeClaimDS1Page } = this.acknowledgeClaimPageFactory;
    await confirmAcknowledgeClaimDS1Page.verifyContent(this.ccdCaseData);
    await confirmAcknowledgeClaimDS1Page.submit();
  }

  async confirmAcknowledgeClaimDS2() {
    const { confirmAcknowledgeClaimDS2Page } = this.acknowledgeClaimPageFactory;
    await confirmAcknowledgeClaimDS2Page.verifyContent(this.ccdCaseData);
    await confirmAcknowledgeClaimDS2Page.submit();
  }

  async responseIntention2v1() {
    const { responseIntention2v1Page } = this.acknowledgeClaimPageFactory;
    await responseIntention2v1Page.verifyContent(this.ccdCaseData);
    await responseIntention2v1Page.selectRejectAll();
    await responseIntention2v1Page.submit();
  }

  async responseIntention1v2SS() {
    const { responseIntention1v2SSPage } = this.acknowledgeClaimPageFactory;
    await responseIntention1v2SSPage.verifyContent(this.ccdCaseData);
    await responseIntention1v2SSPage.selectRejectAll();
    await responseIntention1v2SSPage.submit();
  }

  async solicitorReferencesAcknowledgeClaimDS2() {
    const { solicitorReferencesAcknowledgeClaimDS2Page } = this.acknowledgeClaimPageFactory;
    await solicitorReferencesAcknowledgeClaimDS2Page.verifyContent(this.ccdCaseData);
    await solicitorReferencesAcknowledgeClaimDS2Page.submit();
  }
}
