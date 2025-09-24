import { test as base, Page, TestInfo } from '@playwright/test';
import TestData from '../../models/test-data';
import FileSystemHelper from '../../helpers/file-system-helper';

type TestDataFixture = {
  _isSetupTest: boolean;
  _isTeardownTest: boolean;
  _verifyCookiesBanner: boolean;
  _testData: TestData;
};

export const test = base.extend<TestDataFixture>({
  page: async ({ page }, use, testInfo) => {
    await use(page);
    await pageTeardown(page, testInfo);
  },
  _isSetupTest: async ({}, use, testInfo) => {
    await use(testInfo.project.name.endsWith('setup'));
  },
  _isTeardownTest: async ({}, use, testInfo) => {
    await use(testInfo.project.name.endsWith('teardown'));
  },
  _verifyCookiesBanner: async ({}, use, testInfo) => {
    await use(testInfo.tags.includes('@verify-cookies-banner'));
  },
  _testData: async ({}, use, testInfo) => {
    await use({
      workerIndex: testInfo.parallelIndex,
      caseFlags: { caseFlagsDetails: [], activeCaseFlags: 0 }
    });
  }
});

const pageTeardown = async (page: Page, testInfo: TestInfo) => {
  const screenshotAttachment = testInfo.attachments.find((attachment) => attachment.name === 'screenshot');
  const allErrorsAxe = testInfo.errors.length > 0 ? testInfo.errors.every((error) => error.value === 'accessibility') : false;
  if (allErrorsAxe && screenshotAttachment && page.video()) {
    FileSystemHelper.delete(screenshotAttachment.path, { force: true, quiet: true });
    FileSystemHelper.delete(await page.video().path(), { force: true, quiet: true });
    test.fail();
  } else if (screenshotAttachment) {
    await testInfo.attach('failed.png', { path: screenshotAttachment.path });
    FileSystemHelper.delete(screenshotAttachment.path, { force: true, quiet: true });
  }
  testInfo.errors.reverse();
};
