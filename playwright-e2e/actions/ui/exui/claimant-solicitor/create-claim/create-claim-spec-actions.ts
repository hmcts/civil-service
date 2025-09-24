import BaseTestData from '../../../../../base/base-test-data.ts';
import claimantDefendantPartyTypes from '../../../../../constants/claimant-defendant-party-types.ts';
import { AllMethodsStep } from '../../../../../decorators/test-steps.ts';
import TestData from '../../../../../models/test-data.ts';
import CreateClaimPageFactory from '../../../../../pages/exui/claimant-defendant-solicitor/create-claim/create-claim-page-factory.ts';

@AllMethodsStep()
export default class CreateClaimSpecActions extends BaseTestData {
  private createClaimPageFactory: CreateClaimPageFactory;

  constructor(createClaimPageFactory: CreateClaimPageFactory, testData: TestData) {
    super(testData);
    this.createClaimPageFactory = createClaimPageFactory;
  }

  async checklist() {
    const { checkListPage } = this.createClaimPageFactory;
    await checkListPage.verifyContent();
    await checkListPage.submit();
  }

  async eligibiltySpec() {
    const { eligibilitySpecPage } = this.createClaimPageFactory;
    await eligibilitySpecPage.verifyContent();
    await eligibilitySpecPage.submit();
  }

  async references() {
    const { referencesPage } = this.createClaimPageFactory;
    await referencesPage.verifyContent();
    await referencesPage.enterReferences();
    await referencesPage.submit();
  }

  async claimant() {
    const { claimantPage } = this.createClaimPageFactory;
    await claimantPage.verifyContent();
    await claimantPage.chooseIndividualAndEnterDetails();
    super.setClaimant1PartyType = claimantDefendantPartyTypes.INDIVIDUAL;
    await claimantPage.submit();
  }

  async noAddAnotherClaimant() {
    const { addAnotherClaimantPage } = this.createClaimPageFactory;
    await addAnotherClaimantPage.verifyContent();
    await addAnotherClaimantPage.selectNo();
    await addAnotherClaimantPage.submit();
  }

  async addAnotherClaimant() {
    const { addAnotherClaimantPage } = this.createClaimPageFactory;
    await addAnotherClaimantPage.verifyContent();
    await addAnotherClaimantPage.selectYes();
    await addAnotherClaimantPage.submit();
  }

  async secondClaimant() {
    const { secondClaimantPage } = this.createClaimPageFactory;
    await secondClaimantPage.verifyContent();
    await secondClaimantPage.chooseIndividualAndEnterDetails();
    super.setClaimant2PartyType = claimantDefendantPartyTypes.INDIVIDUAL;
    await secondClaimantPage.submit();
  }

  async claimantDetails() {
    const { notificationsPage } = this.createClaimPageFactory;
    await notificationsPage.verifyContent();
    await notificationsPage.selectYes();
    await notificationsPage.submit();

    const { claimantSolicitorOrganisationPage } = this.createClaimPageFactory;
    await claimantSolicitorOrganisationPage.verifyContent();
    await claimantSolicitorOrganisationPage.enterReference();
    await claimantSolicitorOrganisationPage.selectOrganisation();
    await claimantSolicitorOrganisationPage.submit();

    const { specCorrespondenceAddressPage } = this.createClaimPageFactory;
    await specCorrespondenceAddressPage.verifyContent();
    await specCorrespondenceAddressPage.selectYesAndEnterAddress();
    await specCorrespondenceAddressPage.submit();
  }

  async defendant() {
    const { defendantPage } = this.createClaimPageFactory;
    await defendantPage.verifyContent();
    await defendantPage.chooseCompanyAndEnterDetails();
    super.setDefendant1PartyType = claimantDefendantPartyTypes.COMPANY;
    await defendantPage.submit();
  }

