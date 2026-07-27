import BaseDataBuilder from '../../../../base/base-data-builder';
import {
  defendantSolicitor1User,
  defendantSolicitor2User,
} from '../../../../config/users/exui-users';
import ClaimTrack from '../../../../constants/cases/claim-track';
import ClaimType from '../../../../constants/cases/claim-type';
import partys from '../../../../constants/users/partys';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import { Party } from '../../../../models/users/partys';
import User from '../../../../models/users/user';
import evidenceUploadRespondentDataBuilderComponents from './evidence-upload-respondent-data-builder-components';

@AllMethodsStep()
export default class EvidenceUploadRespondentDataBuilder extends BaseDataBuilder {
  async buildDS1Fast() {
    return this.buildData({ claimTrack: ClaimTrack.FAST_CLAIM });
  }

  async buildDS1Fast1v2SS() {
    return this.buildData({ claimTrack: ClaimTrack.FAST_CLAIM, claimType: ClaimType.ONE_VS_TWO_SAME_SOL });
  }

  async buildDS2Fast1v2SS() {
    return this.buildData({
      claimTrack: ClaimTrack.FAST_CLAIM,
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
      user: defendantSolicitor2User,
    });
  }

  async buildDS1Small() {
    return this.buildData();
  }

  protected async buildData(
    {
      claimTrack = ClaimTrack.SMALL_CLAIM,
      claimType = ClaimType.ONE_VS_ONE,
      witness1Party = partys.DEFENDANT_1_WITNESS_1,
      witness2Party = partys.DEFENDANT_1_WITNESS_2,
      expertParty = partys.DEFENDANT_1_EXPERT_1,
      user = defendantSolicitor1User
    } : {
      claimTrack?: ClaimTrack,
      claimType?: ClaimType
      witness1Party?: Party,
      witness2Party?: Party,
      expertParty?: Party,
      user?: User
    } = {}
  ) {
    const { civilServiceRequests } = this.requestsFactory;
    const doc1 = await civilServiceRequests.uploadTestDocument(user);
    const doc2 = await civilServiceRequests.uploadTestDocument(user);
    const doc3 = await civilServiceRequests.uploadTestDocument(user);
    const doc4 = await civilServiceRequests.uploadTestDocument(user);

    return {
      ...evidenceUploadRespondentDataBuilderComponents.evidenceUpload,
      ...evidenceUploadRespondentDataBuilderComponents.selectUploadOptions(claimType),
      ...evidenceUploadRespondentDataBuilderComponents.documentSelection(claimTrack),
      ...evidenceUploadRespondentDataBuilderComponents.documentUploadFastTrack(
        claimTrack,
        witness1Party,
        expertParty,
        doc1,
        doc2,
        doc3,
        doc4,
      ),
      ...evidenceUploadRespondentDataBuilderComponents.documentUploadSmallClaim(
        claimTrack,
        witness1Party,
        witness2Party,
        expertParty,
        doc1,
        doc2,
        doc3,
        doc4,
      ),
      ...evidenceUploadRespondentDataBuilderComponents.undefine
    };
  }
}
