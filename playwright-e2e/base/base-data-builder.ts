import RequestsFactory from '../requests/requests-factory';
import TestData from '../models/test-utils/test-data';
import BaseTestData from './base-test-data';

export default abstract class BaseDataBuilder extends BaseTestData {
  protected requestsFactory: RequestsFactory;

  constructor(requestsFactory: RequestsFactory, testData: TestData) {
    super(testData);
    this.requestsFactory = requestsFactory;
  }

  // eslint-disable-next-line no-unused-vars
  protected abstract buildData(...args: any[]): any;
}
