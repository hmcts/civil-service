import BasePageActionsFactory from '../../../../base/base-page-actions-factory';
import CaseProceedsInCasemanActions from '../caseworker/case-proceeds-in-caseman-actions.ts';
import CreateCaseFlagsPageFactory from '../../../../pages/exui/hearing-center-admin/create-case-flags/create-case-flags-page-factory';
import ManageStayPageFactory from '../../../../pages/exui/hearing-center-admin/manage-stay/manage-stay-page-factory';
import CaseProceedsInCasemanPageFactory from '../../../../pages/exui/caseworker/case-proceeds-in-caseman/case-proceeds-in-caseman-page-factory.ts';
import ManageCaseFlagsPageFactory from '../../../../pages/exui/hearing-center-admin/manage-case-flags/manage-case-flags-page-factory';
import StayCasePageFactory from '../../../../pages/exui/hearing-center-admin/stay-case/stay-case-page-factory';
import CreateCaseFlagsActions from './create-case-flags/create-case-flags-actions';
import CreateCaseFlagsSpecActions from './create-case-flags/create-case-flags-spec-actions';
import ManageStayActions from './manage-stay-actions';
import ManageCaseFlagsActions from './manage-case-flags-actions';
import StayCaseActions from './stay-case-actions';
import ReferJudgeDefenceReceivedActions from './refer-judge-defence-received/refer-judge-defence-received-actions.ts';
import ReferJudgeDefenceReceivedPageFactory from '../../../../pages/exui/hearing-center-admin/refer-to-judge-defended-claim/refer-to-judge-defended-claim-page-factory';
import SetAsideJudgmentActions from './set-aside-judgment/set-aside-judgment-actions';
import SetAsideJudgmentPageFactory from '../../../../pages/exui/hearing-center-admin/set-aside-judgment/set-aside-judgment-page-factory';
import HearingScheduledActions from './hearing-scheduled-actions';
import HearingScheduledPageFactory from '../../../../pages/exui/hearing-center-admin/hearing-scheduled/hearing-scheduled-page-factory';
import TransferOnlineCaseActions from './transfer-online-case-actions';
import TransferOnlineCasePageFactory from '../../../../pages/exui/hearing-center-admin/transfer-online-case/transfer-online-case-page-factory';
import RequestHearingActions from './request-hearing-actions';
import RequestHearingPageFactory from '../../../../pages/exui/hearing-center-admin/request-hearing/request-hearing-page-factory';
import SettleClaimActions from './settle-claim-actions';
import SettleClaimPageFactory from '../../../../pages/exui/hearing-center-admin/settle-claim/settle-claim-page-factory';
import SettleClaimMarkPaidFullPageFactory from '../../../../pages/exui/claimant-defendant-solicitor/settle-claim-mark-paid-full/settle-claim-mark-paid-full-page-factory.ts';

export default class HearingCenterAdminActionsFactory extends BasePageActionsFactory {
  get caseProceedsInCasemanActions() {
    return new CaseProceedsInCasemanActions(
      new CaseProceedsInCasemanPageFactory(this.page),
      this.testData,
    );
  }

  get createCaseFlagsActions() {
    return new CreateCaseFlagsActions(new CreateCaseFlagsPageFactory(this.page), this.testData);
  }

  get createCaseFlagsSpecActions() {
    return new CreateCaseFlagsSpecActions(new CreateCaseFlagsPageFactory(this.page), this.testData);
  }

  get manageCaseFlagsActions() {
    return new ManageCaseFlagsActions(new ManageCaseFlagsPageFactory(this.page), this.testData);
  }

  get stayCaseActions() {
    return new StayCaseActions(new StayCasePageFactory(this.page), this.testData);
  }

  get manageStayActions() {
    return new ManageStayActions(new ManageStayPageFactory(this.page), this.testData);
  }

  get referJudgeDefenceReceivedActions() {
    return new ReferJudgeDefenceReceivedActions(
      new ReferJudgeDefenceReceivedPageFactory(this.page),
      this.testData,
    );
  }

  get setAsideJudgmentActions() {
    return new SetAsideJudgmentActions(new SetAsideJudgmentPageFactory(this.page), this.testData);
  }

  get hearingScheduledActions() {
    return new HearingScheduledActions(new HearingScheduledPageFactory(this.page), this.testData);
  }

  get transferOnlineCaseActions() {
    return new TransferOnlineCaseActions(
      new TransferOnlineCasePageFactory(this.page),
      this.testData,
    );
  }

  get requestHearingActions() {
    return new RequestHearingActions(new RequestHearingPageFactory(this.page), this.testData);
  }

  get settleClaimActions() {
    return new SettleClaimActions(
      new SettleClaimPageFactory(this.page),
      new SettleClaimMarkPaidFullPageFactory(this.page),
      this.testData,
    );
  }
}
