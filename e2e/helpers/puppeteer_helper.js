module.exports = class PuppeteerHelpers extends Helper {

  /**
   * Finds elements described by selector.
   * If element cannot be found an empty collection is returned.
   *
   * @param selector - element selector
   * @returns {Promise<Array>} - promise holding either collection of elements or empty collection if element is not found
   */
  async locateSelector(selector) {
    return this.helpers['Puppeteer']._locate(selector);
  }

  async hasSelector(selector) {
    return (await this.locateSelector(selector)).length;
  }

  /**
   * Finds element described by locator.
   * If element cannot be found immediately function waits specified amount of time or globally configured `waitForTimeout` period.
   * If element still cannot be found after the waiting time an undefined is returned.
   *
   * @param locator - element CSS locator
   * @param sec - optional time in seconds to wait
   * @returns {Promise<undefined|*>} - promise holding either an element or undefined if element is not found
   */
  async waitForSelector(locator, sec) {
    const waitTimeout = sec ? sec * 1000 : this.helpers['Puppeteer'].options.waitForTimeout;
    const context = await this.helpers['Puppeteer']._getContext();
    try {
      return await context.waitForSelector(locator, {timeout: waitTimeout});
    } catch (error) {
      return undefined;
    }
  }
};
