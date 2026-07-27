import BaseDataBuilder from '../../../../../base/base-data-builder';
import ClaimType from '../../../../../constants/cases/claim-type';
import DefendantResponseType from '../../../../../constants/ccd-events/defendant-response/unspec/defendant-response-type';
import partys from '../../../../../constants/users/partys';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import { Party } from '../../../../../models/users/partys';
import acknowledgeClaimDataBuilderComponents from './acknowledge-claim-data-builder-components';

@AllMethodsStep()
export default class AcknowledgeClaimDataBuilder extends BaseDataBuilder {
  async buildDataDS1FullDefence() {
    return this.buildData({ claimType: ClaimType.ONE_VS_ONE });
  }

  async buildDataDS1FullDefence2v1() {
    return this.buildData({ claimType: ClaimType.TWO_VS_ONE });
  }

  async buildDataDS1FullDefence1v2SS() {
    return this.buildData({ claimType: ClaimType.ONE_VS_TWO_SAME_SOL });
  }

  async buildDataDS2FullDefence() {
    return this.buildData({
      claimType: ClaimType.ONE_VS_TWO_DIFF_SOL,
      defendantSolicitorParty: partys.DEFENDANT_SOLICITOR_2,
    });
  }

  protected async buildData(
    {
      claimType = ClaimType.ONE_VS_ONE,
      defendantResponseType = DefendantResponseType.FULL_DEFENCE,
      defendantSolicitorParty = partys.DEFENDANT_SOLICITOR_1
    } : {
      claimType?: ClaimType,
      defendantResponseType?: DefendantResponseType
      defendantSolicitorParty?: Party
    } = {}
  ) {
    return {
      ...acknowledgeClaimDataBuilderComponents.confirmNameAddress(this.ccdCaseData, defendantSolicitorParty),
      ...acknowledgeClaimDataBuilderComponents.responseIntention(claimType, defendantResponseType, defendantSolicitorParty),
      ...acknowledgeClaimDataBuilderComponents.solicitorReferences(this.ccdCaseData, claimType, defendantSolicitorParty),
    };
  }
}
