import BasePageFactory from '../../base/base-page-factory';
import PageCookiesManager from './page-cookies-manager';

export default class PageUtilsFactory extends BasePageFactory {
  get pageCookiesManager() {
    return new PageCookiesManager(this.page);
  }
}
