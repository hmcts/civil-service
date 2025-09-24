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
import DefaultJudgmentPageFactory
  from "../../../../pages/exui/claimant-defendant-solicitor/default-judgment/default-judgment-page-factory.ts";
import DefaultJudgmentActions from "./default-judgment/default-judgment-actions.ts";
import ClaimantResponseSpecActions from "./claimant-response/claimant-response-spec-actions.ts";
import DefaultJudgmentSpecActions from "./default-judgment/default-judgment-spec-actions.ts";

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
    return new DefaultJudgmentActions(new DefaultJudgmentPageFactory(this.page), this.testData,);
  }

  get defaultJudgementSpecActions() {
    return new DefaultJudgmentSpecActions(new DefaultJudgmentPageFactory(this.page), this.testData,);
  }
}
