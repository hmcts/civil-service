import { Page } from '@playwright/test';
import TestData from '../models/test-data';

export default abstract class BasePageActionsFactory {
  private _page: Page;
  private _testData: TestData;

  constructor(page: Page, testData: TestData) {
    this._page = page;
    this._testData = testData;
  }

  protected get page() {
    return this._page;
  }

  protected get testData() {
    return this._testData;
  }
}
