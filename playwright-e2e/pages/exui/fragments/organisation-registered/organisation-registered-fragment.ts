import { Page } from '@playwright/test';
import BasePage from '../../../../base/base-page';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import { Party } from '../../../../models/partys';
import ExuiPage from '../../exui-page/exui-page';
import { radioButtons } from './organisation-registered-content';

@AllMethodsStep()
export default class OrganisationRegisteredFragment extends ExuiPage(BasePage) {
  private defendantParty: Party;

  constructor(page: Page, defendantParty: Party) {
    super(page);
    this.defendantParty = defendantParty;
  }

  async verifyContent() {
    await super.runVerifications([super.expectText(radioButtons.organisationRegistered.label)], {
      runAxe: false,
    });
  }

  async selectYes() {
    await super.clickBySelector(
      radioButtons.organisationRegistered.yes.selector(this.defendantParty),
    );
  }

  async selectNo() {
    await super.clickBySelector(
      radioButtons.organisationRegistered.no.selector(this.defendantParty),
    );
  }

  async submit() {
    throw new Error('Method not implemented.');
  }
}
