import BasePageFactory from '../../../../base/base-page-factory.ts';
import DateFragment from '../../fragments/date/date-fragment.ts';
import CaseProceedsInCasemanLRPage from './case-proceeds-in-caseman-lr/case-proceeds-in-caseman-lr-page.ts';
import CaseProceedsInCasemanPage from './case-proceeds-in-caseman/case-proceeds-in-caseman-page.ts';

export default class CaseProceedsInCasemanPageFactory extends BasePageFactory {
  get caseProceedsInCasemanLRPage() {
    const dateFragment = new DateFragment(this.page);
    return new CaseProceedsInCasemanLRPage(this.page, dateFragment);
  }

  get caseProceedsInCasemanPage() {
    const dateFragment = new DateFragment(this.page);
    return new CaseProceedsInCasemanPage(this.page, dateFragment);
  }
}
