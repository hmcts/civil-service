import TestData from '../../../../../models/test-utils/test-data.ts';
import { Step } from '../../../../../decorators/test-steps.ts';
import BaseTestData from '../../../../../base/base-test-data.ts';
import EvidenceUploadPageFactory from '../../../../../pages/exui/claimant-defendant-solicitor/evidence-upload/evidence-upload-page-factory.ts';

const classKey = 'EvidenceUploadRespondentActions';
export default class EvidenceUploadRespondentActions extends BaseTestData {
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
    const { documentSelectionSmallClaimDS1Page } = this.evidenceUploadPageFactory;
    await documentSelectionSmallClaimDS1Page.verifyContent();
    // await documentSelectionSmallClaimDS1Page.selectWitnessStatement();
    await documentSelectionSmallClaimDS1Page.selectExpertsReport();
    // await documentSelectionSmallClaimDS1Page.selectAuthorities();
    await documentSelectionSmallClaimDS1Page.submit();
  }

  @Step(classKey)
  async documentSelectionFastTrack() {
    const { documentSelectionFastTrackDS1Page } = this.evidenceUploadPageFactory;
    await documentSelectionFastTrackDS1Page.verifyContent();
    // await documentSelectionFastTrackDS1Page.selectWitnessStatement();
    await documentSelectionFastTrackDS1Page.selectExpertsReport();
    // await documentSelectionFastTrackDS1Page.selectAuthorities();
    await documentSelectionFastTrackDS1Page.submit();
  }

  @Step(classKey)
  async documentUpload() {
    const { documentUploadDS1Page } = this.evidenceUploadPageFactory;
    await documentUploadDS1Page.verifyContent();
    // await documentUploadDS1Page.addWitnessStatement();
    await documentUploadDS1Page.addExpertsReport();
    // await documentUploadDS1Page.addAuthorities();
    await documentUploadDS1Page.submit();
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
