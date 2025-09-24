import BasePageActionsFactory from '../../../../base/base-page-actions-factory';
import StandardDirectionOrderPageFactory from '../../../../pages/exui/judge-la/standard-directions-order/standard-directions-order-factory';
import StandardDirectionsOrderActions from './standard-directions-order-actions';

export default class JudgeActionsFactory extends BasePageActionsFactory {
  get standardDirectionsOrderActions() {
    return new StandardDirectionsOrderActions(
      new StandardDirectionOrderPageFactory(this.page),
      this.testData,
    );
  }
}
