import TestData from '../../../../../models/test-utils/test-data.ts';
import { Step } from '../../../../../decorators/test-steps.ts';
import BaseTestData from '../../../../../base/base-test-data.ts';
import EvidenceUploadPageFactory from '../../../../../pages/exui/claimant-defendant-solicitor/evidence-upload/evidence-upload-page-factory.ts';

const classKey = 'EvidenceUploadApplicantActions';
export default class EvidenceUploadApplicantActions extends BaseTestData {
  private evidenceUploadPageFactory: EvidenceUploadPageFactory;

  constructor(evidenceUploadPageFactory: EvidenceUploadPageFactory, testData: TestData) {
    super(testData);
    this.evidenceUploadPageFactory = evidenceUploadPageFactory;
  }

  @Step(classKey)
  async evidenceUpload() {
    const { evidenceUploadPage } = this.evidenceUploadPageFactory;
    await evidenceUploadPage.verifyContent();
    await evidenceUploadPage.submit();
  }

  @Step(classKey)
  async documentSelectionSmallClaim() {
    const { documentSelectionSmallClaimClaimantPage } = this.evidenceUploadPageFactory;
    await documentSelectionSmallClaimClaimantPage.verifyContent();
    await documentSelectionSmallClaimClaimantPage.selectWitnessStatement();
    // await documentSelectionSmallClaimClaimantPage.selectExpertsReport();
    // await documentSelectionSmallClaimClaimantPage.selectAuthorities();
    await documentSelectionSmallClaimClaimantPage.submit();
  }

  @Step(classKey)
  async documentSelectionFastTrack() {
    const { documentSelectionFastTrackClaimantPage } = this.evidenceUploadPageFactory;
    await documentSelectionFastTrackClaimantPage.verifyContent();
    await documentSelectionFastTrackClaimantPage.selectWitnessStatement();
    await documentSelectionFastTrackClaimantPage.submit();
  }

  @Step(classKey)
  async documentUpload() {
    const { documentUploadClaimantPage } = this.evidenceUploadPageFactory;
    await documentUploadClaimantPage.verifyContent();
    await documentUploadClaimantPage.addWitnessStatement();
    // await documentUploadClaimantPage.addExpertsReport();
    // await documentUploadClaimantPage.addAuthorities();
    await documentUploadClaimantPage.submit();
  }

  @Step(classKey)
  async submitEvidenceUpload() {
    const { evidenceUploadSubmitPage } = this.evidenceUploadPageFactory;
    await evidenceUploadSubmitPage.verifyContent();
    await evidenceUploadSubmitPage.submit();
  }

  @Step(classKey)
  async evidenceUploadConfirm() {
    const { evidenceUploadConfirmPage } = this.evidenceUploadPageFactory;
    await evidenceUploadConfirmPage.verifyContent();
    await evidenceUploadConfirmPage.submit();
  }
}
