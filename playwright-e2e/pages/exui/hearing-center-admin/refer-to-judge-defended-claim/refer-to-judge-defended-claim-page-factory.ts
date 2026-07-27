import BasePageFactory from '../../../../base/base-page-factory';
import ReferJudgeDefenceReceivedPage from './refer-to-judge-defended-claim/refer-to-judge-defended-claim-page';
import ConfirmReferJudgeDefenceReceivedPage from './confirm-refer-to-judge-defended-claim/confirm-refer-to-judge-defended-claim-page';

export default class ReferJudgeDefenceReceivedPageFactory extends BasePageFactory {
  get referJudgeDefenceReceivedPage() {
    return new ReferJudgeDefenceReceivedPage(this.page);
  }

  get confirmReferJudgeDefenceReceivedPage() {
    return new ConfirmReferJudgeDefenceReceivedPage(this.page);
  }
}
