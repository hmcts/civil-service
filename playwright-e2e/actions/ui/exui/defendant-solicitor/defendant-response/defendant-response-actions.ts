import TestData from '../../../../../models/test-data.ts';
import BaseTestData from '../../../../../base/base-test-data.ts';
import DefendantResponsePageFactory from '../../../../../pages/exui/claimant-defendant-solicitor/response/defendant-response/defendant-response-page-factory.ts';
import { AllMethodsStep } from '../../../../../decorators/test-steps.ts';

@AllMethodsStep()
export default class DefendantResponseActions extends BaseTestData {
  private defendantResponsePageFactory: DefendantResponsePageFactory;

  constructor(defendantResponsePageFactory: DefendantResponsePageFactory, testData: TestData) {
    super(testData);
    this.defendantResponsePageFactory = defendantResponsePageFactory;
  }

  async confirmDetailsDS1() {
    const { confirmDetailsDS1Page } = this.defendantResponsePageFactory;
    await confirmDetailsDS1Page.verifyContent(this.ccdCaseData);
    await confirmDetailsDS1Page.submit();
  }

  async confirmDetailsDS2() {
    const { confirmDetailsDS2Page } = this.defendantResponsePageFactory;
    await confirmDetailsDS2Page.verifyContent(this.ccdCaseData);
    await confirmDetailsDS2Page.submit();
  }

  async singleResponse() {
    const { singleResponsePage } = this.defendantResponsePageFactory;
    await singleResponsePage.verifyContent(this.ccdCaseData);
    await singleResponsePage.selectYes();
    await singleResponsePage.submit();
  }

  async respondentResponseTypeDS1() {
    const { respondentResponseTypeDS1Page } = this.defendantResponsePageFactory;
    await respondentResponseTypeDS1Page.verifyContent(this.ccdCaseData);
    await respondentResponseTypeDS1Page.selectRejectAll();
    await respondentResponseTypeDS1Page.submit();
  }

  async respondentResponseTypeDS2() {
    const { respondentResponseTypeDS2Page } = this.defendantResponsePageFactory;
    await respondentResponseTypeDS2Page.verifyContent(this.ccdCaseData);
    await respondentResponseTypeDS2Page.selectRejectAll();
    await respondentResponseTypeDS2Page.submit();
  }

  async respondentResponseType2v1() {
    const { respondentResponseType2v1Page } = this.defendantResponsePageFactory;
    await respondentResponseType2v1Page.verifyContent(this.ccdCaseData);
    await respondentResponseType2v1Page.selectRejectAll();
    await respondentResponseType2v1Page.submit();
  }

  async solicitorReferencesDefendantResponseDS1() {
    const { solicitorReferencesDefendantResponseDS1Page } = this.defendantResponsePageFactory;
    await solicitorReferencesDefendantResponseDS1Page.verifyContent(this.ccdCaseData);
    await solicitorReferencesDefendantResponseDS1Page.enterReference();
    await solicitorReferencesDefendantResponseDS1Page.submit();
  }

  async solicitorReferencesDefendantResponseDS2() {
    const { solicitorReferencesDefendantResponseDS2Page } = this.defendantResponsePageFactory;
    await solicitorReferencesDefendantResponseDS2Page.verifyContent(this.ccdCaseData);
    await solicitorReferencesDefendantResponseDS2Page.enterReference();
    await solicitorReferencesDefendantResponseDS2Page.submit();
  }

  async uploadDefendantResponseDS1() {
    const { uploadDefendantResponseDS1Page } = this.defendantResponsePageFactory;
    await uploadDefendantResponseDS1Page.verifyContent(this.ccdCaseData);
    await uploadDefendantResponseDS1Page.uploadDefence();
    await uploadDefendantResponseDS1Page.submit();
  }

  async uploadDefendantResponseDS2() {
    const { uploadDefendantResponseDS2Page } = this.defendantResponsePageFactory;
    await uploadDefendantResponseDS2Page.verifyContent(this.ccdCaseData);
    await uploadDefendantResponseDS2Page.uploadDefence();
    await uploadDefendantResponseDS2Page.submit();
  }

  async dqFastTrackDS1() {
    const { fileDirectionsQuestionaireDS1Page } = this.defendantResponsePageFactory;
    await fileDirectionsQuestionaireDS1Page.verifyContent(this.ccdCaseData);
    await fileDirectionsQuestionaireDS1Page.enterDetails();
    await fileDirectionsQuestionaireDS1Page.submit();

    const { fixedRecoverableCostsDS1Page } = this.defendantResponsePageFactory;
    await fixedRecoverableCostsDS1Page.verifyContent(this.ccdCaseData);
    await fixedRecoverableCostsDS1Page.selectYes();
    await fixedRecoverableCostsDS1Page.submit();

    const { disclosureOfNonElectronicDocumentsDS1Page } = this.defendantResponsePageFactory;
    await disclosureOfNonElectronicDocumentsDS1Page.verifyContent(this.ccdCaseData);
    await disclosureOfNonElectronicDocumentsDS1Page.enterDetails();
    await disclosureOfNonElectronicDocumentsDS1Page.submit();
  }

