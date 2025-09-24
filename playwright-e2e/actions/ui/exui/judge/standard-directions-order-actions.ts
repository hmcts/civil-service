import BaseTestData from '../../../../base/base-test-data';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import TestData from '../../../../models/test-data';
import StandardDirectionOrderPageFactory from '../../../../pages/exui/judge-la/standard-directions-order/standard-directions-order-factory';

@AllMethodsStep()
export default class StandardDirectionsOrderActions extends BaseTestData {
  private standardDirectionsOrder: StandardDirectionOrderPageFactory;

  constructor(standardDirectionsOrder: StandardDirectionOrderPageFactory, testData: TestData) {
    super(testData);
    this.standardDirectionsOrder = standardDirectionsOrder;
  }
}
