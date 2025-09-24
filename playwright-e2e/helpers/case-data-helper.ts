import { v4 as uuidv4 } from 'uuid';
import StringHelper from './string-helper';
import ClaimTrack from '../enums/claim-track';
import { Party } from '../models/partys';
import partys from '../constants/partys';
import { ClaimantDefendantPartyType } from '../models/claimant-defendant-party-types';
import claimantDefendantPartyTypes from '../constants/claimant-defendant-party-types';
import CCDCaseData, {
  ExpertAndWitness,
  ClaimantDefendant,
  LitigationFriend,
} from '../models/ccd/ccd-case-data';
import CaseFlags, { CaseFlagDetails } from '../models/case-flag';
import caseFlagLocations from '../constants/case-flags/case-flag-locations';

export default class CaseDataHelper {
  static getNextClaimNumber() {
    return '00' + Math.random().toString(36).slice(-6);
  }

  static setCodeToData(data: any) {
    return {
      code: uuidv4(),
      label: data,
    };
  }

  static setIdToData(data: any) {
    return {
      id: uuidv4(),
      value: data,
    };
  }

  static getPartyDateOfBirth(party: Party) {
    switch (party) {
      case partys.CLAIMANT_1:
        return '1980-05-24';
      case partys.CLAIMANT_2:
        return '1992-08-11';
      case partys.CLAIMANT_1_LITIGATION_FRIEND:
        return '1987-03-17';
      case partys.CLAIMANT_2_LITIGATION_FRIEND:
        return '1995-12-02';
      case partys.DEFENDANT_1:
        return '1984-01-30';
      case partys.DEFENDANT_2:
        return '1990-07-19';
      case partys.DEFENDANT_1_LITIGATION_FRIEND:
        return '1982-09-25';
      case partys.DEFENDANT_2_LITIGATION_FRIEND:
        return '1989-04-06';
    }
  }

  static getPartyPhoneNumber(party: Party) {
    switch (party) {
      case partys.CLAIMANT_1:
        return '07123456789';
      case partys.CLAIMANT_2:
        return '07890123456';
      case partys.CLAIMANT_1_LITIGATION_FRIEND:
        return '07984234567';
      case partys.CLAIMANT_2_LITIGATION_FRIEND:
        return '07456987654';
      case partys.CLAIMANT_EXPERT_1:
        return '07688543210';
      case partys.CLAIMANT_EXPERT_2:
        return '07872345678';
      case partys.CLAIMANT_WITNESS_1:
        return '07501234567';
      case partys.CLAIMANT_WITNESS_2:
        return '07984112233';
      case partys.CLAIMANT_1_MEDIATION_FRIEND:
        return '07984224466';
      case partys.DEFENDANT_1:
        return '07853654321';
      case partys.DEFENDANT_2:
        return '07712345678';
      case partys.DEFENDANT_1_LITIGATION_FRIEND:
        return '07906789012';
      case partys.DEFENDANT_2_LITIGATION_FRIEND:
        return '07321654987';
      case partys.DEFENDANT_SOLICITOR_1:
        return '07987654321';
      case partys.DEFENDANT_SOLICITOR_2:
        return '07987654325';
      case partys.DEFENDANT_1_EXPERT_1:
        return '07311987654';
      case partys.DEFENDANT_1_EXPERT_2:
        return '07615876543';
      case partys.DEFENDANT_2_EXPERT_1:
        return '07893654321';
      case partys.DEFENDANT_2_EXPERT_2:
        return '07456123890';
      case partys.DEFENDANT_1_WITNESS_1:
        return '07865432109';
      case partys.DEFENDANT_1_WITNESS_2:
        return '07713659876';
      case partys.DEFENDANT_2_WITNESS_1:
        return '07592345612';
      case partys.DEFENDANT_2_WITNESS_2:
        return '07985674230';
      case partys.DEFENDANT_1_MEDIATION_FRIEND:
        return '07985366442';
      case partys.DEFENDANT_2_MEDIATION_FRIEND:
        return '07985685321';
    }
  }

  static getPartyPostCode(party: Party) {
    switch (party) {
      case partys.CLAIMANT_1:
        return 'W1A 1AA';
      case partys.CLAIMANT_2:
        return 'BS1 4ST';
      case partys.CLAIMANT_1_LITIGATION_FRIEND:
        return 'CF10 1EP';
      case partys.CLAIMANT_2_LITIGATION_FRIEND:
        return 'LS1 4AP';
      case partys.CLAIMANT_SOLICITOR_1:
        return 'SW1A 1AA';
      case partys.DEFENDANT_1:
        return 'M1 1AE';
      case partys.DEFENDANT_2:
        return 'TN23 1LE';
      case partys.DEFENDANT_1_LITIGATION_FRIEND:
        return 'SO15 2JY';
      case partys.DEFENDANT_2_LITIGATION_FRIEND:
        return 'B1 1AA';
      case partys.DEFENDANT_SOLICITOR_1:
        return 'EX1 1JG';
      case partys.DEFENDANT_SOLICITOR_2:
        return 'M4 5DL';
    }
  }

  static getExpertEstimatedCost(expertParty: Party) {
    switch (expertParty) {
      case partys.CLAIMANT_EXPERT_1:
        return '587';
      case partys.CLAIMANT_EXPERT_2:
        return '344';
      case partys.DEFENDANT_1_EXPERT_1:
        return '762';
      case partys.DEFENDANT_1_EXPERT_2:
        return '231';
      case partys.DEFENDANT_2_EXPERT_1:
        return '915';
      case partys.DEFENDANT_2_EXPERT_2:
        return '478';
    }
  }

