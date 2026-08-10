import { AllMethodsStep } from '../../decorators/test-steps';
import User from '../../models/users/user';
import BaseApi from '../../base/base-api';
import { defendantSolicitor1User, defendantSolicitor2User } from '../../config/users/exui-users';
import CaseRole from '../../constants/cases/case-role';
import UserAssignedCasesHelper from '../../helpers/user-assigned-cases-helper';

@AllMethodsStep()
export default class CaseRoleAssignmentApiSteps extends BaseApi {
  async AssignCaseRoleToDS1() {
    await this.setupApiStep(defendantSolicitor1User);
    const { civilServiceRequests } = this.requestsFactory;
    await civilServiceRequests.assignCaseToDefendant(
      defendantSolicitor1User,
      CaseRole.RESPONDENT_SOLICITOR_ONE,
      this.ccdCaseData?.id,
    );
    await super.fetchAndSetCCDCaseData();
    UserAssignedCasesHelper.addAssignedCaseToUser(defendantSolicitor1User, this.ccdCaseData?.id);
  }

  async AssignCaseRoleToDS2() {
    await this.setupApiStep(defendantSolicitor2User);
    const { civilServiceRequests } = this.requestsFactory;
    await civilServiceRequests.assignCaseToDefendant(
      defendantSolicitor2User,
      CaseRole.RESPONDENT_SOLICITOR_TWO,
      this.ccdCaseData?.id,
    );
    await super.fetchAndSetCCDCaseData();
    UserAssignedCasesHelper.addAssignedCaseToUser(defendantSolicitor2User, this.ccdCaseData?.id);
  }

  async UnassignCasesForUser(user: User) {
    await this.setupApiStep(user);
    const assignedCases = await UserAssignedCasesHelper.getUserAssignedCases(user);
    if (assignedCases) {
      const { civilServiceRequests } = this.requestsFactory;
      await civilServiceRequests.unassignUserFromCases(user, assignedCases);
    }
  }
}
