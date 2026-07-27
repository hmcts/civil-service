import BasePageFactory from '../../../../base/base-page-factory';
import DateFragment from '../../fragments/date/date-fragment';
import SetAsideJudgmentPage from './set-aside-judgment/set-aside-judgment-page';
import SetAsideOrderTypePage from './set-aside-order-type/set-aside-order-type-page';
import SubmitSetAsideJudgmentPage from './submit-set-aside-judgment/submit-set-aside-judgment-page';
import ConfirmSetAsideJudgmentPage from './confirm-set-aside-judgment/confirm-set-aside-judgment-page';

export default class SetAsideJudgmentPageFactory extends BasePageFactory {
  get setAsideJudgmentPage() {
    return new SetAsideJudgmentPage(this.page);
  }

  get setAsideOrderTypePage() {
    const dateFragment = new DateFragment(this.page);
    return new SetAsideOrderTypePage(this.page, dateFragment);
  }

  get submitSetAsideJudgmentPage() {
    return new SubmitSetAsideJudgmentPage(this.page);
  }

  get confirmSetAsideJudgmentPage() {
    return new ConfirmSetAsideJudgmentPage(this.page);
  }
}
