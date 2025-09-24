import IdamActions from '../../../actions/ui/idam/idam-actions';
import exuiUsers from '../../../config/users/exui-users';
import { AllMethodsStep } from '../../../decorators/test-steps';
import User from '../../../models/user';

@AllMethodsStep()
export default class IdamSteps {
  private idamActions: IdamActions;

  constructor(idamActions: IdamActions) {
    this.idamActions = idamActions;
  }

  async ClaimantSolicitorLogin() {
    await this.idamActions.exuiLogin(exuiUsers.claimantSolicitorUser);
  }

  async ClaimantSolicitorBulkScanLogin() {
    await this.idamActions.exuiLogin(exuiUsers.claimantSolicitorBulkScanUser);
  }

  async DefendantSolicitor1Login() {
    await this.idamActions.exuiLogin(exuiUsers.defendantSolicitor1User);
  }

  async DefendantSolicitor2Login() {
    await this.idamActions.exuiLogin(exuiUsers.defendantSolicitor2User);
  }

  async CivilAdminLogin() {
    await this.idamActions.exuiLogin(exuiUsers.civilAdminUser);
  }

  async NBCRegion1Login() {
    await this.idamActions.exuiLogin(exuiUsers.nbcRegion1User);
  }

  async NBCRegion2Login() {
    await this.idamActions.exuiLogin(exuiUsers.nbcRegion2User);
  }

  async NBCRegion4Login() {
    await this.idamActions.exuiLogin(exuiUsers.nbcRegion4User);
  }

  async JudgeRegion1Login() {
    await this.idamActions.exuiLogin(exuiUsers.judgeRegion1User);
  }

  async JudgeRegion2Login() {
    await this.idamActions.exuiLogin(exuiUsers.judgeRegion2User);
  }

  async JudgeRegion4Login() {
    await this.idamActions.exuiLogin(exuiUsers.judgeRegion4User);
  }

  async HearingCentreAdmin1Login() {
    await this.idamActions.exuiLogin(exuiUsers.hearingCenterAdminRegion1User);
  }

  async HearingCentreAdmin2Login() {
    await this.idamActions.exuiLogin(exuiUsers.hearingCenterAdminRegion2User);
  }

  async HearingCentreAdmin4Login() {
    await this.idamActions.exuiLogin(exuiUsers.hearingCenterAdminRegion4User);
  }

  async TribunalCaseworkerRegion4Login() {
    await this.idamActions.exuiLogin(exuiUsers.tribunalCaseworkerRegion4User);
  }

  async ExuiLogin(user: User) {
    await this.idamActions.exuiLogin(user);
  }
}
