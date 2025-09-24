import TestData from '../../../../../models/test-data.ts';
import BaseTestData from '../../../../../base/base-test-data.ts';
import ClaimantResponsePageFactory from '../../../../../pages/exui/claimant-defendant-solicitor/response/claimant-response/claimant-response-page-factory.ts';
import { Step } from '../../../../../decorators/test-steps.ts';

const classKey = 'ClaimantResponseActions';
export default class ClaimantResponseActions extends BaseTestData {
  private claimantResponsePageFactory: ClaimantResponsePageFactory;

  constructor(claimantResponsePageFactory: ClaimantResponsePageFactory, testData: TestData) {
    super(testData);
    this.claimantResponsePageFactory = claimantResponsePageFactory;
  }

  @Step(classKey)
  async respondentResponse() {
    const { respondentResponsePage } = this.claimantResponsePageFactory;
    await respondentResponsePage.verifyContent(this.ccdCaseData);
    await respondentResponsePage.selectYes();
    await respondentResponsePage.submit();
  }

  @Step(classKey)
  async respondentResponse2v1() {
    const { respondentResponse2v1Page } = this.claimantResponsePageFactory;
    await respondentResponse2v1Page.verifyContent(this.ccdCaseData);
    await respondentResponse2v1Page.selectYesBothClaimants();
    await respondentResponse2v1Page.submit();
  }

  @Step(classKey)
  async respondentResponse1v2SS() {
    const { respondentResponse1v2SSPage } = this.claimantResponsePageFactory;
    await respondentResponse1v2SSPage.verifyContent(this.ccdCaseData);
    await respondentResponse1v2SSPage.selectYesBothDefendants();
    await respondentResponse1v2SSPage.submit();
  }

  @Step(classKey)
  async respondentResponse1v2DS() {
    const { respondentResponse1v2DSPage } = this.claimantResponsePageFactory;
    await respondentResponse1v2DSPage.verifyContent(this.ccdCaseData);
    await respondentResponse1v2DSPage.selectYesBothDefendants();
    await respondentResponse1v2DSPage.submit();
  }

  @Step(classKey)
  async defenceResponseDocument1v2SS() {
    const { defenceResponseDocument1v2SSPage } = this.claimantResponsePageFactory;
    await defenceResponseDocument1v2SSPage.verifyContent(this.ccdCaseData);
    await defenceResponseDocument1v2SSPage.uploadDocumentBothDefendants();
    await defenceResponseDocument1v2SSPage.submit();
  }

  @Step(classKey)
  async defenceResponseDocument1v2DS() {
    const { defenceResponseDocument1v2DSPage } = this.claimantResponsePageFactory;
    await defenceResponseDocument1v2DSPage.verifyContent(this.ccdCaseData);
    await defenceResponseDocument1v2DSPage.uploadDocumentBothDefendants();
    await defenceResponseDocument1v2DSPage.submit();
  }

  @Step(classKey)
  async defenceResponseDocument() {
    const { defenceResponseDocumentPage } = this.claimantResponsePageFactory;
    await defenceResponseDocumentPage.verifyContent(this.ccdCaseData);
    await defenceResponseDocumentPage.uploadDocument();
    await defenceResponseDocumentPage.submit();
  }

  @Step(classKey)
  async dqSmallTrack() {
    const { determinationWithoutHearingPage } = this.claimantResponsePageFactory;
    await determinationWithoutHearingPage.verifyContent(this.ccdCaseData);
    await determinationWithoutHearingPage.selectYes();
    await determinationWithoutHearingPage.submit();

    await this.experts();
    await this.witnesses();
    await this.language();

    const { hearingPage } = this.claimantResponsePageFactory;
    await hearingPage.verifyContent(this.ccdCaseData);
    await hearingPage.selectYesAvailabilityRequired();
    await hearingPage.addNewUnavailableDate();
    await hearingPage.selectSingleDate();
    await hearingPage.submit();

    await this.draftDirections();
    await this.hearingSupport();
    await this.vulnerabilityQuestions();
    await this.futherInformation();
  }

