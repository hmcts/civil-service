const {document, listElement, buildAddress} = require('../../api/dataHelper');

const selectedPba = listElement('PBA0077597');
const respondent1 = {
  type: 'INDIVIDUAL',
  individualFirstName: 'John',
  individualLastName: 'Doe',
  individualTitle: 'Sir',
  primaryAddress: buildAddress('respondent')
};
const respondent1WithPartyName = {
  ...respondent1,
  partyName: 'Sir John Doe',
  partyTypeDisplayValue: 'Individual',
};
const applicant1 = {
  type: 'COMPANY',
  companyName: 'Test Inc',
  primaryAddress: buildAddress('applicant')
};
const applicant1WithPartyName = {
  ...applicant1,
  partyName: 'Test Inc',
  partyTypeDisplayValue: 'Company',
};
const applicant1LitigationFriend = {
  fullName: 'Bob the litigant friend',
  hasSameAddressAsLitigant: 'No',
  primaryAddress: buildAddress('litigant friend')
};
const createClaimData = legalRepresentation => {
  return {
    References: {
      solicitorReferences: {
        applicantSolicitor1Reference: 'Applicant reference',
        respondentSolicitor1Reference: 'Respondent reference'
      }
    },
    Court: {
      courtLocation: {
        applicantPreferredCourt: 'Test Preferred Court'
      }
    },
    Claimant: {
      applicant1: applicant1
    },
    ClaimantLitigationFriendRequired: {
      applicant1LitigationFriendRequired: 'Yes',
    },
    ClaimantLitigationFriend: {
      applicant1LitigationFriend: applicant1LitigationFriend
    },
    Notifications: {
      applicantSolicitor1CheckEmail: {
        email: 'civil.damages.claims+organisation.1.solicitor.1@gmail.com',
        correct: 'No'
      },
      applicantSolicitor1UserDetails: {
        email: 'civilunspecified@gmail.com',
        id: 'c18d5f8d-06fa-477d-ac09-5b6129828a5b'
      }
    },
    ClaimantSolicitorOrganisation: {
      applicant1OrganisationPolicy: {
        OrgPolicyReference: 'Claimant policy reference',
        OrgPolicyCaseAssignedRole: '[APPLICANTSOLICITORONE]',
        Organisation: {
          OrganisationID: '0FA7S8S'
        }
      }
    },
    Defendant: {
      respondent1: respondent1
    },
    LegalRepresentation: {
      respondent1Represented: `${legalRepresentation}`
    },
    DefendantSolicitorOrganisation: {
      respondent1OrganisationPolicy: {
        OrgPolicyReference: 'Defendant policy reference',
        OrgPolicyCaseAssignedRole: '[RESPONDENTSOLICITORONE]',
        Organisation: {
          OrganisationID: 'N5AFUXG'
        },
      },
      respondentSolicitor1EmailAddress: 'civilunspecified@gmail.com'
    },
    ClaimType: {
      claimType: 'PERSONAL_INJURY'
    },
    PersonalInjuryType: {
      personalInjuryType: 'ROAD_ACCIDENT'
    },
    Upload: {
      servedDocumentFiles: {
        particularsOfClaimDocument: document('particularsOfClaim.pdf')
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
      paymentReference: 'Applicant reference'
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
        paymentReference: 'Applicant reference',
        applicant1: applicant1WithPartyName,
        respondent1: respondent1WithPartyName,
      },
      ClaimantLitigationFriend: {
        applicant1: applicant1WithPartyName,
        applicant1LitigationFriend: applicant1LitigationFriend,
        applicantSolicitor1CheckEmail: {
          email: 'civil.damages.claims+organisation.1.solicitor.1@gmail.com',
        },
      },
    },
    valid: {
      ...createClaimData('Yes'),
      PaymentReference: {
        paymentReference: 'Applicant reference'
      }
    }
  },
  createClaimLitigantInPerson: {
    valid: createClaimData('No')
  },
};
