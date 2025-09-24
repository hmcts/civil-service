import { Locator, Page } from '@playwright/test';
import { Page as PageCore } from 'playwright-core';
import AxeBuilder from '@axe-core/playwright';
import config from '../config/config';
import Cookie from '../models/cookie';
import { TruthyParams } from '../decorators/truthy-params';
import { pageExpect, test } from '../playwright-fixtures';
import Timer from '../helpers/timer';
import { getDomain } from '../config/urls';
import { BoxedDetailedStep, Step } from '../decorators/test-steps';
import ClassMethodHelper from '../helpers/class-method-helper';
import ExpectError from '../errors/expect-error';

const classKey = 'BasePage';
export default abstract class BasePage {
  private page: Page;

  constructor(page: Page) {
    this.page = page;
  }

  private getNewLocator(
    oldLocator: Locator,
    containerSelector?: string,
    index?: number,
    first?: boolean,
  ) {
    const newLocator = containerSelector
      ? this.page.locator(containerSelector).locator(oldLocator)
      : oldLocator;
    return first ? newLocator.nth(0) : index !== undefined ? newLocator.nth(index) : newLocator;
  }

  @BoxedDetailedStep(classKey, 'selector')
  @TruthyParams(classKey, 'selector')
  protected async clickBySelector(
    selector: string,
    options: { index?: number; timeout?: number; containerSelector?: string; first?: boolean } = {},
  ) {
    if ([options.first, options.index !== undefined].filter((option) => option).length > 1) {
      throw new ExpectError("Cannot use 'first' and 'index' options at the same time");
    }
    let locator = this.page.locator(selector);
    locator = this.getNewLocator(locator, options.containerSelector, options.index, options.first);
    await locator.click({ timeout: options.timeout });
  }

  @BoxedDetailedStep(classKey, 'label')
  @TruthyParams(classKey, 'label')
  protected async clickByLabel(
    label: string,
    options: { timeout?: number; first?: boolean; index?: number; exact?: boolean } = {
      exact: true,
    },
  ) {
    if (options.first && options.index !== undefined) {
      throw new ExpectError("Cannot use 'first' and 'index' options at the same time");
    }
    let locator = this.page.getByLabel(label, { exact: options.exact ?? true });
    locator = this.getNewLocator(locator, undefined, options.index, options.first);
    await locator.click({ timeout: options.timeout });
  }

  @BoxedDetailedStep(classKey, 'subStrings')
  protected async clickByLabelWithSubStrings(
    subStrings: string[],
    options: { timeout?: number; first?: boolean; index?: number; exact?: boolean } = {
      exact: true,
    },
  ) {
    if (options.first && options.index !== undefined) {
      throw new ExpectError("Cannot use 'first' and 'index' options at the same time");
    }
    let locator = this.page.getByLabel(new RegExp(subStrings.join('.*'), 'i'), {
      exact: options.exact ?? true,
    });
    locator = this.getNewLocator(locator, undefined, options.index, options.first);
    await locator.click({ timeout: options.timeout });
  }

  @BoxedDetailedStep(classKey, 'name')
  @TruthyParams(classKey, 'name')
  protected async clickButtonByName(
    name: string,
    options: { timeout?: number; exact?: boolean } = {},
  ) {
    await this.page.getByRole('button', { name, exact: options.exact ?? true }).click(options);
  }

  @BoxedDetailedStep(classKey, 'name')
  @TruthyParams(classKey, 'name')
  protected async clickLink(
    name: string,
    options: { index?: number; timeout?: number; exact?: boolean } = {
      exact: true,
      index: 0,
    },
  ) {
    await this.page
      .getByRole('link', { name, exact: options.exact ?? true })
      .nth(options.index ?? 0)
      .click({ timeout: options.timeout });
  }

  @BoxedDetailedStep(classKey, 'selector')
  @TruthyParams(classKey)
  protected async selectorExists(selector: string): Promise<boolean> {
    await this.page.waitForSelector(selector, { state: 'visible' });
    return await this.page.locator(selector!).isVisible();
  }

  @BoxedDetailedStep(classKey, 'content', 'selector')
  @TruthyParams(classKey)
  protected async elementIncludes(content: string, selector?: string): Promise<boolean> {
    const textContent = await this.page.locator(selector!).textContent();
    if (!textContent) return false;
    return textContent.includes(content);
  }

  @BoxedDetailedStep(classKey, 'url')
  @TruthyParams(classKey, 'url')
  protected async goTo(url: string, options: { force?: boolean } = {}) {
    const { origin, pathname } = new URL(this.page.url());
    if (`${origin}${pathname}` !== url || options.force) {
      await this.page.goto(url);
    }
  }

  protected async isDomain(url: string) {
    const currentDomain = getDomain(this.page.url());
    const urlDomain = getDomain(url);
    return urlDomain === currentDomain;
  }

  @BoxedDetailedStep(classKey, 'text')
  @TruthyParams(classKey, 'text')
  protected async clickByText(
    text: string,
    options: { timeout?: number; exact?: boolean } = { exact: true },
  ) {
    await this.page
      .getByText(text, { exact: options.exact ?? true })
      .click({ timeout: options.timeout });
  }

  @BoxedDetailedStep(classKey, 'input', 'selector')
  @TruthyParams(classKey, 'input', 'selector')
  protected async inputText(
    input: string | number,
    selector: string,
    options: { index?: number; timeout?: number; containerSelector?: string; first?: boolean } = {},
  ) {
    if ([options.first, options.index !== undefined].filter((option) => option).length > 1) {
      throw new ExpectError("Cannot use 'first' and 'index' options at the same time");
    }
    let locator = this.page.locator(selector);
    locator = this.getNewLocator(locator, options.containerSelector, options.index, options.first);
    await locator.fill(input.toString());
  }

  @BoxedDetailedStep(classKey, 'input', 'label')
  @TruthyParams(classKey, 'input', 'label')
  protected async inputTextByLabel(
    input: string | number,
    label: string,
    options: {
      index?: number;
      timeout?: number;
      containerSelector?: string;
      first?: boolean;
      exact?: boolean;
    } = { exact: true },
  ) {
    if ([options.first, options.index !== undefined].filter((option) => option).length > 1) {
      throw new ExpectError("Cannot use 'first' and 'index' options at the same time");
    }

    let locator = this.page.getByLabel(label, { exact: options.exact ?? true });
    locator = this.getNewLocator(locator, options.containerSelector, options.index, options.first);
    await locator.fill(input.toString(), {
      timeout: options.timeout,
    });
  }

