import ClaimType from '../enums/claim-type';

export default class ClaimTypeHelper {
  static isClaimant2(claimType: ClaimType) {
    return claimType === ClaimType.TWO_VS_ONE || claimType === ClaimType.TWO_VS_ONE_LIP;
  }

  static isDefendant1Represented(claimType: ClaimType) {
    return (
      claimType === ClaimType.ONE_VS_ONE ||
      claimType === ClaimType.ONE_VS_TWO_DIFF_SOL ||
      claimType === ClaimType.ONE_VS_TWO_LR_LIP ||
      claimType === ClaimType.ONE_VS_TWO_SAME_SOL ||
      claimType === ClaimType.TWO_VS_ONE
    );
  }

  static isDefendant1RepresentedNotSame(claimType: ClaimType) {
    return claimType === ClaimType.ONE_VS_TWO_DIFF_SOL || claimType === ClaimType.ONE_VS_TWO_LR_LIP;
  }

  static isDefendant1Unrepresented(claimType: ClaimType) {
    return (
      claimType === ClaimType.ONE_VS_ONE_LIP ||
      claimType === ClaimType.ONE_VS_TWO_LIP_LR ||
      claimType === ClaimType.TWO_VS_ONE_LIP
    );
  }

  static isDefendant2(claimType: ClaimType) {
    return (
      claimType === ClaimType.ONE_VS_TWO_DIFF_SOL ||
      claimType === ClaimType.ONE_VS_TWO_LR_LIP ||
      claimType === ClaimType.ONE_VS_TWO_SAME_SOL ||
      claimType === ClaimType.ONE_VS_TWO_LIPS ||
      claimType === ClaimType.ONE_VS_TWO_LIP_LR
    );
  }

  static isDefendant2Represented(claimType: ClaimType) {
    return (
      claimType === ClaimType.ONE_VS_TWO_SAME_SOL ||
      claimType === ClaimType.ONE_VS_TWO_DIFF_SOL ||
      claimType === ClaimType.ONE_VS_TWO_LIP_LR
    );
  }

  static isDefendant2RepresentedNotSame(claimType: ClaimType) {
    return claimType === ClaimType.ONE_VS_TWO_DIFF_SOL || claimType === ClaimType.ONE_VS_TWO_LIP_LR;
  }

  static isDefendant2Unrepresented(claimType: ClaimType) {
    return claimType === ClaimType.ONE_VS_TWO_LIPS || claimType === ClaimType.ONE_VS_TWO_LR_LIP;
  }
}