  @Step(classKey)
  async dqFastTrack() {
    const { fileDirectionsQuestionairePage } = this.claimantResponsePageFactory;
    await fileDirectionsQuestionairePage.verifyContent(this.ccdCaseData);
    await fileDirectionsQuestionairePage.enterDetails();
    await fileDirectionsQuestionairePage.submit();

    const { fixedRecoverableCostsPage } = this.claimantResponsePageFactory;
    await fixedRecoverableCostsPage.verifyContent(this.ccdCaseData);
    await fixedRecoverableCostsPage.selectYes();
    await fixedRecoverableCostsPage.submit();

    const { disclosureOfNonElectronicDocumentsPage } = this.claimantResponsePageFactory;
    await disclosureOfNonElectronicDocumentsPage.verifyContent(this.ccdCaseData);
    await disclosureOfNonElectronicDocumentsPage.enterDetails();
    await disclosureOfNonElectronicDocumentsPage.submit();

    await this.experts();
    await this.witnesses();
    await this.language();

    const { hearingFastPage } = this.claimantResponsePageFactory;
    await hearingFastPage.verifyContent(this.ccdCaseData);
    await hearingFastPage.selectYesAvailabilityRequired();
    await hearingFastPage.addNewUnavailableDate();
    await hearingFastPage.selectSingleDate();
    await hearingFastPage.submit();

    await this.draftDirections();
    await this.hearingSupport();
    await this.vulnerabilityQuestions();
    await this.futherInformation();
  }

  @Step(classKey)
  async statementOfTruth() {
    const { statementOfTruthClaimantResponsePage } = this.claimantResponsePageFactory;
    await statementOfTruthClaimantResponsePage.verifyContent(this.ccdCaseData);
    await statementOfTruthClaimantResponsePage.enterDetails();
    await statementOfTruthClaimantResponsePage.submit();
  }

  @Step(classKey)
  async submitClaimantResponse() {
    const { submitClaimantResponsePage } = this.claimantResponsePageFactory;
    await submitClaimantResponsePage.verifyContent(this.ccdCaseData);
    await submitClaimantResponsePage.submit();
  }

  @Step(classKey)
  async confirmClaimantResponse() {
    const { confirmClaimantResponsePage } = this.claimantResponsePageFactory;
    await confirmClaimantResponsePage.verifyContent(this.ccdCaseData);
    await confirmClaimantResponsePage.submit();
  }

  private async draftDirections() {
    const { draftDirectionsPage } = this.claimantResponsePageFactory;
    await draftDirectionsPage.verifyContent(this.ccdCaseData);
    await draftDirectionsPage.uploadEvidence();
    await draftDirectionsPage.submit();
  }

  private async experts() {
    const { expertsPage } = this.claimantResponsePageFactory;
    await expertsPage.verifyContent(this.ccdCaseData);
    await expertsPage.useExperts();
    await expertsPage.addNewExpert();
    await expertsPage.enterExpertDetails();
    await expertsPage.submit();
  }

  private async witnesses() {
    const { witnessesPage } = this.claimantResponsePageFactory;
    await witnessesPage.verifyContent(this.ccdCaseData);
    await witnessesPage.selectYesWitnesses();
    await witnessesPage.addWitness();
    await witnessesPage.enterWitnessDetails();
    await witnessesPage.submit();
  }

  private async language() {
    const { languagePage } = this.claimantResponsePageFactory;
    await languagePage.verifyContent(this.ccdCaseData);
    await languagePage.selectEnglishAndWelsh();
    await languagePage.submit();
  }

  private async hearingSupport() {
    const { hearingSupportPage } = this.claimantResponsePageFactory;
    await hearingSupportPage.verifyContent(this.ccdCaseData);
    await hearingSupportPage.selectYes();
    await hearingSupportPage.enterSupportRequirementsAdditional();
    await hearingSupportPage.submit();
  }

  private async futherInformation() {
    const { furtherInformationPage } = this.claimantResponsePageFactory;
    await furtherInformationPage.verifyContent(this.ccdCaseData);
    await furtherInformationPage.selectYes();
    await furtherInformationPage.enterFurtherInformation();
    await furtherInformationPage.submit();
  }

  private async vulnerabilityQuestions() {
    const { vulnerabilityQuestionsPage } = this.claimantResponsePageFactory;
    await vulnerabilityQuestionsPage.verifyContent(this.ccdCaseData);
    await vulnerabilityQuestionsPage.selectYes();
    await vulnerabilityQuestionsPage.enterVulnerabilityAdjustments();
    await vulnerabilityQuestionsPage.submit();
  }
}
