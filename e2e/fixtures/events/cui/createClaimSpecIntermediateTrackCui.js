const {listElement, buildAddress} = require('../../../api/dataHelper');
const config = require('../../../config');

const respondent1 = {
  type: 'COMPANY',
  companyName: 'Def Test Org',
  primaryAddress: buildAddress('respondent'),
};
const respondent1WithPartyName = {
  ...respondent1,
  partyName: 'Def Test Org',
  partyTypeDisplayValue: 'Company',
};
const applicant1 = {
  type: 'COMPANY',
  companyName: 'Test Inc',
  primaryAddress: buildAddress('applicant'),
};
const applicant1WithPartyName = {
  ...applicant1,
  partyName: 'Test Inc',
  partyTypeDisplayValue: 'Company',
};

const solicitor1Email = 'hmcts.civil+organisation.1.solicitor.1@gmail.com';
const claimAmount = '2600000';

const validPba = listElement('PBA0088192');
const invalidPba = listElement('PBA0078095');

module.exports = {
  createClaim: () => {
    const userData = {
      userInput: {
        References: {
          CaseAccessCategory: 'SPEC_CLAIM',
          solicitorReferences: {
            applicantSolicitor1Reference: 'Applicant reference',
            respondentSolicitor1Reference: 'Respondent reference',
          },
          ...{
            submittedDate:'2025-06-20T15:59:50'
          },
        },
        Claimant: {
          applicant1: applicant1WithPartyName,
        },
        AddAnotherClaimant: {
          addApplicant2: 'No',
        },
        Notifications: {
          applicantSolicitor1CheckEmail: {
            correct: 'No',
          },
          applicantSolicitor1UserDetails: {
            email: 'civilmoneyclaimsdemo@gmail.com',
          },
        },
        ClaimantSolicitorOrganisation: {
          applicant1OrganisationPolicy: {
            OrgPolicyReference: 'Claimant policy reference',
            OrgPolicyCaseAssignedRole: '[APPLICANTSOLICITORONE]',
            Organisation: {
              OrganisationID: config.claimantSolicitorOrgId,
            },
          },
        },
        specCorrespondenceAddress: {
          specApplicantCorrespondenceAddressRequired: 'No',
        },
        Defendant: {
          respondent1: respondent1WithPartyName,
        },
        LegalRepresentation: {
          specRespondent1Represented: 'No',
        },
        specRespondentCorrespondenceAddress: {
          specRespondentCorrespondenceAddressRequired: 'No',
        },
        AddAnotherDefendant: {
          addRespondent2: 'No',
        },
        Details: {
          detailsOfClaim: 'Test details of claim',
        },
        ClaimTimeline: {
          timelineOfEvents: [{
            value: {
              timelineDate: '2021-02-01',
              timelineDescription: 'event 1',
            },
          }],
        },
        EvidenceList: {
          speclistYourEvidenceList: [{
            value: {
              evidenceType: 'CONTRACTS_AND_AGREEMENTS',
              contractAndAgreementsEvidence: 'evidence details',
            },
          }],
        },
        ClaimAmount: {
          claimAmountBreakup: [{
            value: {
              claimReason: 'amount reason',
              claimAmount: claimAmount,
            },
          }],
        },
        ClaimInterest: {
          claimInterest: 'No',
        },

        InterestSummary: {
          claimIssuedPaymentDetails: {
            customerReference: 'Applicant reference',
          },
        },
        PbaNumber: {
          applicantSolicitor1PbaAccounts: {
            list_items: [
              validPba,
              invalidPba,
            ],
            value: validPba,
          },
        },
        StatementOfTruth: {
          uiStatementOfTruth: {
            name: 'John Doe',
            role: 'Test Solicitor',
          },
        },
      },

      midEventData: {
        Notifications: {
          applicantSolicitor1CheckEmail: {
            email: solicitor1Email,
          },
        },
        ClaimAmount: {
          totalClaimAmount: claimAmount / 100,
        },
        ClaimAmountDetails: {
          CaseAccessCategory: 'SPEC_CLAIM',
        },
        InterestSummary: {
          totalInterest: 0,
          applicantSolicitor1PbaAccountsIsEmpty: 'No',
        },
      },

      midEventGeneratedData: {
        ClaimAmount: {
          speclistYourEvidenceList: {
            id: 'string',
          },
          claimAmountBreakupSummaryObject: 'string',
          timelineOfEvents: {
            id: 'string',
          },
          claimAmountBreakup: {
            id: 'string',
          },
        },
        ClaimInterest: {
          calculatedInterest: 'string',
        },
        InterestSummary: {
          applicantSolicitor1PbaAccounts: {
            list_items: 'object',
          },
          claimFee: {
            calculatedAmountInPence: 'string',
            code: 'string',
            version: 'string',
          },
        },
      },
    };

    return userData;
  },
};
