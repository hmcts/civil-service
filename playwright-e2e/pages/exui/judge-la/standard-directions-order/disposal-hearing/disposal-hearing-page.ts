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
  dropdowns,
  inputs,
  radioButtons,
  subheadings,
  containers,
} from './disposal-hearing-content';

@AllMethodsStep()
export default class DisposalHearingPage extends ExuiPage(BasePage) {
  dateFragment: DateFragment;

  constructor(page: Page, dateFragment: DateFragment) {
    super(page);
    this.dateFragment = dateFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData): Promise<void> {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectSubheading(subheadings.orderHearingDetails),
      super.expectSubheading(subheadings.judgesRecital),
      super.expectSubheading(subheadings.judgementForClaimant),
      super.expectSubheading(subheadings.disclosureOfDocuments),
      super.expectSubheading(subheadings.witnessOfFact),
      super.expectSubheading(subheadings.medicalEvidence),
      super.expectSubheading(subheadings.questionsToExperts),
      super.expectSubheading(subheadings.scheduleOfLoss),
      super.expectSubheading(subheadings.hearingTime),
      super.expectSubheading(subheadings.hearingTime),
      super.expectSubheading(subheadings.hearingMethod),
      super.expectSubheading(subheadings.disposalHearingBundle),
      super.expectSubheading(subheadings.claimSettling),
      super.expectSubheading(subheadings.costs),
      super.expectSubheading(subheadings.newDirection),
      super.expectSubheading(subheadings.hearingNotes),
      super.expectText(subheadings.welshLanguage),
      super.expectSubheading(subheadings.importantNotes),
    ]);
  }

  async addJudgesRecital() {
    await super.inputText('judges recital', inputs.judgesRecital.selector);
  }

  async addDisclosureOfDocuments() {
    const date1 = DateHelper.getToday();
    const date2 = DateHelper.addToToday({ days: 1, workingDay: true });
    await super.inputText(
      'disclosure of documents input 1',
      inputs.disclosureOfDocuments.input1.selector,
      { containerSelector: containers.disclosureOfDocuments.selector },
    );
    await super.inputText(
      'disclosure of documents input 2',
      inputs.disclosureOfDocuments.input2.selector,
      { containerSelector: containers.disclosureOfDocuments.selector },
    );
    await this.dateFragment.enterDate(date1, inputs.disclosureOfDocuments.date1.selectorKey);
    await this.dateFragment.enterDate(date2, inputs.disclosureOfDocuments.date2.selectorKey);
  }

  async addWitnessesOfFact() {
    const date2 = DateHelper.getToday();
    const date3 = DateHelper.addToToday({ days: 1, workingDay: true });
    await super.inputText('witnesses of fact input 3', inputs.witnessOfFact.input3.selector);
    await super.inputText('witnesses of fact input 4', inputs.witnessOfFact.input4.selector);
    await super.inputText('witnesses of fact input 5', inputs.witnessOfFact.input5.selector);
    await super.inputText('witnesses of fact input 3', inputs.witnessOfFact.input6.selector);
    await this.dateFragment.enterDate(date2, inputs.witnessOfFact.date2.selectorKey, {
      containerSelector: containers.witnessOfFact.selector,
    });
    await this.dateFragment.enterDate(date3, inputs.witnessOfFact.date3.selectorKey, {
      containerSelector: containers.witnessOfFact.selector,
    });
  }

  async addMedicalEvidence() {
    const date = DateHelper.getToday();
    await super.inputText('medical evidence input 1', inputs.medicalEvidence.input.selector);
    await this.dateFragment.enterDate(date, inputs.medicalEvidence.date.selectorKey, {
      containerSelector: containers.medicalEvidence.selector,
    });
  }

  async addQuestionsToExperts() {
    const date = DateHelper.getToday();
    await this.dateFragment.enterDate(date, inputs.questionsToExperts.date.selectorKey, {
      containerSelector: containers.questionsToExperts.selector,
    });
  }

  async addScheduleOfLoss() {
    await super.inputText('schedule of loss input 2', inputs.scheduleOfLoss.input2.selector);
    await super.inputText('schedule of loss input 3', inputs.scheduleOfLoss.input3.selector);
    await super.inputText('schedule of loss input 4', inputs.scheduleOfLoss.input4.selector);

    const date2 = DateHelper.getToday();
    const date3 = DateHelper.addToToday({ days: 1, workingDay: true });
    const date4 = DateHelper.addToToday({ days: 3, workingDay: true });
    await this.dateFragment.enterDate(date2, inputs.scheduleOfLoss.date2.selectorKey, {
      containerSelector: containers.schedulesOfLoss.selector,
    });
    await this.dateFragment.enterDate(date3, inputs.scheduleOfLoss.date3.selectorKey, {
      containerSelector: containers.schedulesOfLoss.selector,
    });
    await this.dateFragment.enterDate(date4, inputs.scheduleOfLoss.date4.selectorKey, {
      containerSelector: containers.schedulesOfLoss.selector,
    });
  }

  async addHearingTime() {
    const dateFrom = DateHelper.getToday();
    const dateTo = DateHelper.addToToday({ days: 1, workingDay: true });
    await super.inputText('hearing time input', inputs.hearingTime.input.selector);
    await super.inputText('1', inputs.hearingTime.otherHours.selector);
    await super.inputText('30', inputs.hearingTime.otherMinutes.selector);
    await this.dateFragment.enterDate(dateFrom, inputs.hearingTime.dateFrom.selectorKey, {
      containerSelector: containers.schedulesOfLoss.selector,
    });
    await this.dateFragment.enterDate(dateFrom, inputs.hearingTime.dateTo.selectorKey, {
      containerSelector: containers.schedulesOfLoss.selector,
    });
  }

  async addHearingMethod() {
    await Promise.all([
      super.expectText(radioButtons.hearingMethod.label),
      super.expectLabel(radioButtons.hearingMethod.inPerson.label),
      super.expectLabel(radioButtons.hearingMethod.telephone.label),
      super.expectLabel(radioButtons.hearingMethod.video.label),
      super.expectLabel(dropdowns.hearingMethod.label),
    ]);

    await super.clickByText(radioButtons.hearingMethod.telephone.label);
  }

  async addDisposalHearingBundle() {
    await Promise.all([
      super.expectText(checkboxes.disposalHearingBundle.bundleType.label),
      super.expectLabel(checkboxes.disposalHearingBundle.bundleType.documents.label),
      super.expectLabel(checkboxes.disposalHearingBundle.bundleType.electronic.label),
      super.expectLabel(checkboxes.disposalHearingBundle.bundleType.summary.label),
    ]);

    await super.inputText('disposal hearing bundle input', inputs.disposalHearingBundle.selector);
    await super.clickBySelector(checkboxes.disposalHearingBundle.bundleType.documents.selector);
    await super.clickBySelector(checkboxes.disposalHearingBundle.bundleType.electronic.selector);
    await super.clickBySelector(checkboxes.disposalHearingBundle.bundleType.summary.selector);
  }

  async addNewDirection() {
    await super.clickBySelector(buttons.addNewDirection.selector);
    await super.expectLabel(inputs.newDirection.label);
    await super.inputText('new direction', inputs.newDirection.selector);
  }

  async addHearingNotes() {
    await super.expectLabel(inputs.hearingNotes.label);
    await super.inputText('hearing notes', inputs.hearingNotes.selector);
  }

  async addWelshLanguage() {
    await super.clickBySelector(checkboxes.welshLanguage.selector);
  }

  async addImportantNotes() {
    await super.inputText('important notes', inputs.importantNotes.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
