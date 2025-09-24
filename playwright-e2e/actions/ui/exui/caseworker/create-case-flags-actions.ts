import BaseTestData from '../../../../base/base-test-data';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import TestData from '../../../../models/test-data';
import CreateCaseFlagsPageFactory from '../../../../pages/exui/caseworker/create-case-flags/create-case-flags-page-factory';

@AllMethodsStep()
export default class CreateCaseFlagsActions extends BaseTestData {
  private createCaseFlagsPageFactory: CreateCaseFlagsPageFactory;

  constructor(createCaseFlagsPageFactory: CreateCaseFlagsPageFactory, testData: TestData) {
    super(testData);
    this.createCaseFlagsPageFactory = createCaseFlagsPageFactory;
  }
}
