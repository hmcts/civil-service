import BaseTestData from '../../../../../base/base-test-data';
import { Step } from '../../../../../decorators/test-steps';
import TestData from '../../../../../models/test-data';
import DefendantResponsePageFactory from '../../../../../pages/exui/claimant-defendant-solicitor/response/defendant-response/defendant-response-page-factory';

const classKey = 'DefendantResponseSpecActions';
export default class DefendantResponseSpecActions extends BaseTestData {
  defendantResponsePageFactory: DefendantResponsePageFactory;

  constructor(defendantResponsePageFactory: DefendantResponsePageFactory, testData: TestData) {
    super(testData);
    this.defendantResponsePageFactory = defendantResponsePageFactory;
  }

  @Step(classKey)
  async respondentChecklist() {
    const { respondentChecklistPage } = this.defendantResponsePageFactory;
    await respondentChecklistPage.verifyContent();
    await respondentChecklistPage.submit();
  }

  @Step(classKey)
  async responseConfirmNameAddressDS1() {
    const { responseConfirmNameAddressDS1Page } = this.defendantResponsePageFactory;
    await responseConfirmNameAddressDS1Page.verifyContent(this.ccdCaseData);
    await responseConfirmNameAddressDS1Page.selectYesAddress();
    await responseConfirmNameAddressDS1Page.submit();
  }

  @Step(classKey)
  async responseConfirmNameAddressDS2() {
    const { responseConfirmNameAddressDS2Page } = this.defendantResponsePageFactory;
    await responseConfirmNameAddressDS2Page.verifyContent(this.ccdCaseData);
    await responseConfirmNameAddressDS2Page.selectYesAddress();
    await responseConfirmNameAddressDS2Page.submit();
  }

  @Step(classKey)
  async responseConfirmDetailsDS1() {
    const { responseConfirmDetailsDS1Page } = this.defendantResponsePageFactory;
    await responseConfirmDetailsDS1Page.verifyContent();
    await responseConfirmDetailsDS1Page.selectYesAddress();
    await responseConfirmDetailsDS1Page.submit();
  }

  @Step(classKey)
  async responseConfirmDetailsDS2() {
    const { responseConfirmDetailsDS2Page } = this.defendantResponsePageFactory;
    await responseConfirmDetailsDS2Page.verifyContent();
    await responseConfirmDetailsDS2Page.selectYesAddress();
    await responseConfirmDetailsDS2Page.submit();
  }

  @Step(classKey)
  async responseConfirmNameAddress1v2() {
    const { responseConfirmNameAddress1v2Page } = this.defendantResponsePageFactory;
    await responseConfirmNameAddress1v2Page.verifyContent(this.ccdCaseData);
    await responseConfirmNameAddress1v2Page.selectYesAddress();
    await responseConfirmNameAddress1v2Page.submit();
  }

  @Step(classKey)
  async singleResponse() {
    const { singleResponsePage } = this.defendantResponsePageFactory;
    await singleResponsePage.verifyContent(this.ccdCaseData);
    await singleResponsePage.selectYes();
    await singleResponsePage.submit();
  }

  @Step(classKey)
  async singleResponse2v1() {
    const { singleResponse2v1Page } = this.defendantResponsePageFactory;
    await singleResponse2v1Page.verifyContent(this.ccdCaseData);
    await singleResponse2v1Page.selectYes();
    await singleResponse2v1Page.submit();
  }

  @Step(classKey)
  async respondentResponseType2v1Spec() {
    const { respondentResponseType2v1SpecPage } = this.defendantResponsePageFactory;
    await respondentResponseType2v1SpecPage.verifyContent(this.ccdCaseData);
    await respondentResponseType2v1SpecPage.selectFullDefenceBothClaimants();
    await respondentResponseType2v1SpecPage.submit();
  }

  @Step(classKey)
  async respondentResponseTypeSpecDS1() {
    const { respondentResponseTypeSpecDS1Page } = this.defendantResponsePageFactory;
    await respondentResponseTypeSpecDS1Page.verifyContent(this.ccdCaseData);
    await respondentResponseTypeSpecDS1Page.selectFullDefence();
    await respondentResponseTypeSpecDS1Page.submit();
  }

