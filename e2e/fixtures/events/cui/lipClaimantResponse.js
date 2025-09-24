const {date} = require('../../../api/dataHelper');
const config = require('../../../config.js');

const lipClaimantData = {
  'PA_REJECT_NO_MEDIATION': {
    event: 'CLAIMANT_RESPONSE_CUI',
    caseDataUpdate: {
      applicant1AcceptAdmitAmountPaidSpec: 'No',
      applicant1LiPResponse: {
        applicant1DQExtraDetails: {
          wantPhoneOrVideoHearing: 'No',
          giveEvidenceYourSelf: 'No',
          determinationWithoutHearingRequired: 'Yes',
        },
        applicant1DQHearingSupportLip: {
          supportRequirementLip: 'No',
          requirementsLip: undefined,
        },
      },
      applicant1DQLanguage: {
        evidence: undefined,
        court: 'ENGLISH',
        documents: 'ENGLISH',
      },
      applicant1DQVulnerabilityQuestions: {
        vulnerabilityAdjustmentsRequired: 'No',
      },
      applicant1DQRequestedCourt: {
        requestHearingAtSpecificCourt: 'No',
      },
      applicant1DQWitnesses: {
        witnessesToAppear: 'No',
      },
      applicant1DQSmallClaimHearing: {
        unavailableDatesRequired: 'No',
      },
      applicant1DQHearingSupport: {
        supportRequirements: 'No',
      },
      applicant1SettleClaim: 'No',
    },
  },
  'FA_ACCEPT_CCJ': {
    event: 'CLAIMANT_RESPONSE_CUI',
    caseDataUpdate: {
      applicant1LiPResponse: {
        applicant1ChoosesHowToProceed: 'REQUEST_A_CCJ',
      },
      applicant1AcceptFullAdmitPaymentPlanSpec: 'Yes',
      applicant1RepaymentOptionForDefendantSpec: 'SET_DATE',
      applicant1SettleClaim: 'Yes',
      applicant1RequestedPaymentDateForDefendantSpec: {
        paymentSetDate: '2024-08-28',
      },
      ccjPaymentPaidSomeOption: 'No',
      ccjPaymentPaidSomeAmount: null,
      ccjJudgmentAmountClaimFee: '80',
      ccjJudgmentLipInterest: '0',
      applicant1: {
        companyName: undefined,
        individualDateOfBirth: '1993-08-28',
        individualFirstName: 'Claimant',
        individualLastName: 'person',
        individualTitle: 'Mr',
        organisationName: undefined,
        partyEmail: 'citizen.user1@gmail.com',
        partyPhone: undefined,
        primaryAddress: {
          AddressLine1: '123',
          AddressLine2: 'Fake Street',
          AddressLine3: undefined,
          PostCode: 'S12eu',
          PostTown: 'sheffield',
        },
        type: 'INDIVIDUAL',
      },
      respondent1: {
        companyName: undefined,
        individualDateOfBirth: '1993-08-28',
        individualFirstName: 'defendant',
        individualLastName: 'person',
        individualTitle: 'mr',
        organisationName: undefined,
        partyEmail: 'civilmoneyclaimsdemo@gmail.com',
        partyPhone: '07800000000',
        primaryAddress: {
          AddressLine1: '123',
          AddressLine2: 'Claim Road',
          AddressLine3: undefined,
          PostCode: 'L7 2PZ',
          PostTown: 'Liverpool',
        },
        type: 'INDIVIDUAL',
      },
      totalClaimAmount: 1500,
    },
  },
  'PA_ACCEPT_CCJ': {
    event: 'CLAIMANT_RESPONSE_CUI',
    caseDataUpdate: {
      applicant1AcceptAdmitAmountPaidSpec: 'Yes',
      applicant1LiPResponse: {
        applicant1ChoosesHowToProceed: 'REQUEST_A_CCJ',
      },
      applicant1AcceptPartAdmitPaymentPlanSpec: 'Yes',
      applicant1RepaymentOptionForDefendantSpec: 'REPAYMENT_PLAN',
      applicant1SettleClaim: 'Yes',
      applicant1SuggestInstalmentsPaymentAmountForDefendantSpec: 100,
      applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec: 'ONCE_ONE_MONTH',
      applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec: '2025-05-04',
      applicant1RequestedPaymentDateForDefendantSpec: {},
      ccjPaymentPaidSomeOption: 'No',
      ccjPaymentPaidSomeAmount: null,
      ccjJudgmentAmountClaimFee: 80,
      ccjJudgmentLipInterest: 0,
      applicant1: {
        individualDateOfBirth: '1995-08-28',
        individualFirstName: 'Jane',
        individualLastName: 'Doe',
        individualTitle: 'Miss',
        partyEmail: 'civilmoneyclaimsdemo@gmail.com',
        partyPhone: undefined,
        primaryAddress: {
          AddressLine1: '123',
          AddressLine2: 'Fake Street',
          PostCode: 'S12eu',
          PostTown: 'sheffield'
        },
        soleTraderDateOfBirth: null,
        type: 'INDIVIDUAL'
      },
      respondent1: {
        individualDateOfBirth: '1993-08-28',
        individualFirstName: 'defendant',
        individualLastName: 'person',
        individualTitle: 'mr',
        partyEmail: 'civilmoneyclaimsdemo@gmail.com',
        partyPhone: '07800000000',
        primaryAddress: {
          AddressLine1: '123',
          AddressLine2: 'Claim Road',
          PostCode: 'L7 2PZ',
          PostTown: 'Liverpool'
        },
        soleTraderDateOfBirth: null,
        type: 'INDIVIDUAL'
      },
      totalClaimAmount: 1500
    }
  }
};

