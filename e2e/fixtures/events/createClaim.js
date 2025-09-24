const {listElement, buildAddress, date } = require('../../api/dataHelper');
const uuid = require('uuid');
const config = require('../../config.js');
const { getClaimFee } = require('../../claimAmountAndFee');

const docUuid = uuid.v1();

const respondent1 = {
  type: 'INDIVIDUAL',
  individualFirstName: 'John',
  individualLastName: 'Doe',
  individualTitle: 'Sir',
  primaryAddress: buildAddress('respondent'),
  partyEmail: 'johndoe@example.com',
  partyPhone: '07898678902',
};
const respondent2 = {
  type: 'INDIVIDUAL',
  individualFirstName: 'Foo',
  individualLastName: 'Bar',
  individualTitle: 'Dr',
  primaryAddress: buildAddress('second respondent'),
  partyEmail: 'foo.bar@example.com',
  partyPhone: '07898678912',
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

const applicant2 = {
  type: 'INDIVIDUAL',
  individualFirstName: 'Jane',
  individualLastName: 'Doe',
  individualTitle: 'Dr',
  primaryAddress: buildAddress('second applicant')
};

const applicant2WithPartyName = {
  ...applicant2,
  partyName: 'Dr Jane Doe',
  partyTypeDisplayValue: 'Individual',
};

const applicant1LitigationFriend = {
  firstName: 'Bob',
  lastName: 'the litigant friend',
  emailAddress: 'bobthelitigant@litigants.com',
  phoneNumber: '07123456789',
  hasSameAddressAsLitigant: 'No',
  primaryAddress: buildAddress('litigant friend')
};

let selectedPba = listElement('PBAFUNC12345');
const validPba = listElement('PBAFUNC12345');
const invalidPba = listElement('PBA0078095');

let claimantCourt = config.claimantSelectedCourt;
const useHmcEaCourt = config.claimantSelectedCourtHmc;
const useClaimantSelectedCourt = config.claimantSelectedCourt;

const createClaimData = (pbaV3, legalRepresentation, useValidPba, mpScenario, claimAmount = '30000', hmcTest) => {
  selectedPba = useValidPba ? validPba : invalidPba;
  claimantCourt = hmcTest ? useHmcEaCourt : useClaimantSelectedCourt;

  const claimData = {
    References: {
      CaseAccessCategory: 'UNSPEC_CLAIM',
      solicitorReferences: {
        applicantSolicitor1Reference: 'Applicant reference',
        respondentSolicitor1Reference: 'Respondent reference'
      }
    },
    Court: {
      courtLocation: {
        applicantPreferredCourtLocationList: {
          list_items: [
            listElement(claimantCourt)
          ],
          value: listElement(claimantCourt)
        }
      },
      applicant1DQRemoteHearing: {
        remoteHearingRequested: 'No'
      },
      applicant1OrganisationPolicy: {
        OrgPolicyCaseAssignedRole: '[APPLICANTSOLICITORONE]',
        Organisation: {
          OrganisationID: config.claimantSolicitorOrgId,
        }
      }
    },
    Claimant: {
      applicant1: applicant1WithPartyName
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
          OrganisationID: config.applicantSolicitorUser.orgId,
        }
      }
    },
    ClaimantSolicitorServiceAddress: {
      applicantSolicitor1ServiceAddressRequired: 'Yes',
      applicantSolicitor1ServiceAddress:  buildAddress('service')
    },
    AddAnotherClaimant: {
      addApplicant2: 'No'
    },
    ...(mpScenario === 'TWO_V_ONE') ? {
      SecondClaimant: {
        applicant2: applicant2WithPartyName
      },
      SecondClaimantLitigationFriendRequired: {
        applicant2LitigationFriendRequired: 'No'
      },
    }: {},
    Defendant: {
      respondent1: respondent1WithPartyName
    },
    LegalRepresentation: {
      respondent1Represented: `${legalRepresentation}`
    },
    ...(legalRepresentation === 'Yes') ? {
      DefendantSolicitorOrganisation: {
        respondent1OrgRegistered: 'Yes',
        respondent1OrganisationPolicy: {
          OrgPolicyReference: 'Defendant policy reference',
          OrgPolicyCaseAssignedRole: '[RESPONDENTSOLICITORONE]',
          Organisation: {
            OrganisationID: config.defendantSolicitorUser.orgId
          },
        },
      },
      DefendantSolicitorServiceAddress: {
        respondentSolicitor1ServiceAddress: buildAddress('service')
      },
      DefendantSolicitorEmail: {
        respondentSolicitor1EmailAddress: 'civilunspecified@gmail.com'
      },
    }: {},
    AddAnotherDefendant: {
      addRespondent2: 'No'
    },
    ...hasRespondent2(mpScenario) ? {
      SecondDefendant: {},
      SecondDefendantLegalRepresentation: {},
      SecondDefendantSolicitorOrganisation: {},
      SecondDefendantSolicitorServiceAddress: {},
      SecondDefendantSolicitorReference: {},
      SecondDefendantSolicitorEmail: {},
      SameLegalRepresentative: {},
    } : {},
    ClaimTypeUnSpec: {
      claimTypeUnSpec: 'CONSUMER_CREDIT'
    },
    Details: {
      detailsOfClaim: 'Test details of claim'
    },
    Upload: {
      servedDocumentFiles: {
        particularsOfClaimDocument: [
          {
            id: docUuid,
            value: {
              document_url: '${TEST_DOCUMENT_URL}',
              document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
              document_filename: '${TEST_DOCUMENT_FILENAME}'
            }
          }
        ]
      }
    },
    ClaimValue: {
      claimValue: {
        statementOfValueInPennies:  JSON.stringify(claimAmount * 100)
      },
      claimFee: getClaimFee(claimAmount),
      ...isPBAv3(pbaV3) ? {
        paymentTypePBA: 'PBAv3'
      } : {},
    },
    PbaNumber: {
      applicantSolicitor1PbaAccounts: {
        list_items: [
          validPba,
          invalidPba
        ],
        value: selectedPba

      },
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
  switch (mpScenario){
    case 'ONE_V_TWO_ONE_LEGAL_REP': {
      return {
        ...claimData,
        AddAnotherClaimant: {
          addApplicant2: 'No'
        },
        AddAnotherDefendant: {
          addRespondent2: 'Yes'
        },
        SecondDefendant: {
          respondent2: respondent2WithPartyName
        },
        SecondDefendantLegalRepresentation: {
          respondent2Represented: 'Yes'
        },
        SameLegalRepresentative: {
          respondent2SameLegalRepresentative: 'Yes'
        },
      };
    }
    case 'ONE_V_TWO_TWO_LEGAL_REP': {
      return {
        ...claimData,
        AddAnotherClaimant: {
          addApplicant2: 'No'
        },
        AddAnotherDefendant: {
          addRespondent2: 'Yes'
        },
        SecondDefendant: {
          respondent2: respondent2WithPartyName,
        },
        SecondDefendantLegalRepresentation: {
          respondent2Represented: 'Yes'
        },
        SameLegalRepresentative: {
          respondent2SameLegalRepresentative: 'No'
        },
        SecondDefendantSolicitorOrganisation: {
          respondent2OrgRegistered: 'Yes',
          respondent2OrganisationPolicy: {
            OrgPolicyReference: 'Defendant policy reference 2',
            OrgPolicyCaseAssignedRole: '[RESPONDENTSOLICITORTWO]',
            Organisation:

              {OrganisationID: config.secondDefendantSolicitorUser.orgId}
            ,
          },
        },
        SecondDefendantSolicitorServiceAddress: {
          respondentSolicitor2ServiceAddress: buildAddress('service')
        },
        SecondDefendantSolicitorReference: {
          respondentSolicitor2Reference: 'sol2reference'
        },
        SecondDefendantSolicitorEmail: {
          respondentSolicitor2EmailAddress: 'civilunspecified@gmail.com'
        }
      };
    }
    case 'ONE_V_TWO_ONE_LEGAL_REP_ONE_LIP': {
      return {
        ...claimData,
        AddAnotherClaimant: {
          addApplicant2: 'No'
        },
        AddAnotherDefendant: {
          addRespondent2: 'Yes'
        },
        SecondDefendant: {
          respondent2: respondent2WithPartyName,
        },
        SecondDefendantLegalRepresentation: {
          respondent2Represented: 'No'
        }
      };
    }
    case 'ONE_V_TWO_LIPS': {
      delete claimData.SecondDefendantLegalRepresentation;
      delete claimData.LegalRepresentation;

      return {
        ...claimData,
        AddAnotherClaimant: {
          addApplicant2: 'No'
        },
        AddAnotherDefendant: {
          addRespondent2: 'Yes'
        },
        SecondDefendant: {
          respondent2: respondent2WithPartyName
        },
        LegalRepresentation: {
          respondent1Represented: 'No',
          respondent2Represented: 'No'
        }
      };
    }

    case 'TWO_V_ONE': {
      return {
        ...claimData,
        AddAnotherClaimant: {
          addApplicant2: 'Yes'
        }
      };
    }

    case 'ONE_V_ONE_NIHL': {
      delete claimData.ClaimType;
      return {
        ...claimData,
        ClaimType: {
          claimType: 'PERSONAL_INJURY'
        },
        ClaimTypeUnSpec: {
          claimTypeUnSpec: 'PERSONAL_INJURY'
        },
        PersonalInjuryType: {
          personalInjuryType: 'NOISE_INDUCED_HEARING_LOSS'
        },
      };
    }

    case 'ONE_V_ONE':
    default: {
      return claimData;
    }
  }
};

const hasRespondent2 = (mpScenario) => {
  return mpScenario === 'ONE_V_TWO_ONE_LEGAL_REP'
    || mpScenario ===  'ONE_V_TWO_TWO_LEGAL_REP'
    || mpScenario ===  'ONE_V_TWO_ONE_LEGAL_REP_ONE_LIP'
    || mpScenario ===  'ONE_V_TWO_LIPS';
};

const isPBAv3 = (pbaV3) => {
  console.log( 'Pba value in create claim' + pbaV3);
  return pbaV3;
};

module.exports = {
  createClaim: (mpScenario = 'ONE_V_ONE', claimAmount, pbaV3, isMintiEnabled) => {
    return {
      midEventData: {
        ClaimValue: {
          applicantSolicitor1PbaAccounts: {
            list_items: [
              validPba,
              invalidPba
            ]
          },
          applicantSolicitor1PbaAccountsIsEmpty: 'No',
          claimIssuedPaymentDetails: {
            customerReference: 'Applicant reference'
          },
          applicant1: applicant1WithPartyName,
          respondent1: respondent1WithPartyName,
          ...hasRespondent2(mpScenario) ? {
            respondent2: respondent2WithPartyName
          } : {},
          ...isPBAv3(pbaV3) ? {
            paymentTypePBA: 'PBAv3'
          } : {},
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
        }
      },
      valid: {
        ...createClaimData(pbaV3,'Yes', true, mpScenario, claimAmount, isMintiEnabled),
      },
      invalid: {
        Upload: {
          servedDocumentFiles: {
            particularsOfClaimDocument: [
              {
                id: docUuid,
                value: {
                  document_url: '${TEST_DOCUMENT_URL}',
                  document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                  document_filename: '${TEST_DOCUMENT_FILENAME}'
                }
              },
              {
                id: docUuid,
                value: {
                  document_url: '${TEST_DOCUMENT_URL}',
                  document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                  document_filename: '${TEST_DOCUMENT_FILENAME}'
                }
              }
            ]
          }
        },
        Court: {
          courtLocation: {
            applicantPreferredCourt: ['3a3', '21', '3333']
          }
        }
      }
    };
  },

  createClaimLitigantInPerson: {
    valid: createClaimData(false,'No', true, 'ONE_V_ONE')
  },
  createClaimLRLIP: {
    valid: createClaimData(false,'Yes', true, 'ONE_V_TWO_ONE_LEGAL_REP_ONE_LIP')
  },
  createClaimLIPLIP: {
    valid: createClaimData(false,'No', true, 'ONE_V_TWO_LIPS', '11000')
  },
  createClaimWithTerminatedPBAAccount: {
    valid: createClaimData(true,'Yes', false)
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
  },
  cosNotifyClaim : (lip1, lip2) => {
    return {
      ...(lip1) ? {
        cosNotifyClaimDefendant1: {
          cosDateOfServiceForDefendant: date(-1),
          cosDateDeemedServedForDefendant: date(-1),
          cosServedDocumentFiles: 'sample text',
          cosRecipient: 'sample text',
          cosRecipientServeType: 'HANDED',
          cosRecipientServeLocation: 'sample text',
          cosRecipientServeLocationOwnerType: 'SOLICITOR',
          cosRecipientServeLocationType: 'USUAL_RESIDENCE',
          cosSender: 'sample text',
          cosSenderFirm: 'sample text',
          cosSenderStatementOfTruthLabel: [
            'CERTIFIED'
          ]
        }
      }: {},
      ...(lip2) ? {
        cosNotifyClaimDefendant2: {
          cosDateOfServiceForDefendant: date(-1),
          cosDateDeemedServedForDefendant: date(-1),
          cosServedDocumentFiles: 'sample text',
          cosRecipient: 'sample text',
          cosRecipientServeType: 'HANDED',
          cosRecipientServeLocation: 'sample text',
          cosRecipientServeLocationOwnerType: 'SOLICITOR',
          cosRecipientServeLocationType: 'USUAL_RESIDENCE',
          cosSender: 'sample text',
          cosSenderFirm: 'sample text',
          cosSenderStatementOfTruthLabel: [
            'CERTIFIED'
          ]
        }
      }: {},
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
  },
  cosNotifyClaimDetails : (lip1, lip2) => {
    return {
      ...(lip1) ? {
        cosNotifyClaimDetails1: {
          cosDateOfServiceForDefendant: date(-1),
          cosDateDeemedServedForDefendant: date(2),
          cosServedDocumentFiles: 'sample text',
          cosEvidenceDocument: [
            {
              id: docUuid,
              value: {
                document_url: '${TEST_DOCUMENT_URL}',
                document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                document_filename: '${TEST_DOCUMENT_FILENAME}'
              }
            }
          ],
          cosRecipient: 'sample text',
          cosRecipientServeType: 'HANDED',
          cosRecipientServeLocation: 'sample text',
          cosRecipientServeLocationOwnerType: 'SOLICITOR',
          cosRecipientServeLocationType: 'USUAL_RESIDENCE',
          cosSender: 'sample text',
          cosSenderFirm: 'sample text',
          cosSenderStatementOfTruthLabel: [
            'CERTIFIED'
          ]
        }
      }: {},
      ...(lip2) ? {
        cosNotifyClaimDetails2: {
          cosDateOfServiceForDefendant: date(-1),
          cosDateDeemedServedForDefendant: date(2),
          cosServedDocumentFiles: 'sample text',
          cosEvidenceDocument: [
            {
              id: docUuid,
              value: {
                document_url: '${TEST_DOCUMENT_URL}',
                document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                document_filename: '${TEST_DOCUMENT_FILENAME}'
              }
            }
          ],
          cosRecipient: 'sample text',
          cosRecipientServeType: 'HANDED',
          cosRecipientServeLocation: 'sample text',
          cosRecipientServeLocationOwnerType: 'SOLICITOR',
          cosRecipientServeLocationType: 'USUAL_RESIDENCE',
          cosSender: 'sample text',
          cosSenderFirm: 'sample text',
          cosSenderStatementOfTruthLabel: [
            'CERTIFIED'
          ]
        }
      }: {},
    };
  },
};
