import BasePageActionsFactory from '../../../../base/base-page-actions-factory';
import SdoPageFactory from '../../../../pages/exui/judge-la/sdo/sdo-page-factory';
import SdoDJPageFactory from '../../../../pages/exui/judge-la/sdo-dj/sdo-dj-page-factory';
import NotSuitableSdoPageFactory from '../../../../pages/exui/judge-la/not-suitable-for-sdo/not-suitable-sdo-page-factory';
import SdoActions from './sdo-actions';
import SdoDJActions from './sdo-dj-actions';
import NotSuitableSdoActions from './not-suitable-sdo-actions';
import GenerateDirectionsOrderActions from './generate-directions-order-actions';
import GenerateDirectionsOrderPageFactory from '../../../../pages/exui/judge-la/generate-directions-order/generate-directions-order-page-factory';
import DecisionOnReconsiderationRequestActions from './decision-on-reconsideration-request-actions';
import DecisionOnReconsiderationRequestPageFactory from '../../../../pages/exui/judge-la/decision-on-reconsideration-request/decision-on-reconsideration-request-page-factory';

export default class JudgeLAActionsFactory extends BasePageActionsFactory {
  get sdoActions() {
    return new SdoActions(new SdoPageFactory(this.page), this.testData);
  }

  get sdoDJActions() {
    return new SdoDJActions(new SdoDJPageFactory(this.page), this.testData);
  }

  get notSuitableSdoActions() {
    return new NotSuitableSdoActions(new NotSuitableSdoPageFactory(this.page), this.testData);
  }

  get generateDirectionsOrderActions() {
    return new GenerateDirectionsOrderActions(
      new GenerateDirectionsOrderPageFactory(this.page),
      this.testData,
    );
  }

  get decisionOnReconsiderationRequestActions() {
    return new DecisionOnReconsiderationRequestActions(
      new DecisionOnReconsiderationRequestPageFactory(this.page),
      this.testData,
    );
  }
}
