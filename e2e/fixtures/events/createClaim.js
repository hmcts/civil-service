const {document, element, listElement, buildAddress} = require('../../api/dataHelper');

const selectedPba = listElement('PBA0077597');
const createClaimData = legalRepresentation => {
  return {
    References: {
      solicitorReferences: {
        applicantSolicitor1Reference: 'Applicant test reference',
        respondentSolicitor1Reference: 'Respondent test reference'
      }
    },
    Court: {
      courtLocation: {
        applicantPreferredCourt: 'Test Preferred Court'
      }
    },
    Claimant: {
      applicant1: {
        type: 'COMPANY',
        companyName: 'Test Inc',
        primaryAddress: buildAddress('applicant')
      }
    },
    ClaimantLitigationFriendRequired: {
      applicant1LitigationFriendRequired: 'Yes',
    },
    ClaimantLitigationFriend: {
      applicant1LitigationFriend: {
        fullName: 'Bob the litigant friend',
        hasSameAddressAsLitigant: 'No',
        primaryAddress: buildAddress('litigant friend')
      }
    },
    ClaimantSolicitorOrganisation:{
      applicant1OrganisationPolicy : {
        OrgPolicyReference : 'Claimant policy reference',
        OrgPolicyCaseAssignedRole : '[APPLICANTSOLICITORONE]',
        Organisation : {
          OrganisationID : '0FA7S8S'
        }
      }
    },
    Defendant: {
      respondent1: {
        type: 'INDIVIDUAL',
        individualFirstName: 'John',
        individualLastName: 'Doe',
        individualTitle: 'Sir',
        individualDateOfBirth: null,
        primaryAddress: buildAddress('respondent')
      }
    },
    LegalRepresentation: {
      respondent1Represented: `${legalRepresentation}`
    },
    DefendantSolicitorOrganisation: {
      respondent1OrganisationPolicy : {
        OrgPolicyReference : 'Defendant policy reference',
        OrgPolicyCaseAssignedRole : '[RESPONDENTSOLICITORONE]',
        Organisation : {
          OrganisationID : 'N5AFUXG'
        }
      }
    },
    ClaimType: {
      claimType: 'PERSONAL_INJURY'
    },
    PersonalInjuryType: {
      personalInjuryType: 'ROAD_ACCIDENT'
    },
    Upload: {
      servedDocumentFiles: {
        particularsOfClaim: [element(document('particularsOfClaim.pdf'))]
      }
    },
    ClaimValue: {
      claimValue: {
        statementOfValueInPennies: '3000000'
      }
    },
    PbaNumber: {
      applicantSolicitor1PbaAccounts: {
        list_items: [
          selectedPba,
          listElement('PBA0078094')
        ],
        value: selectedPba

      }
    },
    PaymentReference: {
      paymentReference: 'Applicant test reference'
    },
    StatementOfTruth: {
      applicantSolicitor1ClaimStatementOfTruth: {
        name: 'John Doe',
        role: 'Test Solicitor'
      }
    },
  };
};

module.exports = {
  createClaim: {
    midEventData: {
      ClaimValue: {
        applicantSolicitor1PbaAccounts: {
          list_items: [
            selectedPba,
            listElement('PBA0078094')
          ]
        },
        applicantSolicitor1PbaAccountsIsEmpty: 'No',
        claimFee: {
          calculatedAmountInPence: '150000',
          code: 'FEE0209',
          version: '1'
        },
        paymentReference: 'Applicant test reference',
        applicant1: {
          type: 'COMPANY',
          companyName: 'Test Inc',
          partyName: 'Test Inc',
          partyTypeDisplayValue: 'Company',
          primaryAddress: buildAddress('applicant')
        },
        respondent1: {
          type: 'INDIVIDUAL',
          individualFirstName: 'John',
          individualLastName: 'Doe',
          individualTitle: 'Sir',
          partyName: 'Sir John Doe',
          partyTypeDisplayValue: 'Individual',
          primaryAddress: buildAddress('respondent')
        }
      },
    },
    valid: {
      ...createClaimData('Yes'),
      PaymentReference: {
        paymentReference: 'Applicant test reference'
      }
    }
  },
  createClaimLitigantInPerson: {
    valid: createClaimData('No')
  },
};
