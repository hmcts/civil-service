import BasePageActionsFactory from '../../../../base/base-page-actions-factory';
import CreateClaimPageFactory from '../../../../pages/exui/claimant-defendant-solicitor/create-claim/create-claim-page-factory';
import NotifyClaimDetailsPageFactory from '../../../../pages/exui/claimant-defendant-solicitor/notify-claim-details/notify-claim-details-page-factory';
import NotifyClaimPageFactory from '../../../../pages/exui/claimant-defendant-solicitor/notify-claim/notify-claim-page-factory';
import ClaimantResponsePageFactory from '../../../../pages/exui/claimant-defendant-solicitor/response/claimant-response/claimant-response-page-factory';
import ClaimantResponseActions from './claimant-response/claimant-response-actions';
import CreateClaimSpecActions from './create-claim/create-claim-spec-actions';
import CreateClaimActions from './create-claim/create-claim-actions';
import NotifyClaimActions from './notify-claim-actions';
import NotifyClaimDetailsActions from './notify-claim-details-actions';
import DefaultJudgmentPageFactory from '../../../../pages/exui/claimant-defendant-solicitor/default-judgment/default-judgment-page-factory.ts';
import DefaultJudgmentActions from './default-judgment/default-judgment-actions.ts';
import ClaimantResponseSpecActions from './claimant-response/claimant-response-spec-actions.ts';
import DefaultJudgmentSpecActions from './default-judgment/default-judgment-spec-actions.ts';
import EvidenceUploadApplicantActions from './evidence-upload/evidence-upload-actions.ts';
import EvidenceUploadPageFactory from '../../../../pages/exui/claimant-defendant-solicitor/evidence-upload/evidence-upload-page-factory.ts';
import DiscontinueClaimClaimantActions from './discontinue-claim-claimant/discontinue-claim-claimant-actions.ts';
import DiscontinueClaimClaimantPageFactory from '../../../../pages/exui/claimant-defendant-solicitor/discontinue-claim/discontinue-claim-claimant-page-factory.ts';
import RequestForReconsiderationActions from './request-for-reconsideration-actions';
import RequestForReconsiderationPageFactory from '../../../../pages/exui/claimant-defendant-solicitor/request-for-reconsideration/request-for-reconsideration-page-factory';
import SettleClaimMarkPaidFullActions from './settle-claim-mark-paid-full-actions';
import SettleClaimMarkPaidFullPageFactory from '../../../../pages/exui/claimant-defendant-solicitor/settle-claim-mark-paid-full/settle-claim-mark-paid-full-page-factory.ts';

export default class ClaimantSolicitorActionsFactory extends BasePageActionsFactory {
  get createClaimActions() {
    return new CreateClaimActions(new CreateClaimPageFactory(this.page), this.testData);
  }

  get createClaimSpecActions() {
    return new CreateClaimSpecActions(new CreateClaimPageFactory(this.page), this.testData);
  }

  get notifyClaimActions() {
    return new NotifyClaimActions(new NotifyClaimPageFactory(this.page), this.testData);
  }

  get notifyClaimDetailsActions() {
    return new NotifyClaimDetailsActions(
      new NotifyClaimDetailsPageFactory(this.page),
      this.testData,
    );
  }

  get claimantResponseActions() {
    return new ClaimantResponseActions(new ClaimantResponsePageFactory(this.page), this.testData);
  }

  get claimantResponseSpecActions() {
    return new ClaimantResponseSpecActions(
      new ClaimantResponsePageFactory(this.page),
      this.testData,
    );
  }

  get defaultJudgementActions() {
    return new DefaultJudgmentActions(new DefaultJudgmentPageFactory(this.page), this.testData);
  }

  get defaultJudgementSpecActions() {
    return new DefaultJudgmentSpecActions(new DefaultJudgmentPageFactory(this.page), this.testData);
  }

  get evidenceUploadApplicantActions() {
    return new EvidenceUploadApplicantActions(
      new EvidenceUploadPageFactory(this.page),
      this.testData,
    );
  }

  get discontinueClaimClaimantActions() {
    return new DiscontinueClaimClaimantActions(
      new DiscontinueClaimClaimantPageFactory(this.page),
      this.testData,
    );
  }

  get requestForReconsiderationActions() {
    return new RequestForReconsiderationActions(
      new RequestForReconsiderationPageFactory(this.page),
      this.testData,
    );
  }

  get settleClaimMarkPaidFullActions() {
    return new SettleClaimMarkPaidFullActions(
      new SettleClaimMarkPaidFullPageFactory(this.page),
      this.testData,
    );
  }
}