  @Step(classKey)
  async respondentResponseTypeSpecDS2() {
    const { respondentResponseTypeSpecDS2Page } = this.defendantResponsePageFactory;
    await respondentResponseTypeSpecDS2Page.verifyContent(this.ccdCaseData);
    await respondentResponseTypeSpecDS2Page.selectFullDefence();
    await respondentResponseTypeSpecDS2Page.submit();
  }

  @Step(classKey)
  async defenceRouteDS1() {
    const { defenceRouteDS1Page } = this.defendantResponsePageFactory;
    await defenceRouteDS1Page.verifyContent(this.ccdCaseData);
    await defenceRouteDS1Page.selectDisputesClaim();
    await defenceRouteDS1Page.submit();
  }

  @Step(classKey)
  async defenceRouteDS2() {
    const { defenceRouteDS2Page } = this.defendantResponsePageFactory;
    await defenceRouteDS2Page.verifyContent(this.ccdCaseData);
    await defenceRouteDS2Page.selectDisputesClaim();
    await defenceRouteDS2Page.submit();
  }

  @Step(classKey)
  async uploadDefendantResponseSpecDS1() {
    const { uploadDefendantResponseSpecDS1Page } = this.defendantResponsePageFactory;
    await uploadDefendantResponseSpecDS1Page.verifyContent(this.ccdCaseData);
    await uploadDefendantResponseSpecDS1Page.enterDisputeReason();
    await uploadDefendantResponseSpecDS1Page.submit();
  }

  @Step(classKey)
  async uploadDefendantResponseSpecDS2() {
    const { uploadDefendantResponseSpecDS2Page } = this.defendantResponsePageFactory;
    await uploadDefendantResponseSpecDS2Page.verifyContent(this.ccdCaseData);
    await uploadDefendantResponseSpecDS2Page.enterDisputeReason();
    await uploadDefendantResponseSpecDS2Page.submit();
  }

  @Step(classKey)
  async timelineDS1() {
    const { howToAddTimelineDS1Page } = this.defendantResponsePageFactory;
    await howToAddTimelineDS1Page.verifyContent(this.ccdCaseData);
    await howToAddTimelineDS1Page.selectUpload();
    await howToAddTimelineDS1Page.submit();

    const { howToAddTimelineUploadDS1Page } = this.defendantResponsePageFactory;
    await howToAddTimelineUploadDS1Page.verifyContent(this.ccdCaseData);
    await howToAddTimelineUploadDS1Page.uploadDoc();
    await howToAddTimelineUploadDS1Page.submit();
  }

  @Step(classKey)
  async timelineDS2() {
    const { howToAddTimelineDS2Page } = this.defendantResponsePageFactory;
    await howToAddTimelineDS2Page.verifyContent(this.ccdCaseData);
    await howToAddTimelineDS2Page.selectUpload();
    await howToAddTimelineDS2Page.submit();

    const { howToAddTimelineUploadDS2Page } = this.defendantResponsePageFactory;
    await howToAddTimelineUploadDS2Page.verifyContent(this.ccdCaseData);
    await howToAddTimelineUploadDS2Page.uploadDoc();
    await howToAddTimelineUploadDS2Page.submit();
  }

  @Step(classKey)
  async oldMediationDS1() {
    const { mediationDS1Page } = this.defendantResponsePageFactory;
    await mediationDS1Page.verifyContent(this.ccdCaseData);
    await mediationDS1Page.selectYes();
    await mediationDS1Page.submit();
  }

  @Step(classKey)
  async oldMediationDS2() {
    const { mediationDS2Page } = this.defendantResponsePageFactory;
    await mediationDS2Page.verifyContent(this.ccdCaseData);
    await mediationDS2Page.selectYes();
    await mediationDS2Page.submit();
  }

  @Step(classKey)
  async mediationDS1() {
    const { mediationContactInformationDS1Page } = this.defendantResponsePageFactory;
    await mediationContactInformationDS1Page.verifyContent(this.ccdCaseData);
    await mediationContactInformationDS1Page.enterMediationContactDetails();
    await mediationContactInformationDS1Page.submit();

    const { mediationAvailabilityDS1Page } = this.defendantResponsePageFactory;
    await mediationAvailabilityDS1Page.verifyContent(this.ccdCaseData);
    await mediationAvailabilityDS1Page.selectYes();
    await mediationAvailabilityDS1Page.addNewUnavailableDate();
    await mediationAvailabilityDS1Page.selectSingleDate();
    await mediationAvailabilityDS1Page.submit();
  }