  @BoxedDetailedStep(classKey, 'selector')
  @TruthyParams(classKey, 'input', 'selector')
  protected async inputSensitiveText(
    input: string | number,
    selector: string,
    options: { index?: number; timeout?: number; containerSelector?: string; first?: boolean } = {},
  ) {
    if ([options.first, options.index !== undefined].filter((option) => option).length > 1) {
      throw new ExpectError("Cannot use 'first' and 'index' options at the same time");
    }
    let locator = this.page.locator(selector);
    locator = this.getNewLocator(locator, options.containerSelector, options.index, options.first);
    await locator.fill(input.toString());
  }

  @TruthyParams(classKey)
  protected async getText(selector: string) {
    return (await this.page.textContent(selector)) ?? undefined;
  }

  @BoxedDetailedStep(classKey)
  protected async getCurrentUrl() {
    return this.page.url();
  }

  @BoxedDetailedStep(classKey, 'option', 'selector')
  @TruthyParams(classKey, 'selector')
  protected async selectFromDropdown(
    option: string | number,
    selector: string,
    options: { timeout?: number } = {},
  ) {
    if (typeof option === 'number')
      await this.page.selectOption(selector, { index: option }, { timeout: options.timeout });
    else await this.page.selectOption(selector, option, { timeout: options.timeout });
  }

  @BoxedDetailedStep(classKey, 'option', 'selector')
  @TruthyParams(classKey, 'selector')
  protected async selectFromDropdownByLabel(
    option: string | number,
    selector: string,
    options: { timeout?: number; exact?: boolean } = { exact: true },
  ) {
    if (typeof option === 'number')
      await this.page
        .getByLabel(selector, { exact: options.exact ?? true })
        .selectOption({ index: option }, { timeout: options.timeout });
    else
      await this.page
        .getByLabel(selector, { exact: options.exact ?? true })
        .selectOption(option, { timeout: options.timeout });
  }

  //This method is hidden from reports (no '@BoxedDetailedStep') but used in multiple places in this file.
  private async _getCookies(): Promise<Cookie[]> {
    return await this.page.context().cookies();
  }

  @BoxedDetailedStep(classKey)
  protected async getCookies(): Promise<Cookie[]> {
    return await this._getCookies();
  }

  @BoxedDetailedStep(classKey)
  protected async reload() {
    await this.page.reload();
  }

  @BoxedDetailedStep(classKey)
  protected async clearCookies() {
    let retries = 2;
    while ((await this._getCookies()).length > 0 && retries >= 0) {
      await this.page.context().clearCookies();
      retries--;
    }
  }

  @BoxedDetailedStep(classKey)
  protected async addCookies(cookies: Cookie[]) {
    let retries = 2;
    const currentCookiesAmount = (await this._getCookies()).length;
    while ((await this._getCookies()).length <= currentCookiesAmount && retries >= 0) {
      await this.page.context().addCookies(cookies);
      retries--;
    }
  }

  @BoxedDetailedStep(classKey, 'filePath', 'selector')
  @TruthyParams(classKey)
  protected async uploadFile(filePath: string, selector: string) {
    await this.page.locator(selector).setInputFiles([]);
    await this.page.locator(selector).setInputFiles([filePath]);
  }

  @BoxedDetailedStep(classKey, 'selector')
  @TruthyParams(classKey, 'selector')
  protected async waitForSelectorToBeVisible(selector: string, options: { timeout?: number } = {}) {
    const locator = this.page.locator(selector);
    await locator.waitFor({ state: 'visible', timeout: options.timeout });
  }

  @BoxedDetailedStep(classKey, 'text')
  @TruthyParams(classKey, 'text')
  protected async waitForTextToBeVisible(text: string, options: { timeout?: number } = {}) {
    const locator = this.page.getByText(text);
    await locator.waitFor({ state: 'visible', timeout: options.timeout });
  }

  @BoxedDetailedStep(classKey, 'selector')
  protected async waitForSelectorToDetach(selector: string, options: { timeout?: number } = {}) {
    const locator = this.page.locator(selector);
    try {
      await locator.waitFor({ state: 'attached', timeout: 75 });
      // eslint-disable-next-line no-empty
    } catch (err) {}
    await locator.waitFor({ state: 'detached', ...options });
  }

  @BoxedDetailedStep(classKey, 'selector')
  protected async waitForSelectorToBeHidden(selector: string, options: { timeout?: number } = {}) {
    const locator = this.page.locator(selector);
    try {
      await locator.waitFor({ state: 'visible', timeout: 75 });
      // eslint-disable-next-line no-empty
    } catch (err) {}
    await locator.waitFor({ state: 'hidden', ...options });
  }

  protected async waitForUrlToChange(options: { timeout?: number } = {}) {
    const url = new URL(this.page.url());
    await this.page.waitForURL(
      (newUrl) => {
        return url.pathname !== newUrl.pathname;
      },
      { timeout: options.timeout },
    );
  }

  @BoxedDetailedStep(classKey, 'text')
  @TruthyParams(classKey, 'text')
  protected async waitForTextToDetach(text: string, options: { timeout?: number } = {}) {
    const locator = this.page.getByText(text);
    try {
      await locator.waitFor({ state: 'attached', timeout: 75 });
      // eslint-disable-next-line no-empty
    } catch (err) {}
    await locator.waitFor({ state: 'detached', ...options });
  }

  public async pause() {
    await this.page.pause();
  }

  public async wait(time: number) {
    await this.page.waitForTimeout(time);
  }

  abstract verifyContent(...args: any[]): Promise<void>;

  protected async runVerifications(
    expects?: Promise<void> | Promise<void>[],
    {
      runAxe = true,
      axeExclusions = [],
      useAxeCache = true,
      axePageInsertName,
    }: {
      runAxe?: boolean;
      axeExclusions?: string[];
      useAxeCache?: boolean;
      axePageInsertName?: string | number;
    } = {},
  ) {
    if (expects) {
      Array.isArray(expects) ? await Promise.all(expects) : await expects;
    }

    if (config.runAxeTests && runAxe) {
      await this.expectAxeToPass(axeExclusions, useAxeCache, axePageInsertName);
    }
  }

