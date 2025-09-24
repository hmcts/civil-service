import { Page } from '@playwright/test';
import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import RemoteHearingFragment from '../../../../fragments/remote-hearing/remote-hearing-fragment';
import { dropdowns, subheadings, inputs } from './court-content';

@AllMethodsStep()
export default class CourtPage extends ExuiPage(BasePage) {
  private remoteHearingFragment: RemoteHearingFragment;

  constructor(page: Page, remoteHearingFragment: RemoteHearingFragment) {
    super(page);
    this.remoteHearingFragment = remoteHearingFragment;
  }

  async verifyContent() {
    await super.runVerifications([
      super.verifyHeadings(),
      super.expectSubheading(subheadings.courtLocationCode),
      super.expectLabel(dropdowns.courtLocation.label),
      super.expectLabel(inputs.courtReason.label, { count: 1 }),
      this.remoteHearingFragment.verifyContent(),
    ]);
  }

  async chooseCourtLocation() {
    await super.selectFromDropdown(
      dropdowns.courtLocation.options.london,
      dropdowns.courtLocation.selector,
    );
    await super.inputText('No reason for claimant court location', inputs.courtReason.selector);
  }

  async selectNoForRemoteHearing() {
    await this.remoteHearingFragment.selectNo();
  }

  async selectYesForRemoteHearing() {
    await this.remoteHearingFragment.selectYes();
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