  @Step(classKey)
  async mediationDS2() {
    const { mediationContactInformationDS2Page } = this.defendantResponsePageFactory;
    await mediationContactInformationDS2Page.verifyContent(this.ccdCaseData);
    await mediationContactInformationDS2Page.enterMediationContactDetails();
    await mediationContactInformationDS2Page.submit();

    const { mediationAvailabilityDS2Page } = this.defendantResponsePageFactory;
    await mediationAvailabilityDS2Page.verifyContent(this.ccdCaseData);
    await mediationAvailabilityDS2Page.selectNo();
    await mediationAvailabilityDS2Page.submit();
  }

  @Step(classKey)
  async applicationDS1() {
    const { applicationDS1Page } = this.defendantResponsePageFactory;
    await applicationDS1Page.verifyContent(this.ccdCaseData);
    await applicationDS1Page.selectYes();
    await applicationDS1Page.enterAdditionalInformation();
    await applicationDS1Page.submit();
  }

  @Step(classKey)
  async statementOfTruthDefendantResponseDS2() {
    const { statementOfTruthDefendantResponseDS2Page } = this.defendantResponsePageFactory;
    await statementOfTruthDefendantResponseDS2Page.verifyContent(this.ccdCaseData);
    await statementOfTruthDefendantResponseDS2Page.enterDetails();
    await statementOfTruthDefendantResponseDS2Page.submit();
  }

  @Step(classKey)
  async confirmDefendantResponseSpec() {
    const { confirmDefendantResponseSpecPage } = this.defendantResponsePageFactory;
    await confirmDefendantResponseSpecPage.verifyContent(this.ccdCaseData);
    await confirmDefendantResponseSpecPage.submit();
  }

  @Step(classKey)
  async confirm1v2SSDefendantResponseSpec() {
    const { confirm1v2SSDefendantResponseSpecPage } = this.defendantResponsePageFactory;
    await confirm1v2SSDefendantResponseSpecPage.verifyContent(this.ccdCaseData);
    await confirm1v2SSDefendantResponseSpecPage.submit();
  }

  @Step(classKey)
  async dqFastTrackDS1() {
    const { fileDirectionsQuestionaireDS1Page } = this.defendantResponsePageFactory;
    await fileDirectionsQuestionaireDS1Page.verifyContent(this.ccdCaseData);
    await fileDirectionsQuestionaireDS1Page.enterDetails();
    await fileDirectionsQuestionaireDS1Page.submit();

    const { fixedRecoverableCostsDS1Page } = this.defendantResponsePageFactory;
    await fixedRecoverableCostsDS1Page.verifyContent(this.ccdCaseData);
    await fixedRecoverableCostsDS1Page.selectYes();
    await fixedRecoverableCostsDS1Page.submit();

    const { disclosureOfElectronicDocumentsLRSpecDS1Page } = this.defendantResponsePageFactory;
    await disclosureOfElectronicDocumentsLRSpecDS1Page.verifyContent(this.ccdCaseData);
    await disclosureOfElectronicDocumentsLRSpecDS1Page.enterDetails();
    await disclosureOfElectronicDocumentsLRSpecDS1Page.submit();

    const { disclosureOfNonElectronicDocumentsLRSpecDS1Page } = this.defendantResponsePageFactory;
    await disclosureOfNonElectronicDocumentsLRSpecDS1Page.verifyContent(this.ccdCaseData);
    await disclosureOfNonElectronicDocumentsLRSpecDS1Page.enterDetails();
    await disclosureOfNonElectronicDocumentsLRSpecDS1Page.submit();

    const { disclosureReportDS1Page } = this.defendantResponsePageFactory;
    await disclosureReportDS1Page.verifyContent(this.ccdCaseData);
    await disclosureReportDS1Page.enterDetails();
    await disclosureReportDS1Page.submit();
  }