  protected async retryReloadRunVerifications(
    assertions: () => Promise<void>[] | Promise<void>,
    {
      runAxe = true,
      axeExclusions = [],
      useAxeCache = true,
      retries,
      message,
      axePageInsertName,
    }: {
      runAxe?: boolean;
      axeExclusions?: string[];
      useAxeCache?: boolean;
      retries?: number;
      message?: string;
      axePageInsertName?: string;
    } = {},
  ) {
    await this.retryReload(assertions, undefined, { message, retries });

    if (config.runAxeTests && runAxe) {
      await this.expectAxeToPass(axeExclusions, useAxeCache, axePageInsertName);
    }
  }

  @BoxedDetailedStep(classKey)
  private async expectAxeToPass(
    axeExclusions: string[],
    useAxeCache: boolean,
    axePageInsertName?: string | number,
  ) {
    const axeBuilder = new AxeBuilder({ page: this.page as PageCore })
      .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22a', 'wcag22aa'])
      .setLegacyMode(true);

    for (const axeExclusion of axeExclusions) {
      axeBuilder.exclude(axeExclusion);
    }

    const pageName = ClassMethodHelper.formatClassName(
      axePageInsertName !== undefined
        ? `${this.constructor.name.slice(0, -4)}${axePageInsertName}Page`
        : this.constructor.name,
    );

    const errorsNumBefore = test.info().errors.length;
    if (useAxeCache) {
      await pageExpect.soft(pageName).toHaveNoAxeViolationsCache(axeBuilder, this.page);
    } else {
      await pageExpect.soft(pageName).toHaveNoAxeViolations(axeBuilder, this.page);
    }
    const errorsAfter = test.info().errors;

    if (errorsAfter.length > errorsNumBefore) {
      errorsAfter[errorsAfter.length - 1].value = 'accessibility';
    }
  }

  @BoxedDetailedStep(classKey, 'domain')
  protected async expectDomain(
    domain: string,
    options: { message?: string; timeout?: number } = {},
  ) {
    await pageExpect(this.page, { message: options.message }).toHaveURL(
      new RegExp(`https?://${domain}.*`),
      {
        timeout: options.timeout,
      },
    );
  }

  @BoxedDetailedStep(classKey, 'path')
  protected async expectUrlStart(
    path: string,
    options: { message?: string; timeout?: number } = {},
  ) {
    await pageExpect(this.page, { message: options.message }).toHaveURL(new RegExp(`^${path}`), {
      timeout: options.timeout,
    });
  }

  @BoxedDetailedStep(classKey, 'endpoints')
  protected async expectUrlEnd(
    endpoints: string | string[],
    options: { timeout?: number; message?: string } = {},
  ) {
    const regex = new RegExp(
      Array.isArray(endpoints) ? `(${endpoints.join('|')})$` : `${endpoints}$`,
    );
    await pageExpect(this.page, { message: options.message }).toHaveURL(regex, {
      timeout: options.timeout,
    });
  }

  @BoxedDetailedStep(classKey, 'currentUrl')
  protected async expectUrlToChange(
    currentUrl: string,
    options: { timeout?: number; message?: string } = {},
  ) {
    pageExpect(this.page, { message: options.message }).not.toHaveURL(currentUrl, {
      timeout: options.timeout,
    });
  }

