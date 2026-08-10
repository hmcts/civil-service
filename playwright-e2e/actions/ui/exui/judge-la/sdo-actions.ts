import BaseTestData from '../../../../base/base-test-data';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import TestData from '../../../../models/test-utils/test-data';
import SdoPageFactory from '../../../../pages/exui/judge-la/sdo/sdo-page-factory';

@AllMethodsStep()
export default class SdoActions extends BaseTestData {
  private sdoPageFactory: SdoPageFactory;

  constructor(sdoPageFactory: SdoPageFactory, testData: TestData) {
    super(testData);
    this.sdoPageFactory = sdoPageFactory;
  }

  async enterJudgementYes() {
    const { sdoPage } = this.sdoPageFactory;
    await sdoPage.verifyContent(this.ccdCaseData);
    await sdoPage.selectYes();
    await sdoPage.submit();
  }

  async enterJudgementNo() {
    const { sdoPage } = this.sdoPageFactory;
    await sdoPage.verifyContent(this.ccdCaseData);
    await sdoPage.selectNo();
    await sdoPage.submit();
  }

  async selectSmallTrackSum() {
    const { claimsTrackSmallPage } = this.sdoPageFactory;
    await claimsTrackSmallPage.verifyContent(this.ccdCaseData);
    await claimsTrackSmallPage.selectYes();
    await claimsTrackSmallPage.submit();
  }

  async selectSmallTrackDRH() {
    const { claimsTrackSmallPage } = this.sdoPageFactory;
    await claimsTrackSmallPage.verifyContent(this.ccdCaseData);
    await claimsTrackSmallPage.selectYesDisputeResolutionHearing();
    await claimsTrackSmallPage.submit();
  }

  async allocateSmallTrackNo() {
    const { claimsTrackSmallPage } = this.sdoPageFactory;
    await claimsTrackSmallPage.verifyContent(this.ccdCaseData);
    await claimsTrackSmallPage.selectNo();
    await claimsTrackSmallPage.submit();
  }

  async selectFastTrack() {
    const { claimsTrackPage } = this.sdoPageFactory;
    await claimsTrackPage.verifyContent(this.ccdCaseData);
    await claimsTrackPage.selectFastTrack();
    await claimsTrackPage.submit();
  }

  async selectSmallClaimNoSum() {
    const { claimsTrackPage } = this.sdoPageFactory;
    await claimsTrackPage.verifyContent(this.ccdCaseData);
    await claimsTrackPage.selectSmallClaims();
    await claimsTrackPage.submit();
  }

  async selectFastTrackNIHL() {
    const { claimsTrackPage } = this.sdoPageFactory;
    await claimsTrackPage.verifyContent(this.ccdCaseData);
    await claimsTrackPage.selectFastTrack();
    await claimsTrackPage.selectNoiseInducedHearingLoss();
    await claimsTrackPage.submit();
  }

  async orderTypeDisposalHearing() {
    const { orderTypePage } = this.sdoPageFactory;
    await orderTypePage.verifyContent(this.ccdCaseData);
    await orderTypePage.selectDisposalHearing();
    await orderTypePage.submit();
  }

  async orderTypeTrail() {
    const { orderTypePage } = this.sdoPageFactory;
    await orderTypePage.verifyContent(this.ccdCaseData);
    await orderTypePage.selectTrail();
    await orderTypePage.submit();
  }

  async disposalHearingDetails() {
    const { disposalHearingPage } = this.sdoPageFactory;
    await disposalHearingPage.verifyContent(this.ccdCaseData);
    await disposalHearingPage.addHearingTime();
    await disposalHearingPage.submit();
  }

  async smallTrackDetails() {
    const { smallClaimsPage } = this.sdoPageFactory;
    await smallClaimsPage.verifyContent(this.ccdCaseData);
    await smallClaimsPage.removeHearingTime();
    await smallClaimsPage.submit();
  }

  async sdoDRHDetails() {
    const { sdoR2SmallClaimsPage } = this.sdoPageFactory;
    await sdoR2SmallClaimsPage.verifyContent(this.ccdCaseData);
    await sdoR2SmallClaimsPage.submit();
  }

  async fastTrackDetails() {
    const { fastTrackPage } = this.sdoPageFactory;
    await fastTrackPage.verifyContent(this.ccdCaseData);
    await fastTrackPage.addAllocation();
    await fastTrackPage.addHearingTime();
    await fastTrackPage.submit();
  }

  async sdoNIHLDetails() {
    const { sdoR2FastTrackPage } = this.sdoPageFactory;
    await sdoR2FastTrackPage.verifyContent(this.ccdCaseData);
    await sdoR2FastTrackPage.submit();
  }

  async orderPreview() {
    const { orderPreviewSdoPage } = this.sdoPageFactory;
    await orderPreviewSdoPage.verifyContent(this.ccdCaseData);
    await orderPreviewSdoPage.submit();
  }

  async submitSdo() {
    const { submitSdoPage } = this.sdoPageFactory;
    await submitSdoPage.verifyContent(this.ccdCaseData);
    await submitSdoPage.submit();
  }

  async confirmSdo() {
    const { confirmSdoPage } = this.sdoPageFactory;
    await confirmSdoPage.verifyContent(this.ccdCaseData);
    await confirmSdoPage.submit();
  }
}
