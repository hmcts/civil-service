import { Page } from '@playwright/test';
import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import DateHelper from '../../../../../helpers/date-helper';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import DateFragment from '../../../fragments/date/date-fragment';
import {
  heading,
  buttons,
  checkboxes,
  inputs,
  radioButtons,
  subheadings,
  containers,
} from './sdo-r2-small-claims-content';
import { getFormattedCaseId } from '../../../mixin-pages/exui-page/exui-content';

@AllMethodsStep()
export default class SdoR2SmallClaimsPage extends ExuiPage(BasePage) {
  dateFragment: DateFragment;

  constructor(page: Page, dateFragment: DateFragment) {
    super(page);
    this.dateFragment = dateFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData): Promise<void> {
    await super.runVerifications([
      super.expectHeading(heading),
      super.expectHeading(getFormattedCaseId(ccdCaseData.id!), {exact: false}),
      super.expectHeading(ccdCaseData.caseNamePublic!, {exact: false}),
      super.expectSubheading(subheadings.warning, {headingLevel: 3}),
      super.expectSubheading(subheadings.judgesRecital, {headingLevel: 3}),
      super.expectSubheading(subheadings.allocation, {headingLevel: 3}),
      super.expectSubheading(subheadings.disputeResolutionHearing, {headingLevel: 3}),
      super.expectSubheading(subheadings.legalReprentationForDRH, {headingLevel: 4}),
      super.expectSubheading(subheadings.judgePowersAtDRH, {headingLevel: 4}),
      super.expectSubheading(subheadings.paymentProtectionInsurance, {headingLevel: 3}),
      super.expectSubheading(subheadings.witnessStatements, {headingLevel: 3}),
      super.expectSubheading(subheadings.uploadOfDocuments, {headingLevel: 3}),
      super.expectSubheading(subheadings.addNewDirection),
      super.expectSubheading(subheadings.hearing, {headingLevel: 3}),
      super.expectSubheading(subheadings.welshLanguage, {headingLevel: 3}),
      super.expectSubheading(subheadings.importantNotes, {headingLevel: 3}),
    ]);
  }

  async addPaymentProtectionInsurance() {
    await super.clickBySelector(checkboxes.includePaymentProtectionInsurance.selector);
    await super.expectText(inputs.ppiDate.label);

    const ppiDate = DateHelper.addToToday({ days: 1, workingDay: true });
    await this.dateFragment.enterDate(ppiDate, inputs.ppiDate.selectorKey);
  }

  async addWitnessStatements() {
    await super.expectLabel(inputs.witnessStatements.statementOfWtinesses.label);
    await super.expectText(radioButtons.witnessStatements.restrictNumWitnesses.label);
    await super.expectText(radioButtons.witnessStatements.restrictNumPages.label);

    await super.inputText(
      'witness statement',
      inputs.witnessStatements.statementOfWtinesses.selector,
    );
    await this.dateFragment.enterDate(
      DateHelper.addToToday({ days: 28, workingDay: true }),
      inputs.witnessStatements.deadlineDate.selectorKey,
    );
  }

  async restrictNumWitnesses() {
    await super.clickBySelector(radioButtons.witnessStatements.restrictNumWitnesses.yes.selector);
    await super.expectLabel(inputs.witnessStatements.numClaimantWitnesses.label);
    await super.expectLabel(inputs.witnessStatements.numDefendantWitnesses.label);

    await super.inputText('1', inputs.witnessStatements.numClaimantWitnesses.selector);
    await super.inputText('2', inputs.witnessStatements.numDefendantWitnesses.selector);
    await super.inputText(
      'Party is not included',
      inputs.witnessStatements.partyIsCountedAsWitnessText.selector,
    );
  }

  async restrictNumPages() {
    await super.clickBySelector(radioButtons.witnessStatements.restrictNumPages.yes.selector);
    await super.expectLabel(inputs.witnessStatements.numPages.label);
    await super.inputText(
      'witness statement must be no more than',
      inputs.witnessStatements.witnessShouldNotMoreThanText.selector,
    );
    await super.inputText('3', inputs.witnessStatements.numPages.selector);
    await super.inputText('font details', inputs.witnessStatements.fontDetails.selector);
  }

  async addUploadOfDocuments() {
    await super.expectText(inputs.uploadOfDocuments.label);
    await super.inputText(
      'Make sure to upload the files as pdfs',
      inputs.uploadOfDocuments.description.selector,
    );
  }

  async addNewDirection() {
    await super.clickBySelector(buttons.addNewDirection.selector);
    await super.expectLabel(inputs.newDirection.label);
    await super.inputText('New direction', inputs.newDirection.selector);
  }

  async addHearing() {
    const listFrom = DateHelper.addToToday({ days: 1, workingDay: true });
    const dateTo = DateHelper.addToToday({ days: 2, workingDay: true });
    await super.clickBySelector(radioButtons.hearing.trialOnOptions.hearingWindow.selector);
    await super.expectText(inputs.hearing.dateTo.label);
    await this.dateFragment.enterDate(listFrom, inputs.hearing.listFrom.selectorKey, {
      containerSelector: containers.sdoR2SmallClaimsHearing.selector,
    });
    await this.dateFragment.enterDate(listFrom, inputs.hearing.listFrom.selectorKey);
    await this.dateFragment.enterDate(listFrom, inputs.hearing.dateTo.selectorKey);

    await super.clickBySelector(radioButtons.hearing.lengthOfHearing.other.selector);
    await super.expectLabel(inputs.hearing.lengthOfHearing.days.label);
    await super.expectLabel(inputs.hearing.lengthOfHearing.hours.label);
    await super.expectLabel(inputs.hearing.lengthOfHearing.minutes.label);
    await super.inputText('3', inputs.hearing.lengthOfHearing.days.selector);
    await super.inputText('5', inputs.hearing.lengthOfHearing.hours.selector);
    await super.inputText('0', inputs.hearing.lengthOfHearing.minutes.selector);

    await super.clickByText(radioButtons.hearing.methodOfHearing.telephone.label);

    await super.inputText('bundle documents', inputs.hearing.bundleOfDocuments.selector);

    await super.inputText('hearing notes', inputs.hearing.hearingNotes.selector);
  }

  async addUseOfWelshLanguage() {
    await super.clickBySelector(checkboxes.includeWelshLanguage.selector);
  }

  async addImportantNotes() {
    const date = DateHelper.addToToday({ days: 3, workingDay: true });
    await super.inputText('important notes', inputs.importantNotes.notes.selector);
    await this.dateFragment.enterDate(date, inputs.importantNotes.date.selectorKey);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
