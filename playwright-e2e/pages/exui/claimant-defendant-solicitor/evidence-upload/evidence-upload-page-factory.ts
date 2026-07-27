import BasePageFactory from '../../../../base/base-page-factory.ts';
import EvidenceUploadPage from './evidence-upload/evidence-upload-page.ts';
import DocumentSelectionSmallClaimPage from './document-selection-small-claim/document-selection-small-claim-page.ts';
import DocumentSelectionFastTrackPage from './document-selection-fast-track/document-selection-fast-track-page.ts';
import DocumentUploadPage from './document-upload/document-upload-page.ts';
import EvidenceUploadSubmitPage from './evidence-upload-submit/evidence-upload-applicant-submit-page.ts';
import EvidenceUploadConfirmPage from './evidence-upload-confirm/evidence-upload-confirm-page.ts';
import partys from '../../../../constants/users/partys.ts';
import DateFragment from '../../fragments/date/date-fragment.ts';

export default class EvidenceUploadPageFactory extends BasePageFactory {
  get evidenceUploadPage() {
    return new EvidenceUploadPage(this.page);
  }
  get documentSelectionSmallClaimClaimantPage() {
    return new DocumentSelectionSmallClaimPage(this.page, partys.CLAIMANT_1);
  }

  get documentSelectionSmallClaimDS1Page() {
    return new DocumentSelectionSmallClaimPage(this.page, partys.DEFENDANT_SOLICITOR_1);
  }

  get documentSelectionFastTrackClaimantPage() {
    return new DocumentSelectionFastTrackPage(this.page, partys.CLAIMANT_1);
  }

  get documentSelectionFastTrackDS1Page() {
    return new DocumentSelectionFastTrackPage(this.page, partys.DEFENDANT_SOLICITOR_1);
  }

  get documentUploadClaimantPage() {
    const dataFragment = new DateFragment(this.page);
    return new DocumentUploadPage(this.page, dataFragment, partys.CLAIMANT_1, partys.CLAIMANT_WITNESS_1, partys.CLAIMANT_EXPERT_1);
  }

  get documentUploadDS1Page() {
    const dataFragment = new DateFragment(this.page);
    return new DocumentUploadPage(this.page, dataFragment, partys.DEFENDANT_SOLICITOR_1, partys.DEFENDANT_1_WITNESS_1, partys.DEFENDANT_1_EXPERT_1);
  }

  get evidenceUploadSubmitPage() {
    return new EvidenceUploadSubmitPage(this.page);
  }
  get evidenceUploadConfirmPage() {
    return new EvidenceUploadConfirmPage(this.page);
  }
}