  @Step(classKey)
  async dqDS1() {
    const { expertsDS1Page } = this.defendantResponsePageFactory;
    await expertsDS1Page.verifyContent(this.ccdCaseData);
    await expertsDS1Page.useExperts();
    await expertsDS1Page.addNewExpert();
    await expertsDS1Page.enterExpertDetails();
    await expertsDS1Page.submit();

    const { witnessesSpecDS1Page } = this.defendantResponsePageFactory;
    await witnessesSpecDS1Page.verifyContent(this.ccdCaseData);
    await witnessesSpecDS1Page.addWitnesses();
    await witnessesSpecDS1Page.enterWitnessDetails();
    await witnessesSpecDS1Page.submit();

    await this.languageDS1();

    const { hearingLRSpecDS1Page } = this.defendantResponsePageFactory;
    await hearingLRSpecDS1Page.verifyContent(this.ccdCaseData);
    await hearingLRSpecDS1Page.selectYesAvailabilityRequired();
    await hearingLRSpecDS1Page.addNewUnavailableDate();
    await hearingLRSpecDS1Page.selectSingleDate();
    await hearingLRSpecDS1Page.submit();

    await this.requestedCourtLRSpecDS1();
    await this.hearingSupportDS1();
    await this.vulnerabilityQuestionsSpecDS1();
  }

  @Step(classKey)
  async dqSmallTrackDS1() {
    const { determinationWithoutHearingDS1Page } = this.defendantResponsePageFactory;
    await determinationWithoutHearingDS1Page.verifyContent(this.ccdCaseData);
    await determinationWithoutHearingDS1Page.selectNo();
    await determinationWithoutHearingDS1Page.submit();

    const { smallClaimExpertsDS1Page } = this.defendantResponsePageFactory;
    await smallClaimExpertsDS1Page.verifyContent(this.ccdCaseData);
    await smallClaimExpertsDS1Page.selectYesExperts();
    await smallClaimExpertsDS1Page.enterExpertDetails();
    await smallClaimExpertsDS1Page.submit();

    const { smallClaimWitnessesDS1Page } = this.defendantResponsePageFactory;
    await smallClaimWitnessesDS1Page.verifyContent(this.ccdCaseData);
    await smallClaimWitnessesDS1Page.selectYes();
    await smallClaimWitnessesDS1Page.addWitness();
    await smallClaimWitnessesDS1Page.enterWitnessDetails();
    await smallClaimWitnessesDS1Page.submit();

    await this.languageDS1();

    const { smallClaimHearingDS1Page } = this.defendantResponsePageFactory;
    await smallClaimHearingDS1Page.verifyContent(this.ccdCaseData);
    await smallClaimHearingDS1Page.selectYesAvailabilityRequired();
    await smallClaimHearingDS1Page.addNewUnavailableDate();
    await smallClaimHearingDS1Page.selectSingleDate();
    await smallClaimHearingDS1Page.selectYesInterpreter();
    await smallClaimHearingDS1Page.enterTypeOfInterpreter();
    await smallClaimHearingDS1Page.submit();

    await this.requestedCourtLRSpecDS1();
    await this.hearingSupportDS1();
    await this.vulnerabilityQuestionsSpecDS1();
  }