  async defendantDetails() {
    const { legalRepresentationSpecPage } = this.createClaimPageFactory;
    await legalRepresentationSpecPage.verifyContent();
    await legalRepresentationSpecPage.selectYes();
    await legalRepresentationSpecPage.submit();

    const { defendantSolicitorOrganisationSpecPage } = this.createClaimPageFactory;
    await defendantSolicitorOrganisationSpecPage.verifyContent();
    await defendantSolicitorOrganisationSpecPage.selectOrganisation();
    await defendantSolicitorOrganisationSpecPage.submit();

    const { defendantSolicitorEmailSpecPage } = this.createClaimPageFactory;
    await defendantSolicitorEmailSpecPage.verifyContent();
    await defendantSolicitorEmailSpecPage.enterEmail();
    await defendantSolicitorEmailSpecPage.submit();

    const { specRespondentCorrespondenceAddressPage } = this.createClaimPageFactory;
    await specRespondentCorrespondenceAddressPage.verifyContent();
    await specRespondentCorrespondenceAddressPage.selectYesAndEnterAddress();
    await specRespondentCorrespondenceAddressPage.submit();
  }

  async noAddAnotherDefendant() {
    const { addAnotherDefendantPage } = this.createClaimPageFactory;
    await addAnotherDefendantPage.verifyContent();
    await addAnotherDefendantPage.selectNo();
    await addAnotherDefendantPage.submit();
  }

  async addAnotherDefendant() {
    const { addAnotherDefendantPage } = this.createClaimPageFactory;
    await addAnotherDefendantPage.verifyContent();
    await addAnotherDefendantPage.selectYes();
    await addAnotherDefendantPage.submit();
  }

  async secondDefendant() {
    const { secondDefendantPage } = this.createClaimPageFactory;
    await secondDefendantPage.verifyContent();
    await secondDefendantPage.chooseIndividualAndEnterDetails();
    super.setDefendant2PartyType = claimantDefendantPartyTypes.INDIVIDUAL;
    await secondDefendantPage.submit();
  }

  async secondDefedantSSDetails() {
    await this.legalRepresentationRespondent2();

    const { sameLegalRepresentativePage } = this.createClaimPageFactory;
    await sameLegalRepresentativePage.verifyContent();
    await sameLegalRepresentativePage.selectYes();
    await sameLegalRepresentativePage.submit();
  }

  async secondDefendantDSDetails() {
    await this.legalRepresentationRespondent2();

    const { sameLegalRepresentativePage } = this.createClaimPageFactory;
    await sameLegalRepresentativePage.verifyContent();
    await sameLegalRepresentativePage.selectNo();
    await sameLegalRepresentativePage.submit();

    const { secondDefendantSolicitorOrganisationSpecPage } = this.createClaimPageFactory;
    await secondDefendantSolicitorOrganisationSpecPage.verifyContent();
    await secondDefendantSolicitorOrganisationSpecPage.selectOrganisation();
    await secondDefendantSolicitorOrganisationSpecPage.submit();

    const { secondDefendantSolicitorEmailSpecPage } = this.createClaimPageFactory;
    await secondDefendantSolicitorEmailSpecPage.verifyContent();
    await secondDefendantSolicitorEmailSpecPage.enterEmail();
    await secondDefendantSolicitorEmailSpecPage.submit();

    const { specRespondent2CorrespondenceAddressPage } = this.createClaimPageFactory;
    await specRespondent2CorrespondenceAddressPage.verifyContent();
    await specRespondent2CorrespondenceAddressPage.selectYesAndEnterAddress();
    await specRespondent2CorrespondenceAddressPage.submit();
  }

  async claimDetailsFastTrack() {
    await this.flightDelayClaim();
    await this.detailsSpec();
    await this.uploadClaimDocument();
    await this.claimTimeLineUpload();
    await this.evidenceList();

    const { claimAmountPage } = this.createClaimPageFactory;
    await claimAmountPage.verifyContent();
    await claimAmountPage.addNew();
    await claimAmountPage.enterClaimDetailsFastTrack();
    await claimAmountPage.submit();

    const { claimAmountDetailsPage } = this.createClaimPageFactory;
    await claimAmountDetailsPage.verifyContent();
    await claimAmountDetailsPage.verifyFastTrack();
    await claimAmountDetailsPage.submit();

    await this.claimInterest();

    const { interestSummaryPage } = this.createClaimPageFactory;
    await interestSummaryPage.verifyContent();
    await interestSummaryPage.verifyFastTrack();
    await interestSummaryPage.submit();

    await this.pbaNumber();
    await this.fixedCommencementCosts();
  }

