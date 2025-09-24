import {
  claimantSolicitorUser,
  defendantSolicitor1User,
  defendantSolicitor2User,
} from '../../../../../config/users/exui-users';
import partys from '../../../../../constants/partys';
import CaseRole from '../../../../../enums/case-role';
import ClaimTrack from '../../../../../enums/claim-track';
import ClaimType from '../../../../../enums/claim-type';
import CaseDataHelper from '../../../../../helpers/case-data-helper';
import ClaimTypeHelper from '../../../../../helpers/claim-type-helper';
import { ClaimantDefendantPartyType } from '../../../../../models/claimant-defendant-party-types';

const references = {
  References: {
    solicitorReferences: {
      applicantSolicitor1Reference: 'Claimant Solicitor Reference',
      respondentSolicitor1Reference: 'Defendant Solicitor Reference',
    },
  },
};

const claimant1 = (partyType: ClaimantDefendantPartyType) => {
  return {
    Claimant: {
      applicant1: CaseDataHelper.buildClaimantAndDefendantData(partys.CLAIMANT_1, partyType),
    },
  };
};

const claimant2 = (claimType: ClaimType, partyType: ClaimantDefendantPartyType) => {
  if (ClaimTypeHelper.isClaimant2(claimType))
    return {
      AddAnotherClaimant: {
        addApplicant2: 'Yes',
      },
      SecondClaimant: {
        applicant2: CaseDataHelper.buildClaimantAndDefendantData(partys.CLAIMANT_2, partyType),
      },
    };
  return {
    AddAnotherClaimant: {
      addApplicant2: 'No',
    },
  };
};

const claimantSolicitor1 = {
  Notifications: {
    applicantSolicitor1CheckEmail: {
      email: claimantSolicitorUser.email,
      correct: 'No',
    },
    applicantSolicitor1UserDetails: {
      email: claimantSolicitorUser.email,
    },
  },
  ClaimantSolicitorOrganisation: {
    applicant1OrganisationPolicy: {
      OrgPolicyReference: 'Claimant policy reference',
      OrgPolicyCaseAssignedRole: `[${CaseRole.APPLICANT_SOLICITOR_ONE}]`,
      Organisation: {
        OrganisationID: claimantSolicitorUser.orgId,
      },
    },
  },
  SpecCorrespondenceAddress: {
    specApplicantCorrespondenceAddressRequired: 'Yes',
    specApplicantCorrespondenceAddressdetails: CaseDataHelper.buildAddressData(
      partys.CLAIMANT_SOLICITOR_1,
    ),
  },
};

const defendant1 = (partyType: ClaimantDefendantPartyType) => ({
  Defendant: {
    respondent1: {
      ...CaseDataHelper.buildClaimantAndDefendantData(partys.DEFENDANT_1, partyType),
      individualDateOfBirth: undefined,
      soleTraderDateOfBirth: undefined,
    },
  },
});

const defendantSolicitor1 = (claimType: ClaimType) => {
  if (ClaimTypeHelper.isDefendant1Represented(claimType))
    return {
      LegalRepresentation: {
        specRespondent1Represented: 'Yes',
      },
      DefendantSolicitorOrganisation: {
        respondent1OrgRegistered: 'Yes',
        respondent1OrganisationPolicy: {
          OrgPolicyReference: 'Defendant policy reference',
          OrgPolicyCaseAssignedRole: `[${CaseRole.RESPONDENT_SOLICITOR_ONE}]`,
          Organisation: {
            OrganisationID: defendantSolicitor1User.orgId,
          },
        },
      },
      DefendantSolicitorEmail: {
        respondentSolicitor1EmailAddress: defendantSolicitor1User.email,
      },
      SpecRespondentCorrespondenceAddress: {
        specRespondentCorrespondenceAddressRequired: 'Yes',
        specRespondentCorrespondenceAddressdetails: CaseDataHelper.buildAddressData(
          partys.DEFENDANT_SOLICITOR_2,
        ),
      },
    };
  return {
    LegalRepresentation: {
      specRespondent1Represented: 'No',
    },
  };
};

