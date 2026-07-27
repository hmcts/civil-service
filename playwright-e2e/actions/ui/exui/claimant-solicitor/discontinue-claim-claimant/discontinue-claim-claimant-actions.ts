import { AllMethodsStep } from '../../../../../decorators/test-steps';
import BaseTestData from '../../../../../base/base-test-data';
import TestData from '../../../../../models/test-utils/test-data';
import DiscontinueClaimClaimantPageFactory from '../../../../../pages/exui/claimant-defendant-solicitor/discontinue-claim/discontinue-claim-claimant-page-factory';

@AllMethodsStep()
export default class DiscontinueClaimClaimantActions extends BaseTestData {
  private discontinueClaimPageFactory: DiscontinueClaimClaimantPageFactory;

  constructor(discontinueClaimPageFactory: DiscontinueClaimClaimantPageFactory, testData: TestData) {
    super(testData);
    this.discontinueClaimPageFactory = discontinueClaimPageFactory;
  }

  async selectBothClaimantsDiscontinuing() {
    const { multipleClaimantPage } = this.discontinueClaimPageFactory;
    await multipleClaimantPage.verifyContent(this.ccdCaseData, this.claimant1PartyType!, this.claimant2PartyType!);
    await multipleClaimantPage.selectBoth();
    await multipleClaimantPage.submit();
  }

  async courtPermissionYes() {
    const { courtPermissionPage } = this.discontinueClaimPageFactory;
    await courtPermissionPage.verifyContent(this.ccdCaseData);
    await courtPermissionPage.selectPermissionRequiredYes();
    await courtPermissionPage.submit();
  }

  async permissionGrantedYes() {
    const { permissionGrantedPage } = this.discontinueClaimPageFactory;
    await permissionGrantedPage.verifyContent(this.ccdCaseData);
    await permissionGrantedPage.selectPermissionGrantedYes();
    await permissionGrantedPage.submit();
  }

  async selectDiscontinueAgainstBothDefendantsYes() {
    const { discontinuingAgainstDefendantsPage } = this.discontinueClaimPageFactory;
    await discontinuingAgainstDefendantsPage.verifyContent(this.ccdCaseData);
    await discontinuingAgainstDefendantsPage.selectDiscontinuingAgainstBothDefendantsYes();
    await discontinuingAgainstDefendantsPage.submit();
  }

  async fullDiscontinuance() {
    const { discontinuanceTypePage } = this.discontinueClaimPageFactory;
    await discontinuanceTypePage.verifyContent(this.ccdCaseData);
    await discontinuanceTypePage.selectFullDiscontinuance();
    await discontinuanceTypePage.submit();
  }

  async submitDiscontinueClaimPage() {
    const { submitDiscontinueClaimClaimantPage: submitDiscontinueClaimPage } = this.discontinueClaimPageFactory;
    await submitDiscontinueClaimPage.verifyContent(this.ccdCaseData);
    await submitDiscontinueClaimPage.submit();
  }

  async confirmDiscontinueClaimPage() {
    const { confirmDiscontinueClaimClaimantPage: confirmDiscontinueClaimPage } = this.discontinueClaimPageFactory;
    await confirmDiscontinueClaimPage.verifyContent(this.ccdCaseData);
    await confirmDiscontinueClaimPage.submit();
  }
}
