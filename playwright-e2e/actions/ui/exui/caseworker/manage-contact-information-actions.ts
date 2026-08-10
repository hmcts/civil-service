import BaseTestData from '../../../../base/base-test-data.ts';
import { AllMethodsStep } from '../../../../decorators/test-steps.ts';
import TestData from '../../../../models/test-utils/test-data.ts';
import ManageContactInformationPageFactory from '../../../../pages/exui/caseworker/manage-contact-information/manage-contact-information-page-factory.ts';

@AllMethodsStep()
export default class ManageContactInformationActions extends BaseTestData {
  private manageContactInformationPageFactory: ManageContactInformationPageFactory;

  constructor(
    manageContactInformationPageFactory: ManageContactInformationPageFactory,
    testData: TestData) {
    super(testData);
    this.manageContactInformationPageFactory = manageContactInformationPageFactory;
  }

  async partySelectionClaimant1() {
    const { partySelectionPage } = this.manageContactInformationPageFactory;
    await partySelectionPage.verifyContent(this.ccdCaseData, this.claimant1PartyType!, this.defendant1PartyType!);
    await partySelectionPage.selectClaimant1();
    await partySelectionPage.submit();
  }

  async partySelectionSpecDefendant1() {
    const { partySelectionSpecPage } = this.manageContactInformationPageFactory;
    await partySelectionSpecPage.verifyContent(this.ccdCaseData, this.claimant1PartyType!, this.defendant1PartyType!);
    await partySelectionSpecPage.selectDefendant1();
    await partySelectionSpecPage.submit();
  }

  async applicantParty1() {
    const { applicant1PartyPage } = this.manageContactInformationPageFactory;
    await applicant1PartyPage.verifyContent(this.ccdCaseData, this.claimant1PartyType!);
    await applicant1PartyPage.updateApplicantDetails(this.claimant1PartyType!);
    await applicant1PartyPage.submit();
  }

  async defendantParty1() {
    const { defendant1PartyPage } = this.manageContactInformationPageFactory;
    await defendant1PartyPage.verifyContent(this.ccdCaseData, this.defendant1PartyType!);
    await defendant1PartyPage.updateDefendant1Details(this.defendant1PartyType!);
    await defendant1PartyPage.submit();
  }

  async manageContactInformationSubmit() {
    const { manageContactInformationSubmitPage } = this.manageContactInformationPageFactory;
    await manageContactInformationSubmitPage.verifyContent(this.ccdCaseData);
    await manageContactInformationSubmitPage.enterEventDetails();
    await manageContactInformationSubmitPage.submit();
  }

  async manageContactInformationConfirm() {
    const { manageContactInformationConfirmPage } = this.manageContactInformationPageFactory;
    await manageContactInformationConfirmPage.verifyContent(this.ccdCaseData);
  }
}