  async dqFastTrackDS2() {
    const { fileDirectionsQuestionaireDS2Page } = this.defendantResponsePageFactory;
    await fileDirectionsQuestionaireDS2Page.verifyContent(this.ccdCaseData);
    await fileDirectionsQuestionaireDS2Page.enterDetails();
    await fileDirectionsQuestionaireDS2Page.submit();

    const { fixedRecoverableCostsDS2Page } = this.defendantResponsePageFactory;
    await fixedRecoverableCostsDS2Page.verifyContent(this.ccdCaseData);
    await fixedRecoverableCostsDS2Page.selectYes();
    await fixedRecoverableCostsDS2Page.submit();

    const { disclosureOfNonElectronicDocumentsDS2Page } = this.defendantResponsePageFactory;
    await disclosureOfNonElectronicDocumentsDS2Page.verifyContent(this.ccdCaseData);
    await disclosureOfNonElectronicDocumentsDS2Page.enterDetails();
    await disclosureOfNonElectronicDocumentsDS2Page.submit();
  }

  async dqSmallTrackDS1() {
    const { determinationWithoutHearingDS1Page } = this.defendantResponsePageFactory;
    await determinationWithoutHearingDS1Page.verifyContent(this.ccdCaseData);
    await determinationWithoutHearingDS1Page.selectYes();
    await determinationWithoutHearingDS1Page.submit();
  }

  async dqSmallTrackDS2() {
    const { determinationWithoutHearingDS2Page } = this.defendantResponsePageFactory;
    await determinationWithoutHearingDS2Page.verifyContent(this.ccdCaseData);
    await determinationWithoutHearingDS2Page.selectYes();
    await determinationWithoutHearingDS2Page.submit();
  }

  async dqDS1() {
    const { expertsDS1Page } = this.defendantResponsePageFactory;
    await expertsDS1Page.verifyContent(this.ccdCaseData);
    await expertsDS1Page.useExperts();
    await expertsDS1Page.addNewExpert();
    await expertsDS1Page.enterExpertDetails();
    await expertsDS1Page.submit();

    const { witnessesDS1Page } = this.defendantResponsePageFactory;
    await witnessesDS1Page.verifyContent(this.ccdCaseData);
    await witnessesDS1Page.selectYesWitnesses();
    await witnessesDS1Page.addWitness();
    await witnessesDS1Page.enterWitnessDetails();
    await witnessesDS1Page.submit();

    const { languageDS1Page } = this.defendantResponsePageFactory;
    await languageDS1Page.verifyContent(this.ccdCaseData);
    await languageDS1Page.selectEnglishAndWelsh();
    await languageDS1Page.submit();

    const { hearingDS1Page } = this.defendantResponsePageFactory;
    await hearingDS1Page.verifyContent(this.ccdCaseData);
    await hearingDS1Page.selectYesAvailabilityRequired();
    await hearingDS1Page.addNewUnavailableDate();
    await hearingDS1Page.selectSingleDate();
    await hearingDS1Page.submit();

    const { draftDirectionsDS1Page } = this.defendantResponsePageFactory;
    await draftDirectionsDS1Page.verifyContent(this.ccdCaseData);
    await draftDirectionsDS1Page.uploadEvidence();
    await draftDirectionsDS1Page.submit();

    const { requestedCourtDS1Page } = this.defendantResponsePageFactory;
    await requestedCourtDS1Page.verifyContent(this.ccdCaseData);
    await requestedCourtDS1Page.selectCourtLocation();
    await requestedCourtDS1Page.enterPreferredCourtReason();
    await requestedCourtDS1Page.selectNoRemoteHearing();
    await requestedCourtDS1Page.submit();

    const { hearingSupportDS1Page } = this.defendantResponsePageFactory;
    await hearingSupportDS1Page.verifyContent(this.ccdCaseData);
    await hearingSupportDS1Page.selectYes();
    await hearingSupportDS1Page.enterSupportRequirementsAdditional();
    await hearingSupportDS1Page.submit();

    const { vulnerabilityQuestionsDS1Page } = this.defendantResponsePageFactory;
    await vulnerabilityQuestionsDS1Page.verifyContent(this.ccdCaseData);
    await vulnerabilityQuestionsDS1Page.selectYes();
    await vulnerabilityQuestionsDS1Page.enterVulnerabilityAdjustments();
    await vulnerabilityQuestionsDS1Page.submit();

    const { furtherInformationDS1Page } = this.defendantResponsePageFactory;
    await furtherInformationDS1Page.verifyContent(this.ccdCaseData);
    await furtherInformationDS1Page.selectYes();
    await furtherInformationDS1Page.enterFurtherInformation();
    await furtherInformationDS1Page.submit();
  }

