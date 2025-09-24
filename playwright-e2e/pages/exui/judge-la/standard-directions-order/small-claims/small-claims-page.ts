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
  heading,
  inputs,
  radioButtons,
  subheadings,
} from './small-claims-content';

@AllMethodsStep()
export default class SmallClaimsPage extends ExuiPage(BasePage) {
  dateFragment: DateFragment;

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
      super.expectSubheading(subheadings.hearingTime),
      super.expectSubheading(subheadings.hearingMethod),
      super.expectText(radioButtons.hearingMethod.label),
      super.expectLabel(radioButtons.hearingMethod.inPerson.label),
      super.expectLabel(radioButtons.hearingMethod.telephone.label),
      super.expectLabel(radioButtons.hearingMethod.video.label),
      super.expectLabel(dropdowns.hearingLocation.label),
      super.expectSubheading(subheadings.hearingNotes),
      super.expectText(inputs.hearingNotes.hintText),
      super.expectText(subheadings.welshLanguage),
      super.expectSubheading(subheadings.importantNotes),
      super.expectSubheading(subheadings.documents),
      super.expectSubheading(subheadings.witnessStatement),
      super.expectSubheading(subheadings.addNewDirection),
    ]);
  }

  async addHearingTime() {
    const dateFrom = DateHelper.getToday();
    const dateTo = DateHelper.addToToday({ days: 1, workingDay: true });
    await this.dateFragment.enterDate(dateFrom, inputs.hearingTime.dateFrom.selectorKey);
    await this.dateFragment.enterDate(dateTo, inputs.hearingTime.dateTo.selectorKey);

    // TODO - work out why this does not work
    await super.retryClickBySelector(radioButtons.hearingTime.other.selector, () =>
      super.expectOptionChecked(radioButtons.hearingTime.other.selector),
    );

    await super.expectText(inputs.hearingTime.otherHours.label);
    await super.expectText(inputs.hearingTime.otherMinutes.label);
    await super.inputText('3', inputs.hearingTime.otherHours.selector);
    await super.inputText('0', inputs.hearingTime.otherMinutes.selector);
    await super.inputText('info about hearing time', inputs.hearingTime.input.selector);
  }

  async removeHearingTime() {
    await super.clickBySelector(checkboxes.hearingTime.selector);
  }

  async addHearingMethod() {
    await super.clickByText(radioButtons.hearingMethod.telephone.label);
  }

  async addHearingNotes() {
    await super.inputText('hearing notes', inputs.hearingNotes.selector);
  }

  async addWelshLanguage() {
    await super.clickBySelector(checkboxes.welshLanguage.selector);
  }

  async addImportantNotes() {
    await super.inputText('Important notes', inputs.importantNotes.selector);
  }

  async addDocuments() {
    await super.inputText('docs input 1', inputs.documents.input1.selector);
    await super.inputText('docs input 2', inputs.documents.input2.selector);
  }

  async addWitnessStatement() {
    await super.inputText(
      'witness statement',
      inputs.witnessStatement.statementOfWitnesses.selector,
    );
  }

  async restrictNumWitnesses() {
    await super.clickBySelector(radioButtons.witnessStatement.restrictNumWitnesses.yes.selector);
    await super.expectLabel(inputs.witnessStatement.numClaimantWitnesses.label);
    await super.expectText(inputs.witnessStatement.numDefendantWitnesses.label);
    await super.inputText('1', inputs.witnessStatement.numClaimantWitnesses.selector);
    await super.inputText('2', inputs.witnessStatement.numDefendantWitnesses.selector);
    await super.inputText(
      'Party can count as witness',
      inputs.witnessStatement.partyIsCountedAsWitnessText.selector,
    );
  }

  async restrictNumPages() {
    await super.clickBySelector(radioButtons.witnessStatement.restrictNumPages.yes.selector);
    await super.expectLabel(inputs.witnessStatement.numPages.label);
    await super.inputText(
      'Each statement should be no more than',
      inputs.witnessStatement.witnessShouldNotMoreThanText.selector,
    );
    await super.inputText('4', inputs.witnessStatement.numPages.selector);
    await super.inputText('font details', inputs.witnessStatement.fontDetails.selector);
  }

  async addNewDirection() {
    await super.clickBySelector(buttons.addNewDirection.selector);
    await super.expectLabel(inputs.newDirection.label);
    await super.inputText('new direction', inputs.newDirection.selector);
  }

  async addFlightDelay() {
    await super.expectSubheading(subheadings.flightDelay);
    await super.expectSubheading(subheadings.relatedClaims);
    await super.expectSubheading(subheadings.legalArguments);

    await super.inputText('Flight delay related claims', inputs.flightDelay.relatedClaim.selector);
    await super.inputText(
      'Flight delay legal arguments',
      inputs.flightDelay.legalArduments.selector,
    );
  }

  async addCreditHire() {
    await super.inputText('Credit hire input 1', inputs.creditHire.input1.selector);
    await super.inputText('Credit hire input 2', inputs.creditHire.input2.selector);
    await super.inputText('Credit hire input 3', inputs.creditHire.input3.selector);
    await super.inputText('Credit hire input 4', inputs.creditHire.input4.selector);
    await super.inputText('Credit hire input 5', inputs.creditHire.input5.selector);
    await super.inputText('Credit hire input 6', inputs.creditHire.input6.selector);
    await super.inputText('Credit hire input 7', inputs.creditHire.input7.selector);
    await super.inputText('Credit hire input 8', inputs.creditHire.input8.selector);

    const date1 = DateHelper.getToday();
    const date2 = DateHelper.addToToday({ days: 1, workingDay: true });
    const date3 = DateHelper.addToToday({ days: 2, workingDay: true });
    const date4 = DateHelper.addToToday({ days: 3, workingDay: true });

    await this.dateFragment.enterDate(date1, inputs.creditHire.date1.selectorKey);
    await this.dateFragment.enterDate(date2, inputs.creditHire.date2.selectorKey);
    await this.dateFragment.enterDate(date3, inputs.creditHire.date3.selectorKey);
    await this.dateFragment.enterDate(date4, inputs.creditHire.date4.selectorKey);
  }

  async addRoadTrafficAccident() {
    await super.expectSubheading(subheadings.roadTrafficAccident);
    await super.inputText('Road Traffic accident', inputs.roadTrafficAccident.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
