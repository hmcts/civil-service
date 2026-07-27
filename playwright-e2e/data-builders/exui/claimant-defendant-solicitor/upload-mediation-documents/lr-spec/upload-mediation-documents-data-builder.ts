import BaseDataBuilder from '../../../../../base/base-data-builder';
import {
  claimantSolicitorUser,
  defendantSolicitor1User,
  defendantSolicitor2User,
} from '../../../../../config/users/exui-users';
import ClaimType from '../../../../../constants/cases/claim-type';
import partys from '../../../../../constants/users/partys';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import { ClaimantDefendantPartyType } from '../../../../../models/users/claimant-defendant-party-types';
import { Party } from '../../../../../models/users/partys';
import User from '../../../../../models/users/user';
import uploadMediationDocumentsDataBuilderComponents from './upload-mediation-documents-data-builder-components';



@AllMethodsStep({ methodNamesToIgnore: ['buildData'] })
export default class UploadMediationDocumentsDataBuilder extends BaseDataBuilder {
  async buildDS1() {
    return this.buildData({claimantDefendantSolicitorParty: partys.DEFENDANT_SOLICITOR_1})
  }

  async buildDS2() {
    return this.buildData({claimantDefendantSolicitorParty: partys.DEFENDANT_SOLICITOR_2})
  }

  async buildCS1() {
    return this.buildData()
  }

  async buildCS12v1() {
    return this.buildData({ claimType: ClaimType.TWO_VS_ONE })
  }

  protected async buildData({
    claimType = ClaimType.ONE_VS_ONE,
    claimantDefendantSolicitorParty = partys.CLAIMANT_SOLICITOR_1,
  } : {
    claimType?: ClaimType,
    claimantDefendantSolicitorParty?: Party
  } = {}) {
    const uploadUsersByParty = new Map<Party, User>([
      [partys.CLAIMANT_SOLICITOR_1, claimantSolicitorUser],
      [partys.DEFENDANT_SOLICITOR_1, defendantSolicitor1User],
      [partys.DEFENDANT_SOLICITOR_2, defendantSolicitor2User],
    ]);
    const user = uploadUsersByParty.get(claimantDefendantSolicitorParty);

    const { civilServiceRequests } = this.requestsFactory;
    const nonAttendanceStatementDoc = await civilServiceRequests.uploadTestDocument(user!);
    const referredDoc = await civilServiceRequests.uploadTestDocument(user!);

    return {
      ...uploadMediationDocumentsDataBuilderComponents.explanation,
      ...uploadMediationDocumentsDataBuilderComponents.whoIsDocumentFor(
        claimType,
        claimantDefendantSolicitorParty,
        this.claimant1PartyType!,
        this.defendant1PartyType!,
        this.defendant2PartyType!,
      ),
      ...uploadMediationDocumentsDataBuilderComponents.documentType,
      ...uploadMediationDocumentsDataBuilderComponents.documentUpload(
        nonAttendanceStatementDoc,
        referredDoc,
        claimantDefendantSolicitorParty,
      ),
    };
  }
}
