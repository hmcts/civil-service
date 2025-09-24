import { Page } from '@playwright/test';
import BasePage from '../../../../base/base-page';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import ExuiPage from '../../exui-page/exui-page';
import { radioButtons } from './choose-party-type-content';
import { Party } from '../../../../models/partys';

@AllMethodsStep()
export default class ChoosePartyTypeFragment extends ExuiPage(BasePage) {
  private claimantDefendantParty: Party;

  constructor(page: Page, claimantDefendantParty: Party) {
    super(page);
    this.claimantDefendantParty = claimantDefendantParty;
  }

  async verifyContent() {
    await super.runVerifications(
      [
        super.expectLabel(radioButtons.individual.label),
        super.expectLabel(radioButtons.company.label),
        super.expectLabel(radioButtons.organisation.label),
        super.expectLabel(radioButtons.soleTrader.label),
      ],
      {
        runAxe: false,
      },
    );
  }

  async selectIndivdual() {
    await super.clickBySelector(radioButtons.individual.selector(this.claimantDefendantParty));
  }

  async selectCompany() {
    await super.clickBySelector(radioButtons.company.selector(this.claimantDefendantParty));
  }

  async selectOrganisation() {
    await super.clickBySelector(radioButtons.organisation.selector(this.claimantDefendantParty));
  }

  async selectSoleTrader() {
    await super.clickBySelector(radioButtons.soleTrader.selector(this.claimantDefendantParty));
  }

  async submit() {
    throw new Error('Method not implemented.');
  }
}
