import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd/ccd-case-data';
import ExuiPage from '../../../exui-page/exui-page';
import { checkboxes, radioButtons } from './claims-track-content';

@AllMethodsStep()
export default class ClaimsTrackPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData): Promise<void> {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLegend(radioButtons.claimsTrack.label),
      super.expectLabel(radioButtons.claimsTrack.smallClaims.label),
      super.expectLabel(radioButtons.claimsTrack.fast.label),
    ]);
  }

  async selectSmallClaims() {
    await super.clickBySelector(radioButtons.claimsTrack.smallClaims.selector);
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

  async selectSmallTrackAdditionalDirections() {
    await super.clickBySelector(checkboxes.fastTrack.buildingDispute.selector);
    await super.clickBySelector(checkboxes.fastTrack.clinicialNegligence.selector);
    await super.clickBySelector(checkboxes.fastTrack.creditHire.selector);
    await super.clickBySelector(checkboxes.fastTrack.employersLiability.selector);
    await super.clickBySelector(checkboxes.fastTrack.housingDisrepair.selector);
    await super.clickBySelector(checkboxes.fastTrack.personalInjury.selector);
    await super.clickBySelector(checkboxes.fastTrack.roadTrafficAccident.selector);
  }

  async selectFastTrack() {
    await super.clickBySelector(radioButtons.claimsTrack.fast.selector);
    await Promise.all([
      super.expectLabel(checkboxes.fastTrack.label),
      super.expectLabel(checkboxes.fastTrack.buildingDispute.label),
      super.expectLabel(checkboxes.fastTrack.clinicialNegligence.label),
      super.expectLabel(checkboxes.fastTrack.creditHire.label, { count: 1 }),
      super.expectLabel(checkboxes.fastTrack.employersLiability.label),
      super.expectLabel(checkboxes.fastTrack.housingDisrepair.label),
      super.expectLabel(checkboxes.fastTrack.noiseInducedHearingLoss.label),
      super.expectLabel(checkboxes.fastTrack.personalInjury.label),
      super.expectLabel(checkboxes.fastTrack.roadTrafficAccident.label, { count: 1 }),
    ]);
  }

  async selectFastTrackAdditionalDirections() {
    await super.clickBySelector(checkboxes.fastTrack.buildingDispute.selector);
    await super.clickBySelector(checkboxes.fastTrack.clinicialNegligence.selector);
    await super.clickBySelector(checkboxes.fastTrack.creditHire.selector);
    await super.clickBySelector(checkboxes.fastTrack.employersLiability.selector);
    await super.clickBySelector(checkboxes.fastTrack.housingDisrepair.selector);
    await super.clickBySelector(checkboxes.fastTrack.personalInjury.selector);
    await super.clickBySelector(checkboxes.fastTrack.roadTrafficAccident.selector);
  }

  async selectNoiseInducedHearingLoss() {
    await super.clickBySelector(checkboxes.fastTrack.noiseInducedHearingLoss.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
