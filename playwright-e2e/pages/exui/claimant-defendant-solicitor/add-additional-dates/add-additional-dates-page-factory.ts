import BasePageFactory from '../../../../base/base-page-factory';
import DateFragment from '../../fragments/date/date-fragment';
import AddAdditionalDatesPage from './unspec/add-additional-dates/add-additional-dates-page';
import ConfirmAddAdditionalDatesPage from './unspec/confirm-add-additional-dates/confirm-add-additional-dates-page';
import SubmitAddAdditionalDatesPage from './unspec/submit-add-additional-dates/submit-add-additional-dates-page';

export default class AddAdditionalDatesPageFactory extends BasePageFactory {
  get addAdditionalDatesPage() {
    return new AddAdditionalDatesPage(this.page, new DateFragment(this.page));
  }

  get submitAddAdditionalDatesPage() {
    return new SubmitAddAdditionalDatesPage(this.page);
  }

  get confirmAddAdditionalDatesPage() {
    return new ConfirmAddAdditionalDatesPage(this.page);
  }
}
