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
  dropdowns,
  heading,
  inputs,
  radioButtons,
  subheadings,
} from './fast-track-content';

@AllMethodsStep()
export default class FastTrackPage extends ExuiPage(BasePage) {
  private dateFragment: DateFragment;

  constructor(page: Page, dateFragment: DateFragment) {
    super(page);
    this.dateFragment = dateFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(heading),
      super.expectSubheading(subheadings.judgesRecital),
      super.expectSubheading(subheadings.allocation),
      super.expectLegend(radioButtons.allocation.assignComplexityBand.label),
      super.expectSubheading(subheadings.altDisputeResolution),
      super.expectSubheading(subheadings.variationOfDirections),
      super.expectSubheading(subheadings.settlement),
      super.expectSubheading(subheadings.disclosureOfDocuments),
      this.dateFragment.verifyContent(inputs.disclosureOfDocuments.date1.selectorKey, {
        containerSelector: containers.disclosureDocuments.selector,
      }),
      this.dateFragment.verifyContent(inputs.disclosureOfDocuments.date2.selectorKey, {
        containerSelector: containers.disclosureDocuments.selector,
      }),
      this.dateFragment.verifyContent(inputs.disclosureOfDocuments.date3.selectorKey, {
        containerSelector: containers.disclosureDocuments.selector,
      }),
      super.expectSubheading(subheadings.witnessesOfFact),
      this.dateFragment.verifyContent(inputs.witnessesOfFact.deadlineDate.selectorKey),
      super.expectSubheading(subheadings.schedulesOfLoss),
      this.dateFragment.verifyContent(inputs.scheduleOfLoss.date1.selectorKey, {
        containerSelector: containers.schedulesOfLoss.selector,
      }),
      this.dateFragment.verifyContent(inputs.scheduleOfLoss.date2.selectorKey, {
        containerSelector: containers.schedulesOfLoss.selector,
      }),
      super.expectSubheading(subheadings.hearingTime),
      super.expectSubheading(subheadings.hearingMethod),
      super.expectSubheading(subheadings.newDirection),
      super.expectSubheading(subheadings.hearingNotes),
      super.expectLabel(inputs.hearingNotes.label),
      super.expectText(subheadings.welshLanguage),
      super.expectSubheading(subheadings.importantNotes),
    ]);
  }

  async addAllocation() {
    await super.clickBySelector(radioButtons.allocation.assignComplexityBand.yes.selector);
    await Promise.all([
      super.expectLabel(radioButtons.allocation.allocationComplexity.band1.label),
      super.expectLabel(radioButtons.allocation.allocationComplexity.band2.label),
      super.expectLabel(radioButtons.allocation.allocationComplexity.band3.label),
      super.expectLabel(radioButtons.allocation.allocationComplexity.band4.label),
      super.expectLabel(inputs.allocationReasons.label),
    ]);
    await super.clickBySelector(radioButtons.allocation.allocationComplexity.band1.selector);
    await super.inputText('Allocation reason', inputs.allocationReasons.selector);
  }

  async addDisclosureOfDocuments() {
    const date1 = DateHelper.getToday();
    const date2 = DateHelper.addToToday({ days: 1, workingDay: true });
    const date3 = DateHelper.addToToday({ days: 2, workingDay: true });
    await super.inputText(
      'disclosure of documents input 1',
      inputs.disclosureOfDocuments.input1.selector,
    );
    await super.inputText(
      'disclosure of documents input 2',
      inputs.disclosureOfDocuments.input2.selector,
    );
    await super.inputText(
      'disclosure of documents input 3',
      inputs.disclosureOfDocuments.input3.selector,
    );
    await super.inputText(
      'disclosure of documents input 4',
      inputs.disclosureOfDocuments.input4.selector,
    );
    await this.dateFragment.enterDate(date1, inputs.disclosureOfDocuments.date1.selectorKey, {
      containerSelector: containers.disclosureDocuments.selector,
    });
    await this.dateFragment.enterDate(date2, inputs.disclosureOfDocuments.date2.selectorKey, {
      containerSelector: containers.disclosureDocuments.selector,
    });
    await this.dateFragment.enterDate(date3, inputs.disclosureOfDocuments.date3.selectorKey, {
      containerSelector: containers.disclosureDocuments.selector,
    });
  }

  async addWitnessesOfFact() {
    await Promise.all([
      super.expectText(inputs.witnessesOfFact.statementsOfWitnesses.label),
      super.expectText(radioButtons.witnessesOfFact.restrictNumWitnesses.label),
      super.expectText(radioButtons.witnessesOfFact.restrictNumPages.label),
      super.expectText(inputs.witnessesOfFact.deadline.label),
    ]);

    await super.inputText(
      'statement of witnesses',
      inputs.witnessesOfFact.statementsOfWitnesses.selector,
    );
    await super.inputText('deadline', inputs.witnessesOfFact.deadline.selector);
    await super.inputText('deadline text', inputs.witnessesOfFact.deadlineText.selector);
    const date = DateHelper.getToday();
    await this.dateFragment.enterDate(date, inputs.witnessesOfFact.deadlineDate.selectorKey);
  }

  async restrictNumWitnesess() {
    await super.clickBySelector(radioButtons.witnessesOfFact.restrictNumWitnesses.yes.selector);
    await super.expectLabel(inputs.witnessesOfFact.numClaimantWitnesses.label);
    await super.expectLabel(inputs.witnessesOfFact.numDefendantWitnesses.label);

    await super.inputText('1', inputs.witnessesOfFact.numClaimantWitnesses.selector);
    await super.inputText('2', inputs.witnessesOfFact.numDefendantWitnesses.selector);
    await super.inputText(
      'Party is witness',
      inputs.witnessesOfFact.partyIsCountedAsWitnessText.selector,
    );
  }

  async restrictNumPages() {
    await super.clickBySelector(radioButtons.witnessesOfFact.restrictNumPages.yes.selector);
    await super.expectLabel(inputs.witnessesOfFact.numPages.label);

    await super.inputText(
      'witness should not have more than',
      inputs.witnessesOfFact.witnessShouldNotMoreThanText.selector,
    );
    await super.inputText('3', inputs.witnessesOfFact.numPages.selector);
    await super.inputText('font details', inputs.witnessesOfFact.fontDetails.selector);
  }

  async addScheduleOfLoss() {
    await super.inputText('schedule of loss input 1', inputs.scheduleOfLoss.input1.selector);
    await super.inputText('schedule of loss input 2', inputs.scheduleOfLoss.input2.selector);
    await super.inputText('schedule of loss input 3', inputs.scheduleOfLoss.input3.selector);
    const date1 = DateHelper.getToday();
    const date2 = DateHelper.addToToday({ days: 1, workingDay: true });
    await this.dateFragment.enterDate(date1, inputs.scheduleOfLoss.date1.selectorKey, {
      containerSelector: containers.schedulesOfLoss.selector,
    });
    await this.dateFragment.enterDate(date2, inputs.scheduleOfLoss.date2.selectorKey, {
      containerSelector: containers.schedulesOfLoss.selector,
    });
  }

  async addHearingTime() {
    await Promise.all([
      super.expectText(inputs.hearingTime.dateFrom.label),
      super.expectLegend(inputs.hearingTime.dateTo.label),
      super.expectLegend(radioButtons.hearingTime.label),
      super.expectLabel(radioButtons.hearingTime.oneHour.label),
      super.expectLabel(radioButtons.hearingTime.oneHourThirtyMins.label),
      super.expectLabel(radioButtons.hearingTime.twoHours.label),
      super.expectLabel(radioButtons.hearingTime.threeHours.label),
      super.expectLabel(radioButtons.hearingTime.fourHours.label),
      super.expectLabel(radioButtons.hearingTime.fiveHours.label),
      super.expectLabel(radioButtons.hearingTime.other.label),
    ]);

    const dateFrom = DateHelper.getToday();
    const dateTo = DateHelper.addToToday({ days: 1, workingDay: true });
    await this.dateFragment.enterDate(dateFrom, inputs.hearingTime.dateFrom.selectorKey);
    await this.dateFragment.enterDate(dateTo, inputs.hearingTime.dateTo.selectorKey);

    await super.clickBySelector(radioButtons.hearingTime.other.selector);
    await super.expectLabel(inputs.hearingTime.otherHours.label, { exact: true });
    await super.expectLabel(inputs.hearingTime.otherMinutes.label);

    await super.inputText('4', inputs.hearingTime.otherHours.selector);
    await super.inputText('30', inputs.hearingTime.otherMinutes.selector);
    await super.inputText('hearing time help text 1', inputs.hearingTime.helpText1.selector);
  }

  async addHearingMethod() {
    await super.expectText(radioButtons.hearingMethod.label);
    await super.expectLabel(dropdowns.hearingMethod.label);
    await super.expectLabel(radioButtons.hearingMethod.inPerson.label);
    await super.expectLabel(radioButtons.hearingMethod.telephone.label);
    await super.expectLabel(radioButtons.hearingMethod.video.label);
    await super.clickByText(radioButtons.hearingMethod.telephone.label);
  }

  async addBuildingDispute() {
    await super.expectSubheading(subheadings.buildingDispute);

    await super.inputText('building dispute input 1', inputs.buildingDispute.input1.selector);
    await super.inputText('building dispute input 2', inputs.buildingDispute.input2.selector);
    await super.inputText('building dispute input 3', inputs.buildingDispute.input3.selector);
    await super.inputText('building dispute input 4', inputs.buildingDispute.input4.selector);
    const date1 = DateHelper.getToday();
    const date2 = DateHelper.addToToday({ days: 1, workingDay: true });
    await this.dateFragment.enterDate(date1, inputs.buildingDispute.date1.selectorKey, {
      containerSelector: containers.buildingDispute.selector,
    });
    await this.dateFragment.enterDate(date2, inputs.buildingDispute.date2.selectorKey, {
      containerSelector: containers.buildingDispute.selector,
    });
  }

  async addClinicalNegligence() {
    await super.expectSubheading(subheadings.clinicalNegligence);
    await super.expectText(inputs.clinicalNegliegence.input1.hintText);

    await super.inputText(
      'clinical negligence input 1',
      inputs.clinicalNegliegence.input1.selector,
    );
    await super.inputText(
      'clinical negligence input 2',
      inputs.clinicalNegliegence.input2.selector,
    );
    await super.inputText(
      'clinical negligence input 3',
      inputs.clinicalNegliegence.input3.selector,
    );
    await super.inputText(
      'clinical negligence input 4',
      inputs.clinicalNegliegence.input4.selector,
    );
  }

  async addCreditHire() {
    await super.inputText('credit hire input 1', inputs.creditHire.input1.selector);
    await super.inputText('credit hire input 2', inputs.creditHire.input2.selector);
    await super.inputText('credit hire input 3', inputs.creditHire.input3.selector);
    await super.inputText('credit hire input 4', inputs.creditHire.input4.selector);
    await super.inputText('credit hire input 5', inputs.creditHire.input5.selector);
    await super.inputText('credit hire input 6', inputs.creditHire.input6.selector);
    await super.inputText('credit hire input 7', inputs.creditHire.input7.selector);
    await super.inputText('credit hire input 8', inputs.creditHire.input8.selector);
    const date1 = DateHelper.getToday();
    const date2 = DateHelper.addToToday({ days: 1, workingDay: true });
    const date3 = DateHelper.addToToday({ days: 2, workingDay: true });
    const date4 = DateHelper.addToToday({ days: 1, workingDay: true });
    await this.dateFragment.enterDate(date1, inputs.creditHire.date1.selectorKey, {
      containerSelector: containers.creditHire.selector,
    });
    await this.dateFragment.enterDate(date2, inputs.creditHire.date2.selectorKey, {
      containerSelector: containers.creditHire.selector,
    });
    await this.dateFragment.enterDate(date3, inputs.creditHire.date3.selectorKey, {
      containerSelector: containers.creditHire.selector,
    });
    await this.dateFragment.enterDate(date4, inputs.creditHire.date4.selectorKey, {
      containerSelector: containers.creditHire.selector,
    });
  }

  async addHousingDisrepair() {
    await super.expectSubheading(subheadings.housingDisrepair);

    await super.inputText('housing disrepair input 1', inputs.housingDisrepair.input1.selector);
    await super.inputText('housing disrepair input 2', inputs.housingDisrepair.input2.selector);
    await super.inputText('housing disrepair input 3', inputs.housingDisrepair.input3.selector);
    await super.inputText('housing disrepair input 4', inputs.housingDisrepair.input4.selector);

    const date1 = DateHelper.getToday();
    const date2 = DateHelper.addToToday({ days: 1, workingDay: true });

    await this.dateFragment.enterDate(date1, inputs.housingDisrepair.date1.selectorKey, {
      containerSelector: containers.housingDisrepair.selector,
    });
    await this.dateFragment.enterDate(date2, inputs.housingDisrepair.date2.selectorKey, {
      containerSelector: containers.housingDisrepair.selector,
    });
  }

  async addExpertEvidence() {
    await super.expectSubheading(subheadings.expertEvidence);

    await super.inputText('expert evidence input 1', inputs.expertEvidence.input1.selector);
    await super.inputText('expert evidence input 2', inputs.expertEvidence.input2.selector);
    await super.inputText('expert evidence input 3', inputs.expertEvidence.input3.selector);
    await super.inputText('expert evidence input 4', inputs.expertEvidence.input4.selector);
    const date2 = DateHelper.getToday();
    const date3 = DateHelper.addToToday({ days: 1, workingDay: true });
    const date4 = DateHelper.addToToday({ days: 2, workingDay: true });

    await this.dateFragment.enterDate(date2, inputs.expertEvidence.date2.selectorKey, {
      containerSelector: containers.personalInjury.selector,
    });
    await this.dateFragment.enterDate(date3, inputs.expertEvidence.date3.selectorKey, {
      containerSelector: containers.personalInjury.selector,
    });
    await this.dateFragment.enterDate(date4, inputs.expertEvidence.date4.selectorKey, {
      containerSelector: containers.personalInjury.selector,
    });
  }

  async addRoadTrafficeAccident() {
    await super.expectSubheading(subheadings.roadTrafficAccident);
    await super.inputText('road traffic accident', inputs.roadTrafficAccident.input.selector);
    const date = DateHelper.getToday();
    await this.dateFragment.enterDate(date, inputs.roadTrafficAccident.date.selectorKey);
  }

  async addNewDirection() {
    await super.clickBySelector(buttons.addNewDirection.selector);
    await super.expectLabel(inputs.newDirection.label);
    await super.inputText('new direction', inputs.newDirection.selector);
  }

  async addHearingNotes() {
    await super.inputText('hearing notes', inputs.hearingNotes.selector);
  }

  async addWelshLanguage() {
    await super.clickBySelector(checkboxes.includeWelshLanguage.selector);
  }

  async addImportantNotes() {
    await super.inputText('important notes', inputs.importantNotes.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
