const config = require('../../../../config.js');

module.exports = {
  createDefendantResponse: () => {
    return {
      event: 'DEFENDANT_RESPONSE_CUI',
      caseDataUpdate: {
        respondent1ClaimResponseTypeForSpec: 'PART_ADMISSION',
        defenceAdmitPartPaymentTimeRouteRequired: 'IMMEDIATELY',
        respondent1RepaymentPlan: undefined,
        respondToClaimAdmitPartLRspec: {
          whenWillThisAmountBePaid: '2025-04-26',
        },
        responseClaimMediationSpecRequired: 'No',
        specAoSApplicantCorrespondenceAddressRequired: 'Yes',
        totalClaimAmount: 1500,
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
            AddressLine3: '',
            PostCode: 'L7 2PZ',
            PostTown: 'Liverpool',
          },
          type: 'INDIVIDUAL',
        },
        respondent1LiPResponse: {
          timelineComment: undefined,
          evidenceComment: undefined,
          respondent1MediationLiPResponse: undefined,
          respondent1DQExtraDetails: {
            wantPhoneOrVideoHearing: 'No',
            whyPhoneOrVideoHearing: '',
            giveEvidenceYourSelf: 'No',
            determinationWithoutHearingRequired: 'Yes',
            determinationWithoutHearingReason: '',
            considerClaimantDocumentsDetails: '',
            respondent1DQLiPExpert: {
              expertCanStillExamineDetails: '',
            }
          },
          respondent1DQHearingSupportLip: {
            supportRequirementLip: 'No',
            requirementsLip: undefined,
          },
          respondent1ResponseLanguage: 'ENGLISH',
        },
        specDefenceAdmittedRequired: 'No',
        respondToAdmittedClaimOwingAmountPounds: '500',
        respondToAdmittedClaimOwingAmount: '50000',
        detailsOfWhyDoesYouDisputeTheClaim: 'test',
        specClaimResponseTimelineList: 'MANUAL',
        disabilityPremiumPayments: 'No',
        respondent1DQHomeDetails: {
          type: 'PRIVATE_RENTAL',
          typeOtherDetails: '',
        },
        respondent1PartnerAndDependent: {
          liveWithPartnerRequired: 'No',
          partnerAgedOver: undefined,
          haveAnyChildrenRequired: 'No',
          supportedAnyoneFinancialRequired: 'No',
        },
        defenceAdmitPartEmploymentTypeRequired: 'No',
        respondToClaimAdmitPartUnemployedLRspec: {
          unemployedComplexTypeRequired: 'RETIRED',
          lengthOfUnemployment: {
            numberOfYearsInUnemployment: null,
            numberOfMonthsInUnemployment: null,
          },
          otherUnemployment: '',
        },
        respondent1CourtOrderPaymentOption: 'No',
        respondent1CourtOrderDetails: [],
        respondent1DQVulnerabilityQuestions: {
          vulnerabilityAdjustmentsRequired: 'No',
          vulnerabilityAdjustments: undefined,
        },
        respondent1DQRequestedCourt: {
          requestHearingAtSpecificCourt: 'No',
          otherPartyPreferredSite: '',
          responseCourtCode: '121',
          caseLocation: {
            region: config.defendantSelectedCourt,
            baseLocation: config.defendantSelectedCourt
          }
        },
        respondent1DQWitnesses: {
          witnessesToAppear: 'No'
        },
        respondent1DQHearingSmallClaim: {
          unavailableDatesRequired: 'No',
          smallClaimUnavailableDate: undefined,
        },
        respondent1LiPResponseCarm: {
          isMediationContactNameCorrect: 'No',
          alternativeMediationContactPerson: 'new defendant cp',
          isMediationEmailCorrect: 'No',
          alternativeMediationEmail: 'defendantmediation@email.com',
          isMediationPhoneCorrect: 'No',
          alternativeMediationTelephone: '07744444444',
          hasUnavailabilityNextThreeMonths: 'No',
        },
      },
    };
  }
};
