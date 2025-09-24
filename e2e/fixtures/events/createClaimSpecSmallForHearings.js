const {listElement, buildAddress} = require('../../api/dataHelper');
const config = require('../../config.js');


const respondent1 = {
  type: 'INDIVIDUAL',
  individualFirstName: 'John',
  individualLastName: 'Doe',
  individualTitle: 'Sir',
  partyEmail: 'johndoe@example.com',
  partyPhone: '07898678902',
  primaryAddress: buildAddress('respondent')
};
const respondent1WithPartyName = {
  ...respondent1,
  partyName: 'Sir John Doe',
  partyTypeDisplayValue: 'Individual',
};
const applicant1 = {
  type: 'INDIVIDUAL',
  individualFirstName: 'Test',
  individualLastName: 'Inc',
  partyEmail: 'testinc@example.com',
  partyPhone: '07898878902',
  primaryAddress: buildAddress('applicant')
};
const applicant1WithPartyName = {
  ...applicant1,
  partyName: 'Test Inc',
  partyTypeDisplayValue: 'Individual',
};

const isPBAv3 = (pbaV3) => {
  return pbaV3;
};

const solicitor1Email = 'hmcts.civil+organisation.1.solicitor.1@gmail.com';
const claimAmount = '50000';

const validPba = listElement('PBA0088192');
const invalidPba = listElement('PBA0078095');

module.exports = {

  createClaim: (mpScenario, pbaV3) => {
    return {
      userInput: {
        References: {
          CaseAccessCategory: 'SPEC_CLAIM',
          solicitorReferences: {
            applicantSolicitor1Reference: 'Applicant reference',
            respondentSolicitor1Reference: 'Respondent reference'
          }
        },
        Claimant: {
          applicant1: applicant1WithPartyName
        },
        AddAnotherClaimant: {
          addApplicant2: 'No'
        },
        Notifications: {
          applicantSolicitor1CheckEmail: {
            correct: 'No',
          },
          applicantSolicitor1UserDetails: {
            email: solicitor1Email
          }
        },
        ClaimantSolicitorOrganisation: {
          applicant1OrganisationPolicy: {
            OrgPolicyReference: 'Claimant policy reference',
            OrgPolicyCaseAssignedRole: '[APPLICANTSOLICITORONE]',
            Organisation: {
              OrganisationID: config.claimantSolicitorOrgId
            }
          }
        },
        specCorrespondenceAddress: {
          specApplicantCorrespondenceAddressRequired: 'No'
        },
        Defendant: {
          respondent1: respondent1WithPartyName
        },
        LegalRepresentation: {
          specRespondent1Represented: 'Yes',
        },

        DefendantSolicitorOrganisation: {
          respondent1OrgRegistered: 'Yes',
          respondent1OrganisationPolicy: {
            OrgPolicyReference: 'Defendant policy reference',
            OrgPolicyCaseAssignedRole: '[RESPONDENTSOLICITORONE]',
            Organisation: {
              OrganisationID: config.defendant1SolicitorOrgId
            },
          },
        },
        DefendantSolicitorEmail: {
          respondentSolicitor1EmailAddress: 'civilunspecified@gmail.com'
        },

        specRespondentCorrespondenceAddress: {
          specRespondentCorrespondenceAddressRequired: 'No'
        },
        AddAnotherDefendant: {
          addRespondent2: 'No'
        },
        Details: {
          detailsOfClaim: 'Test details of claim'
        },
        ClaimTimeline: {
          timelineOfEvents: [{
            value: {
              timelineDate: '2021-02-01',
              timelineDescription: 'event 1'
            }
          }]
        },
        EvidenceList: {
          speclistYourEvidenceList: [{
            value: {
              evidenceType: 'CONTRACTS_AND_AGREEMENTS',
              contractAndAgreementsEvidence: 'evidence details'
            }
          }]
        },
        ClaimAmount: {
          claimAmountBreakup: [{
            value: {
              claimReason: 'amount reason',
              claimAmount: claimAmount
            }
          }]
        },
        ClaimInterest: {
          claimInterest: 'No'
        },
        InterestSummary: {
          claimIssuedPaymentDetails: {
            customerReference: 'Applicant reference'
          },
          ...isPBAv3(pbaV3) ? {
            paymentTypePBASpec: 'PBAv3'
          } : {},
        },
        PbaNumber: {
          applicantSolicitor1PbaAccounts: {
            list_items: [
              validPba,
              invalidPba
            ],
            value: validPba
          }
        },
        StatementOfTruth: {
          uiStatementOfTruth: {
            name: 'John Doe',
            role: 'Test Solicitor'
          }
        },
      },

      midEventData: {
        Notifications: {
          applicantSolicitor1CheckEmail: {
            email: solicitor1Email
          }
        },
        ClaimAmount: {
          totalClaimAmount: claimAmount / 100
        },
        ClaimAmountDetails: {
          CaseAccessCategory: 'SPEC_CLAIM'
        },
        InterestSummary: {
          totalInterest: 0,
          applicantSolicitor1PbaAccountsIsEmpty: 'No',
          ...isPBAv3(pbaV3) ? {
            paymentTypePBASpec: 'PBAv3'
          } : {},
        },
      },

      midEventGeneratedData: {
        ClaimAmount: {
          speclistYourEvidenceList: {
            id: 'string'
          },
          claimAmountBreakupSummaryObject: 'string',
          timelineOfEvents: {
            id: 'string'
          },
          claimAmountBreakup: {
            id: 'string'
          }
        },
        ClaimInterest: {
          calculatedInterest: 'string'
        },
        InterestSummary: {
          applicantSolicitor1PbaAccounts: {
            list_items: 'object'
          },
          claimFee: {
            calculatedAmountInPence: 'string',
            code: 'string',
            version: 'string'
          },
          ...isPBAv3(pbaV3) ? {
            paymentTypePBASpec: 'string'
          } : {},
        },

      }
    };
  },
  serviceUpdateDto: (caseId, paymentStatus) => {
    return {
      service_request_reference: '1324646546456',
      ccd_case_number: caseId,
      service_request_amount: '167.00',
      service_request_status: paymentStatus,
      payment: {
        payment_amount: 167.00,

        payment_reference: '13213223',
        payment_method: 'by account',
        case_reference: 'example of case ref'
      }
    };
  }
};
