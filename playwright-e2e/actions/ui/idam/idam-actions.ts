import User from '../../../models/user';
import config from '../../../config/config';
import { AllMethodsStep } from '../../../decorators/test-steps';
import TestData from '../../../models/test-data';
import BaseApi from '../../../base/base-api';
import RequestsFactory from '../../../requests/requests-factory';
import CookiesHelper from '../../../helpers/cookies-helper';
import IdamPageFactory from '../../../pages/idam/idam-page-factory';
import PageUtilsFactory from '../../../pages/utils/page-utils-factory';

@AllMethodsStep()
export default class IdamActions extends BaseApi {
  private isSetupTest: boolean;
  private isTeardownTest: boolean;
  private verifyCookiesBanner: boolean;
  private pageUtilsFactory: PageUtilsFactory;
  private idamPageFactory: IdamPageFactory;

  constructor(
    pageUtilsFactory: PageUtilsFactory,
    idamPageFactory: IdamPageFactory,
    requestsFactory: RequestsFactory,
    isSetupTest: boolean,
    isTeardownTest: boolean,
    verifyCookiesBanner: boolean,
    testData: TestData,
  ) {
    super(requestsFactory, testData);
    this.isSetupTest = isSetupTest;
    this.isTeardownTest = isTeardownTest;
    this.verifyCookiesBanner = verifyCookiesBanner;
    this.pageUtilsFactory = pageUtilsFactory;
    this.idamPageFactory = idamPageFactory;
  }

  async exuiLogin(user: User) {
    const { pageCookiesManager } = this.pageUtilsFactory;
    await pageCookiesManager.cookiesSignOut();
    if (!config.runSetup || this.isSetupTest || !(await CookiesHelper.cookiesExist(user))) {
      const { loginPage } = this.idamPageFactory;

      if (this.verifyCookiesBanner) {
        const { idamsCookiesBanner } = this.idamPageFactory;
        await loginPage.openManageCase();
        await idamsCookiesBanner.verifyContent();
        await idamsCookiesBanner.acceptCookies();
      } else {
        await pageCookiesManager.addIdamCookies();
        await this.setupUserData(user);
        await pageCookiesManager.addExuiCookies(user);
        await loginPage.openManageCase();
      }
      await loginPage.verifyContent();
      await loginPage.manageCaseLogin(user);
    } else {
      const cookies = await CookiesHelper.getCookies(user, this.isTeardownTest);
      await pageCookiesManager.cookiesLogin(user, cookies);
    }
  }
}
