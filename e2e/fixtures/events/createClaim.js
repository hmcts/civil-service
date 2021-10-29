const { document, element, listElement, buildAddress } = require('../../api/dataHelper');
const config = require('../../config.js');
const uuid = require('uuid');

const respondent1 = {
  type: 'INDIVIDUAL',
  individualFirstName: 'John',
  individualLastName: 'Doe',
  individualTitle: 'Sir',
  primaryAddress: buildAddress('respondent')
};
const respondent2 = {
  type: 'INDIVIDUAL',
  individualFirstName: 'Foo',
  individualLastName: 'Bar',
  individualTitle: 'Dr',
  primaryAddress: buildAddress('second respondent')
};
const respondent1WithPartyName = {
  ...respondent1,
  partyName: 'Sir John Doe',
  partyTypeDisplayValue: 'Individual',
};
const respondent2WithPartyName = {
  ...respondent2,
  partyName: 'Dr Foo Bar',
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

let selectedPba = listElement('PBA0088192');
const validPba = listElement('PBA0088192');
const invalidPba = listElement('PBA0078095');

const createClaimData = (legalRepresentation, useValidPba) => {
  selectedPba = useValidPba ? validPba : invalidPba;
  const claimData = {
    References: {
      solicitorReferences: {
        applicantSolicitor1Reference: 'Applicant reference',
        respondentSolicitor1Reference: 'Respondent reference'
      }
    },
    Court: {
      courtLocation: {
        applicantPreferredCourt: '344'
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
        email: 'hmcts.civil+organisation.1.solicitor.1@gmail.com',
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
          OrganisationID: 'Q1KOKP2'
        }
      }
    },
    ClaimantSolicitorServiceAddress: {
      applicantSolicitor1ServiceAddress:  buildAddress('service')
    },
    AddAnotherClaimant: {},
    Defendant: {
      respondent1: respondent1
    },
    LegalRepresentation: {
      respondent1Represented: `${legalRepresentation}`
    },
    DefendantSolicitorOrganisation: {
      respondent1OrgRegistered: 'Yes',
      respondent1OrganisationPolicy: {
        OrgPolicyReference: 'Defendant policy reference',
        OrgPolicyCaseAssignedRole: '[RESPONDENTSOLICITORONE]',
        Organisation: {
          OrganisationID: '79ZRSOU'
        },
      },
    },
    DefendantSolicitorServiceAddress: {
      respondentSolicitor1ServiceAddress: buildAddress('service')
    },
    DefendantSolicitorEmail: {
      respondentSolicitor1EmailAddress: 'civilunspecified@gmail.com'
    },
    AddAnotherDefendant: {},
    SecondDefendant: {},
    SecondDefendantLegalRepresentation: {},
    SameLegalRepresentative: {},
    ClaimType: {
      claimType: 'PERSONAL_INJURY'
    },
    PersonalInjuryType: {
      personalInjuryType: 'ROAD_ACCIDENT'
    },
    Details: {
      detailsOfClaim: 'Test details of claim'
    },
    Upload: {
      servedDocumentFiles: {
        particularsOfClaimDocument: [
          {
            id: uuid.v1(),
            value: {
              document_url: "${TEST_DOCUMENT_URL}",
              document_binary_url: "${TEST_DOCUMENT_BINARY_URL}",
              document_filename: "${TEST_DOCUMENT_FILENAME}"
            }
          }
        ]
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
          validPba,
          invalidPba
        ],
        value: selectedPba

      }
    },
    PaymentReference: {
      claimIssuedPaymentDetails:  {
        customerReference: 'Applicant reference'
      }
    },
    StatementOfTruth: {
      uiStatementOfTruth: {
        name: 'John Doe',
        role: 'Test Solicitor'
      }
    },
  };

  if (config.multipartyTestsEnabled) {
    return {
      ...claimData,
      AddAnotherClaimant: {
        addApplicant2: 'No'
      },
      AddAnotherDefendant: {
        addRespondent2: 'Yes'
      },
      SecondDefendant: {
        respondent2: respondent2
      },
      SecondDefendantLegalRepresentation: {
        respondent2Represented: 'Yes'
      },
      SameLegalRepresentative: {
        respondent2SameLegalRepresentative: 'Yes'
      },
    };
  }
  return claimData;
};

module.exports = {
  createClaim: {
    midEventData: {
      ClaimValue: {
        applicantSolicitor1PbaAccounts: {
          list_items: [
            validPba,
            invalidPba
          ]
        },
        applicantSolicitor1PbaAccountsIsEmpty: 'No',
        claimFee: {
          calculatedAmountInPence: '150000',
          code: 'FEE0209',
          version: '3'
        },
        claimIssuedPaymentDetails:  {
          customerReference: 'Applicant reference'
        },
        applicant1: applicant1WithPartyName,
        respondent1: respondent1WithPartyName,
        ...config.multipartyTestsEnabled ? {
          respondent2: respondent2WithPartyName
        } : {}
      },
      ClaimantLitigationFriend: {
        applicant1: applicant1WithPartyName,
        applicant1LitigationFriend: applicant1LitigationFriend,
        applicantSolicitor1CheckEmail: {
          email: 'hmcts.civil+organisation.1.solicitor.1@gmail.com',
        },
      },
      // otherwise applicantSolicitor1ClaimStatementOfTruth: [undefined]
      StatementOfTruth: {
        applicantSolicitor1ClaimStatementOfTruth: {}
      },
    },
    valid: {
      ...createClaimData('Yes', true),
      PaymentReference: {
        claimIssuedPaymentDetails:  {
          customerReference: 'Applicant reference'
        }
      }
    },
    invalid:{
      Upload:{
        servedDocumentFiles: {
          particularsOfClaimDocument: [
            {
              id: uuid.v1(),
              value: {
                document_url: "${TEST_DOCUMENT_URL}",
                document_binary_url: "${TEST_DOCUMENT_BINARY_URL}",
                document_filename: "${TEST_DOCUMENT_FILENAME}"
              }
            },
            {
              id: uuid.v1(),
              value: {
                document_url: "${TEST_DOCUMENT_URL}",
                document_binary_url: "${TEST_DOCUMENT_BINARY_URL}",
                document_filename: "${TEST_DOCUMENT_FILENAME}"
              }
            }
          ]
            //[element(document('particularsOfClaim.pdf')),element(document('particularsOfClaim.pdf'))]
        }
      },
      Court: {
        courtLocation: {
          applicantPreferredCourt: ['3a3','21','3333']
        }
      }
    }
  },
  createClaimLitigantInPerson: {
    valid: createClaimData('No', true)
  },
  createClaimWithTerminatedPBAAccount: {
    valid: createClaimData('Yes', false)
  },
  createClaimRespondentSolFirmNotInMyHmcts: {
    valid: {
      ...createClaimData('Yes', true),
      DefendantSolicitorOrganisation: {
        respondent1OrgRegistered: 'No'
      },
      UnRegisteredDefendantSolicitorOrganisation: {
        respondentSolicitor1OrganisationDetails: {
          organisationName: 'Test org',
          phoneNumber: '0123456789',
          email: 'test@example.com',
          dx: 'test dx',
          fax: '123123123',
          address: buildAddress('org')
        }
      },
    }
  }
};
