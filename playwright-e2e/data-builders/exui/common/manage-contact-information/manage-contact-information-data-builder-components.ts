import partys from '../../../../constants/users/partys';
import CaseDataHelper from '../../../../helpers/case-data-helper';
import { ClaimantDefendantPartyType } from '../../../../models/users/claimant-defendant-party-types';
import { Party } from '../../../../models/users/partys';

const partySelection = (party: Party, claimant1PartyType: ClaimantDefendantPartyType) => {
  if (party === partys.CLAIMANT_1) {
    const claimantPartyNameCodeData = CaseDataHelper.setCodeToData(
      CaseDataHelper.buildClaimantAndDefendantData(partys.CLAIMANT_1, claimant1PartyType)
        .partyName,
    );

    return {
      PartySelection: {
        partyChosen: {
          list_items: [claimantPartyNameCodeData],
          value: claimantPartyNameCodeData,
        },
      },
    };
  }

  if (party === partys.DEFENDANT_SOLICITOR_1) {
    const defendant1LRIndividuals = CaseDataHelper.setCodeToData(
      'DEFENDANT_1_LR_INDIVIDUALS',
    );

    return {
      PartySelection: {
        partyChosen: {
          list_items: [defendant1LRIndividuals],
          value: defendant1LRIndividuals,
        },
      },
    };
  }

  return {};
};

const claimant1Party = (party: Party, claimant1PartyType: ClaimantDefendantPartyType) => {
  if (party === partys.CLAIMANT_1) {
    const claimantData = CaseDataHelper.buildClaimantAndDefendantData(
      partys.CLAIMANT_1,
      claimant1PartyType,
      true,
    );

    return {
      Applicant1Party: {
        applicant1: {
          individualDateOfBirth: claimantData.individualDateOfBirth,
          partyEmail: claimantData.partyEmail,
          partyName: claimantData.partyName,
          partyPhone: claimantData.partyPhone,
          primaryAddress: claimantData.primaryAddress,
        },
      },
    };
  }

  return {};
};

const legalRepresentativeIndividuals = (party: Party) => {
  if (party === partys.DEFENDANT_SOLICITOR_1) {
    const solicitorData = CaseDataHelper.buildSolicitorData(party, true);

    return {
      LegalRepresentativeIndividuals: {
        updateLRIndividualsForm: [
          {
            value: {
              firstName: solicitorData.firstName,
              lastName: solicitorData.lastName,
              emailAddress: solicitorData.emailAddress,
              phoneNumber: solicitorData.phoneNumber,
            },
          },
        ],
      },
    };
  }

  return {};
};

const manageContactInformationDataComponents = {
  partySelection,
  claimant1Party,
  legalRepresentativeIndividuals,
};

export default manageContactInformationDataComponents;
