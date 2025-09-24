import { AllMethodsStep } from '../../decorators/test-steps';
import User from '../../models/user';
import BaseApi from '../../base/base-api';
import { defendantSolicitor1User, defendantSolicitor2User } from '../../config/users/exui-users';
import CaseRole from '../../enums/case-role';
import UserAssignedCasesHelper from '../../helpers/user-assigned-cases-helper';

@AllMethodsStep()
export default class CaseRoleAssignmentApiSteps extends BaseApi {
  async AssignCaseRoleToDS1() {
    await this.setupUserData(defendantSolicitor1User);
    const { civilServiceRequests } = this.requestsFactory;
    await civilServiceRequests.assignCaseToDefendant(
      defendantSolicitor1User,
      this.ccdCaseData.id,
      CaseRole.RESPONDENT_SOLICITOR_ONE,
    );
    UserAssignedCasesHelper.addAssignedCaseToUser(defendantSolicitor1User, this.ccdCaseData.id);
  }

  async AssignCaseRoleToDS2() {
    await this.setupUserData(defendantSolicitor2User);
    const { civilServiceRequests } = this.requestsFactory;
    await civilServiceRequests.assignCaseToDefendant(
      defendantSolicitor2User,
      this.ccdCaseData.id,
      CaseRole.RESPONDENT_SOLICITOR_TWO,
    );
    UserAssignedCasesHelper.addAssignedCaseToUser(defendantSolicitor2User, this.ccdCaseData.id);
  }

  async UnassignCasesForUser(user: User) {
    const assignedCases = await UserAssignedCasesHelper.getUserAssignedCases(user);
    if (assignedCases) {
      await this.setupUserData(user);
      const { civilServiceRequests } = this.requestsFactory;
      await civilServiceRequests.unassignUserFromCases(user, assignedCases);
    }
  }
}
