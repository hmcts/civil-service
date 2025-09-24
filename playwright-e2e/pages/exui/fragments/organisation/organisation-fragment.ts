import { Page } from '@playwright/test';
import BasePage from '../../../../base/base-page';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import { Party } from '../../../../models/partys';
import ExuiPage from '../../exui-page/exui-page';
import { inputs, subheadings, links } from './organisation-content';

@AllMethodsStep()
export default class OrganisationFragment extends ExuiPage(BasePage) {
  private claimantDefendantParty: Party;

  constructor(page: Page, claimantDefendantParty: Party) {
    super(page);
    this.claimantDefendantParty = claimantDefendantParty;
  }

  async verifyContent() {
    await super.runVerifications(
      [
        super.expectLabel(inputs.organisationReference.label),
        super.expectSubheading(subheadings.organisations),
        super.expectLabel(inputs.search.label),
        super.expectSubheading(subheadings.search),
      ],
      {
        runAxe: false,
      },
    );
  }

  async enterReference() {
    await super.inputText(
      `${this.claimantDefendantParty.key} Org Ref`,
      inputs.organisationReference.selector(this.claimantDefendantParty),
    );
  }

  async searchForOrganisation(organisationName: string) {
    await super.inputText(organisationName, inputs.search.selector);
    await super.expectText(organisationName);
    await super.clickBySelector(links.selectOrganisation.selector(organisationName));
  }

  async submit() {
    throw new Error('Method not implemented.');
  }
}
