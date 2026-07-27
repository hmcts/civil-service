import { v4 as uuidv4 } from 'uuid';
import StringHelper from './string-helper';
import ClaimTrack from '../constants/cases/claim-track';
import { Party } from '../models/users/partys';
import partys from '../constants/users/partys';
import { ClaimantDefendantPartyType } from '../models/users/claimant-defendant-party-types';
import claimantDefendantPartyTypes from '../constants/users/claimant-defendant-party-types';

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
      case partys.DEFENDANT_COMMON_LITIGATION_FRIEND:
        return '1985-11-14';
    }
  }

  static getPartyDateOfBirthUpdated(party: Party) {
    switch (party) {
      case partys.CLAIMANT_1:
        return '1981-06-25';
      case partys.CLAIMANT_2:
        return '1993-09-12';
      case partys.CLAIMANT_1_LITIGATION_FRIEND:
        return '1988-04-18';
      case partys.CLAIMANT_2_LITIGATION_FRIEND:
        return '1996-12-03';
      case partys.DEFENDANT_1:
        return '1985-02-01';
      case partys.DEFENDANT_2:
        return '1991-08-20';
      case partys.DEFENDANT_1_LITIGATION_FRIEND:
        return '1983-10-26';
      case partys.DEFENDANT_2_LITIGATION_FRIEND:
        return '1990-05-07';
      case partys.DEFENDANT_COMMON_LITIGATION_FRIEND:
        return '1986-12-15';
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
      case partys.DEFENDANT_COMMON_LITIGATION_FRIEND:
        return '07398765432';
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
      case partys.DEFENDANT_COMMON_LITIGATION_FRIEND:
        return 'NE1 4ST';
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

  static buildAddressData(party: Party, update = false) {
    const suffix = update ? '-updated' : '';

    return {
      AddressLine1: `Flat 12 - ${party.key}${suffix}`,
      AddressLine2: `House 15 - 17 - ${party.key}${suffix}`,
      AddressLine3: `Street - ${party.key}${suffix}`,
      PostTown: `Town - ${party.key}${suffix}`,
      County: `County - ${party.key}${suffix}`,
      Country: `Country - ${party.key}${suffix}`,
      PostCode: this.getPartyPostCode(party),
    };
  }

  static buildClaimantAndDefendantData(
    claimantDefendantParty: Party,
    claimantDefendantPartyType: ClaimantDefendantPartyType,
    update = false,
  ): any {
    const suffix = update ? '-updated' : '';

    const commonPartyData = {
      type: claimantDefendantPartyType.type,
      partyEmail: `${claimantDefendantParty.key}@${claimantDefendantPartyType.key}${suffix}.com`,
      partyPhone: this.getPartyPhoneNumber(claimantDefendantParty),
      primaryAddress: this.buildAddressData(claimantDefendantParty, update),
    };

    const partyKey = StringHelper.capitalise(claimantDefendantParty.key);
    const partyTypeKey = StringHelper.capitalise(claimantDefendantPartyType.key);

    switch (claimantDefendantPartyType) {
      case claimantDefendantPartyTypes.INDIVIDUAL:
        return {
          ...commonPartyData,
          individualTitle: 'Mx',
          individualFirstName: `${partyKey}${suffix}`,
          individualLastName: `${partyTypeKey}${suffix}`,
          individualDateOfBirth: this.getPartyDateOfBirth(claimantDefendantParty),
          partyName: `Mx ${partyKey}${suffix} ${partyTypeKey}${suffix}`,
        };

      case claimantDefendantPartyTypes.COMPANY:
        return {
          ...commonPartyData,
          companyName: `${partyKey}${suffix} ${partyTypeKey}${suffix}`,
          partyName: `${partyKey}${suffix} ${partyTypeKey}${suffix}`,
        };

      case claimantDefendantPartyTypes.SOLE_TRADER:
        return {
          ...commonPartyData,
          soleTraderTitle: 'Mx',
          soleTraderFirstName: `${partyKey}${suffix}`,
          soleTraderLastName: `${partyTypeKey}${suffix}`,
          soleTraderTradingAs: `${partyKey} Trade${suffix}`,
          soleTraderDateOfBirth: this.getPartyDateOfBirth(claimantDefendantParty),
          partyName: `Mx ${partyKey}${suffix} ${partyTypeKey}${suffix}`,
        };
      case claimantDefendantPartyTypes.ORGANISATION:
        return {
          ...commonPartyData,
          organisationName: `${partyKey}${suffix} ${partyTypeKey}${suffix}`,
          partyName: `${partyKey}${suffix} ${partyTypeKey}${suffix}`,
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

  static buildLitigationFriendData(litigationFriendParty: Party, update = false) {
    const suffix = update ? '-updated' : '';

    return {
      firstName: `${StringHelper.capitalise(litigationFriendParty.key)}${suffix}`,
      lastName: `Litigation${suffix}`,
      emailAddress: `${litigationFriendParty.key}@litigants${suffix}.com`,
      phoneNumber: this.getPartyPhoneNumber(litigationFriendParty),
      hasSameAddressAsLitigant: 'No',
      primaryAddress: this.buildAddressData(litigationFriendParty, update),
      partyName: `${StringHelper.capitalise(litigationFriendParty.key)}${suffix} Litigation${suffix}`,
    };
  }

  static buildExpertData(expertParty: Party, update = false) {
    const suffix = update ? '-updated' : '';

    return {
      firstName: `${StringHelper.capitalise(expertParty.key)}${suffix}`,
      lastName: `Expert${suffix}`,
      emailAddress: `${expertParty.key}@experts${suffix}.com`,
      phoneNumber: this.getPartyPhoneNumber(expertParty),
      fieldOfExpertise: `Field of expertise - ${expertParty.key}${suffix}`,
      whyRequired: `Reason required - ${expertParty.key}`,
      estimatedCost: this.getExpertEstimatedCost(expertParty),
      partyName: `${StringHelper.capitalise(expertParty.key)}${suffix} Expert${suffix}`,
    };
  }

  static buildMediationData(mediationFriendParty: Party) {
    return {
      firstName: StringHelper.capitalise(mediationFriendParty.key),
      lastName: 'Mediation',
      emailAddress: `${mediationFriendParty.key}@mediation.com`,
      telephoneNumber: this.getPartyPhoneNumber(mediationFriendParty),
    };
  }

  static buildWitnessData(witnessParty: Party, update = false) {
    const suffix = update ? '-updated' : '';

    return {
      firstName: `${StringHelper.capitalise(witnessParty.key)}${suffix}`,
      lastName: `Witness${suffix}`,
      phoneNumber: this.getPartyPhoneNumber(witnessParty),
      emailAddress: `${witnessParty.key}@witnesses${suffix}.com`,
      reasonForWitness: `Reason for witness - ${witnessParty.key}`,
      partyName: `${StringHelper.capitalise(witnessParty.key)}${suffix} Witness${suffix}`,
    };
  }

  static buildSolicitorData(solicitorParty: Party, update = false) {
    const suffix = update ? '-updated' : '';

    return {
      firstName: `${StringHelper.capitalise(solicitorParty.key)}${suffix}`,
      lastName: `Witness${suffix}`,
      phoneNumber: this.getPartyPhoneNumber(solicitorParty),
      emailAddress: `${solicitorParty.key}@solicitors${suffix}.com`,
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

  static getClaimTrack(claimAmount: number) {
    if (claimAmount <= 10000) return ClaimTrack.SMALL_CLAIM;
    if (claimAmount > 10000 && claimAmount <= 25000) return ClaimTrack.FAST_CLAIM;
    if (claimAmount > 25000 && claimAmount <= 100000) return ClaimTrack.INTERMEDIATE_CLAIM;
    if (claimAmount > 100000) return ClaimTrack.MULTI_CLAIM;
  }
}