  async dqDS2() {
    const { expertsDS2Page } = this.defendantResponsePageFactory;
    await expertsDS2Page.verifyContent(this.ccdCaseData);
    await expertsDS2Page.useExperts();
    await expertsDS2Page.addNewExpert();
    await expertsDS2Page.enterExpertDetails();
    await expertsDS2Page.submit();

    const { witnessesDS2Page } = this.defendantResponsePageFactory;
    await witnessesDS2Page.verifyContent(this.ccdCaseData);
    await witnessesDS2Page.selectYesWitnesses();
    await witnessesDS2Page.addWitness();
    await witnessesDS2Page.enterWitnessDetails();
    await witnessesDS2Page.submit();

    const { languageDS2Page } = this.defendantResponsePageFactory;
    await languageDS2Page.verifyContent(this.ccdCaseData);
    await languageDS2Page.selectEnglishAndWelsh();
    await languageDS2Page.submit();

    const { hearingDS2Page } = this.defendantResponsePageFactory;
    await hearingDS2Page.verifyContent(this.ccdCaseData);
    await hearingDS2Page.selectNoAvailabilityRequired();
    await hearingDS2Page.submit();

    const { draftDirectionsDS2Page } = this.defendantResponsePageFactory;
    await draftDirectionsDS2Page.verifyContent(this.ccdCaseData);
    await draftDirectionsDS2Page.uploadEvidence();
    await draftDirectionsDS2Page.submit();

    const { requestedCourtDS2Page } = this.defendantResponsePageFactory;
    await requestedCourtDS2Page.verifyContent(this.ccdCaseData);
    await requestedCourtDS2Page.selectCourtLocation();
    await requestedCourtDS2Page.enterPreferredCourtReason();
    await requestedCourtDS2Page.selectNoRemoteHearing();
    await requestedCourtDS2Page.submit();

    const { hearingSupportDS2Page } = this.defendantResponsePageFactory;
    await hearingSupportDS2Page.verifyContent(this.ccdCaseData);
    await hearingSupportDS2Page.selectYes();
    await hearingSupportDS2Page.enterSupportRequirementsAdditional();
    await hearingSupportDS2Page.submit();

    const { vulnerabilityQuestionsDS2Page } = this.defendantResponsePageFactory;
    await vulnerabilityQuestionsDS2Page.verifyContent(this.ccdCaseData);
    await vulnerabilityQuestionsDS2Page.selectYes();
    await vulnerabilityQuestionsDS2Page.enterVulnerabilityAdjustments();
    await vulnerabilityQuestionsDS2Page.submit();

    const { furtherInformationDS2Page } = this.defendantResponsePageFactory;
    await furtherInformationDS2Page.verifyContent(this.ccdCaseData);
    await furtherInformationDS2Page.selectYes();
    await furtherInformationDS2Page.enterFurtherInformation();
    await furtherInformationDS2Page.submit();
  }

  async statementOfTruthDS1() {
    const { statementOfTruthDefendantResponseDS1Page } = this.defendantResponsePageFactory;
    await statementOfTruthDefendantResponseDS1Page.verifyContent(this.ccdCaseData);
    await statementOfTruthDefendantResponseDS1Page.enterDetails();
    await statementOfTruthDefendantResponseDS1Page.submit();
  }

  async statementOfTruthDS2() {
    const { statementOfTruthDefendantResponseDS2Page } = this.defendantResponsePageFactory;
    await statementOfTruthDefendantResponseDS2Page.verifyContent(this.ccdCaseData);
    await statementOfTruthDefendantResponseDS2Page.enterDetails();
    await statementOfTruthDefendantResponseDS2Page.submit();
  }

  async submitDefendantResponse() {
    const { submitDefendantResponsePage } = this.defendantResponsePageFactory;
    await submitDefendantResponsePage.verifyContent(this.ccdCaseData);
    await submitDefendantResponsePage.submit();
  }

  async confirmDefendantResponse() {
    const { confirmDefendantResponsePage } = this.defendantResponsePageFactory;
    await confirmDefendantResponsePage.verifyContent(this.ccdCaseData);
    await confirmDefendantResponsePage.submit();
  }

  async confirmDefendantResponse1v2DS() {
    const { confirm1v2DSDefendantResponsePage } = this.defendantResponsePageFactory;
    await confirm1v2DSDefendantResponsePage.verifyContent(this.ccdCaseData);
    await confirm1v2DSDefendantResponsePage.submit();
  }
}
