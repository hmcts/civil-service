import TestData from '../models/test-utils/test-data';
import BaseTestData from './base-test-data';

export default abstract class BaseSchemaBuilder extends BaseTestData {
  constructor(testData: TestData) {
    super(testData);
  }

  // eslint-disable-next-line no-unused-vars
  protected abstract buildSchema(...args: any[]): any;
}
