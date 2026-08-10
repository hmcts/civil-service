'use strict';

// eslint-disable-next-line no-undef
const Helper = codecept_helper;
const helperName = 'Playwright';
class PlaywrightHelper extends Helper {

  async waitForNavigationToComplete(locator) {
    const page = this.helpers[helperName].page;
    await page.waitForSelector(locator, 10);
    await Promise.all([
      page.waitForNavigation(0),
      await page.click(locator)
    ]);
  }

  async reloadPage() {
    const page = this.helpers[helperName].page;
    await page.reload({waitUntil:'commit'});
  }

  async clickTab(tabTitle) {
    const page = this.helpers[helperName].page;
    const tabSelector = `div[role='tab'] >> '${tabTitle}'`;
    await page.locator(tabSelector).click();
  }
}
module.exports = PlaywrightHelper;
