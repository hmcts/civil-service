import TestData from '../models/test-utils/test-data';

export default abstract class BaseSchemaBuilderFactory {
  private _testData: TestData;

  constructor(testData: TestData) {
    this._testData = testData;
  }

  protected get testData() {
    return this._testData;
  }
}
