import { Page } from '@playwright/test';
import BasePage from '../../../../base/base-page';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import { Party } from '../../../../models/partys';
import ExuiPage from '../../exui-page/exui-page';
import { inputs } from './solicitor-reference-content';

@AllMethodsStep()
export default class SolicitorReferenceFragment extends ExuiPage(BasePage) {
  private claimantDefendantParty: Party;
  private solicitorParty: Party;

  constructor(page: Page, claimantDefendantParty: Party, solicitorParty: Party) {
    super(page);
    this.claimantDefendantParty = claimantDefendantParty;
    this.solicitorParty = solicitorParty;
  }

  async verifyContent() {
    await super.runVerifications(
      [
        super.expectLabel(inputs.reference.label(this.claimantDefendantParty), {
          count: 1,
        }),
      ],
      {
        runAxe: false,
      },
    );
  }

  async enterReference() {
    await super.inputText(
      `Solicitor Reference - ${this.claimantDefendantParty.key}`,
      inputs.reference.selector(this.solicitorParty),
    );
  }

  async submit() {
    throw new Error('Method not implemented.');
  }
}
