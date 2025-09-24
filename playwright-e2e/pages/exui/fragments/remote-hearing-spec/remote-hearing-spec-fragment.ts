import { Page } from '@playwright/test';
import BasePage from '../../../../base/base-page';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import { Party } from '../../../../models/partys';
import ExuiPage from '../../exui-page/exui-page';
import { radioButtons, inputs, subheadings } from './remote-hearing-spec-content';

@AllMethodsStep()
export default class RemoteHearingSpecFragment extends ExuiPage(BasePage) {
  private claimantDefendantParty: Party;

  constructor(page: Page, claimantDefendantParty: Party) {
    super(page);
    this.claimantDefendantParty = claimantDefendantParty;
  }

  async verifyContent() {
    await super.runVerifications(
      [
        super.expectSubheading(subheadings.remoteHearing, { count: 1 }),
        super.expectText(radioButtons.remoteHearing.label, { count: 1 }),
        super.expectText(radioButtons.remoteHearing.hintText, { count: 1 }),
      ],
      {
        runAxe: false,
      },
    );
  }

  async selectYes() {
    await super.clickBySelector(
      radioButtons.remoteHearing.yes.selector(this.claimantDefendantParty),
    );
    await super.inputText(
      `Court location reason - ${this.claimantDefendantParty.key}`,
      inputs.remoteHearingReason.selector(this.claimantDefendantParty),
    );
  }

  async selectNo() {
    await super.clickBySelector(
      radioButtons.remoteHearing.no.selector(this.claimantDefendantParty),
    );
  }

  async submit() {
    throw new Error('Method not implemented.');
  }
}
