const { date } = require('../../../../api/dataHelper');

const individualClaim = {
  event: 'CREATE_LIP_CLAIM',
  caseDataUpdate: {
    applicant1: {
      individualDateOfBirth: '1995-08-28',
      individualFirstName: 'Jane',
      individualLastName: 'Doe',
      individualTitle: 'Miss',
      partyEmail: 'civilmoneyclaimsdemo@gmail.com',
      partyPhone: '07446777177',
      primaryAddress: {
        AddressLine1: '123',
        AddressLine2: 'Fake Street',
        AddressLine3: '',
        PostCode: 'S12eu',
        PostTown: 'sheffield',
      },
      type: 'INDIVIDUAL',
    },
    respondent1: {
      individualDateOfBirth: null,
      individualFirstName: 'John',
      individualLastName: 'Doe',
      individualTitle: 'Sir',
      partyEmail: 'civilmoneyclaimsdemo@gmail.com',
      partyPhone: '07800000000',
      primaryAddress: {
        AddressLine1: 'TestAddressLine1',
        AddressLine2: 'TestAddressLine2',
        AddressLine3: 'TestAddressLine3',
        PostCode: 'IG61JD',
        PostTown: 'TestCity',
      },
      type: 'INDIVIDUAL',
    },
    applicant1Represented: 'No',
    totalClaimAmount: 0,
    claimAmountBreakup: [
      {
        id: '0',
        value: {
          claimAmount: 0,
          claimReason: 'Injury',
        },
      },
    ],
    detailsOfClaim: 'Injury',
    claimInterest: 'No',
    claimantUserDetails: {
      email: 'civilmoneyclaimsdemo@gmail.com',
      id: '',
    },
    specRespondent1Represented: 'No',
    helpWithFees: {
      helpWithFee: 'No',
      helpWithFeesReferenceNumber: '',
    },
    pcqId: '4c10fec5-1278-45f3-89f0-d3d016d47f95',
    respondent1AdditionalLipPartyDetails: {
      contactPerson: 'Test Company',
    },
    applicant1AdditionalLipPartyDetails: {
      correspondenceAddress: {
        AddressLine1: '123',
        AddressLine2: 'Test Street',
        AddressLine3: '',
        PostCode: 'L7 2pz',
        PostTown: 'Liverpool',
      },
      contactPerson: 'Test Company',
    },
    timelineOfEvents: [
      {
        id: '0',
        value: {
          timelineDate: '2000-01-01',
          timelineDescription: 'test',
        },
      },
    ],
    claimFee: {
      calculatedAmountInPence: '45500',
      version: '3',
      code: 'FEE0208',
    },
    claimantBilingualLanguagePreference: undefined,
  },
};
module.exports = {
  createClaimUnrepresentedClaimant: (claimAmount, userId, typeOfData = '') => {
    const createClaimData = {
      event: 'CREATE_LIP_CLAIM',
      caseDataUpdate: {
        applicant1: {
          individualDateOfBirth: null,
          organisationName: 'Test Inc',
          partyEmail: 'civilmoneyclaimsdemo@gmail.com',
          partyPhone: '07711111111',
          primaryAddress: {
            AddressLine1: '1',
            AddressLine2: '',
            AddressLine3: '',
            PostCode: 'E1 6AN',
            PostTown: 'London'
          },
          soleTraderDateOfBirth: null,
          type: 'ORGANISATION'
        },
        respondent1: {
          individualDateOfBirth: null,
          organisationName: 'Sir John Doe',
          partyEmail: 'civilmoneyclaimsdemo@gmail.com',
          partyPhone: '07777777777',
          primaryAddress: {
            AddressLine1: '1',
            AddressLine2: '',
            AddressLine3: '',
            PostCode: 'E1 6AN',
            PostTown: 'London'
          },
          soleTraderDateOfBirth: null,
          type: 'ORGANISATION'
        },
        applicant1Represented: 'No',
        totalClaimAmount: claimAmount,
        claimAmountBreakup: [
          {
            id: '0',
            value: {
              claimAmount: '100000',
              claimReason: 'sdf'
            }
          }
        ],
        detailsOfClaim: 'asd',
        speclistYourEvidenceList: [
          {
            id: '0',
            value: {
              evidenceType: 'CONTRACTS_AND_AGREEMENTS',
              contractAndAgreementsEvidence: 'asd'
            }
          }
        ],
        claimInterest: 'No',
        claimantUserDetails: {
          email: 'civilmoneyclaimsdemo@gmail.com',
          id: userId
        },
        respondent1LiPResponse: {
          respondent1DQExtraDetails: {
            whyPhoneOrVideoHearing: '',
            determinationWithoutHearingReason: '',
            considerClaimantDocumentsDetails: '',
            respondent1DQLiPExpert: {
              expertCanStillExamineDetails: ''
            }
          },
          respondent1DQHearingSupportLip: {},
          respondent1LiPContactPerson: ''
        },
        specRespondent1Represented: 'No',
        helpWithFees: {
          helpWithFee: 'No',
          helpWithFeesReferenceNumber: ''
        },
        respondent1AdditionalLipPartyDetails: {
          correspondenceAddress: {},
          contactPerson: ''
        },
        applicant1AdditionalLipPartyDetails: {
          correspondenceAddress: {
            AddressLine1: '',
            AddressLine2: '',
            AddressLine3: '',
            PostCode: '',
            PostTown: ''
          },
          contactPerson: 'claimant contact person'
        },
        claimFee: {
          calculatedAmountInPence: '3500',
          code: 'FEE0202',
          version: '4'
        }
      }
    };

    if (typeOfData === 'INDIVIDUAL') {
      individualClaim.caseDataUpdate.totalClaimAmount = claimAmount;
      individualClaim.caseDataUpdate.claimAmountBreakup[0].value.claimAmount = claimAmount;
      individualClaim.caseDataUpdate.claimantUserDetails.id = userId;
      return individualClaim;
    }


    return createClaimData;
  },

  issueClaim: () => {
    const claimIssueData = {
      event: 'CREATE_CLAIM_SPEC_AFTER_PAYMENT',
      caseDataUpdate: {
        issueDate: date(0),
        caseManagementLocation: {
          region: '2',
          baseLocation: '201339'
        },
      }
    };
    return claimIssueData;
  },
};
