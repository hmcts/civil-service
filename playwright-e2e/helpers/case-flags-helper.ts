import caseFlagLocations from '../constants/case-flags/case-flag-locations';
import CaseFlags, { CaseFlagDetails } from '../models/case-flag';
import CCDCaseData, {
  ClaimantDefendant,
  LitigationFriend,
  ExpertAndWitness,
  CCDCaseFlagsDetails,
} from '../models/ccd/ccd-case-data';
import DateHelper from './date-helper';

export default class CaseFlagsHelper {
  static updateCaseFlagsObject(caseFlagsObjectToBeUpdated: CaseFlags, caseFlagsObject: CaseFlags) {
    caseFlagsObjectToBeUpdated.caseFlagsDetails.push(...caseFlagsObject.caseFlagsDetails);
    caseFlagsObjectToBeUpdated.activeCaseFlags += caseFlagsObject.activeCaseFlags;
  }

  private static checkForOtherCaseFlagType(ccdCaseFlagDetails: CCDCaseFlagsDetails) {
    return ccdCaseFlagDetails.value?.name === 'Other'
      ? ccdCaseFlagDetails.value?.otherDescription
      : ccdCaseFlagDetails.value?.name;
  }

  static getCaseFlagsForClaimantDefendant(claimantDefendant?: ClaimantDefendant): CaseFlags {
    const caseFlagDetails: CaseFlagDetails[] =
      claimantDefendant?.flags?.details?.map((detail) => {
        console.log(detail.value.dateTimeCreated);
        return {
          caseFlagLocation: `${claimantDefendant.flags.partyName} (${claimantDefendant.flags.roleOnCase})`,
          caseFlagType: this.checkForOtherCaseFlagType(detail),
          active: detail.value.status === 'Active',
          creationDate: DateHelper.formatDateToString(detail.value.dateTimeCreated, {
            outputFormat: 'DD Mon YYYY',
          }),
          caseFlagComment: detail.value.flagComment,
        };
      }) || [];
    const activeCaseFlags = caseFlagDetails?.filter((caseFlag) => caseFlag.active).length || 0;
    if (claimantDefendant) {
      console.log(
        `Total case flags: ${caseFlagDetails.length} for party ${claimantDefendant?.flags?.partyName}`,
      );
      console.log(
        `Active case flags: ${activeCaseFlags} for party ${claimantDefendant?.partyName}`,
      );
    }
    return { caseFlagsDetails: caseFlagDetails, activeCaseFlags };
  }

  static getCaseFlagsForLitigationFriend(litigationFriend?: LitigationFriend): CaseFlags {
    const caseFlagDetails: CaseFlagDetails[] =
      litigationFriend?.flags?.details?.map((detail) => ({
        caseFlagLocation: `${litigationFriend.flags.partyName} (${litigationFriend.flags.roleOnCase})`,
        caseFlagType: this.checkForOtherCaseFlagType(detail),
        active: detail.value.status === 'Active',
        creationDate: DateHelper.formatDateToString(detail.value.dateTimeCreated, {
          outputFormat: 'DD Mon YYYY',
        }),
        caseFlagComment: detail.value.flagComment,
      })) || [];
    const activeCaseFlags = caseFlagDetails?.filter((caseFlag) => caseFlag.active).length || 0;
    if (litigationFriend) {
      console.log(
        `Total case flags: ${caseFlagDetails.length} for party ${litigationFriend?.flags?.partyName}`,
      );
      console.log(
        `Active case flags: ${activeCaseFlags} for party ${litigationFriend?.flags?.partyName}`,
      );
    }
    return { caseFlagsDetails: caseFlagDetails, activeCaseFlags };
  }

  static getCaseFlagsForExpertAndWitness(expertOrWitnesses?: ExpertAndWitness[]): CaseFlags {
    const caseFlags: CaseFlags = { caseFlagsDetails: [], activeCaseFlags: 0 };
    if (expertOrWitnesses) {
      for (const expertOrWitness of expertOrWitnesses) {
        const caseFlagDetails: CaseFlagDetails[] =
          expertOrWitness?.value?.flags?.details?.map((detail) => ({
            caseFlagLocation: `${expertOrWitness?.value?.flags?.partyName} (${expertOrWitness?.value?.flags?.roleOnCase})`,
            caseFlagType: this.checkForOtherCaseFlagType(detail),
            active: detail.value.status === 'Active',
            creationDate: DateHelper.formatDateToString(detail.value.dateTimeCreated, {
              outputFormat: 'DD Mon YYYY',
            }),
            caseFlagComment: detail.value.flagComment,
          })) || [];
        const activeCaseFlags = caseFlagDetails?.filter((caseFlag) => caseFlag.active).length || 0;
        if (expertOrWitness) {
          console.log(
            `Total case flags: ${caseFlagDetails.length} for party ${expertOrWitness?.value?.flags?.partyName}`,
          );
          console.log(
            `Active case flags: ${activeCaseFlags} for party ${expertOrWitness?.value?.flags?.partyName}`,
          );
        }
        this.updateCaseFlagsObject(
          { caseFlagsDetails: caseFlagDetails, activeCaseFlags },
          caseFlags,
        );
      }
    }
    return caseFlags;
  }

  static getCaseLevelFlags(ccdCaseData?: CCDCaseData): CaseFlags {
    const caseFlagDetails: CaseFlagDetails[] =
      ccdCaseData?.caseFlags?.details?.map((detail) => ({
        caseFlagLocation: caseFlagLocations.CASE_LEVEL,
        caseFlagType: this.checkForOtherCaseFlagType(detail),
        active: detail.value.status === 'Active',
        creationDate: DateHelper.formatDateToString(detail.value.dateTimeCreated, {
          outputFormat: 'DD Mon YYYY',
        }),
        caseFlagComment: detail.value.flagComment,
      })) || [];
    const activeCaseFlags =
      ccdCaseData?.caseFlags?.details?.filter((detail) => detail.value.status === 'Active')
        ?.length || 0;
    console.log(`Total case flags: ${caseFlagDetails.length} for ${caseFlagLocations.CASE_LEVEL}`);
    console.log(`Active case flags: ${activeCaseFlags} for ${caseFlagLocations.CASE_LEVEL}`);
    return { caseFlagsDetails: caseFlagDetails, activeCaseFlags };
  }
}
