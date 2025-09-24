import BasePageFactory from '../../../base/base-page-factory';
import CaseDetailsPage from './case-details/case-details-page';
import CaseFilterPage from './case-filter/case-filter-page';
import CaseListPage from './case-list/case-list-page';
import ExuiCookiesBanner from './exui-cookies-banner/exui-cookies-banner';
import ExuiNavBar from './exui-nav-bar/exui-nav-bar';

export default class ExuiDashboardPageFactory extends BasePageFactory {
  get exuiCookiesBanner() {
    return new ExuiCookiesBanner(this.page);
  }

  get navBar() {
    return new ExuiNavBar(this.page);
  }

  get caseListPage() {
    return new CaseListPage(this.page);
  }

  get caseFilterPage() {
    return new CaseFilterPage(this.page);
  }

  get caseDetailsPage() {
    return new CaseDetailsPage(this.page);
  }
}
