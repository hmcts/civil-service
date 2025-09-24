import { Page } from '@playwright/test';
import BasePage from '../../../../base/base-page';
import filePaths from '../../../../config/file-paths';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import DateHelper from '../../../../helpers/date-helper';
import ExuiPage from '../../exui-page/exui-page';
import {
  heading,
  inputs,
  buttons,
  checkboxes,
  dropdowns,
  radioButtons,
} from './certificate-of-service-notify-claim-content';
import { Party } from '../../../../models/partys';
import DateFragment from '../date/date-fragment';

@AllMethodsStep()
export default class CertificateOfServiceNotifyClaimFragment extends ExuiPage(BasePage) {
  private dateFragment: DateFragment;
  private defendantParty: Party;

  constructor(page: Page, defendantParty: Party) {
    super(page);
    this.dateFragment = new DateFragment(page);
    this.defendantParty = defendantParty;
  }

  async verifyContent() {
    await super.runVerifications(
      [
        super.expectHeading(heading(this.defendantParty)),
        super.expectLegend(inputs.dateDeemedServed.label),
        super.expectLegend(inputs.dateDeemedServed.label),
        super.expectLabel(inputs.statementOfTruth.firm.label),
        super.expectLabel(inputs.documentsServed.label),
        super.expectLabel(inputs.statementOfTruth.name.label),
        super.expectLabel(inputs.notifyClaimRecipient.label),
        super.expectLabel(dropdowns.locationType.label),
        super.expectLabel(dropdowns.serveType.label),
        super.expectLabel(radioButtons.docsServed.litigationFriend.label),
        super.expectLabel(radioButtons.docsServed.defendant.label),
        super.expectLabel(radioButtons.docsServed.litigationFriend.label),
        super.expectLabel(radioButtons.docsServed.solicitor.label),
        super.expectText(checkboxes.signedTrue.label, { count: 1 }),
      ],
      { runAxe: false },
    );
  }

  async fillCertificateOfService() {
    let dateDeemedServed: Date;
    let dateOfService: Date;

    if (this.defendantParty.number === 1) {
      dateDeemedServed = DateHelper.addToToday({ days: 2, workingDay: true });
      dateOfService = DateHelper.getToday();
    } else {
      dateDeemedServed = DateHelper.subtractFromToday({ days: 14, addDayAfter4pm: true });
      dateOfService = DateHelper.subtractFromToday({
        days: 14,
        workingDay: true,
        addDayAfter4pm: true,
      });
    }

    await this.dateFragment.enterDate(dateDeemedServed, inputs.dateDeemedServed.selectorKey);
    await this.dateFragment.enterDate(dateOfService, inputs.dateOfService.selectorKey);

    await super.inputText(
      `Test Documents - ${this.defendantParty.key}`,
      inputs.documentsServed.selector(this.defendantParty),
    );
    await super.inputText(
      `${this.defendantParty.key}`,
      inputs.notifyClaimRecipient.selector(this.defendantParty),
    );
    await super.selectFromDropdown(
      dropdowns.locationType.options[this.defendantParty.number - 1],
      dropdowns.locationType.selector(this.defendantParty),
    );
    await super.inputText(
      `Test Address - ${this.defendantParty.key}`,
      inputs.documentsServedLocation.selector(this.defendantParty),
    );
    await super.clickBySelector(radioButtons.docsServed.claimant.selector(this.defendantParty));
    await super.selectFromDropdown(
      dropdowns.serveType.options[this.defendantParty.number - 1],
      dropdowns.serveType.selector(this.defendantParty),
    );
  }

  async uploadSupportingEvidence() {
    await super.clickBySelector(buttons.addNewSupportingEvidence.selector(this.defendantParty));
    await super.retryUploadFile(
      filePaths.testPdfFile,
      inputs.evidenceDocument.selector(this.defendantParty),
    );
  }

  async fillStatementOfTruth() {
    await super.inputText(
      `Name - ${this.defendantParty.key}`,
      inputs.statementOfTruth.name.selector(this.defendantParty),
    );
    await super.inputText(
      `Law firm - ${this.defendantParty.key}`,
      inputs.statementOfTruth.firm.selector(this.defendantParty),
    );
    await super.clickBySelector(checkboxes.signedTrue.selector(this.defendantParty));
  }

  async submit() {
    throw new Error('Method not implemented.');
  }
}