  @BoxedDetailedStep(classKey, 'text')
  protected async expectHeading(
    text: string | number,
    options: {
      containerSelector?: string;
      index?: number;
      first?: boolean;
      count?: number;
      all?: boolean;
      ignoreDuplicates?: boolean;
      message?: string;
      exact?: boolean;
      timeout?: number;
    } = { exact: true },
  ) {
    if (
      [
        options.first,
        options.index !== undefined,
        options.ignoreDuplicates,
        options.count !== undefined,
        options.all,
      ].filter((option) => option).length > 1
    ) {
      throw new ExpectError(
        "Cannot use 'first', 'index', 'count', 'ignoreDuplicates' and 'all' options at the same time",
      );
    }

    if (options.count === 0) {
      throw new ExpectError("'count' cannot be set to 0");
    }

    let locator = this.page.getByRole('heading', {
      name: text.toString(),
      level: 1,
      exact: options.exact ?? true,
    });
    locator = this.getNewLocator(locator, options.containerSelector, options.index, options.first);

    if (options.ignoreDuplicates) {
      await pageExpect(locator, { message: options.message }).atLeastOneToBeVisible({
        timeout: options.timeout,
      });
    } else if (options.count !== undefined) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(options.count, {
        timeout: options.timeout,
      });
    } else if (options.all) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(null, {
        timeout: options.timeout,
      });
    } else {
      await pageExpect(locator, {
        message: options.message,
      }).toBeVisible({ timeout: options.timeout });
    }
  }

  @BoxedDetailedStep(classKey, 'text')
  @TruthyParams(classKey, 'text')
  protected async expectNoHeading(
    text: string | number,
    options: {
      message?: string;
      exact?: boolean;
      timeout?: number;
    } = { exact: true },
  ) {
    const locator = this.page.getByRole('heading', {
      name: text.toString(),
      level: 1,
      exact: options.exact ?? true,
    });

    try {
      await locator.waitFor({ state: 'visible', timeout: 75 });
      // eslint-disable-next-line no-empty
    } catch (err) {}
    await pageExpect(locator, { message: options.message }).toBeHidden({
      timeout: options.timeout,
    });
  }

  @BoxedDetailedStep(classKey, 'text')
  protected async expectSubheading(
    text: string,
    options: {
      containerSelector?: string;
      index?: number;
      first?: boolean;
      count?: number;
      all?: boolean;
      ignoreDuplicates?: boolean;
      message?: string;
      exact?: boolean;
      timeout?: number;
    } = { exact: true },
  ) {
    if (
      [
        options.first,
        options.index !== undefined,
        options.ignoreDuplicates,
        options.count !== undefined,
        options.all,
      ].filter((option) => option).length > 1
    ) {
      throw new ExpectError(
        "Cannot use 'first', 'index', 'count', 'ignoreDuplicates' and 'all' options at the same time",
      );
    }

    if (options.count === 0) {
      throw new ExpectError("'count' cannot be set to 0");
    }

    let locator = this.page.getByRole('heading', {
      name: text.toString(),
      level: 2,
      exact: options.exact ?? true,
    });
    locator = this.getNewLocator(locator, options.containerSelector, options.index, options.first);

    if (options.ignoreDuplicates) {
      await pageExpect(locator, { message: options.message }).atLeastOneToBeVisible({
        timeout: options.timeout,
      });
    } else if (options.count !== undefined) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(options.count, {
        timeout: options.timeout,
      });
    } else if (options.all) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(null, {
        timeout: options.timeout,
      });
    } else {
      await pageExpect(locator, {
        message: options.message,
      }).toBeVisible({ timeout: options.timeout });
    }
  }

  @BoxedDetailedStep(classKey, 'text')
  @TruthyParams(classKey, 'text')
  protected async expectNoSubheading(
    text: string | number,
    options: {
      message?: string;
      exact?: boolean;
      containerSelector?: string;
      all?: boolean;
      index?: number;
      first?: boolean;
      timeout?: number;
    } = { exact: true },
  ) {
    if (
      [options.first, options.index !== undefined, options.all].filter((option) => option).length >
      1
    ) {
      throw new ExpectError("Cannot use 'first', 'index', 'all' options at the same time");
    }

    let locator = this.page.getByRole('heading', {
      name: text.toString(),
      level: 2,
      exact: options.exact ?? true,
    });
    locator = this.getNewLocator(locator, options.containerSelector, options.index, options.first);

    try {
      await locator.waitFor({ state: 'visible', timeout: 75 });
      // eslint-disable-next-line no-empty
    } catch (err) {}
    if (options.all) {
      await pageExpect(locator, { message: options.message }).allToBeHidden({
        timeout: options.timeout,
      });
    } else {
      await pageExpect(locator, { message: options.message }).toBeHidden({
        timeout: options.timeout,
      });
    }
  }

  @BoxedDetailedStep(classKey, 'selector')
  protected async expectSelector(
    selector: string,
    options: {
      containerSelector?: string;
      index?: number;
      first?: boolean;
      all?: boolean;
      count?: number;
      ignoreDuplicates?: boolean;
      message?: string;
      timeout?: number;
    } = {},
  ) {
    if (
      [
        options.first,
        options.index !== undefined,
        options.ignoreDuplicates,
        options.count !== undefined,
        options.all,
      ].filter((option) => option).length > 1
    ) {
      throw new ExpectError(
        "Cannot use 'first', 'index', 'count', 'ignoreDuplicates' and 'all' options at the same time",
      );
    }

    if (options.count === 0) {
      throw new ExpectError("'count' cannot be set to 0");
    }

    let locator = this.page.locator(selector);
    locator = this.getNewLocator(locator, options.containerSelector, options.index, options.first);

    if (options.ignoreDuplicates) {
      await pageExpect(locator, { message: options.message }).atLeastOneToBeVisible({
        timeout: options.timeout,
      });
    } else if (options.count !== undefined) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(options.count, {
        timeout: options.timeout,
      });
    } else if (options.all) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(null, {
        timeout: options.timeout,
      });
    } else {
      await pageExpect(locator, { message: options.message }).toBeVisible({
        timeout: options.timeout,
      });
    }
  }

  @BoxedDetailedStep(classKey, 'selector')
  protected async expectNoSelector(
    selector: string,
    options: {
      containerSelector?: string;
      all?: boolean;
      index?: number;
      first?: boolean;
      message?: string;
      timeout?: number;
    } = {},
  ) {
    if (
      [options.first, options.index !== undefined, options.all].filter((option) => option).length >
      1
    ) {
      throw new ExpectError("Cannot use 'first', 'index', 'all' options at the same time");
    }

    let locator = this.page.locator(selector);
    locator = this.getNewLocator(locator, options.containerSelector, options.index, options.first);

    try {
      await locator.waitFor({ state: 'visible', timeout: 75 });
      // eslint-disable-next-line no-empty
    } catch (err) {}
    if (options.all) {
      await pageExpect(locator, { message: options.message }).allToBeHidden({
        timeout: options.timeout,
      });
    } else {
      await pageExpect(locator, { message: options.message }).toBeHidden({
        timeout: options.timeout,
      });
    }
  }

  @BoxedDetailedStep(classKey, 'text')
  @TruthyParams(classKey, 'text')
  protected async expectText(
    text: string | number,
    options: {
      message?: string;
      exact?: boolean;
      containerSelector?: string;
      all?: boolean;
      index?: number;
      first?: boolean;
      ignoreDuplicates?: boolean;
      count?: number;
      timeout?: number;
    } = { exact: true },
  ) {
    if (
      [
        options.first,
        options.index !== undefined,
        options.ignoreDuplicates,
        options.count !== undefined,
        options.all,
      ].filter((option) => option).length > 1
    ) {
      throw new ExpectError(
        "Cannot use 'first', 'index', 'count', 'ignoreDuplicates' and 'all' options at the same time",
      );
    }

    if (options.count === 0) {
      throw new ExpectError("'count' cannot be set to 0");
    }

    let locator = this.page.getByText(text.toString(), { exact: options.exact ?? true });
    locator = this.getNewLocator(locator, options.containerSelector, options.index, options.first);

    if (options.ignoreDuplicates) {
      await pageExpect(locator, { message: options.message }).atLeastOneToBeVisible({
        timeout: options.timeout,
      });
    } else if (options.count !== undefined) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(options.count, {
        timeout: options.timeout,
      });
    } else if (options.all) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(null, {
        timeout: options.timeout,
      });
    } else {
      await pageExpect(locator, { message: options.message }).toBeVisible({
        timeout: options.timeout,
      });
    }
  }

  @BoxedDetailedStep(classKey, 'text')
  @TruthyParams(classKey, 'text')
  protected async expectNoText(
    text: string | number,
    options: {
      message?: string;
      exact?: boolean;
      containerSelector?: string;
      all?: boolean;
      index?: number;
      first?: boolean;
      timeout?: number;
    } = { exact: true },
  ) {
    if (
      [options.first, options.index !== undefined, options.all].filter((option) => option).length >
      1
    ) {
      throw new ExpectError("Cannot use 'first', 'index', 'all' options at the same time");
    }

    let locator = this.page.getByText(text.toString(), { exact: options.exact ?? true });
    locator = this.getNewLocator(locator, options.containerSelector, options.index, options.first);

    try {
      await locator.waitFor({ state: 'visible', timeout: 75 });
      // eslint-disable-next-line no-empty
    } catch (err) {}
    if (options.all) {
      await pageExpect(locator, { message: options.message }).allToBeHidden({
        timeout: options.timeout,
      });
    } else {
      await pageExpect(locator, { message: options.message }).toBeHidden({
        timeout: options.timeout,
      });
    }
  }

  @BoxedDetailedStep(classKey, 'label')
  protected async expectLabel(
    label: string,
    options: {
      containerSelector?: string;
      index?: number;
      first?: boolean;
      count?: number;
      all?: boolean;
      exact?: boolean;
      ignoreDuplicates?: boolean;
      message?: string;
      timeout?: number;
    } = { exact: true },
  ) {
    if (
      [
        options.first,
        options.index !== undefined,
        options.ignoreDuplicates,
        options.count !== undefined,
        options.all,
      ].filter((option) => option).length > 1
    ) {
      throw new ExpectError(
        "Cannot use 'first', 'index', 'count', 'ignoreDuplicates' and 'all' options at the same time",
      );
    }

    if (options.count === 0) {
      throw new ExpectError("'count' cannot be set to 0");
    }

    let locator = this.page.locator('label').getByText(label, { exact: options.exact ?? true });
    locator = this.getNewLocator(locator, options.containerSelector, options.index, options.first);

    if (options.ignoreDuplicates) {
      await pageExpect(locator, { message: options.message }).atLeastOneToBeVisible({
        timeout: options.timeout,
      });
    } else if (options.count !== undefined) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(options.count, {
        timeout: options.timeout,
      });
    } else if (options.all) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(null, {
        timeout: options.timeout,
      });
    } else {
      await pageExpect(locator, {
        message: options.message,
      }).toBeVisible({
        timeout: options.timeout,
      });
    }
  }

  @BoxedDetailedStep(classKey, 'subStrings')
  protected async expectLabelWithSubstrings(
    subStrings: string[],
    options: {
      containerSelector?: string;
      index?: number;
      first?: boolean;
      count?: number;
      all?: boolean;
      exact?: boolean;
      ignoreDuplicates?: boolean;
      message?: string;
      timeout?: number;
    } = { exact: true },
  ) {
    if (
      [options.first, options.index !== undefined, options.all].filter((option) => option).length >
      1
    ) {
      throw new ExpectError("Cannot use 'first', 'index', 'all' options at the same time");
    }
    let locator = this.page
      .locator('label')
      .getByText(new RegExp(subStrings.join('.*'), 'i'), { exact: options.exact ?? true });

    locator = this.getNewLocator(locator, options.containerSelector, options.index, options.first);
    if (options.ignoreDuplicates) {
      await pageExpect(locator, { message: options.message }).atLeastOneToBeVisible({
        timeout: options.timeout,
      });
    } else if (options.count !== undefined) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(options.count, {
        timeout: options.timeout,
      });
    } else if (options.all) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(null, {
        timeout: options.timeout,
      });
    } else {
      await pageExpect(locator, {
        message: options.message,
      }).toBeVisible({
        timeout: options.timeout,
      });
    }
  }

  @BoxedDetailedStep(classKey, 'label')
  @TruthyParams(classKey, 'label')
  protected async expectNoLabel(
    label: string,
    options: {
      message?: string;
      exact?: boolean;
      containerSelector?: string;
      all?: boolean;
      index?: number;
      first?: boolean;
      timeout?: number;
    } = { exact: true },
  ) {
    if (
      [options.first, options.index !== undefined, options.all].filter((option) => option).length >
      1
    ) {
      throw new ExpectError("Cannot use 'first', 'index', 'all' options at the same time");
    }

    let locator = this.page.locator('label').getByText(label, { exact: options.exact ?? true });
    locator = this.getNewLocator(locator, options.containerSelector, options.index, options.first);

    try {
      await locator.waitFor({ state: 'visible', timeout: 75 });
      // eslint-disable-next-line no-empty
    } catch (err) {}
    if (options.all) {
      await pageExpect(locator, { message: options.message }).allToBeHidden({
        timeout: options.timeout,
      });
    } else {
      await pageExpect(locator, { message: options.message }).toBeHidden({
        timeout: options.timeout,
      });
    }
  }

  @BoxedDetailedStep(classKey, 'name')
  protected async expectLink(
    name: string,
    options: {
      containerSelector?: string;
      index?: number;
      first?: boolean;
      count?: number;
      all?: boolean;
      ignoreDuplicates?: boolean;
      message?: string;
      exact?: boolean;
      timeout?: number;
    } = { exact: true },
  ) {
    if (
      [
        options.first,
        options.index !== undefined,
        options.ignoreDuplicates,
        options.count !== undefined,
        options.all,
      ].filter((option) => option).length > 1
    ) {
      throw new ExpectError(
        "Cannot use 'first', 'index', 'count', 'ignoreDuplicates' and 'all' options at the same time",
      );
    }

    if (options.count === 0) {
      throw new ExpectError("'count' cannot be set to 0");
    }

    let locator = this.page.getByRole('link', { name, exact: options.exact ?? true });
    locator = this.getNewLocator(locator, options.containerSelector, options.index, options.first);

    if (options.ignoreDuplicates) {
      await pageExpect(locator, { message: options.message }).atLeastOneToBeVisible({
        timeout: options.timeout,
      });
    } else if (options.count !== undefined) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(options.count, {
        timeout: options.timeout,
      });
    } else if (options.all) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(null, {
        timeout: options.timeout,
      });
    } else {
      await pageExpect(locator, {
        message: options.message,
      }).toBeVisible({
        timeout: options.timeout,
      });
    }
  }

  @BoxedDetailedStep(classKey, 'label')
  protected async expectLegend(
    label: string,
    options: {
      containerSelector?: string;
      index?: number;
      first?: boolean;
      count?: number;
      all?: boolean;
      ignoreDuplicates?: boolean;
      message?: string;
      exact?: boolean;
      timeout?: number;
    } = { exact: true },
  ) {
    if (
      [
        options.first,
        options.index !== undefined,
        options.ignoreDuplicates,
        options.count !== undefined,
        options.all,
      ].filter((option) => option).length > 1
    ) {
      throw new ExpectError(
        "Cannot use 'first', 'index', 'count', 'ignoreDuplicates' and 'all' options at the same time",
      );
    }

    if (options.count === 0) {
      throw new ExpectError("'count' cannot be set to 0");
    }

    let locator = this.page.locator('legend').getByText(label, { exact: options.exact ?? true });
    locator = this.getNewLocator(locator, options.containerSelector, options.index, options.first);

    if (options.ignoreDuplicates) {
      await pageExpect(locator, { message: options.message }).atLeastOneToBeVisible({
        timeout: options.timeout,
      });
    } else if (options.count !== undefined) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(options.count, {
        timeout: options.timeout,
      });
    } else if (options.all) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(null, {
        timeout: options.timeout,
      });
    } else {
      await pageExpect(locator, {
        message: options.message,
      }).toBeVisible({
        timeout: options.timeout,
      });
    }
  }

  @BoxedDetailedStep(classKey, 'label', 'selector')
  protected async expectLabelForInput(
    label: string,
    selector: string,
    options: { message?: string; timeout?: number; exact?: boolean } = {},
  ) {
    const inputId = await this.page
      .locator(selector)
      .getAttribute('id', { timeout: options.timeout });
    await pageExpect(
      this.page
        .locator('label')
        .getByText(label, { exact: options.exact ?? true })
        .locator(`[for="${inputId}"]`),
      {
        message: options.message,
      },
    ).toBeVisible({ timeout: options.timeout });
  }

  @BoxedDetailedStep(classKey, 'label', 'selector')
  protected async expectRadioLabel(
    label: string,
    selector: string,
    options: {
      containerSelector?: string;
      index?: number;
      first?: boolean;
      count?: number;
      all?: boolean;
      ignoreDuplicates?: boolean;
      message?: string;
      exact?: boolean;
      timeout?: number;
    } = { exact: true },
  ) {
    if (
      [
        options.first,
        options.index !== undefined,
        options.ignoreDuplicates,
        options.count !== undefined,
        options.all,
      ].filter((option) => option).length > 1
    ) {
      throw new ExpectError(
        "Cannot use 'first', 'index', 'count', 'ignoreDuplicates' and 'all' options at the same time",
      );
    }

    let locator = this.page.locator(selector);
    locator = this.getNewLocator(locator, options.containerSelector, options.index, options.first);

    const inputId = await locator.getAttribute('id', { timeout: options.timeout });
    await pageExpect(
      this.page.locator(`label[for="${inputId}"]`).getByText(label, { exact: true }),
      {
        message: options.message,
      },
    ).toBeVisible({ timeout: options.timeout });

    if (options.ignoreDuplicates) {
      await pageExpect(locator, { message: options.message }).atLeastOneToBeVisible({
        timeout: options.timeout,
      });
    } else if (options.count !== undefined) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(options.count, {
        timeout: options.timeout,
      });
    } else if (options.all) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(null, {
        timeout: options.timeout,
      });
    } else {
      await pageExpect(locator, {
        message: options.message,
      }).toBeVisible({
        timeout: options.timeout,
      });
    }
  }

  @BoxedDetailedStep(classKey, 'selector')
  protected async expectRadioYesLabel(
    selector: string,
    options: { message?: string; timeout?: number } = {},
  ) {
    const inputId = await this.page
      .locator(selector)
      .getAttribute('id', { timeout: options.timeout });
    await pageExpect(
      this.page.locator(`label[for="${inputId}"]`).getByText('Yes', { exact: true }),
      {
        message: options.message,
      },
    ).toBeVisible({ timeout: options.timeout });
  }

  @BoxedDetailedStep(classKey, 'selector')
  protected async expectRadioNoLabel(
    selector: string,
    options: { message?: string; timeout?: number } = {},
  ) {
    const inputId = await this.page
      .locator(selector)
      .getAttribute('id', { timeout: options.timeout });
    await pageExpect(
      this.page.locator(`label[for="${inputId}"]`).getByText('No', { exact: true }),
      {
        message: options.message,
      },
    ).toBeVisible({ timeout: options.timeout });
  }

  @BoxedDetailedStep(classKey, 'name')
  protected async expectTab(
    name: string,
    options: { message?: string; exact?: boolean; timeout?: number } = { exact: true },
  ) {
    await pageExpect(this.page.getByRole('tab', { name, exact: options.exact ?? true }), {
      message: options.message,
    }).toBeVisible({
      timeout: options.timeout,
    });
  }

  @BoxedDetailedStep(classKey, 'name')
  protected async expectNoTab(
    name: string,
    options: { message?: string; exact?: boolean; timeout?: number } = { exact: true },
  ) {
    await pageExpect(this.page.getByRole('tab', { name, exact: options.exact ?? true }), {
      message: options.message,
    }).toBeHidden({
      timeout: options.timeout,
    });
  }

  @BoxedDetailedStep(classKey, 'name')
  protected async expectButton(
    name: string,
    options: { message?: string; exact?: boolean; timeout?: number } = { exact: true },
  ) {
    await pageExpect(this.page.getByRole('button', { name, exact: options.exact ?? true }), {
      message: options.message,
    }).toBeVisible({
      timeout: options.timeout,
    });
  }

  @BoxedDetailedStep(classKey, 'name')
  protected async expectNoButton(
    name: string,
    options: { message?: string; exact?: boolean; timeout?: number } = { exact: true },
  ) {
    await pageExpect(this.page.getByRole('button', { name, exact: options.exact ?? true }), {
      message: options.message,
    }).toBeHidden({
      timeout: options.timeout,
    });
  }

  @BoxedDetailedStep(classKey, 'selector')
  @TruthyParams(classKey, 'selector')
  protected async expectOptionChecked(
    selector: string,
    options: { message?: string; timeout?: number } = {},
  ) {
    await pageExpect(this.page.locator(selector), { message: options.message }).toBeChecked({
      timeout: options.timeout,
    });
  }

  @BoxedDetailedStep(classKey, 'selector', 'text')
  @TruthyParams(classKey, 'selector', 'text')
  protected async expectInputValue(
    selector: string,
    text: string,
    options: { message?: string; timeout?: number } = {},
  ) {
    await pageExpect(this.page.locator(selector), { message: options.message }).toHaveValue(text, {
      timeout: options.timeout,
    });
  }

  @BoxedDetailedStep(classKey, 'selector', 'option')
  @TruthyParams(classKey, 'selector', 'option')
  protected async expectDropdownOption(
    option: string | number,
    selector: string,
    options: { message?: string; timeout?: number; exact?: boolean } = { exact: true },
  ) {
    let locator: Locator;

    if (typeof option === 'number')
      locator = this.page.locator(selector).getByRole('option').nth(option);
    else
      locator = this.page
        .locator(selector)
        .getByRole('option', { name: option, exact: options.exact ?? true });

    await pageExpect(locator, {
      message: options.message,
    }).toBeAttached({ timeout: options.timeout });
  }

  @BoxedDetailedStep(classKey, 'rowName', 'value')
  @TruthyParams(classKey, 'rowName', 'value')
  protected async expectTableValueByRowName(
    rowName: string,
    value: string | number,
    options: {
      containerSelector?: string;
      index?: number;
      first?: boolean;
      count?: number;
      all?: boolean;
      ignoreDuplicates?: boolean;
      message?: string;
      exact?: boolean;
      timeout?: number;
    } = { exact: true },
  ) {
    if (
      [
        options.first,
        options.index !== undefined,
        options.ignoreDuplicates,
        options.count !== undefined,
        options.all,
      ].filter((option) => option).length > 1
    ) {
      throw new ExpectError(
        "Cannot use 'first', 'index', 'count', 'ignoreDuplicates' and 'all' options at the same time",
      );
    }

    let locator = this.page
      .locator('tr', {
        has: this.page.locator(`*:has-text("${rowName}")`),
      })
      .getByRole('cell', { exact: options.exact ?? true, name: value.toString() });
    locator = this.getNewLocator(locator, options.containerSelector, options.index, options.first);

    if (options.ignoreDuplicates) {
      await pageExpect(locator, { message: options.message }).atLeastOneToBeVisible({
        timeout: options.timeout,
      });
    } else if (options.count !== undefined) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(options.count, {
        timeout: options.timeout,
      });
    } else if (options.all) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(null, {
        timeout: options.timeout,
      });
    } else {
      await pageExpect(locator, {
        message: options.message,
      }).toBeVisible({
        timeout: options.timeout,
      });
    }
  }

  @BoxedDetailedStep(classKey, 'value')
  @TruthyParams(classKey, 'value')
  protected async expectDataCellValue(
    value: string | number,
    options: {
      containerSelector?: string;
      index?: number;
      first?: boolean;
      count?: number;
      all?: boolean;
      ignoreDuplicates?: boolean;
      message?: string;
      exact?: boolean;
      timeout?: number;
    } = { exact: true },
  ) {
    if (
      [
        options.first,
        options.index !== undefined,
        options.ignoreDuplicates,
        options.count !== undefined,
        options.all,
      ].filter((option) => option).length > 1
    ) {
      throw new ExpectError(
        "Cannot use 'first', 'index', 'count', 'ignoreDuplicates' and 'all' options at the same time",
      );
    }

    if (options.count === 0) {
      throw new ExpectError("'count' cannot be set to 0");
    }

    let locator = this.page.getByRole('cell', {
      name: value.toString(),
      exact: options.exact ?? true,
    });
    locator = this.getNewLocator(locator, options.containerSelector, options.index, options.first);

    if (options.ignoreDuplicates) {
      await pageExpect(locator, { message: options.message }).atLeastOneToBeVisible({
        timeout: options.timeout,
      });
    } else if (options.count !== undefined) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(options.count, {
        timeout: options.timeout,
      });
    } else if (options.all) {
      await pageExpect(locator, { message: options.message }).someToBeVisible(null, {
        timeout: options.timeout,
      });
    } else {
      await pageExpect(locator, {
        message: options.message,
      }).toBeVisible({
        timeout: options.timeout,
      });
    }
  }

  @BoxedDetailedStep(classKey, 'text', 'selector')
  @TruthyParams(classKey, 'text', 'selector')
  protected async expectTableRowValue(
    text: string,
    selector: string,
    options: { message?: string; rowNum: number; timeout?: number; tableName?: string } = {
      rowNum: 0,
    },
  ) {
    await pageExpect(this.page.locator(`${selector} >> tr`).nth(options.rowNum).getByText(text), {
      message: options.message,
    }).toBeVisible({ timeout: options.timeout });
  }

  protected async retryAction(
    actions: () => Promise<void>,
    assertions: () => Promise<void>[] | Promise<void>,
    actionAfterFirstAttempt?: () => Promise<void>,
    options: {
      retries?: number;
      assertFirst?: boolean;
      message?: string;
    } = {},
  ): Promise<void> {
    let { retries = 2, assertFirst = false } = options;

    while (retries >= 0) {
      if (!assertFirst) await actions();
      const promises = assertions();

      try {
        await (Array.isArray(promises) ? Promise.all(promises) : promises);
        break;
      } catch (error) {
        if (retries <= 0) throw error;
        console.log(options.message ?? 'Action failed, trying again');
        console.log(`Retries: ${retries} remaining`);
        retries--;
        assertFirst = false;

        if (actionAfterFirstAttempt) {
          await actionAfterFirstAttempt();
        }
      }
    }
  }

  @Step(classKey)
  @TruthyParams(classKey, 'selector')
  protected async retryClickBySelector(
    selector: string,
    assertions: () => Promise<void>[] | Promise<void>,
    actionAfterFirstAttempt?: () => Promise<void>,
    { retries = 2, message }: { retries?: number; message?: string } = {},
  ) {
    await this.retryAction(
      () => this.clickBySelector(selector),
      assertions,
      actionAfterFirstAttempt,
      {
        retries,
        message: message ?? `Click action failed, selector: ${selector}, trying again`,
      },
    );
  }

  @Step(classKey)
  @TruthyParams(classKey, 'name')
  protected async retryClickLink(
    name: string,
    assertions: () => Promise<void>[] | Promise<void>,
    actionAfterFirstAttempt?: () => Promise<void>,
    { retries = 2, message }: { retries?: number; message?: string } = {},
  ) {
    await this.retryAction(() => this.clickLink(name), assertions, actionAfterFirstAttempt, {
      retries,
      message: message ?? `Click action failed, link: ${name} trying again`,
    });
  }

  @Step(classKey)
  @TruthyParams(classKey, 'option', 'selector')
  protected async retrySelectFromDropdown(
    option: string,
    selector: string,
    assertions: () => Promise<void>[] | Promise<void>,
    actionAfterFirstAttempt?: () => Promise<void>,
    { retries = 2, message }: { retries?: number; message?: string } = {},
  ) {
    await this.retryAction(
      async () => {
        await this.selectFromDropdown(0, selector);
        await this.selectFromDropdown(option, selector);
      },
      assertions,
      actionAfterFirstAttempt,
      {
        retries,
        message:
          message ??
          `Select from dropdown action failed, option: ${option}, selector: ${selector} trying again`,
      },
    );
  }

  @Step(classKey)
  @TruthyParams(classKey, 'option', 'label')
  protected async retrySelectFromDropdownByLabel(
    option: string,
    label: string,
    assertions: () => Promise<void>[] | Promise<void>,
    actionAfterFirstAttempt?: () => Promise<void>,
    { retries = 2, message }: { retries?: number; message?: string } = {},
  ) {
    await this.retryAction(
      async () => {
        await this.selectFromDropdownByLabel(0, label);
        await this.selectFromDropdownByLabel(option, label);
      },
      assertions,
      actionAfterFirstAttempt,
      {
        retries,
        message:
          message ??
          `Select from dropdown action failed, option: ${option}, label: ${label} trying again`,
      },
    );
  }

  @Step(classKey)
  protected async retryReload(
    assertions: () => Promise<void>[] | Promise<void>,
    actionAfterFirstAttempt?: () => Promise<void>,
    { retries = 2, message }: { retries?: number; message?: string } = {},
  ) {
    await this.retryAction(() => this.reload(), assertions, actionAfterFirstAttempt, {
      retries,
      message: message ?? 'Assertion failed, reloading page and trying again',
      assertFirst: true,
    });
  }

  @Step(classKey)
  protected async retryGoTo(
    url: string,
    assertions: () => Promise<void>[] | Promise<void>,
    actionAfterFirstAttempt?: () => Promise<void>,
    { retries = 2, message }: { retries?: number; message?: string } = {},
  ) {
    await this.retryAction(
      () => this.goTo(url, { force: true }),
      assertions,
      actionAfterFirstAttempt,
      {
        retries,
        message: message ?? 'Navigation failed, trying again',
      },
    );
  }

  protected async retryActionTimeout(
    actions: () => Promise<void>,
    assertions: () => Promise<void>[] | Promise<void>,
    actionAfterFirstAttempt?: () => Promise<void>,
    {
      interval = 5_000,
      timeout = config.playwright.toPassTimeout,
      message,
      assertFirst = false,
    }: {
      interval?: number;
      timeout?: number;
      message?: string;
      assertFirst?: boolean;
    } = {},
  ) {
    let attempts = 0;
    const timer = new Timer(timeout);
    await pageExpect(async () => {
      if (!assertFirst) await actions();
      attempts++;
      const promises = assertions();
      try {
        await (Array.isArray(promises) ? Promise.all(promises) : promises);
      } catch (error) {
        console.log(message);
        console.log(`Attempts: ${attempts}, Timeout in ${timer.remainingTime} second(s)`);
        assertFirst = false;
        if (actionAfterFirstAttempt) await actionAfterFirstAttempt();
        throw error;
      }
    }).toPass({
      intervals: [interval],
      timeout: timeout,
    });
  }

  @Step(classKey)
  @TruthyParams(classKey, 'selector')
  protected async retryClickBySelectorTimeout(
    selector: string,
    assertions: () => Promise<void>[] | Promise<void>,
    actionAfterFirstAttempt?: () => Promise<void>,
    {
      interval,
      timeout,
      message,
    }: {
      interval?: number;
      timeout?: number;
      message?: string;
    } = {},
  ) {
    await this.retryActionTimeout(
      () => this.clickBySelector(selector),
      assertions,
      actionAfterFirstAttempt,
      {
        interval,
        timeout,
        message: message ?? `Click action failed, selector: ${selector}, trying again`,
      },
    );
  }

  @Step(classKey)
  @TruthyParams(classKey, 'name')
  protected async retryClickLinkTimeout(
    name: string,
    assertions: () => Promise<void>[] | Promise<void>,
    actionAfterFirstAttempt?: () => Promise<void>,
    {
      interval,
      timeout,
      message,
    }: {
      interval?: number;
      timeout?: number;
      message?: string;
    } = {},
  ) {
    await this.retryActionTimeout(() => this.clickLink(name), assertions, actionAfterFirstAttempt, {
      interval,
      timeout,
      message: message ?? `Click action failed, link: ${name} trying again`,
    });
  }

  @Step(classKey)
  @TruthyParams(classKey, 'option', 'selector')
  protected async retrySelectFromDropdownTimeout(
    option: string,
    selector: string,
    assertions: () => Promise<void>[] | Promise<void>,
    actionAfterFirstAttempt?: () => Promise<void>,
    {
      interval,
      timeout,
      message,
    }: {
      interval?: number;
      timeout?: number;
      message?: string;
    } = {},
  ) {
    await this.retryActionTimeout(
      async () => {
        await this.selectFromDropdown(0, selector);
        await this.selectFromDropdown(option, selector);
      },
      assertions,
      actionAfterFirstAttempt,
      {
        interval,
        timeout,
        message:
          message ??
          `Select from dropdown action failed, option: ${option}, selector: ${selector} trying again`,
      },
    );
  }

  @Step(classKey)
  protected async retryReloadTimeout(
    assertions: () => Promise<void>[] | Promise<void>,
    actionAfterFirstAttempt?: () => Promise<void>,
    {
      interval,
      timeout,
      message,
    }: {
      interval?: number;
      timeout?: number;
      message?: string;
    } = {},
  ) {
    await this.retryActionTimeout(() => this.reload(), assertions, actionAfterFirstAttempt, {
      interval,
      timeout,
      assertFirst: true,
      message: message ?? 'Assertion failed, reloading page and trying again',
    });
  }
}
