import BasePageActionsFactory from '../../../../base/base-page-actions-factory';
import CaseProceedsInCasemanPageFactory from '../../../../pages/exui/caseworker/case-proceeds-in-caseman/case-proceeds-in-caseman-page-factory.ts';
import CaseProceedsInCasemanActions from './case-proceeds-in-caseman-actions.ts';
import ManageDocumentsActions from './manage-documents-actions.ts';
import ManageDocumentsPageFactory from '../../../../pages/exui/caseworker/manage-documents/manage-documents-page-factory.ts';
import ManageContactInformationActions from './manage-contact-information-actions.ts';
import ManageContactInformationPageFactory from '../../../../pages/exui/caseworker/manage-contact-information/manage-contact-information-page-factory.ts';
import MediationUnsuccessfulActions from './mediation-unsuccessful-actions.ts';
import MediationUnsuccessfulPageFactory from '../../../../pages/exui/caseworker/mediation-unsuccessful/mediation-unsuccessful-page-factory.ts';
import ReferJudgeDefenceReceivedActions from '../hearing-center-admin/refer-judge-defence-received/refer-judge-defence-received-actions.ts';
import ReferJudgeDefenceReceivedPageFactory from '../../../../pages/exui/hearing-center-admin/refer-to-judge-defended-claim/refer-to-judge-defended-claim-page-factory';
import SetAsideJudgmentActions from '../hearing-center-admin/set-aside-judgment/set-aside-judgment-actions';
import SetAsideJudgmentPageFactory from '../../../../pages/exui/hearing-center-admin/set-aside-judgment/set-aside-judgment-page-factory';

export default class CaseworkerActionsFactory extends BasePageActionsFactory {

  get caseProceedsInCasemanActions() {
    return new CaseProceedsInCasemanActions(
      new CaseProceedsInCasemanPageFactory(this.page),
      this.testData,
    );
  }

  get manageDocumentsActions() {
    return new ManageDocumentsActions(
      new ManageDocumentsPageFactory(this.page),
      this.testData);
  }

  get manageContactInformationActions() {
    return new ManageContactInformationActions(
      new ManageContactInformationPageFactory(this.page),
      this.testData);
  }

  get mediationUnsuccessfulActions() {
    return new MediationUnsuccessfulActions(
      new MediationUnsuccessfulPageFactory(this.page),
      this.testData,
    );
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
}
