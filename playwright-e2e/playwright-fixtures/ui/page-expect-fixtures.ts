import test, { expect as baseExpect, Locator, Page } from '@playwright/test';
import config from '../../config/config';
import AxeBuilder from '@axe-core/playwright';
import AxeCacheHelper from '../../helpers/axe-cache-helper';
import { PageResult } from '../../models/axe-results';
import PromiseHelper from '../../helpers/promise-helper';

export const expect = baseExpect
  .extend({
    async toHaveNoAxeViolationsCache(pageName: string, axeBuilder: AxeBuilder, page: Page) {
      const assertionName = 'toHaveNoAxeViolationsCache';
      const testInfo = test.info();
      let matcherResult: any;
      let pageResults: PageResult;

      pageResults = await AxeCacheHelper.getAxePageResult(testInfo.project.name, pageName);

      if (!pageResults) {
        let pass = true;
        let screenshot: Buffer;
        const results = await axeBuilder.analyze();
        const violations = violationFingerprints(results);
        if (violations.length > 0) {
          pass = false;
          screenshot = await page.screenshot({ fullPage: true });
        }
        pageResults = await AxeCacheHelper.writeAxePageResult(testInfo.project.name, pageName, testInfo.title, pass, violations, screenshot);
      }

      if (!pageResults.pass) {
        if (pageResults.violationsInfo) {
          const violationsAttachmentExist = testInfo.attachments.some((attachment) => attachment.name === pageResults.violationsInfo.fileName);
          if (!violationsAttachmentExist)
            await testInfo.attach(pageResults.violationsInfo.fileName, {
              path: pageResults.violationsInfo.filePath
            });
        }
        if (pageResults.screenshotInfo) {
          const screenshotAttachmentExists = testInfo.attachments.some((attachment) => attachment.name === pageResults.screenshotInfo.fileName);
          if (!screenshotAttachmentExists)
            await testInfo.attach(pageResults.screenshotInfo.fileName, {
              path: pageResults.screenshotInfo.filePath
            });
        }
      }

      try {
        baseExpect(pageResults.pass ? 0 : pageResults.violationsInfo.length).toBe(0);
      } catch (e: any) {
        matcherResult = e.matcherResult;
      }

      const message = pageResults.pass
        ? () =>
            this.utils.matcherHint(assertionName, undefined, undefined, {
              isNot: this.isNot
            }) +
            '\n\n' +
            `Expected: ${this.isNot ? 'not ' : ''}${pageName} to have 0 violation(s)\n` +
            `Received: ${pageName} with 0 violation(s)`
        : () =>
            this.utils.matcherHint(assertionName, undefined, undefined, {
              isNot: this.isNot
            }) +
            '\n\n' +
            `Expected: ${pageName} to have 0 violation(s)\n` +
            `Received: ${pageName} with ${pageResults.violationsInfo.length} violation(s), please check attached file: ${pageResults.violationsInfo.fileName}, for more details.`;

      return {
        message,
        pass: pageResults.pass,
        name: assertionName,
        expected: 0,
        actual: matcherResult?.actual
      };
    },

    async toHaveNoAxeViolations(pageName: string, axeBuilder: AxeBuilder, page: Page) {
      const assertionName = 'toHaveNoAxeViolations';
      let violationsFileName: string;
      let pass = true;
      let matcherResult: any;

      const results = await axeBuilder.analyze();
      const violations = violationFingerprints(results);

      if (violations.length > 0) {
        pass = false;
        violationsFileName = `${pageName}-accessibility-violations`;
        let screenshotFileName = `${pageName}-accessibility-failure`;
        const violationsFilesLen = test.info().attachments.filter((attachment) => attachment.name.startsWith(violationsFileName)).length;
        const violationsScreenshotLen = test.info().attachments.filter((attachment) => attachment.name.startsWith(violationsFileName)).length;

        if (violationsFilesLen > 0 || violationsScreenshotLen > 0) {
          const maxViolationNum = Math.max(violationsFilesLen, violationsScreenshotLen);
          violationsFileName += `-(${maxViolationNum + 1})`;
          screenshotFileName += `-(${maxViolationNum + 1})`;
        }

        violationsFileName += '.json';
        screenshotFileName += '.png';

        await test.info().attach(violationsFileName, {
          body: JSON.stringify(violations, null, 2),
          contentType: 'application/json'
        });
        const screenshot = await page.screenshot({ fullPage: true });
        await test.info().attach(screenshotFileName, {
          body: screenshot,
          contentType: 'image/png'
        });
      }

      try {
        baseExpect(pass ? 0 : violations.length).toBe(0);
      } catch (e: any) {
        matcherResult = e.matcherResult;
      }

      const message = pass
        ? () =>
            this.utils.matcherHint(assertionName, undefined, undefined, {
              isNot: this.isNot
            }) +
            '\n\n' +
            `Expected: ${this.isNot ? 'not ' : ''}${pageName} to have 0 violation(s)\n` +
            `Received: ${pageName} with 0 violation(s)`
        : () =>
            this.utils.matcherHint(assertionName, undefined, undefined, {
              isNot: this.isNot
            }) +
            '\n\n' +
            `Expected: ${pageName} to have 0 violation(s)\n` +
            `Received: ${pageName} with ${violations.length} violation(s), please check attached file: ${violationsFileName}, for more details.`;

      return {
        message,
        pass,
        name: assertionName,
        expected: 0,
        actual: matcherResult?.actual
      };
    },

    async atLeastOneToBeVisible(locator: Locator, options?: { timeout?: number }) {
      const assertionName = 'atLeastOneToBeVisible';
      let pass: boolean;
      let matcherResult: any;
      let locatorCount: number;

      try {
        await baseExpect(locator).not.toHaveCount(0, { timeout: 5000 });
        locatorCount = await locator.count();
        const promises = [];
        for (let i = 0; i < locatorCount; i++) {
          promises.push(baseExpect(locator.nth(i)).toBeVisible({ timeout: options.timeout }));
        }
        await Promise.race(promises);
        pass = true;
      } catch (error) {
        pass = false;
        matcherResult = error.matcherResult;
      }

      const message = pass
        ? () =>
            this.utils.matcherHint(assertionName, undefined, undefined, { isNot: this.isNot }) +
            '\n\n' +
            `Locator: ${locator}\n` +
            `Expected: ${this.isNot ? 'not' : ''}at least one locator to be visible\n` +
            (matcherResult ? `Received: ${locatorCount === undefined ? 'locator count ' + this.utils.printReceived(0) : this.utils.printReceived(matcherResult.actual)}` : '')
        : () =>
            this.utils.matcherHint(assertionName, undefined, undefined, { isNot: this.isNot }) +
            '\n\n' +
            `Locator: ${locator}\n` +
            `Expected: ${this.isNot ? 'not' : ''}at least one locator to be visible\n` +
            (matcherResult ? `Received: ${locatorCount === undefined ? 'locator count ' + this.utils.printReceived(0) : this.utils.printReceived(matcherResult.actual)}` : '');

      return {
        message,
        pass,
        name: assertionName,
        actual: matcherResult?.actual
      };
    },

    async someToBeVisible(locator: Locator, count: number | null, options?: { timeout?: number }) {
      const assertionName = 'someToBeVisible';
      let pass: boolean;
      let matcherResult: any;
      let locatorCount: number;
      let passCount = 0;

      try {
        await baseExpect(locator).not.toHaveCount(0, { timeout: options.timeout });
        locatorCount = await locator.count();
        const promises = [];
        for (let i = 0; i < locatorCount; i++) {
          promises.push(baseExpect(locator.nth(i)).toBeVisible({ timeout: 500 }));
        }
        const results = await PromiseHelper.someSettled(promises, count, 100);
        results.forEach((result) => {
          if (result.status === 'fulfilled') passCount++;
        });
        if (count !== null) baseExpect(passCount).toEqual(count);
        else baseExpect(passCount).toEqual(locatorCount);
        pass = true;
      } catch (error) {
        pass = false;
        matcherResult = error.matcherResult;
      }
      const message = pass
        ? () =>
            this.utils.matcherHint(assertionName, undefined, undefined, { isNot: this.isNot }) +
            '\n\n' +
            `Locator: ${locator}\n` +
            `Expected: ${this.isNot ? 'not' : ''}${count !== null ? count + ' ' : locatorCount + ' '}matching locator(s) to be visible\n` +
            (matcherResult ? `Received: ${locatorCount !== undefined ? locatorCount : 0} matching locator(s) ${locatorCount ? `and ${passCount} ${passCount === 1 ? 'was' : 'were'} visible` : ''}` : '')
        : () =>
            this.utils.matcherHint(assertionName, undefined, undefined, { isNot: this.isNot }) +
            '\n\n' +
            `Locator: ${locator}\n` +
            `Expected: ${this.isNot ? 'not' : ''}${count !== null ? count + ' ' : locatorCount + ' '}matching locator(s) to be visible\n` +
            (matcherResult ? `Received: ${locatorCount !== undefined ? locatorCount : 0} matching locator(s) ${locatorCount ? `and ${passCount} ${passCount === 1 ? 'was' : 'were'} visible` : ''}` : '');

      return {
        message,
        pass,
        name: assertionName,
        actual: matcherResult?.actual
      };
    },

    async allToBeHidden(locator: Locator, options?: { timeout?: number }) {
      const assertionName = 'allToBeHidden';
      let pass: boolean;
      let matcherResult: any;
      let locatorCount: number;
      let passCount = 0;

      try {
        locatorCount = await locator.count();
        const promises = [];
        for (let i = 0; i < locatorCount; i++) {
          promises.push(baseExpect(locator.nth(i)).toBeHidden({ timeout: options.timeout }));
        }
        const results = await Promise.allSettled(promises);
        results.forEach((result) => {
          if (result.status === 'fulfilled') passCount++;
        });
        baseExpect(passCount).toEqual(locatorCount);
        pass = true;
      } catch (error) {
        pass = false;
        matcherResult = error.matcherResult;
      }
      const message = pass
        ? () =>
            this.utils.matcherHint(assertionName, undefined, undefined, { isNot: this.isNot }) +
            '\n\n' +
            `Locator: ${locator}\n` +
            `Expected: ${this.isNot ? 'not ' : ''}all locator(s) to be hidden\n` +
            (matcherResult ? `Received: ${locatorCount !== undefined ? locatorCount : 0} matching locator(s) ${locatorCount ? `and ${passCount} ${passCount === 1 ? 'was' : 'were'} hidden` : ''}` : '')
        : () =>
            this.utils.matcherHint(assertionName, undefined, undefined, { isNot: this.isNot }) +
            '\n\n' +
            `Locator: ${locator}\n` +
            `Expected: ${this.isNot ? 'not ' : ''}all locator(s) to be hidden\n` +
            (matcherResult ? `Received: ${locatorCount !== undefined ? locatorCount : 0} matching locator(s) ${locatorCount ? `and ${passCount} ${passCount === 1 ? 'was' : 'were'} hidden` : ''}` : '');

      return {
        message,
        pass,
        name: assertionName,
        actual: matcherResult?.actual
      };
    }
  })
  .configure({ soft: config.playwright.softExpect });

function violationFingerprints(accessibilityScanResults: any) {
  const violationFingerprints = accessibilityScanResults.violations.map((violation: any) => ({
    rule: violation.id,
    // These are CSS selectors which uniquely identify each element with
    // a violation of the rule in question.
    targets: violation.nodes.map((node: any) => node.target)
  }));
  return violationFingerprints;
}
