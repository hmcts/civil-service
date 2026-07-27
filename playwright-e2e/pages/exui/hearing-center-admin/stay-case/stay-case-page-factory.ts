import BasePageFactory from '../../../../base/base-page-factory';
import ConfirmStayCasePage from './confirm-stay-case/confirm-stay-case-page';
import StayCasePage from './stay-case/stay-case-page';

export default class StayCasePageFactory extends BasePageFactory {
  get stayCasePage() {
    return new StayCasePage(this.page);
  }

  get confirmStayCasePage() {
    return new ConfirmStayCasePage(this.page);
  }
}
