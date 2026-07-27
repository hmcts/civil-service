import RequestsFactory from '../requests/requests-factory';
import TestData from '../models/test-utils/test-data';

export default abstract class BaseDataBuilderFactory {
  private _requestsFactory: RequestsFactory;
  private _testData: TestData;

  constructor(requestsFactory: RequestsFactory, testData: TestData) {
    this._requestsFactory = requestsFactory;
    this._testData = testData;
  }

  protected get requestsFactory() {
    return this._requestsFactory;
  }

  protected get testData() {
    return this._testData;
  }
}
