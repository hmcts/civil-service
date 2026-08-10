import BasePageFactory from '../../../../base/base-page-factory';
import ConfirmManageStayLiftStayPage from './confirm-manage-stay/confirm-manage-stay-lift-stay-page';
import SubmitManageStayPage from './submit-manage-stay/submit-manage-stay-page';
import ManageStayJudicialReferralInMediationPage from './manage-stay-judical-referral-in-mediation/manage-stay-judical-referral-in-mediation-page';
import ManageStayOptionsPage from './manage-stay-options/manage-stay-options-page';
import ManageStayRequestUpdatePage from './manage-stay-request-update/manage-stay-request-update-page';
import ConfirmManageStayRequestUpdatePage from './confirm-manage-stay/confirm-manage-stay-request-update-page';

export default class ManageStayPageFactory extends BasePageFactory {
  get manageStayOptionsPage() {
    return new ManageStayOptionsPage(this.page);
  }

  get manageStayRequestUpdatePage() {
    return new ManageStayRequestUpdatePage(this.page);
  }

  get manageStayJudicialReferralInMediationPage() {
    return new ManageStayJudicialReferralInMediationPage(this.page);
  }

  get submitManageStayPage() {
    return new SubmitManageStayPage(this.page);
  }

  get confirmManageStayRequestUpdatePage() {
    return new ConfirmManageStayRequestUpdatePage(this.page);
  }

  get confirmManageStayLiftStayPage() {
    return new ConfirmManageStayLiftStayPage(this.page);
  }
}
