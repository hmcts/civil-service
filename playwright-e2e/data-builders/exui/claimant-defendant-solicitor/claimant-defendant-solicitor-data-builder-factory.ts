import BaseDataBuilderFactory from '../../../base/base-data-builder-factory';
import AcknowledgeClaimDataBuilder from './acknowledge-claim/unspec/acknowledge-claim-data-builder';
import AddDefendantLitigationFriendDataBuilder from './add-defendant-litigation-friend/unspec/add-defendant-litigation-friend-data-builder';
import AddOrAmendClaimDocumentsDataBuilder from './add-or-amend-claim-documents/unspec/add-or-amend-claim-documents-data-builder';
import ClaimantResponseDataBuilder from './claimant-response/unspec/claimant-response-data-builder';
import ClaimantResponseSpecDataBuilder from './claimant-response/lr-spec/claimant-response-spec-data-builder';
import DefendantResponseDataBuilder from './defendant-response/unspec/defendant-response-data-builder';
import DefendantResponseSpecDataBuilder from './defendant-response/lr-spec/defendant-response-spec-data-builder';
import EvidenceUploadApplicantDataBuilder from './evidence-upload-applicant/evidence-upload-applicant-data-builder';
import EvidenceUploadRespondentDataBuilder from './evidence-upload-respondent/evidence-upload-respondent-data-builder';
import CreateClaimSpecDataBuilder from './create-claim/lr-spec/create-claim-spec-data-builder';
import CreateClaimDataBuilder from './create-claim/unspec/create-claim-data-builder';
import InformAgreedExtensionDateDataBuilder from './inform-agreed-extension-date/unspec/inform-agreed-extension-date-data-builder';
import InformAgreedExtensionDateSpecDataBuilder from './inform-agreed-extension-date/lr-spec/inform-agreed-extension-date-spec-data-builder';
import ManageContactInformationDataBuilder from '../common/manage-contact-information/manage-contact-information-data-builder';
import NotifyClaimDataBuilder from './notify-claim/unspec/notify-claim-data-builder';
import NotifyClaimDetailsDataBuilder from './notify-claim-details/unspec/notify-claim-details-data-builder';
import ServiceRequestDataBuilder from './service-request/service-request-data-builder';
import DefaultJudgementDataBuilder from './default-judgement/unspec/default-judgement-data-builder.ts';
import DefaultJudgementSpecDataBuilder from './default-judgement/lr-spec/default-judgement-spec-data-builder.ts';
import UploadMediationDocumentsDataBuilder from './upload-mediation-documents/lr-spec/upload-mediation-documents-data-builder';

export default class ClaimantDefendantSolicitorDataBuilderFactory extends BaseDataBuilderFactory {
  get acknowledgeClaimDataBuilder() {
    return new AcknowledgeClaimDataBuilder(this.requestsFactory, this.testData);
  }

  get addOrAmendClaimDocumentsDataBuilder() {
    return new AddOrAmendClaimDocumentsDataBuilder(this.requestsFactory, this.testData);
  }

  get addDefendantLitigationFriendDataBuilder() {
    return new AddDefendantLitigationFriendDataBuilder(this.requestsFactory, this.testData);
  }

  get createClaimDataBuilder() {
    return new CreateClaimDataBuilder(this.requestsFactory, this.testData);
  }

  get createClaimSpecDataBuilder() {
    return new CreateClaimSpecDataBuilder(this.requestsFactory, this.testData);
  }

  get notifyClaimDataBuilder() {
    return new NotifyClaimDataBuilder(this.requestsFactory, this.testData);
  }

  get notifyClaimDetailsDataBuilder() {
    return new NotifyClaimDetailsDataBuilder(this.requestsFactory, this.testData);
  }

  get defendantResponseDataBuilder() {
    return new DefendantResponseDataBuilder(this.requestsFactory, this.testData);
  }

  get defendantResponseSpecDataBuilder() {
    return new DefendantResponseSpecDataBuilder(this.requestsFactory, this.testData);
  }

  get evidenceUploadApplicantDataBuilder() {
    return new EvidenceUploadApplicantDataBuilder(this.requestsFactory, this.testData);
  }

  get evidenceUploadRespondentDataBuilder() {
    return new EvidenceUploadRespondentDataBuilder(this.requestsFactory, this.testData);
  }

  get claimantResponseDataBuilder() {
    return new ClaimantResponseDataBuilder(this.requestsFactory, this.testData);
  }

  get claimantResponseSpecDataBuilder() {
    return new ClaimantResponseSpecDataBuilder(this.requestsFactory, this.testData);
  }

  get informAgreedExtensionDateDataBuilder() {
    return new InformAgreedExtensionDateDataBuilder(this.requestsFactory, this.testData);
  }

  get informAgreedExtensionDateSpecDataBuilder() {
    return new InformAgreedExtensionDateSpecDataBuilder(this.requestsFactory, this.testData);
  }

  get manageContactInformationDataBuilder() {
    return new ManageContactInformationDataBuilder(this.requestsFactory, this.testData);
  }

  get serviceRequestDataBuilder() {
    return new ServiceRequestDataBuilder(this.requestsFactory, this.testData);
  }

  get defaultJudgementDataBuilder() {
    return new DefaultJudgementDataBuilder(this.requestsFactory, this.testData);
  }

  get defaultJudgementSpecDataBuilder() {
    return new DefaultJudgementSpecDataBuilder(this.requestsFactory, this.testData);
  }

  get uploadMediationDocumentsDataBuilder() {
    return new UploadMediationDocumentsDataBuilder(this.requestsFactory, this.testData);
  }
}
