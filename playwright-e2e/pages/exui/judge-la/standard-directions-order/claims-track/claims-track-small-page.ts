import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd/ccd-case-data';
import ExuiPage from '../../../exui-page/exui-page';
import { checkboxes, radioButtons } from './claims-track-content';

@AllMethodsStep()
export default class DrawDirectionsOrderClaimsTrackPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData): Promise<void> {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLegend(radioButtons.smallTrack.label),
      super.expectRadioYesLabel(radioButtons.smallTrack.yes.selector),
      super.expectRadioNoLabel(radioButtons.smallTrack.no.selector),
    ]);
  }

  async selectYes() {
    await super.clickBySelector(radioButtons.smallTrack.yes.selector);
    await Promise.all([
      super.expectLegend(checkboxes.smallClaims.label, { count: 1 }),
      super.expectLabel(checkboxes.smallClaims.creditHire.label, { count: 1 }),
      super.expectLabel(checkboxes.smallClaims.roadTrafficAccident.label, { count: 1 }),
      super.expectLabel(checkboxes.smallClaims.disputeResolutionHearing.label, { count: 1 }),
      super.expectLabel(checkboxes.smallClaims.flightDelay.label, { count: 1 }),
    ]);
    await super.clickBySelector(checkboxes.smallClaims.creditHire.selector);
    await super.clickBySelector(checkboxes.smallClaims.roadTrafficAccident.selector);
    await super.clickBySelector(checkboxes.smallClaims.disputeResolutionHearing.selector);
    await super.clickBySelector(checkboxes.smallClaims.flightDelay.selector);
  }

  async selectNo() {
    await super.clickBySelector(radioButtons.smallTrack.no.selector);
  }

  async submit(...args: any[]): Promise<void> {
    await super.retryClickSubmit();
  }
}