const defendant2 = (claimType: ClaimType, partyType: ClaimantDefendantPartyType) => {
  if (ClaimTypeHelper.isDefendant2(claimType))
    return {
      AddAnotherDefendant: {
        addRespondent2: 'Yes',
      },
      SecondDefendant: {
        respondent2: {
          ...CaseDataHelper.buildClaimantAndDefendantData(partys.DEFENDANT_2, partyType),
          individualDateOfBirth: undefined,
          soleTraderDateOfBirth: undefined,
        },
      },
    };
  return {
    AddAnotherDefendant: {
      addRespondent2: 'No',
    },
  };
};

const defendant2Represented = (claimType: ClaimType) => {
  if (ClaimTypeHelper.isDefendant2Represented(claimType))
    return {
      LegalRepresentationRespondent2: {
        specRespondent2Represented: 'Yes',
      },
    };
  else if (ClaimTypeHelper.isDefendant2Unrepresented(claimType)) {
    return {
      LegalRepresentationRespondent2: {
        specRespondent2Represented: 'No',
      },
    };
  }
  return {};
};

const defendant2SameSolicitor = (claimType: ClaimType) => {
  if (claimType === ClaimType.ONE_VS_TWO_SAME_SOL)
    return {
      SameLegalRepresentative: {
        respondent2SameLegalRepresentative: 'Yes',
      },
    };
  else if (claimType === ClaimType.ONE_VS_TWO_DIFF_SOL)
    return {
      SameLegalRepresentative: {
        respondent2SameLegalRepresentative: 'No',
      },
    };
  return {};
};

const defendantSolicitor2 = (claimType: ClaimType) => {
  if (ClaimTypeHelper.isDefendant2RepresentedNotSame(claimType))
    return {
      SecondDefendantSolicitorOrganisation: {
        respondent2OrgRegistered: 'Yes',
        respondent2OrganisationPolicy: {
          OrgPolicyReference: 'Defendant policy reference 2',
          OrgPolicyCaseAssignedRole: `[${CaseRole.RESPONDENT_SOLICITOR_TWO}]`,
          Organisation: { OrganisationID: defendantSolicitor2User.orgId },
        },
      },
      SecondDefendantSolicitorEmail: {
        respondentSolicitor2EmailAddress: defendantSolicitor2User.email,
      },
      SpecRespondent2CorrespondenceAddress: {
        specRespondent2CorrespondenceAddressRequired: 'Yes',
        specRespondent2CorrespondenceAddressdetails: CaseDataHelper.buildAddressData(
          partys.DEFENDANT_SOLICITOR_2,
        ),
      },
    };
  return {};
};

const claimDetails = (claimTrack: ClaimTrack) => ({
  FlightDelayClaim: {
    isFlightDelayClaim: 'No',
  },
  Details: {
    detailsOfClaim: 'Test details of claim',
  },
  ClaimTimeline: {
    timelineOfEvents: [
      {
        value: {
          timelineDate: '2021-02-01',
          timelineDescription: 'event 1',
        },
      },
    ],
  },
  EvidenceList: {
    speclistYourEvidenceList: [
      {
        value: {
          evidenceType: 'CONTRACTS_AND_AGREEMENTS',
          contractAndAgreementsEvidence: 'evidence details',
        },
      },
    ],
  },
  ClaimAmount: {
    claimAmountBreakup: [
      {
        value: {
          claimReason: 'amount reason',
          claimAmount: `${CaseDataHelper.getClaimValue(claimTrack) * 100}`,
        },
      },
    ],
  },
  ClaimAmountDetails: {
    totalClaimAmount: CaseDataHelper.getClaimValue(claimTrack),
  },
  ClaimInterest: {
    claimInterest: 'No',
  },
  InterestSummary: {},
  PbaNumber: {},
  FixedCommencementCosts: {
    fixedCosts: {
      claimFixedCosts: 'Yes',
      fixedCostAmount: '10000',
    },
  },
});

const statementOfTruth = {
  StatementOfTruth: {
    uiStatementOfTruth: {
      name: claimantSolicitorUser.name,
      role: 'Solicitor',
    },
  },
};

const createClaimSpecData = {
  references,
  claimant1,
  claimant2,
  claimantSolicitor1,
  defendant1,
  defendantSolicitor1,
  defendant2,
  defendant2Represented,
  defendant2SameSolicitor,
  defendantSolicitor2,
  claimDetails,
  statementOfTruth,
};

export default createClaimSpecData;