  async claimDetailsSmallTrack() {
    await this.flightDelayClaim();
    await this.detailsSpec();
    await this.uploadClaimDocument();
    await this.claimTimeLineUpload();
    await this.evidenceList();

    const { claimAmountPage } = this.createClaimPageFactory;
    await claimAmountPage.verifyContent();
    await claimAmountPage.addNew();
    await claimAmountPage.enterClaimDetailsSmallTrack();
    await claimAmountPage.submit();

    const { claimAmountDetailsPage } = this.createClaimPageFactory;
    await claimAmountDetailsPage.verifyContent();
    await claimAmountDetailsPage.verifySmallTrack();
    await claimAmountDetailsPage.submit();

    await this.claimInterest();

    const { interestSummaryPage } = this.createClaimPageFactory;
    await interestSummaryPage.verifyContent();
    await interestSummaryPage.verifySmallTrack();
    await interestSummaryPage.submit();

    await this.pbaNumber();
    await this.fixedCommencementCosts();
  }

  async statementOfTruthCreateClaim() {
    const { statementOfTruthCreateClaimPage } = this.createClaimPageFactory;
    await statementOfTruthCreateClaimPage.verifyContent();
    await statementOfTruthCreateClaimPage.enterDetails();
    await statementOfTruthCreateClaimPage.submit();
  }

  async submitCreateClaim() {
    const { submitCreateClaimPage } = this.createClaimPageFactory;
    await submitCreateClaimPage.verifyContent(super.ccdCaseData);
    await submitCreateClaimPage.submit();
  }

  async confirmCreateClaimSpec() {
    const { confirmCreateClaimSpecPage } = this.createClaimPageFactory;
    await confirmCreateClaimSpecPage.verifyContent();
    await confirmCreateClaimSpecPage.submit();
  }

  private async flightDelayClaim() {
    const { flightDelayClaimPage } = this.createClaimPageFactory;
    await flightDelayClaimPage.verifyContent();
    await flightDelayClaimPage.selectNo();
    await flightDelayClaimPage.submit();
  }

  private async detailsSpec() {
    const { detailsSpecPage } = this.createClaimPageFactory;
    await detailsSpecPage.verifyContent();
    await detailsSpecPage.enterDetails();
    await detailsSpecPage.submit();
  }

  private async uploadClaimDocument() {
    const { uploadClaimDocumentPage } = this.createClaimPageFactory;
    await uploadClaimDocumentPage.verifyContent();
    await uploadClaimDocumentPage.selectUpload();
    await uploadClaimDocumentPage.submit();
  }

  private async claimTimeLineUpload() {
    const { claimTimelineUploadPage } = this.createClaimPageFactory;
    await claimTimelineUploadPage.verifyContent();
    await claimTimelineUploadPage.uploadDocument();
    await claimTimelineUploadPage.submit();
  }

  private async evidenceList() {
    const { evidenceListPage } = this.createClaimPageFactory;
    await evidenceListPage.verifyContent();
    await evidenceListPage.addNew();
    await evidenceListPage.enterEvidence1Details();
    await evidenceListPage.submit();
  }

  private async claimInterest() {
    const { claimInterestPage } = this.createClaimPageFactory;
    await claimInterestPage.verifyContent();
    await claimInterestPage.selectNo();
    await claimInterestPage.submit();
  }

  private async pbaNumber() {
    const { pbaNumberPage } = this.createClaimPageFactory;
    await pbaNumberPage.verifyContent();
    await pbaNumberPage.submit();
  }

  private async fixedCommencementCosts() {
    const { fixedCommencementCostsPage } = this.createClaimPageFactory;
    await fixedCommencementCostsPage.verifyContent();
    await fixedCommencementCostsPage.selectYesAndEnterAmount();
    await fixedCommencementCostsPage.submit();
  }

  private async legalRepresentationRespondent2() {
    const { legalRepresentationRespondent2Page } = this.createClaimPageFactory;
    await legalRepresentationRespondent2Page.verifyContent();
    await legalRepresentationRespondent2Page.selectYes();
    await legalRepresentationRespondent2Page.submit();
  }
}
