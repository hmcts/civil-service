const {listElement, buildAddress} = require('../../../../api/dataHelper');
const config = require('../../../../config.js');

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

const solicitor1Email = 'hmcts.civil+organisation.1.solicitor.1@gmail.com';
const claimAmount = '150000';

const validPba = listElement('PBA0088192');
const invalidPba = listElement('PBA0078095');

module.exports = {
  createClaim: (mpScenario) => {
    const userData = {
      userInput: {
        References: {
          superClaimType: 'SPEC_CLAIM',
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
            OrgPolicyCaseAssignedRole: '[APPLICANTSOLICITORONESPEC]',
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
            OrgPolicyCaseAssignedRole: '[RESPONDENTSOLICITORONESPEC]',
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
          }
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
          superClaimType: 'SPEC_CLAIM',
        },
        InterestSummary: {
          totalInterest: 0,
          applicantSolicitor1PbaAccountsIsEmpty: 'No',
        }
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
          }
        }
      }
    };

    switch (mpScenario) {
      case 'ONE_V_ONE':
        userData.userInput = {
          ...userData.userInput
        };
        break;
      case 'ONE_V_TWO':
        userData.userInput = {
          ...userData.userInput,
          AddAnotherDefendant: {
            addRespondent2: 'Yes'
          },

          SecondDefendant: {
            respondent2: {
              type: 'ORGANISATION',
              organisationName: 'Second Defendant',
              primaryAddress: {
                AddressLine1: '123 Second Close',
                PostTown: 'Second Town',
                PostCode: 'NR5 9LL'
              }
            }
          },

          LegalRepresentationRespondent2: {
            specRespondent2Represented: 'Yes',
            respondent2: {
              type: 'ORGANISATION',
              organisationName: 'Second Defendant',
              partyName: 'Second Defendant',
              primaryAddress: {
                AddressLine1: '123 Second Close',
                PostTown: 'Second Town',
                PostCode: 'NR5 9LL'
              }
            }
          },

          SecondDefendantSolicitorEmail: {
            respondentSolicitor2EmailAddress: 'civilmoneyclaimsdemo@gmail.com'
          },

          SameLegalRepresentative: {
            respondent2SameLegalRepresentative: 'No'
          },

          SecondDefendantSolicitorOrganisation: {
            respondent2OrgRegistered: 'Yes',
            respondent2OrganisationPolicy: {
              OrgPolicyCaseAssignedRole: '[RESPONDENTSOLICITORTWOSPEC]',
              Organisation: {
                OrganisationID: config.defendant2SolicitorOrgId,
                OrganisationName: 'Civil - Organisation 2'
              }
            }
          },

          InterestSummary: {
            claimIssuedPaymentDetails: {
              customerReference: 'Applicant reference'
            }
          },
        };

        userData.midEventData = {
          ...userData.midEventData,

          LegalRepresentationRespondent2: {
            specRespondent2Represented: 'Yes',
            respondent2: {
              type: 'ORGANISATION',
              organisationName: 'Second Defendant',
              partyName: 'Second Defendant',
              partyTypeDisplayValue: 'Organisation',
              primaryAddress: {
                AddressLine1: '123 Second Close',
                PostTown: 'Second Town',
                PostCode: 'NR5 9LL'
              }
            }
          },
        };
        break;

      case 'ONE_V_TWO_SAME_SOL':
        userData.userInput = {
          ...userData.userInput,
          AddAnotherDefendant: {
            addRespondent2: 'Yes'
          },

          SecondDefendant: {
            respondent2: {
              type: 'ORGANISATION',
              organisationName: 'Second Defendant',
              primaryAddress: {
                AddressLine1: '123 Second Close',
                PostTown: 'Second Town',
                PostCode: 'NR5 9LL'
              }
            }
          },

          LegalRepresentationRespondent2: {
            specRespondent2Represented: 'Yes',
            respondent2: {
              type: 'ORGANISATION',
              organisationName: 'Second Defendant',
              partyName: 'Second Defendant',
              primaryAddress: {
                AddressLine1: '123 Second Close',
                PostTown: 'Second Town',
                PostCode: 'NR5 9LL'
              }
            }
          },

          SecondDefendantSolicitorEmail: {
            respondentSolicitor2EmailAddress: 'civilmoneyclaimsdemo@gmail.com'
          },

          SameLegalRepresentative: {
            respondent2SameLegalRepresentative: 'Yes'
          },
        };

        userData.midEventData = {
          ...userData.midEventData,

          LegalRepresentationRespondent2: {
            specRespondent2Represented: 'Yes',
            respondent2: {
              type: 'ORGANISATION',
              organisationName: 'Second Defendant',
              partyName: 'Second Defendant',
              partyTypeDisplayValue: 'Organisation',
              primaryAddress: {
                AddressLine1: '123 Second Close',
                PostTown: 'Second Town',
                PostCode: 'NR5 9LL'
              }
            }
          },
        };
        break;

      case 'TWO_V_ONE':
        userData.userInput = {
          ...userData.userInput,
          AddAnotherClaimant: {
            addApplicant2: 'Yes'
          },

          SecondClaimant: {
            applicant2: {
              type: 'ORGANISATION',
              organisationName: 'Claim 2',
              partyName: 'Claim 2',
              partyTypeDisplayValue: 'Organisation',
              primaryAddress: {
                AddressLine1: '43 Montgomery Close',
                PostTown: 'Norwich',
                PostCode: 'NR5 9LL'
              }
            }
          },
          SecondDefendantSolicitorEmail: {
            respondentSolicitor2EmailAddress: 'civilmoneyclaimsdemo@gmail.com'
          }
        };
        break;
    }

    return userData;
  },

  createClaimDataByPage: (mpScenario) => {
    let userData = {
      References: {
        userInput: {
          superClaimType: 'SPEC_CLAIM',
          solicitorReferences: {
            applicantSolicitor1Reference: 'Applicant reference',
            respondentSolicitor1Reference: 'Respondent reference'
          }
        }
      },
      Claimant: {
        userInput: {
          applicant1: applicant1WithPartyName
        }
      },
      AddAnotherClaimant: {
        userInput: {
          addApplicant2: 'TWO_V_ONE'===mpScenario?'Yes':'No'
        }
      },
      Notifications: {
        userInput: {
          applicantSolicitor1CheckEmail: {
            correct: 'No',
          },
          applicantSolicitor1UserDetails: {
            email: solicitor1Email
          }
        },
        expected: {
          applicantSolicitor1CheckEmail: {
            email: solicitor1Email
          }
        }
      },
      ClaimantSolicitorOrganisation: {
        userInput: {
          applicant1OrganisationPolicy: {
            OrgPolicyReference: 'Claimant policy reference',
            OrgPolicyCaseAssignedRole: '[APPLICANTSOLICITORONESPEC]',
            Organisation: {
              OrganisationID: config.claimantSolicitorOrgId
            }
          }
        }
      },
      specCorrespondenceAddress: {
        userInput: {
          specApplicantCorrespondenceAddressRequired: 'No'
        }
      },
      Defendant: {
        userInput: {
          respondent1: respondent1WithPartyName
        }
      },
      LegalRepresentation: {
        userInput: {
          specRespondent1Represented: 'Yes',
        }
      },
      DefendantSolicitorOrganisation: {
        userInput: {
          respondent1OrgRegistered: 'Yes',
          respondent1OrganisationPolicy: {
            OrgPolicyReference: 'Defendant policy reference',
            OrgPolicyCaseAssignedRole: '[RESPONDENTSOLICITORONESPEC]',
            Organisation: {
              OrganisationID: config.defendant1SolicitorOrgId
            },
          },
        }
      },
      DefendantSolicitorEmail: {
        userInput: {
          respondentSolicitor1EmailAddress: 'civilunspecified@gmail.com'
        }
      },
      specRespondentCorrespondenceAddress: {
        userInput: {
          specRespondentCorrespondenceAddressRequired: 'No'
        }
      },
      AddAnotherDefendant: {
        userInput: {
          addRespondent2: ('ONE_V_TWO' === mpScenario
            || 'ONE_V_TWO_SAME_SOL' === mpScenario) ? 'Yes' : 'No'
        }
      },
      Details: {
        userInput: {
          detailsOfClaim: 'Test details of claim'
        }
      },
      ClaimTimeline: {
        userInput: {
          timelineOfEvents: [{
            value: {
              timelineDate: '2021-02-01',
              timelineDescription: 'event 1'
            }
          }]
        }
      },
      EvidenceList: {
        userInput: {
          speclistYourEvidenceList: [{
            value: {
              evidenceType: 'CONTRACTS_AND_AGREEMENTS',
              contractAndAgreementsEvidence: 'evidence details'
            }
          }]
        }
      },
      ClaimAmount: {
        userInput: {
          claimAmountBreakup: [{
            value: {
              claimReason: 'amount reason',
              claimAmount: claimAmount
            }
          }]
        },
        expected: {
          totalClaimAmount: claimAmount / 100
        },
        generated: {
          speclistYourEvidenceList: {
            type: 'array'
          },
          claimAmountBreakupSummaryObject: 'string',
          timelineOfEvents: {
            id: 'string'
          },
          claimAmountBreakup: {
            id: 'string'
          }
        }
      },
      ClaimInterest: {
        userInput: {
          claimInterest: 'No'
        },
        generated: {
          calculatedInterest: 'string'
        }
      },
      InterestSummary: {
        userInput: {
          claimIssuedPaymentDetails: {
            customerReference: 'Applicant reference'
          }
        },
        expected: {
          totalInterest: 0,
          applicantSolicitor1PbaAccountsIsEmpty: 'No',
        },
        generated: {
          applicantSolicitor1PbaAccounts: {
            list_items: 'object'
          },
          claimFee: {
            calculatedAmountInPence: 'string',
            code: 'string',
            version: 'string'
          }
        }
      },
      PbaNumber: {
        userInput: {
          applicantSolicitor1PbaAccounts: {
            list_items: [
              validPba,
              invalidPba
            ],
            value: validPba
          }
        }
      },
      StatementOfTruth: {
        userInput: {
          uiStatementOfTruth: {
            name: 'John Doe',
            role: 'Test Solicitor'
          }
        }
      }
    };

    switch (mpScenario) {
      case 'ONE_V_TWO':
        userData = {
          ...userData,
          SecondDefendant: {
            userInput: {
              respondent2: {
                type: 'ORGANISATION',
                organisationName: 'Second Defendant',
                primaryAddress: {
                  AddressLine1: '123 Second Close',
                  PostTown: 'Second Town',
                  PostCode: 'NR5 9LL'
                }
              }
            }
          },
          LegalRepresentationRespondent2: {
            userInput: {
              specRespondent2Represented: 'Yes',
              respondent2: {
                type: 'ORGANISATION',
                organisationName: 'Second Defendant',
                partyName: 'Second Defendant',
                primaryAddress: {
                  AddressLine1: '123 Second Close',
                  PostTown: 'Second Town',
                  PostCode: 'NR5 9LL'
                }
              }
            },
            expected: {
              specRespondent2Represented: 'Yes',
              respondent2: {
                type: 'ORGANISATION',
                organisationName: 'Second Defendant',
                partyName: 'Second Defendant',
                partyTypeDisplayValue: 'Organisation',
                primaryAddress: {
                  AddressLine1: '123 Second Close',
                  PostTown: 'Second Town',
                  PostCode: 'NR5 9LL'
                }
              }
            }
          },
          SecondDefendantSolicitorEmail: {
            userInput: {
              respondentSolicitor2EmailAddress: 'civilmoneyclaimsdemo@gmail.com'
            }
          },

          SameLegalRepresentative: {
            userInput: {
              respondent2SameLegalRepresentative: 'No'
            }
          },
          SecondDefendantSolicitorOrganisation: {
            userInput: {
              respondent2OrgRegistered: 'Yes',
              respondent2OrganisationPolicy: {
                OrgPolicyCaseAssignedRole: '[RESPONDENTSOLICITORTWOSPEC]',
                Organisation: {
                  OrganisationID: '79ZRSOU',
                  OrganisationName: 'Civil - Organisation 2'
                }
              }
            }
          }
        };
        break;

      case 'ONE_V_TWO_SAME_SOL':
        userData = {
          ...userData,
          SecondDefendant: {
            userInput: {
              respondent2: {
                type: 'ORGANISATION',
                organisationName: 'Second Defendant',
                primaryAddress: {
                  AddressLine1: '123 Second Close',
                  PostTown: 'Second Town',
                  PostCode: 'NR5 9LL'
                }
              }
            }
          },
          LegalRepresentationRespondent2: {
            userInput: {
              specRespondent2Represented: 'Yes',
              respondent2: {
                type: 'ORGANISATION',
                organisationName: 'Second Defendant',
                partyName: 'Second Defendant',
                primaryAddress: {
                  AddressLine1: '123 Second Close',
                  PostTown: 'Second Town',
                  PostCode: 'NR5 9LL'
                }
              }
            },
            expected: {
              specRespondent2Represented: 'Yes',
              respondent2: {
                type: 'ORGANISATION',
                organisationName: 'Second Defendant',
                partyName: 'Second Defendant',
                partyTypeDisplayValue: 'Organisation',
                primaryAddress: {
                  AddressLine1: '123 Second Close',
                  PostTown: 'Second Town',
                  PostCode: 'NR5 9LL'
                }
              }
            }
          },
          SecondDefendantSolicitorEmail: {
            userInput: {
              respondentSolicitor2EmailAddress: 'civilmoneyclaimsdemo@gmail.com'
            }
          },

          SameLegalRepresentative: {
            userInput: {
              respondent2SameLegalRepresentative: 'Yes'
            }
          }
        };
        break;

      case 'TWO_V_ONE':
        userData = {
          ...userData,
          SecondClaimant: {
            userInput: {
              applicant2: {
                type: 'ORGANISATION',
                organisationName: 'Claim 2',
                partyName: 'Claim 2',
                partyTypeDisplayValue: 'Organisation',
                primaryAddress: {
                  AddressLine1: '43 Montgomery Close',
                  PostTown: 'Norwich',
                  PostCode: 'NR5 9LL'
                }
              }
            }
          }
        };
        break;
    }

    return userData;
  },

  createClaimForAccessProfiles: (mpScenario) => {
    const userData = {
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
          }
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
        }
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
          }
        }
      }
    };

    switch (mpScenario) {
      case 'ONE_V_ONE':
        userData.userInput = {
          ...userData.userInput
        };
        break;
      case 'ONE_V_TWO':
        userData.userInput = {
          ...userData.userInput,
          AddAnotherDefendant: {
            addRespondent2: 'Yes'
          },

          SecondDefendant: {
            respondent2: {
              type: 'ORGANISATION',
              organisationName: 'Second Defendant',
              primaryAddress: {
                AddressLine1: '123 Second Close',
                PostTown: 'Second Town',
                PostCode: 'NR5 9LL'
              }
            }
          },

          LegalRepresentationRespondent2: {
            specRespondent2Represented: 'Yes',
            respondent2: {
              type: 'ORGANISATION',
              organisationName: 'Second Defendant',
              partyName: 'Second Defendant',
              primaryAddress: {
                AddressLine1: '123 Second Close',
                PostTown: 'Second Town',
                PostCode: 'NR5 9LL'
              }
            }
          },

          SecondDefendantSolicitorEmail: {
            respondentSolicitor2EmailAddress: 'civilmoneyclaimsdemo@gmail.com'
          },

          SameLegalRepresentative: {
            respondent2SameLegalRepresentative: 'No'
          },

          SecondDefendantSolicitorOrganisation: {
            respondent2OrgRegistered: 'Yes',
            respondent2OrganisationPolicy: {
              OrgPolicyCaseAssignedRole: '[RESPONDENTSOLICITORTWO]',
              Organisation: {
                OrganisationID: config.defendant2SolicitorOrgId,
                OrganisationName: 'Civil - Organisation 2'
              }
            }
          },

          InterestSummary: {
            claimIssuedPaymentDetails: {
              customerReference: 'Applicant reference'
            }
          },
        };

        userData.midEventData = {
          ...userData.midEventData,

          LegalRepresentationRespondent2: {
            specRespondent2Represented: 'Yes',
            respondent2: {
              type: 'ORGANISATION',
              organisationName: 'Second Defendant',
              partyName: 'Second Defendant',
              partyTypeDisplayValue: 'Organisation',
              primaryAddress: {
                AddressLine1: '123 Second Close',
                PostTown: 'Second Town',
                PostCode: 'NR5 9LL'
              }
            }
          },
        };
        break;

      case 'ONE_V_TWO_SAME_SOL':
        userData.userInput = {
          ...userData.userInput,
          AddAnotherDefendant: {
            addRespondent2: 'Yes'
          },

          SecondDefendant: {
            respondent2: {
              type: 'ORGANISATION',
              organisationName: 'Second Defendant',
              primaryAddress: {
                AddressLine1: '123 Second Close',
                PostTown: 'Second Town',
                PostCode: 'NR5 9LL'
              }
            }
          },

          LegalRepresentationRespondent2: {
            specRespondent2Represented: 'Yes',
            respondent2: {
              type: 'ORGANISATION',
              organisationName: 'Second Defendant',
              partyName: 'Second Defendant',
              primaryAddress: {
                AddressLine1: '123 Second Close',
                PostTown: 'Second Town',
                PostCode: 'NR5 9LL'
              }
            }
          },

          SecondDefendantSolicitorEmail: {
            respondentSolicitor2EmailAddress: 'civilmoneyclaimsdemo@gmail.com'
          },

          SameLegalRepresentative: {
            respondent2SameLegalRepresentative: 'Yes'
          },

          SecondDefendantSolicitorOrganisation: {
            respondent2OrgRegistered: 'Yes',
            respondent2OrganisationPolicy: {
              OrgPolicyCaseAssignedRole: '[RESPONDENTSOLICITORONE]',
              Organisation: {
                OrganisationID: config.defendant1SolicitorOrgId,
              }
            }
          },
        };

        userData.midEventData = {
          ...userData.midEventData,

          LegalRepresentationRespondent2: {
            specRespondent2Represented: 'Yes',
            respondent2: {
              type: 'ORGANISATION',
              organisationName: 'Second Defendant',
              partyName: 'Second Defendant',
              partyTypeDisplayValue: 'Organisation',
              primaryAddress: {
                AddressLine1: '123 Second Close',
                PostTown: 'Second Town',
                PostCode: 'NR5 9LL'
              }
            }
          },
        };
        break;

      case 'TWO_V_ONE':
        userData.userInput = {
          ...userData.userInput,
          AddAnotherClaimant: {
            addApplicant2: 'Yes'
          },

          SecondClaimant: {
            applicant2: {
              type: 'ORGANISATION',
              organisationName: 'Claim 2',
              partyName: 'Claim 2',
              partyTypeDisplayValue: 'Organisation',
              primaryAddress: {
                AddressLine1: '43 Montgomery Close',
                PostTown: 'Norwich',
                PostCode: 'NR5 9LL'
              }
            }
          },
          SecondDefendantSolicitorEmail: {
            respondentSolicitor2EmailAddress: 'civilmoneyclaimsdemo@gmail.com'
          }
        };
        break;
    }

    return userData;
  },

  createClaimDataByPageForAccessProfiles: (mpScenario) => {
    let userData = {
      References: {
        userInput: {
          CaseAccessCategory: 'SPEC_CLAIM',
          solicitorReferences: {
            applicantSolicitor1Reference: 'Applicant reference',
            respondentSolicitor1Reference: 'Respondent reference'
          }
        }
      },
      Claimant: {
        userInput: {
          applicant1: applicant1WithPartyName
        }
      },
      AddAnotherClaimant: {
        userInput: {
          addApplicant2: 'TWO_V_ONE'===mpScenario?'Yes':'No'
        }
      },
      Notifications: {
        userInput: {
          applicantSolicitor1CheckEmail: {
            correct: 'No',
          },
          applicantSolicitor1UserDetails: {
            email: solicitor1Email
          }
        },
        expected: {
          applicantSolicitor1CheckEmail: {
            email: solicitor1Email
          }
        }
      },
      ClaimantSolicitorOrganisation: {
        userInput: {
          applicant1OrganisationPolicy: {
            OrgPolicyReference: 'Claimant policy reference',
            OrgPolicyCaseAssignedRole: '[APPLICANTSOLICITORONE]',
            Organisation: {
              OrganisationID: config.claimantSolicitorOrgId
            }
          }
        }
      },
      specCorrespondenceAddress: {
        userInput: {
          specApplicantCorrespondenceAddressRequired: 'No'
        }
      },
      Defendant: {
        userInput: {
          respondent1: respondent1WithPartyName
        }
      },
      LegalRepresentation: {
        userInput: {
          specRespondent1Represented: 'Yes',
        }
      },
      DefendantSolicitorOrganisation: {
        userInput: {
          respondent1OrgRegistered: 'Yes',
          respondent1OrganisationPolicy: {
            OrgPolicyReference: 'Defendant policy reference',
            OrgPolicyCaseAssignedRole: '[RESPONDENTSOLICITORONE]',
            Organisation: {
              OrganisationID: config.defendant1SolicitorOrgId
            },
          },
        }
      },
      DefendantSolicitorEmail: {
        userInput: {
          respondentSolicitor1EmailAddress: 'civilunspecified@gmail.com'
        }
      },
      specRespondentCorrespondenceAddress: {
        userInput: {
          specRespondentCorrespondenceAddressRequired: 'No'
        }
      },
      AddAnotherDefendant: {
        userInput: {
          addRespondent2: ('ONE_V_TWO' === mpScenario
            || 'ONE_V_TWO_SAME_SOL' === mpScenario) ? 'Yes' : 'No'
        }
      },
      Details: {
        userInput: {
          detailsOfClaim: 'Test details of claim'
        }
      },
      ClaimTimeline: {
        userInput: {
          timelineOfEvents: [{
            value: {
              timelineDate: '2021-02-01',
              timelineDescription: 'event 1'
            }
          }]
        }
      },
      EvidenceList: {
        userInput: {
          speclistYourEvidenceList: [{
            value: {
              evidenceType: 'CONTRACTS_AND_AGREEMENTS',
              contractAndAgreementsEvidence: 'evidence details'
            }
          }]
        }
      },
      ClaimAmount: {
        userInput: {
          claimAmountBreakup: [{
            value: {
              claimReason: 'amount reason',
              claimAmount: claimAmount
            }
          }]
        },
        expected: {
          totalClaimAmount: claimAmount / 100
        },
        generated: {
          speclistYourEvidenceList: {
            type: 'array'
          },
          claimAmountBreakupSummaryObject: 'string',
          timelineOfEvents: {
            id: 'string'
          },
          claimAmountBreakup: {
            id: 'string'
          }
        }
      },
      ClaimInterest: {
        userInput: {
          claimInterest: 'No'
        },
        generated: {
          calculatedInterest: 'string'
        }
      },
      InterestSummary: {
        userInput: {
          claimIssuedPaymentDetails: {
            customerReference: 'Applicant reference'
          }
        },
        expected: {
          totalInterest: 0,
          applicantSolicitor1PbaAccountsIsEmpty: 'No',
        },
        generated: {
          applicantSolicitor1PbaAccounts: {
            list_items: 'object'
          },
          claimFee: {
            calculatedAmountInPence: 'string',
            code: 'string',
            version: 'string'
          }
        }
      },
      PbaNumber: {
        userInput: {
          applicantSolicitor1PbaAccounts: {
            list_items: [
              validPba,
              invalidPba
            ],
            value: validPba
          }
        }
      },
      StatementOfTruth: {
        userInput: {
          uiStatementOfTruth: {
            name: 'John Doe',
            role: 'Test Solicitor'
          }
        }
      }
    };

    switch (mpScenario) {
      case 'ONE_V_TWO':
        userData = {
          ...userData,
          SecondDefendant: {
            userInput: {
              respondent2: {
                type: 'ORGANISATION',
                organisationName: 'Second Defendant',
                primaryAddress: {
                  AddressLine1: '123 Second Close',
                  PostTown: 'Second Town',
                  PostCode: 'NR5 9LL'
                }
              }
            }
          },
          LegalRepresentationRespondent2: {
            userInput: {
              specRespondent2Represented: 'Yes',
              respondent2: {
                type: 'ORGANISATION',
                organisationName: 'Second Defendant',
                partyName: 'Second Defendant',
                primaryAddress: {
                  AddressLine1: '123 Second Close',
                  PostTown: 'Second Town',
                  PostCode: 'NR5 9LL'
                }
              }
            },
            expected: {
              specRespondent2Represented: 'Yes',
              respondent2: {
                type: 'ORGANISATION',
                organisationName: 'Second Defendant',
                partyName: 'Second Defendant',
                partyTypeDisplayValue: 'Organisation',
                primaryAddress: {
                  AddressLine1: '123 Second Close',
                  PostTown: 'Second Town',
                  PostCode: 'NR5 9LL'
                }
              }
            }
          },
          SecondDefendantSolicitorEmail: {
            userInput: {
              respondentSolicitor2EmailAddress: 'civilmoneyclaimsdemo@gmail.com'
            }
          },

          SameLegalRepresentative: {
            userInput: {
              respondent2SameLegalRepresentative: 'No'
            }
          },
          SecondDefendantSolicitorOrganisation: {
            userInput: {
              respondent2OrgRegistered: 'Yes',
              respondent2OrganisationPolicy: {
                OrgPolicyCaseAssignedRole: '[RESPONDENTSOLICITORTWO]',
                Organisation: {
                  OrganisationID: '79ZRSOU',
                  OrganisationName: 'Civil - Organisation 2'
                }
              }
            }
          }
        };
        break;

      case 'ONE_V_TWO_SAME_SOL':
        userData = {
          ...userData,
          SecondDefendant: {
            userInput: {
              respondent2: {
                type: 'ORGANISATION',
                organisationName: 'Second Defendant',
                primaryAddress: {
                  AddressLine1: '123 Second Close',
                  PostTown: 'Second Town',
                  PostCode: 'NR5 9LL'
                }
              }
            }
          },
          LegalRepresentationRespondent2: {
            userInput: {
              specRespondent2Represented: 'Yes',
              respondent2: {
                type: 'ORGANISATION',
                organisationName: 'Second Defendant',
                partyName: 'Second Defendant',
                primaryAddress: {
                  AddressLine1: '123 Second Close',
                  PostTown: 'Second Town',
                  PostCode: 'NR5 9LL'
                }
              }
            },
            expected: {
              specRespondent2Represented: 'Yes',
              respondent2: {
                type: 'ORGANISATION',
                organisationName: 'Second Defendant',
                partyName: 'Second Defendant',
                partyTypeDisplayValue: 'Organisation',
                primaryAddress: {
                  AddressLine1: '123 Second Close',
                  PostTown: 'Second Town',
                  PostCode: 'NR5 9LL'
                }
              }
            }
          },
          SecondDefendantSolicitorEmail: {
            userInput: {
              respondentSolicitor2EmailAddress: 'civilmoneyclaimsdemo@gmail.com'
            }
          },

          SameLegalRepresentative: {
            userInput: {
              respondent2SameLegalRepresentative: 'Yes'
            }
          }
        };
        break;

      case 'TWO_V_ONE':
        userData = {
          ...userData,
          SecondClaimant: {
            userInput: {
              applicant2: {
                type: 'ORGANISATION',
                organisationName: 'Claim 2',
                partyName: 'Claim 2',
                partyTypeDisplayValue: 'Organisation',
                primaryAddress: {
                  AddressLine1: '43 Montgomery Close',
                  PostTown: 'Norwich',
                  PostCode: 'NR5 9LL'
                }
              }
            }
          }
        };
        break;
    }

    return userData;
  }
};
