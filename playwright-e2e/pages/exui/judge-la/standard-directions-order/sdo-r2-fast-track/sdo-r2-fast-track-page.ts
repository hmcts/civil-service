import { Page } from '@playwright/test';
import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import DateHelper from '../../../../../helpers/date-helper';
import CCDCaseData from '../../../../../models/ccd/ccd-case-data';
import ExuiPage from '../../../exui-page/exui-page';
import DateFragment from '../../../fragments/date/date-fragment';
import {
  buttons,
  checkboxes,
  containers,
  inputs,
  radioButtons,
  subheadings,
} from './sdo-r2-fast-track-content';

@AllMethodsStep()
export default class SdoR2FastTrackPage extends ExuiPage(BasePage) {
  dateFragment: DateFragment;

  constructor(page: Page, dateFragment: DateFragment) {
    super(page);
    this.dateFragment = dateFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectText(subheadings.warning),
      super.expectText(subheadings.judgesRecital),
      super.expectText(subheadings.allocation),
      super.expectText(subheadings.altDisputeResolution),
      super.expectText(subheadings.variationOfDirections),
      super.expectText(subheadings.settlement),
      super.expectText(subheadings.disclosureOfDocuments),
      super.expectText(inputs.disclosureOfDocuments.standardDisclosure.label),
      super.expectText(inputs.disclosureOfDocuments.inspection.label),
      super.expectLabel(inputs.disclosureOfDocuments.requestWillBeCompiled.label),
      this.dateFragment.verifyContent(
        inputs.disclosureOfDocuments.standardDisclosureDate.selectorKey,
      ),
      this.dateFragment.verifyContent(inputs.disclosureOfDocuments.inspectionDate.selectorKey),
      super.expectText(subheadings.witnessOfFact),
      super.expectText(inputs.witnessesOfFact.statementOfWitnesses.label),
      super.expectText(radioButtons.witnessesOfFact.restrictNumWitnesses.label),
      super.expectText(radioButtons.witnessesOfFact.restrictNumPages.label),
      super.expectText(inputs.witnessesOfFact.deadline.label),
      this.dateFragment.verifyContent(inputs.witnessesOfFact.deadlineDate.selectorKey),
      super.expectText(subheadings.expertEvidence),
      super.expectText(inputs.expertEvidence.label),
      super.expectText(subheadings.addendumReport),
      super.expectText(inputs.addendumReport.addendumReportUpload.label),
      this.dateFragment.verifyContent(inputs.addendumReport.addendumReportDate.selectorKey),
      super.expectText(subheadings.furtherAudiogram),
      this.dateFragment.verifyContent(inputs.furtherAudiogram.shallUndergoDate.selectorKey),
      this.dateFragment.verifyContent(inputs.furtherAudiogram.serviceReportDate.selectorKey),
      super.expectText(subheadings.questionsClaimantExpert),
      super.expectText(subheadings.permissionDefendantRelyExpertEvidence),
      super.expectText(subheadings.evidenceExpertAcousticEngineer),
      super.expectText(subheadings.questionsToEntExpert),
      super.expectText(subheadings.scheduleOfLoss),
      super.expectText(subheadings.uploadOfDocuments),
      super.expectText(subheadings.newDirection),
      super.expectText(subheadings.trial),
      super.expectText(subheadings.welshLanguage),
      super.expectText(subheadings.importantNotes),
    ]);
  }

  async addDisclosureOfDocuments() {
    const inspectionDate = DateHelper.addToToday({ days: 2, workingDay: true });
    await super.inputText(
      'standard disclosure',
      inputs.disclosureOfDocuments.standardDisclosure.selector,
    );
    await super.inputText('inspection', inputs.disclosureOfDocuments.inspection.selector);
    await super.inputText('in x days', inputs.disclosureOfDocuments.requestWillBeCompiled.selector);
    const standardDisclosureDate = DateHelper.addToToday({ days: 1, workingDay: true });
    await this.dateFragment.enterDate(
      standardDisclosureDate,
      inputs.disclosureOfDocuments.standardDisclosureDate.selectorKey,
    );
    await this.dateFragment.enterDate(
      inspectionDate,
      inputs.disclosureOfDocuments.inspectionDate.selectorKey,
    );
  }

  async addWitnessesOfFact() {
    const date = DateHelper.addToToday({ days: 1, workingDay: true });
    await super.inputText(
      'statement of witneses',
      inputs.witnessesOfFact.statementOfWitnesses.selector,
    );
    await super.inputText('deadline', inputs.witnessesOfFact.deadline.selector);
    await super.inputText('deadline text', inputs.witnessesOfFact.deadlineText.selector);
    await this.dateFragment.enterDate(date, inputs.witnessesOfFact.deadlineDate.selectorKey);
  }

  async restrictNumWitnesses() {
    await super.clickBySelector(radioButtons.witnessesOfFact.restrictNumWitnesses.yes.selector);
    await super.expectLabel(inputs.witnessesOfFact.numClaimantWitnesses.label);
    await super.expectLabel(inputs.witnessesOfFact.numDefendantWitnesses.label);

    await super.inputText('1', inputs.witnessesOfFact.numClaimantWitnesses.selector);
    await super.inputText('2', inputs.witnessesOfFact.numDefendantWitnesses.selector);
    await super.inputText(
      'party counts as witness',
      inputs.witnessesOfFact.partyCountedAsWitness.selector,
    );
  }

  async restrictNumPages() {
    await super.clickBySelector(radioButtons.witnessesOfFact.restrictNumPages.yes.selector);
    await super.expectLabel(inputs.witnessesOfFact.numPages.label);

    await super.inputText(
      'witness should be no more than',
      inputs.witnessesOfFact.witnessShouldNotMoreThanText.selector,
    );
    await super.inputText('13', inputs.witnessesOfFact.numPages.selector);
    await super.inputText('font size', inputs.witnessesOfFact.fontDetails.selector);
  }

  async addExpertEvidence() {
    await super.inputText('expert evidence', inputs.expertEvidence.selector);
  }

  async addAddendumReport() {
    const date = DateHelper.addToToday({ days: 1, workingDay: true });
    await super.inputText('addendum report', inputs.addendumReport.addendumReportUpload.selector);
    await this.dateFragment.enterDate(date, inputs.addendumReport.addendumReportDate.selectorKey);
  }

  async addFurtherAudiogram() {
    await super.expectText(inputs.furtherAudiogram.shallUndergoText.label);
    await super.expectText(inputs.furtherAudiogram.serviceReportText.label);

    const shallUndergoDate = DateHelper.addToToday({ days: 1, workingDay: true });
    const serviceReportDate = DateHelper.addToToday({ days: 2, workingDay: true });
    await super.inputText(
      'claimant shall undergo',
      inputs.furtherAudiogram.shallUndergoText.selector,
    );
    await super.inputText('service report', inputs.furtherAudiogram.serviceReportText.selector);
    await this.dateFragment.enterDate(
      shallUndergoDate,
      inputs.furtherAudiogram.shallUndergoDate.selectorKey,
    );
    await this.dateFragment.enterDate(
      serviceReportDate,
      inputs.furtherAudiogram.serviceReportDate.selectorKey,
    );
  }

  async addQuestionsClaimantExpert() {
    await super.inputText(
      'defendant may ask questions',
      inputs.questionsClaimantExpert.defendantMayAskText.selector,
    );
    await super.inputText(
      'questions answered by',
      inputs.questionsClaimantExpert.questionsShallBeAnsweredText.selector,
    );
    await super.inputText(
      'uploaded to digital portal',
      inputs.questionsClaimantExpert.uploadToDigitalPortalText.selector,
    );
    await super.clickBySelector(radioButtons.questionsClaimantExpert.yes.selector);
    await super.inputText(
      'application to rely details',
      inputs.questionsClaimantExpert.applicationToRelyDetailsText.selector,
    );
    const defendantMayAskDate = DateHelper.addToToday({ days: 1, workingDay: true });
    const questionsShallBeAnsweredDate = DateHelper.addToToday({ days: 2, workingDay: true });
    const applicationToRelyDetailsDate = DateHelper.addToToday({ days: 3, workingDay: true });
    await this.dateFragment.enterDate(
      defendantMayAskDate,
      inputs.questionsClaimantExpert.defendantMayAskDate.selectorKey,
    );
    await this.dateFragment.enterDate(
      questionsShallBeAnsweredDate,
      inputs.questionsClaimantExpert.questionsShallBeAnsweredDate.selectorKey,
    );
    await this.dateFragment.enterDate(
      applicationToRelyDetailsDate,
      inputs.questionsClaimantExpert.applicationToRelyDetailsDate.selectorKey,
    );
  }

  async addPermissionDefendantRelyExpertEvidence() {
    await super.expectText(
      inputs.permissionDefendantRelyExpertEvidence.permissionToRelyOnExpertText.label,
    );
    await super.expectText(
      inputs.permissionDefendantRelyExpertEvidence.jointMeetingOfExpertsText.label,
    );

    const permissionToRelyOnExpertDate = DateHelper.addToToday({ days: 1, workingDay: true });
    const jointMeetingOfExpertsDate = DateHelper.addToToday({ days: 2, workingDay: true });
    await super.inputText(
      'permission to rely expert evidence',
      inputs.permissionDefendantRelyExpertEvidence.permissionToRelyOnExpertText.selector,
    );
    await super.inputText(
      'joint meeting of experts',
      inputs.permissionDefendantRelyExpertEvidence.jointMeetingOfExpertsText.selector,
    );
    await super.inputText(
      'upload to digital portal',
      inputs.permissionDefendantRelyExpertEvidence.uploadedToDigitalPortalText.selector,
    );
    await this.dateFragment.enterDate(
      permissionToRelyOnExpertDate,
      inputs.permissionDefendantRelyExpertEvidence.permissionToRelyOnExpertDate.selectorKey,
    );
    await this.dateFragment.enterDate(
      jointMeetingOfExpertsDate,
      inputs.permissionDefendantRelyExpertEvidence.jointMeetingOfExpertsDate.selectorKey,
    );
  }

  async addEvidenceExpertAcousticEngineer() {
    await super.inputText(
      'party have permission text',
      inputs.evidenceExpertAcousticEngineer.evidenceAcousticEngineerText.selector,
    );
    await super.inputText(
      'instructions of the expert',
      inputs.evidenceExpertAcousticEngineer.instructionOfTheEvidence.selector,
    );
    await super.inputText(
      'instructions of the expert text',
      inputs.evidenceExpertAcousticEngineer.instructionOfTheEvidenceTextArea.selector,
    );
    await super.inputText(
      'expert report',
      inputs.evidenceExpertAcousticEngineer.expertReport.selector,
    );
    await super.inputText(
      'expert report digital portal',
      inputs.evidenceExpertAcousticEngineer.expertReportDigitalPortal.selector,
    );
    await super.inputText('replies', inputs.evidenceExpertAcousticEngineer.replies.selector);
    await super.inputText(
      'replies digital portal',
      inputs.evidenceExpertAcousticEngineer.repliesDigitalPortal.selector,
    );
    await super.inputText(
      'service of order',
      inputs.evidenceExpertAcousticEngineer.serviceOfOrder.selector,
    );

    const instructionOfTheEvidenceDate = DateHelper.addToToday({ days: 1, workingDay: true });
    const expertReportDate = DateHelper.addToToday({ days: 2, workingDay: true });
    const writtenQuestionsDate = DateHelper.addToToday({ days: 3, workingDay: true });
    const repliesDate = DateHelper.addToToday({ days: 4, workingDay: true });

    await this.dateFragment.enterDate(
      instructionOfTheEvidenceDate,
      inputs.evidenceExpertAcousticEngineer.instructionOfTheEvidenceDate.selectorKey,
    );
    await this.dateFragment.enterDate(
      expertReportDate,
      inputs.evidenceExpertAcousticEngineer.expertReportDate.selectorKey,
    );
    await this.dateFragment.enterDate(
      writtenQuestionsDate,
      inputs.evidenceExpertAcousticEngineer.writtenQuestionsDate.selectorKey,
    );
    await this.dateFragment.enterDate(
      repliesDate,
      inputs.evidenceExpertAcousticEngineer.repliesDate.selectorKey,
    );
  }

  async addQuestionsToEntExpert() {
    await super.expectText(inputs.questionsToEntExpert.writtenQuestions.label);
    await super.expectText(inputs.questionsToEntExpert.questionsShallBeAnswered.label, {
      exact: true,
      ignoreDuplicates: true,
    });
    await super.expectText(inputs.questionsToEntExpert.questionsShallBeAnsweredDigitalPortal.label);

    await super.inputText(
      'written questions',
      inputs.questionsToEntExpert.writtenQuestions.selector,
    );
    await super.inputText(
      'written questions digital portal',
      inputs.questionsToEntExpert.writtenQuestionsDigitalPortal.selector,
    );
    await super.inputText(
      'questions be answered',
      inputs.questionsToEntExpert.questionsShallBeAnswered.selector,
    );
    await super.inputText(
      'questions be answered digital portal',
      inputs.questionsToEntExpert.questionsShallBeAnsweredDigitalPortal.selector,
    );

    const writtenQuestionsDate = DateHelper.addToToday({ days: 1, workingDay: true });
    const questionsShallBeAnsweredDate = DateHelper.addToToday({ days: 2, workingDay: true });

    await this.dateFragment.enterDate(
      writtenQuestionsDate,
      inputs.questionsToEntExpert.writtenQuestionsDate.selectorKey,
    );
    await this.dateFragment.enterDate(
      questionsShallBeAnsweredDate,
      inputs.questionsToEntExpert.questionsShallBeAnsweredDate.selectorKey,
    );
  }

  async addScheduleOfLoss() {
    await super.inputText('claimant', inputs.scheduleOfLoss.claimant.selector);
    await super.inputText('defendant', inputs.scheduleOfLoss.defendant.selector);
    await super.clickBySelector(radioButtons.scheduleOfLoss.yes.selector);
    await super.inputText('percuniary loss', inputs.scheduleOfLoss.percuniaryLoss.selector);

    const claimantDate = DateHelper.addToToday({ days: 1, workingDay: true });
    const defendantDate = DateHelper.addToToday({ days: 2, workingDay: true });

    await this.dateFragment.enterDate(claimantDate, inputs.scheduleOfLoss.claimantDate.selectorKey);
    await this.dateFragment.enterDate(
      defendantDate,
      inputs.scheduleOfLoss.defendantDate.selectorKey,
    );
  }

  async addUploadOfDocuments() {
    await super.expectText(inputs.uploadOfDocuments.label);
    await super.inputText('upload of documents', inputs.uploadOfDocuments.selector);
  }

  async addNewDirection() {
    await super.clickBySelector(buttons.addNewDirection.selector);
    await super.expectLabel(inputs.newDirection.label);
    await super.inputText('new direction', inputs.newDirection.selector);
  }

  async addTrial() {
    await super.clickBySelector(radioButtons.trial.trialOnOptions.trialWindow.selector);
    await super.expectText(inputs.trial.dateTo.label);

    await super.clickBySelector(radioButtons.trial.lengthOfTrial.other.selector);
    await super.expectLabel(inputs.trial.trialLengthDays.label);
    await super.expectLabel(inputs.trial.trialLengthHours.label);
    await super.expectLabel(inputs.trial.trialLengthMinutes.label);

    const listFrom = DateHelper.addToToday({ days: 1, workingDay: true });
    const dateTo = DateHelper.addToToday({ days: 2, workingDay: true });

    await this.dateFragment.enterDate(listFrom, inputs.trial.listFrom.selectorKey, {
      containerSelector: containers.sdoR2Trail.selector,
    });
    await this.dateFragment.enterDate(dateTo, inputs.trial.dateTo.selectorKey);

    await super.inputText('1', inputs.trial.trialLengthDays.selector);
    await super.inputText('8', inputs.trial.trialLengthHours.selector);
    await super.inputText('30', inputs.trial.trialLengthMinutes.selector);
    await super.inputText('bundle of documents', inputs.trial.bundleOfDocuments.selector);
    await super.inputText('hearing notes', inputs.trial.hearingNotes.selector);
  }

  async addWelshLanguage() {
    await super.clickBySelector(checkboxes.includeWelshLanguage.selector);
  }

  async addImportantNotes() {
    const date = DateHelper.addToToday({ days: 1, workingDay: true });
    await super.inputText('important notes', inputs.importantNotes.importantNotesText.selector);
    await this.dateFragment.enterDate(date, inputs.importantNotes.importantNotesDate.selectorKey);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