  @Step(classKey)
  async dqSmallTrackDS2() {
    const { determinationWithoutHearingDS2Page } = this.defendantResponsePageFactory;
    await determinationWithoutHearingDS2Page.verifyContent(this.ccdCaseData);
    await determinationWithoutHearingDS2Page.selectNo();
    await determinationWithoutHearingDS2Page.submit();

    const { smallClaimExpertsDS2Page } = this.defendantResponsePageFactory;
    await smallClaimExpertsDS2Page.verifyContent(this.ccdCaseData);
    await smallClaimExpertsDS2Page.selectYesExperts();
    await smallClaimExpertsDS2Page.enterExpertDetails();
    await smallClaimExpertsDS2Page.submit();

    const { smallClaimWitnessesDS2Page } = this.defendantResponsePageFactory;
    await smallClaimWitnessesDS2Page.verifyContent(this.ccdCaseData);
    await smallClaimWitnessesDS2Page.selectYes();
    await smallClaimWitnessesDS2Page.addWitness();
    await smallClaimWitnessesDS2Page.enterWitnessDetails();
    await smallClaimWitnessesDS2Page.submit();

    const { languageDS2Page } = this.defendantResponsePageFactory;
    await languageDS2Page.verifyContent(this.ccdCaseData);
    await languageDS2Page.selectEnglishAndWelsh();
    await languageDS2Page.submit();

    const { smallClaimHearingDS2Page } = this.defendantResponsePageFactory;
    await smallClaimHearingDS2Page.verifyContent(this.ccdCaseData);
    await smallClaimHearingDS2Page.selectNoAvailabilityRequired();
    await smallClaimHearingDS2Page.selectNoInterpreter();
    await smallClaimHearingDS2Page.submit();

    const { requestedCourtLRSpecDS2Page } = this.defendantResponsePageFactory;
    await requestedCourtLRSpecDS2Page.verifyContent(this.ccdCaseData);
    await requestedCourtLRSpecDS2Page.selectCourtLocation();
    await requestedCourtLRSpecDS2Page.selectNoRemoteHearing();
    await requestedCourtLRSpecDS2Page.submit();

    const { hearingSupportDS2Page } = this.defendantResponsePageFactory;
    await hearingSupportDS2Page.verifyContent(this.ccdCaseData);
    await hearingSupportDS2Page.selectYes();
    await hearingSupportDS2Page.enterSupportRequirementsAdditional();
    await hearingSupportDS2Page.submit();

    const { vulnerabilityQuestionsSpecDS2Page } = this.defendantResponsePageFactory;
    await vulnerabilityQuestionsSpecDS2Page.verifyContent(this.ccdCaseData);
    await vulnerabilityQuestionsSpecDS2Page.selectYes();
    await vulnerabilityQuestionsSpecDS2Page.enterVulnerabilityAdjustments();
    await vulnerabilityQuestionsSpecDS2Page.submit();
  }

  @Step(classKey)
  async statementOfTruthDefendantResponseDS1() {
    const { statementOfTruthDefendantResponseDS1Page } = this.defendantResponsePageFactory;
    await statementOfTruthDefendantResponseDS1Page.verifyContent(this.ccdCaseData);
    await statementOfTruthDefendantResponseDS1Page.enterDetails();
    await statementOfTruthDefendantResponseDS1Page.submit();
  }

  @Step(classKey)
  async submitDefendantResponse() {
    const { submitDefendantResponsePage } = this.defendantResponsePageFactory;
    await submitDefendantResponsePage.verifyContent(this.ccdCaseData);
    await submitDefendantResponsePage.submit();
  }

  private async languageDS1() {
    const { languageDS1Page } = this.defendantResponsePageFactory;
    await languageDS1Page.verifyContent(this.ccdCaseData);
    await languageDS1Page.selectEnglishAndWelsh();
    await languageDS1Page.submit();
  }

  private async requestedCourtLRSpecDS1() {
    const { requestedCourtLRSpecDS1Page } = this.defendantResponsePageFactory;
    await requestedCourtLRSpecDS1Page.verifyContent(this.ccdCaseData);
    await requestedCourtLRSpecDS1Page.selectCourtLocation();
    await requestedCourtLRSpecDS1Page.selectNoRemoteHearing();
    await requestedCourtLRSpecDS1Page.submit();
  }

  private async hearingSupportDS1() {
    const { hearingSupportDS1Page } = this.defendantResponsePageFactory;
    await hearingSupportDS1Page.verifyContent(this.ccdCaseData);
    await hearingSupportDS1Page.selectYes();
    await hearingSupportDS1Page.enterSupportRequirementsAdditional();
    await hearingSupportDS1Page.submit();
  }

  private async vulnerabilityQuestionsSpecDS1() {
    const { vulnerabilityQuestionsSpecDS1Page } = this.defendantResponsePageFactory;
    await vulnerabilityQuestionsSpecDS1Page.verifyContent(this.ccdCaseData);
    await vulnerabilityQuestionsSpecDS1Page.selectYes();
    await vulnerabilityQuestionsSpecDS1Page.enterVulnerabilityAdjustments();
    await vulnerabilityQuestionsSpecDS1Page.submit();
  }
}