module.exports = {
  claimantResponse: (carmEnabled, typeOfData = '') => {
    const claimantResponseData = {
      event: 'CLAIMANT_RESPONSE_CUI',
      caseDataUpdate: {
        applicant1LiPResponse: {
          applicant1DQExtraDetails: {
            wantPhoneOrVideoHearing: 'Yes',
            whyPhoneOrVideoHearing: 'bla bla',
            giveEvidenceYourSelf: 'Yes',
            determinationWithoutHearingRequired: 'Yes',
            determinationWithoutHearingReason: '',
            considerClaimantDocumentsDetails: '',
            applicant1DQLiPExpert: {
              caseNeedsAnExpert: 'No',
              expertCanStillExamineDetails: ''
            }
          },
          applicant1DQHearingSupportLip: {
            supportRequirementLip: 'Yes',
            requirementsLip: [
              {
                value: {
                  name: 'Whit Ness',
                  requirements: [
                    'DISABLED_ACCESS'
                  ],
                  signLanguageRequired: '',
                  languageToBeInterpreted: '',
                  otherSupport: ''
                }
              }
            ]
          }
        },
        applicant1DQLanguage: {
          court: 'ENGLISH',
          documents: 'ENGLISH'
        },
        applicant1DQVulnerabilityQuestions: {
          vulnerabilityAdjustmentsRequired: 'Yes',
          vulnerabilityAdjustments: 'vulnerable'
        },
        applicant1DQRequestedCourt: {
          requestHearingAtSpecificCourt: 'Yes',
          otherPartyPreferredSite: '',
          responseCourtCode: '',
          reasonForHearingAtSpecificCourt: 'reasons',
          responseCourtLocations: [],
          caseLocation: {
            region: config.claimantSelectedCourt,
            baseLocation:  config.claimantSelectedCourt
          }
        },
        applicant1DQWitnesses: {
          witnessesToAppear: 'Yes',
          details: [
            {
              value: {
                name: 'Whit',
                firstName: 'Whit',
                lastName: 'Ness',
                emailAddress: '',
                phoneNumber: '',
                reasonForWitness: 'red builds'
              }
            }
          ]
        },
        applicant1DQSmallClaimHearing: {
          unavailableDatesRequired: 'Yes',
          smallClaimUnavailableDate: [
            {
              value: {
                who: 'defendant',
                date: date(6),
                fromDate: date(6),
                unavailableDateType: 'SINGLE_DATE'
              }
            },
            {
              value: {
                who: 'defendant',
                date: date(10),
                fromDate: date(10),
                toDate: date(15),
                unavailableDateType: 'DATE_RANGE'
              }
            }
          ]
        },
        applicant1DQExperts: {},
        applicant1RepaymentOptionForDefendantSpec: 'IMMEDIATELY',
        applicant1ProceedWithClaim: 'Yes',
        applicant1PartAdmitIntentionToSettleClaimSpec: 'No',
        applicant1FullDefenceConfirmAmountPaidSpec: 'Yes',
        applicant1ClaimMediationSpecRequired: {
          hasAgreedFreeMediation: 'No'
        }
      }
    };
    const claimantResponseDataCarm = {
      event: 'CLAIMANT_RESPONSE_CUI',
      caseDataUpdate: {
        applicant1LiPResponse: {
          applicant1DQExtraDetails: {
            wantPhoneOrVideoHearing: 'Yes',
            whyPhoneOrVideoHearing: 'skype',
            giveEvidenceYourSelf: 'Yes',
            determinationWithoutHearingRequired: 'No',
            determinationWithoutHearingReason: 'reasons',
            considerClaimantDocumentsDetails: '',
            applicant1DQLiPExpert: {
              caseNeedsAnExpert: 'No',
              expertCanStillExamineDetails: ''
            }
          },
          applicant1DQHearingSupportLip: {
            supportRequirementLip: 'Yes',
            requirementsLip: [
              {
                value: {
                  name: 'Test Inc',
                  requirements: [
                    'DISABLED_ACCESS'
                  ],
                  signLanguageRequired: '',
                  languageToBeInterpreted: '',
                  otherSupport: ''
                }
              },
              {
                value: {
                  name: 'Whit Ness',
                  requirements: [
                    'HEARING_LOOPS'
                  ],
                  signLanguageRequired: '',
                  languageToBeInterpreted: '',
                  otherSupport: ''
                }
              }
            ]
          },
          applicant1RejectedRepaymentReason: 'reasons'
        },
        applicant1LiPResponseCarm: {
          isMediationContactNameCorrect: 'No',
          alternativeMediationContactPerson: 'new contact person',
          isMediationEmailCorrect: 'No',
          alternativeMediationEmail: 'anotherem@ail.com',
          isMediationPhoneCorrect: 'No',
          alternativeMediationTelephone: '07755555555',
          hasUnavailabilityNextThreeMonths: 'Yes',
          unavailableDatesForMediation: [
            {
              value: {
                who: 'defendant',
                date: date(6),
                fromDate: date(6),
                unavailableDateType: 'SINGLE_DATE'
              }
            },
            {
              value: {
                who: 'defendant',
                date: date(10),
                fromDate: date(10),
                toDate: date(15),
                unavailableDateType: 'DATE_RANGE'
              }
            }
          ]
        },
        applicant1DQLanguage: {
          court: 'ENGLISH',
          documents: 'ENGLISH'
        },
        applicant1DQVulnerabilityQuestions: {
          vulnerabilityAdjustmentsRequired: 'Yes',
          vulnerabilityAdjustments: 'vulnerable'
        },
        applicant1DQRequestedCourt: {
          requestHearingAtSpecificCourt: 'Yes',
          otherPartyPreferredSite: '',
          responseCourtCode: '',
          reasonForHearingAtSpecificCourt: 'reasons',
          responseCourtLocations: [],
          caseLocation: {
            region:  config.claimantSelectedCourt,
            baseLocation:  config.claimantSelectedCourt
          }
        },
        applicant1DQWitnesses: {
          witnessesToAppear: 'Yes',
          details: [
            {
              value: {
                name: 'Whit',
                firstName: 'Whit',
                lastName: 'Ness',
                emailAddress: '',
                phoneNumber: '',
                reasonForWitness: 'terrible things'
              }
            }
          ]
        },
        applicant1DQSmallClaimHearing: {
          unavailableDatesRequired: 'Yes',
          smallClaimUnavailableDate: [
            {
              value: {
                who: 'defendant',
                date: date(6),
                fromDate: date(6),
                unavailableDateType: 'SINGLE_DATE'
              }
            },
            {
              value: {
                who: 'defendant',
                date: date(10),
                fromDate: date(10),
                toDate: date(15),
                unavailableDateType: 'DATE_RANGE'
              }
            }
          ]
        },
        applicant1DQExperts: {},
        applicant1DQHearingSupport: {
          supportRequirements: 'Yes',
          supportRequirementsAdditional: 'Test Inc :Disabled access;Whit Ness :Hearing loop;'
        },
        applicant1PartAdmitIntentionToSettleClaimSpec: 'No',
        applicant1FullDefenceConfirmAmountPaidSpec: 'Yes',
        applicant1ProceedWithClaim: 'Yes',
        applicant1SettleClaim: 'No'
      }
    };
    if (lipClaimantData[typeOfData]) {
      return lipClaimantData[typeOfData];
    }
    return carmEnabled ? claimantResponseDataCarm : claimantResponseData;
  },
  claimantResponsePartAdmitRejectCarm: () => {
    return {
      event: 'CLAIMANT_RESPONSE_CUI',
      caseDataUpdate: {
        applicant1AcceptAdmitAmountPaidSpec: 'No',
        applicant1LiPResponse: {
          applicant1DQExtraDetails: {
            wantPhoneOrVideoHearing: 'No',
            whyPhoneOrVideoHearing: '',
            giveEvidenceYourSelf: 'No',
            determinationWithoutHearingRequired: 'Yes',
            determinationWithoutHearingReason: '',
            considerClaimantDocumentsDetails: '',
            applicant1DQLiPExpert: {
              expertCanStillExamineDetails: ''
            }
          },
          applicant1DQHearingSupportLip: {
            supportRequirementLip: 'No'
          }
        },
        applicant1LiPResponseCarm: {
          isMediationContactNameCorrect: 'Yes',
          isMediationEmailCorrect: 'Yes',
          isMediationPhoneCorrect: 'Yes',
          hasUnavailabilityNextThreeMonths: 'No'
        },
        applicant1DQLanguage: {
          court: 'ENGLISH',
          documents: 'ENGLISH'
        },
        applicant1DQVulnerabilityQuestions: {
          vulnerabilityAdjustmentsRequired: 'No'
        },
        applicant1DQRequestedCourt: {
          otherPartyPreferredSite: '',
          responseCourtCode: '',
          reasonForHearingAtSpecificCourt: 'location',
          responseCourtLocations: [],
          caseLocation: {
            region: config.claimantSelectedCourt,
            baseLocation: config.claimantSelectedCourt
          }
        },
        applicant1DQWitnesses: {
          witnessesToAppear: 'No',
          details: [
            {
              value: {
                name: '',
                firstName: '',
                lastName: '',
                emailAddress: '',
                phoneNumber: '',
                reasonForWitness: ''
              }
            }
          ]
        },
        applicant1DQSmallClaimHearing: {
          unavailableDatesRequired: 'No'
        },
        applicant1DQExperts: {},
        applicant1DQHearingSupport: {
          supportRequirements: 'No'
        },
        applicant1SettleClaim: 'No'
      }
    };
  }
};