  static buildAddressData(party: Party) {
    return {
      AddressLine1: `Flat 12 - ${party.key}`,
      AddressLine2: `House 15 - 17 - ${party.key}`,
      AddressLine3: `Street - ${party.key} `,
      PostTown: `Town - ${party.key}`,
      County: `County - ${party.key}`,
      Country: `Country - ${party.key}`,
      PostCode: this.getPartyPostCode(party),
    };
  }

  static buildClaimantAndDefendantData(
    claimantDefendantParty: Party,
    claimantDefendantPartyType: ClaimantDefendantPartyType,
  ): any {
    const commonPartyData = {
      type: claimantDefendantPartyType.type,
      partyEmail: `${claimantDefendantParty.key}@${claimantDefendantPartyType.key}.com`,
      partyPhone: this.getPartyPhoneNumber(claimantDefendantParty),
      primaryAddress: this.buildAddressData(claimantDefendantParty),
    };

    const partyKey = StringHelper.capitalise(claimantDefendantParty.key);
    const partyTypeKey = StringHelper.capitalise(claimantDefendantPartyType.key);

    switch (claimantDefendantPartyType) {
      case claimantDefendantPartyTypes.INDIVIDUAL:
        return {
          ...commonPartyData,
          individualTitle: 'Mx',
          individualFirstName: partyKey,
          individualLastName: partyTypeKey,
          individualDateOfBirth: this.getPartyDateOfBirth(claimantDefendantParty),
          partyName: `Mx ${partyKey} ${partyTypeKey}`,
        };

      case claimantDefendantPartyTypes.COMPANY:
        return {
          ...commonPartyData,
          companyName: `${partyKey} ${partyTypeKey}`,
          partyName: `${partyKey} ${partyTypeKey}`,
        };

      case claimantDefendantPartyTypes.SOLE_TRADER:
        return {
          ...commonPartyData,
          soleTraderTitle: 'Mx',
          soleTraderFirstName: partyKey,
          soleTraderLastName: partyTypeKey,
          soleTraderTradingAs: `${partyKey} Trade`,
          soleTraderDateOfBirth: this.getPartyDateOfBirth(claimantDefendantParty),
          partyName: `Mx ${partyKey} ${partyTypeKey}`,
        };
      case claimantDefendantPartyTypes.ORGANISATION:
        return {
          ...commonPartyData,
          organisationName: `${partyKey} ${partyTypeKey}`,
          partyName: `${partyKey} ${partyTypeKey}`,
        };
    }
  }

  static buildUnregisteredOrganisationData(solicitorParty: Party) {
    return {
      address: this.buildAddressData(solicitorParty),
      organisationName: `${solicitorParty.key} - Solicitors`,
      phoneNumber: this.getPartyPhoneNumber(solicitorParty),
      email: `${solicitorParty.key}@solicitor.com`,
      DX: `123 - ${solicitorParty.key}`,
      fax: `5550234 - ${solicitorParty.key}`,
    };
  }

  static buildLitigationFriendData(litigationFriendParty: Party) {
    return {
      firstName: StringHelper.capitalise(litigationFriendParty.key),
      lastName: 'Litigation',
      emailAddress: `${litigationFriendParty.key}@litigants.com`,
      phoneNumber: this.getPartyPhoneNumber(litigationFriendParty),
      hasSameAddressAsLitigant: 'No',
      primaryAddress: this.buildAddressData(litigationFriendParty),
      partyName: `${StringHelper.capitalise(litigationFriendParty.key)} Litigation`,
    };
  }

  static buildExpertData(expertParty: Party) {
    return {
      firstName: StringHelper.capitalise(expertParty.key),
      lastName: 'Expert',
      emailAddress: `${expertParty.key}@experts.com`,
      phoneNumber: this.getPartyPhoneNumber(expertParty),
      fieldOfExpertise: `Field of expertise - ${expertParty.key}`,
      whyRequired: `Reason required - ${expertParty.key}`,
      estimatedCost: this.getExpertEstimatedCost(expertParty),
      partyName: `${StringHelper.capitalise(expertParty.key)} Expert`,
    };
  }

  static buildMediationData(mediationFriendParty: Party) {
    return {
      firstName: StringHelper.capitalise(mediationFriendParty.key),
      lastName: 'Mediation',
      emailAddress: `${mediationFriendParty.key}@mediation.com`,
      phoneNumber: this.getPartyPhoneNumber(mediationFriendParty),
    };
  }

  static buildWitnessData(witnessParty: Party) {
    return {
      firstName: StringHelper.capitalise(witnessParty.key),
      lastName: 'Witness',
      phoneNumber: this.getPartyPhoneNumber(witnessParty),
      emailAddress: `${witnessParty.key}@witnesses.com`,
      reasonForWitness: `Reason for witness - ${witnessParty.key}`,
      partyName: `${StringHelper.capitalise(witnessParty.key)} Witness`,
    };
  }

  static getClaimValue(claimTrack: ClaimTrack) {
    switch (claimTrack) {
      case ClaimTrack.SMALL_CLAIM:
        return 100;
      case ClaimTrack.FAST_CLAIM:
        return 11000;
      case ClaimTrack.INTERMEDIATE_CLAIM:
        return 26000;
      case ClaimTrack.MULTI_CLAIM:
        return 110000;
    }
  }
}
