const {listElement, element, date} = require('../../api/dataHelper');
const config = require('../../config.js');
module.exports = {
  respondToClaim: (response = 'FULL_DEFENCE', camundaEvent = 'CREATE_CLAIM_SPEC') => {
    const responseData = {
      userInput: {
        ResponseConfirmNameAddress: {
          specAoSApplicantCorrespondenceAddressRequired: 'Yes',
        },
        ResponseConfirmDetails: {
          specAoSRespondentCorrespondenceAddressRequired: 'Yes'
        },
        RespondentResponseTypeSpec: {
          respondent1ClaimResponseTypeForSpec: 'FULL_DEFENCE'
        },
        defenceRoute: {
          defenceRouteRequired: 'DISPUTES_THE_CLAIM'
        },
        Upload: {
          detailsOfWhyDoesYouDisputeTheClaim: 'details'
        },
        HowToAddTimeline: {
          specClaimResponseTimelineList: 'MANUAL'
        },
        Mediation: {
          responseClaimMediationSpecRequired: 'Yes'
        },
        DeterminationWithoutHearing:{
          deterWithoutHearingRespondent1: {
            deterWithoutHearingYesNo: 'No',
            deterWithoutHearingWhyNot: 'Incredibly valid reasons, respondent 1'
          }
        },
        SmallClaimExperts: {
          responseClaimExpertSpecRequired: 'No'
        },
        SmallClaimWitnesses: {
          responseClaimWitnesses: '1'
        },
        Language: {
          respondent1DQLanguage: {
            court: 'ENGLISH',
            documents: 'ENGLISH'
          }
        },
        SmallClaimHearing: {
          respondent1DQHearingSmallClaim: {
            unavailableDatesRequired: 'No'
          },
          SmallClaimHearingInterpreterRequired: 'No'
        },
        RequestedCourtLocationLRspec: {
          respondToCourtLocation: {
            responseCourtLocations: {
              list_items: [
                listElement(config.defendantSelectedCourt)
              ],
              value: listElement(config.defendantSelectedCourt)
            },
            reasonForHearingAtSpecificCourt: 'Reasons'
          }
        },
        HearingSupport: {
          respondent1DQHearingSupport: {
            supportRequirements: 'Yes',
            supportRequirementsAdditional: 'Additional support reasons'
          },
          respondent1DQHearing: {
            unavailableDatesRequired: 'No'
          }
        },
        VulnerabilityQuestions: {
          respondent1DQVulnerabilityQuestions: {
            vulnerabilityAdjustmentsRequired: 'No'
          }
        },
        StatementOfTruth: {
          uiStatementOfTruth: {
            name: 'name',
            role: 'role'
          }
        }
      },
      midEventData: {
        StatementOfTruth: {
        }
      }
    };

    switch (response) {
      case 'FULL_DEFENCE':
        responseData.userInput = {
          ...responseData.userInput,
          RespondentResponseTypeSpec: {
            respondent1ClaimResponseTypeForSpec: 'FULL_DEFENCE'
          },
          DeterminationWithoutHearing:{
            deterWithoutHearingRespondent1: {
              deterWithoutHearingYesNo: 'No',
              deterWithoutHearingWhyNot: 'Incredibly valid reasons, respondent 1'
            }
          },
          SmallClaimExperts: {
            respondent1DQExperts: {
              expertRequired: 'Yes',
              expertReportsSent: 'NOT_OBTAINED',
              jointExpertSuitable: 'Yes',
              details: [
                element({
                  firstName: 'John',
                  lastName: 'Doe',
                  emailAddress: 'john@doemail.com',
                  phoneNumber: '07111111111',
                  fieldOfExpertise: 'None',
                  whyRequired: 'Testing',
                  estimatedCost: '10000'
                })
              ]
            }
          },
          SmallClaimWitnesses: {
            respondent1DQWitnessesSmallClaim: {
              witnessesToAppear: 'Yes',
              details: [
                element({
                  firstName: 'Witness',
                  lastName: 'One',
                  emailAddress: 'witness@email.com',
                  phoneNumber: '07116778998',
                  reasonForWitness: 'None'
                })
              ]
            }
          },
          defenceRoute: {
            defenceRouteRequired: 'DISPUTES_THE_CLAIM'
          },
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          RespondentResponseTypeSpec: {
            specFullDefenceOrPartAdmission: 'Yes',
            multiPartyResponseTypeFlags: 'FULL_DEFENCE',
            specDefenceFullAdmittedRequired: 'No',
            respondentClaimResponseTypeForSpecGeneric: 'FULL_DEFENCE'
          },

          defenceRoute: {
            responseClaimTrack: 'SMALL_CLAIM',
            respondent1ClaimResponsePaymentAdmissionForSpec: 'DID_NOT_PAY'
          },

          ResponseConfirmNameAddress: {
            businessProcess: {
              status: 'FINISHED',
              camundaEvent: camundaEvent
            },
          }
        };
        break;
      case 'FULL_ADMISSION':
        responseData.userInput = {
          ...responseData.userInput,
          RespondentResponseTypeSpec: {
            respondent1ClaimResponseTypeForSpec: 'FULL_ADMISSION',
            respondentClaimResponseTypeForSpecGeneric: 'FULL_ADMISSION'
          },
          defenceAdmittedPartRoute: {
            specDefenceFullAdmittedRequired: 'No'
          },
          WhenWillClaimBePaid: {
            defenceAdmitPartPaymentTimeRouteRequired: 'IMMEDIATELY'
          },
          Upload: {
            detailsOfWhyDoesYouDisputeTheClaim: 'details'
          },
          HowToAddTimeline: {
            specClaimResponseTimelineList: 'MANUAL'
          },
          ResponseConfirmNameAddress: {
            businessProcess: {
              status: 'FINISHED',
              camundaEvent: camundaEvent
            },
          },
          defenceRoute: {
            specPaidLessAmountOrDisputesOrPartAdmission: 'No',
          }
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          RespondentResponseTypeSpec: {
            specFullDefenceOrPartAdmission: 'No',
            multiPartyResponseTypeFlags: 'FULL_ADMISSION',
            specDefenceFullAdmittedRequired: 'No'
          },
          defenceAdmittedPartRoute: {
            responseClaimTrack: 'SMALL_CLAIM'
          },
          defenceRoute: {
            respondent1ClaimResponsePaymentAdmissionForSpec: 'DID_NOT_PAY',
            responseClaimTrack: 'SMALL_CLAIM'
          }
        };
        break;
      case 'PART_ADMISSION':
        responseData.userInput = {
          ...responseData.userInput,
          RespondentResponseTypeSpec: {
            respondent1ClaimResponseTypeForSpec: 'PART_ADMISSION'
          },
          defenceAdmittedPartRoute: {
            specDefenceAdmittedRequired: 'No',
            respondToAdmittedClaimOwingAmount: '200000'
          },
          WhenWillClaimBePaid: {
            defenceAdmitPartPaymentTimeRouteRequired: 'IMMEDIATELY'
          },
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          RespondentResponseTypeSpec: {
            specFullDefenceOrPartAdmission: 'Yes',
            multiPartyResponseTypeFlags: 'NOT_FULL_DEFENCE',
            specDefenceFullAdmittedRequired: 'No',
            respondentClaimResponseTypeForSpecGeneric: 'PART_ADMISSION'
          },

          defenceAdmittedPartRoute: {
            responseClaimTrack: 'SMALL_CLAIM',
            respondToAdmittedClaimOwingAmountPounds: '2000.00'
          },

          ResponseConfirmNameAddress: {
            businessProcess: {
              status: 'FINISHED',
              camundaEvent: camundaEvent
            }
          },

          defenceRoute: {
            respondent1ClaimResponsePaymentAdmissionForSpec: 'DID_NOT_PAY',
            responseClaimTrack: 'SMALL_CLAIM'
          }
        };
        break;
      case 'COUNTER_CLAIM':
        responseData.userInput = {
          ...responseData.userInput,
          RespondentResponseTypeSpec: {
            respondent1ClaimResponseTypeForSpec: 'COUNTER_CLAIM'
          },
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          RespondentResponseTypeSpec: {
            multiPartyResponseTypeFlags: 'COUNTER_ADMIT_OR_ADMIT_PART',
            specAoSApplicantCorrespondenceAddressRequired: 'Yes',
            specAoSRespondentCorrespondenceAddressRequired: 'Yes',
            specFullDefenceOrPartAdmission: 'No',
            specDefenceFullAdmittedRequired: 'No',
            specApplicantCorrespondenceAddressRequired: 'No',
            specRespondent1Represented: 'Yes',
            respondentClaimResponseTypeForSpecGeneric: 'COUNTER_CLAIM'
          },

          ResponseConfirmNameAddress: {
            businessProcess: {
              status: 'FINISHED',
              camundaEvent: camundaEvent
            }
          },

          defenceRoute: {
            respondent1ClaimResponsePaymentAdmissionForSpec: 'DID_NOT_PAY',
            responseClaimTrack: 'SMALL_CLAIM'
          }
        };
        break;

    }

    return responseData;
  },

  respondToClaimForJudicialReferral: () => {
    return {
      userInput: {
        ResponseConfirmNameAddress: {
          specAoSApplicantCorrespondenceAddressRequired: 'Yes',
        },
        ResponseConfirmDetails: {
          specAoSRespondentCorrespondenceAddressRequired: 'Yes'
        },
        RespondentResponseTypeSpec: {
          respondent1ClaimResponseTypeForSpec: 'FULL_DEFENCE'
        },
        defenceRoute: {
          defenceRouteRequired: 'DISPUTES_THE_CLAIM'
        },
        Upload: {
          detailsOfWhyDoesYouDisputeTheClaim: 'details'
        },
        HowToAddTimeline: {
          specClaimResponseTimelineList: 'MANUAL'
        },
        Mediation: {
          responseClaimMediationSpecRequired: 'No'
        },
        DeterminationWithoutHearing:{
          deterWithoutHearingRespondent1: {
            deterWithoutHearingYesNo: 'No',
            deterWithoutHearingWhyNot: 'Incredibly valid reasons, respondent 1'
          }
        },
        SmallClaimExperts: {
          responseClaimExpertSpecRequired: 'No'
        },
        SmallClaimWitnesses: {
          responseClaimWitnesses: '1'
        },
        Language: {
          respondent1DQLanguage: {
            court: 'ENGLISH',
            documents: 'ENGLISH'
          }
        },
        SmallClaimHearing: {
          respondent1DQHearingSmallClaim: {
            unavailableDatesRequired: 'No'
          },
          SmallClaimHearingInterpreterRequired: 'No'
        },
        RequestedCourtLocationLRspec: {
          respondToCourtLocation: {
            responseCourtLocations: {
              list_items: [
                listElement(config.defendantSelectedCourt)
              ],
              value: listElement(config.defendantSelectedCourt)
            },
            reasonForHearingAtSpecificCourt: 'Reasons'
          }
        },
        HearingSupport: {
          respondent1DQHearingSupport: {
            supportRequirements: 'Yes',
            supportRequirementsAdditional: 'Sir John Doe: Step free wheelchair access'
          },
          respondent1DQHearing: {
            unavailableDatesRequired: 'No'
          }
        },
        VulnerabilityQuestions: {
          respondent1DQVulnerabilityQuestions: {
            vulnerabilityAdjustmentsRequired: 'No'
          }
        },
        StatementOfTruth: {
          uiStatementOfTruth: {
            name: 'name',
            role: 'role'
          }
        }
      },
      midEventData: {
        StatementOfTruth: {
        }
      }
    };
  },

  respondToClaimForCarm: () => {
    return {
      userInput: {
        ResponseConfirmNameAddress: {
          specAoSApplicantCorrespondenceAddressRequired: 'Yes',
        },
        ResponseConfirmDetails: {
          specAoSRespondentCorrespondenceAddressRequired: 'Yes'
        },
        RespondentResponseTypeSpec: {
          respondent1ClaimResponseTypeForSpec: 'FULL_DEFENCE'
        },
        defenceRoute: {
          defenceRouteRequired: 'DISPUTES_THE_CLAIM'
        },
        Upload: {
          detailsOfWhyDoesYouDisputeTheClaim: 'details'
        },
        HowToAddTimeline: {
          specClaimResponseTimelineList: 'MANUAL'
        },
        MediationContactInformation:{
          resp1MediationContactInfo: {
            firstName:'John',
            lastName: 'Maverick',
            emailAddress:'john@doemail.com',
            telephoneNumber:'07111111111'
          }
        },
        MediationAvailability: {
          resp1MediationAvailability: {
            isMediationUnavailablityExists: 'Yes',
            unavailableDatesForMediation: [
              element({
                unavailableDateType: 'SINGLE_DATE',
                date: date(10)
              }),
              element({
                unavailableDateType: 'SINGLE_DATE',
                date: date(55)
              }),
              element({
                fromDate: date(30),
                toDate: date(35),
                unavailableDateType: 'DATE_RANGE',
              }),
              element({
                fromDate: date(40),
                toDate: date(45),
                unavailableDateType: 'DATE_RANGE',
              })
            ]
          }
        },
        DeterminationWithoutHearing:{
          deterWithoutHearingRespondent1: {
            deterWithoutHearingYesNo: 'No',
            deterWithoutHearingWhyNot: 'Incredibly valid reasons, respondent 1'
          }
        },
        SmallClaimExperts: {
          respondent1DQExperts: {
            expertRequired: 'Yes',
            expertReportsSent: 'NOT_OBTAINED',
            jointExpertSuitable: 'Yes',
            details: [
              element({
                firstName: 'John',
                lastName: 'Doe',
                emailAddress: 'john@doemail.com',
                phoneNumber: '07111111111',
                fieldOfExpertise: 'None',
                whyRequired: 'Testing',
                estimatedCost: '10000'
              })
            ]
          }
        },
        SmallClaimWitnesses: {
          respondent1DQWitnessesSmallClaim: {
            witnessesToAppear: 'Yes',
            details: [
              element({
                firstName: 'Witness',
                lastName: 'One',
                emailAddress: 'witness@email.com',
                phoneNumber: '07116778998',
                reasonForWitness: 'None'
              })
            ]
          }
        },
        Language: {
          respondent1DQLanguage: {
            court: 'ENGLISH',
            documents: 'ENGLISH'
          }
        },
        SmallClaimHearing: {
          respondent1DQHearingSmallClaim: {
            unavailableDatesRequired: 'No'
          },
          SmallClaimHearingInterpreterRequired: 'No'
        },
        RequestedCourtLocationLRspec: {
          respondToCourtLocation: {
            responseCourtLocations: {
              list_items: [
                listElement(config.defendantSelectedCourt)
              ],
              value: listElement(config.defendantSelectedCourt)
            },
            reasonForHearingAtSpecificCourt: 'Reasons'
          }
        },
        HearingSupport: {
          respondent1DQHearingSupport: {
            supportRequirements: 'Yes',
            supportRequirementsAdditional: 'Sir John Doe: Step free wheelchair access'
          },
          respondent1DQHearing: {
            unavailableDatesRequired: 'No'
          }
        },
        VulnerabilityQuestions: {
          respondent1DQVulnerabilityQuestions: {
            vulnerabilityAdjustmentsRequired: 'No'
          }
        },
        StatementOfTruth: {
          uiStatementOfTruth: {
            name: 'name',
            role: 'role'
          }
        }
      },
      midEventData: {
      }
    };
  },

  respondToClaimForCarmPartAdmitNotPaid: () => {
    return {
      userInput: {
        ResponseConfirmNameAddress: {
          specAoSApplicantCorrespondenceAddressRequired: 'Yes',
        },
        ResponseConfirmDetails: {
          specAoSRespondentCorrespondenceAddressRequired: 'Yes'
        },
        RespondentResponseTypeSpec: {
          respondent1ClaimResponseTypeForSpec: 'PART_ADMISSION'
        },
        defenceAdmittedPartRoute: {
          specDefenceAdmittedRequired: 'No',
          respondToAdmittedClaimOwingAmount: '50000'
        },
        Upload: {
          detailsOfWhyDoesYouDisputeTheClaim: 'reasons'
        },
        WhenWillClaimBePaid: {
          defenceAdmitPartPaymentTimeRouteRequired: 'IMMEDIATELY'
        },
        HowToAddTimeline: {
          specClaimResponseTimelineList: 'MANUAL'
        },
        MediationContactInformation:{
          resp1MediationContactInfo: {
            firstName:'John',
            lastName: 'Maverick',
            emailAddress:'john@doemail.com',
            telephoneNumber:'07111111111'
          }
        },
        MediationAvailability: {
          resp1MediationAvailability: {
            isMediationUnavailablityExists: 'Yes',
            unavailableDatesForMediation: [
              element({
                unavailableDateType: 'SINGLE_DATE',
                date: date(10)
              }),
              element({
                unavailableDateType: 'SINGLE_DATE',
                date: date(55)
              }),
              element({
                fromDate: date(30),
                toDate: date(35),
                unavailableDateType: 'DATE_RANGE',
              }),
              element({
                fromDate: date(40),
                toDate: date(45),
                unavailableDateType: 'DATE_RANGE',
              })
            ]
          }
        },
        SmallClaimExperts: {
          responseClaimExpertSpecRequired: 'No'
        },
        SmallClaimWitnesses: {
          responseClaimWitnesses: '1'
        },
        Language: {
          respondent1DQLanguage: {
            court: 'ENGLISH',
            documents: 'ENGLISH'
          }
        },
        SmallClaimHearing: {
          respondent1DQHearingSmallClaim: {
            unavailableDatesRequired: 'No'
          },
          SmallClaimHearingInterpreterRequired: 'No'
        },
        RequestedCourtLocationLRspec: {
          respondToCourtLocation: {
            responseCourtLocations: {
              list_items: [
                listElement(config.defendantSelectedCourt)
              ],
              value: listElement(config.defendantSelectedCourt)
            },
            reasonForHearingAtSpecificCourt: 'Reasons'
          },
          respondent1DQRemoteHearingLRspec: {
            remoteHearingRequested: 'Yes',
            reasonForRemoteHearing: 'Some reason'
          }
        },
        HearingSupport: {
          respondent1DQHearingSupport: {
            supportRequirements: 'Yes',
            supportRequirementsAdditional: 'Additional support reasons'
          }
        },
        VulnerabilityQuestions: {
          respondent1DQVulnerabilityQuestions: {
            vulnerabilityAdjustmentsRequired: 'No'
          }
        },
        StatementOfTruth: {
          uiStatementOfTruth: {
            name: 'name',
            role: 'role'
          }
        }
      },
    };
  },

  respondToClaimForCarmPartAdmitStatesPaid: () => {
    return {
      userInput: {
        ResponseConfirmNameAddress: {
          specAoSApplicantCorrespondenceAddressRequired: 'Yes',
        },
        ResponseConfirmDetails: {
          specAoSRespondentCorrespondenceAddressRequired: 'Yes'
        },
        RespondentResponseTypeSpec: {
          respondent1ClaimResponseTypeForSpec: 'PART_ADMISSION'
        },
        defenceAdmittedPartRoute: {
          specDefenceAdmittedRequired: 'Yes',
          respondToAdmittedClaim: {
            howMuchWasPaid: '50000',
            howWasThisAmountPaid: 'CREDIT_CARD',
            whenWasThisAmountPaid: date(-1)
          },
        },
        Upload: {
          detailsOfWhyDoesYouDisputeTheClaim: 'reasons'
        },
        WhenWillClaimBePaid: {
          defenceAdmitPartPaymentTimeRouteRequired: 'IMMEDIATELY'
        },
        HowToAddTimeline: {
          specClaimResponseTimelineList: 'MANUAL'
        },
        MediationContactInformation:{
          resp1MediationContactInfo: {
            firstName:'John',
            lastName: 'Maverick',
            emailAddress:'john@doemail.com',
            telephoneNumber:'07111111111'
          }
        },
        MediationAvailability: {
          resp1MediationAvailability: {
            isMediationUnavailablityExists: 'Yes',
            unavailableDatesForMediation: [
              element({
                unavailableDateType: 'SINGLE_DATE',
                date: date(10)
              }),
              element({
                unavailableDateType: 'SINGLE_DATE',
                date: date(55)
              }),
              element({
                fromDate: date(30),
                toDate: date(35),
                unavailableDateType: 'DATE_RANGE',
              }),
              element({
                fromDate: date(40),
                toDate: date(45),
                unavailableDateType: 'DATE_RANGE',
              })
            ]
          }
        },
        SmallClaimExperts: {
          responseClaimExpertSpecRequired: 'No'
        },
        SmallClaimWitnesses: {
          responseClaimWitnesses: '1'
        },
        Language: {
          respondent1DQLanguage: {
            court: 'ENGLISH',
            documents: 'ENGLISH'
          }
        },
        SmallClaimHearing: {
          respondent1DQHearingSmallClaim: {
            unavailableDatesRequired: 'No'
          },
          SmallClaimHearingInterpreterRequired: 'No'
        },
        RequestedCourtLocationLRspec: {
          respondToCourtLocation: {
            responseCourtLocations: {
              list_items: [
                listElement(config.defendantSelectedCourt)
              ],
              value: listElement(config.defendantSelectedCourt)
            },
            reasonForHearingAtSpecificCourt: 'Reasons'
          },
          respondent1DQRemoteHearingLRspec: {
            remoteHearingRequested: 'Yes',
            reasonForRemoteHearing: 'Some reason'
          }
        },
        HearingSupport: {
          respondent1DQHearingSupport: {
            supportRequirements: 'Yes',
            supportRequirementsAdditional: 'Additional support reasons'
          }
        },
        VulnerabilityQuestions: {
          respondent1DQVulnerabilityQuestions: {
            vulnerabilityAdjustmentsRequired: 'No'
          }
        },
        StatementOfTruth: {
          uiStatementOfTruth: {
            name: 'name',
            role: 'role'
          }
        }
      },
    };
  },

  respondToClaim2: (response = 'FULL_DEFENCE') => {
    const responseData = {
      userInput: {
        ResponseConfirmNameAddress: {
          specAoSRespondent2HomeAddressRequired: 'Yes',
        },
        ResponseConfirmDetails: {
          specAoSRespondent2CorrespondenceAddressRequired: 'Yes'
        },
      },
    };

    switch (response) {
      case 'FULL_DEFENCE':
        responseData.userInput = {
          ...responseData.userInput,
          RespondentResponseTypeSpec: {
            respondent2ClaimResponseTypeForSpec: 'FULL_DEFENCE'
          },
          defenceRoute: {
            defenceRouteRequired2: 'DISPUTES_THE_CLAIM'
          },
          Mediation: {
            responseClaimMediationSpec2Required: 'No'
          },
          DeterminationWithoutHearing:{
            deterWithoutHearingRespondent2: {
              deterWithoutHearingYesNo: 'No',
              deterWithoutHearingWhyNot: 'Incredibly valid reasons, respondent 2'
            }
          },
          SmallClaimExperts: {
            respondent2DQExperts: {
              expertRequired: 'Yes',
              expertReportsSent: 'NOT_OBTAINED',
              jointExpertSuitable: 'Yes',
              details: [
                element({
                  firstName: 'John',
                  lastName: 'Doe',
                  emailAddress: 'john@doemail.com',
                  phoneNumber: '07111111111',
                  fieldOfExpertise: 'None',
                  whyRequired: 'Testing',
                  estimatedCost: '10000'
                })
              ]
            }
          },
          SmallClaimWitnesses: {
            respondent2DQWitnessesSmallClaim: {
              witnessesToAppear: 'Yes',
              details: [
                element({
                  firstName: 'Witness',
                  lastName: 'One',
                  emailAddress: 'witness@email.com',
                  phoneNumber: '07116778998',
                  reasonForWitness: 'None'
                })
              ]
            }
          },
          Language: {
            respondent2DQLanguage: {
              court: 'WELSH',
              documents: 'WELSH'
            }
          },
          SmaillClaimHearing: {
            smallClaimHearingInterpreterDescription2: 'test',
            SmallClaimHearingInterpreter2Required: 'Yes',
            respondent2DQHearingSmallClaim: {
              unavailableDatesRequired: 'No',
            },
          },
          RequestedCourtLocationLRspec: {
            respondToCourtLocation: {
              responseCourtLocations: {
                list_items: [
                  listElement(config.defendantSelectedCourt)
                ],
                value: listElement(config.defendantSelectedCourt)
              },
              reasonForHearingAtSpecificCourt: 'Reasons'
            },
            respondent2DQRemoteHearingLRspec: {
              remoteHearingRequested: 'Yes',
              reasonForRemoteHearing: 'Some reason'
            }
          },
          HearingSupport: {
            respondent2DQHearingSupport: {
              supportRequirements: 'Yes',
              supportRequirementsAdditional: 'Additional support reasons'
            }
          },
          VulnerabilityQuestions: {
            respondent2DQVulnerabilityQuestions: {
              vulnerabilityAdjustmentsRequired: 'Yes',
              vulnerabilityAdjustments: 'test'
            }
          },
          StatementOfTruth: {
            uiStatementOfTruth: {
              name: 'Test',
              role: 'Worker'
            },
            respondent2DQHearing: {
              unavailableDatesRequired: 'No'
            }
          }
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          RespondentResponseTypeSpec: {
            specFullDefenceOrPartAdmission: 'Yes',
            multiPartyResponseTypeFlags: 'FULL_DEFENCE',
            specDefenceFullAdmittedRequired: 'No',
            respondentClaimResponseTypeForSpecGeneric: 'FULL_DEFENCE'
          },

          defenceRoute: {
            responseClaimTrack: 'SMALL_CLAIM',
            respondent1ClaimResponsePaymentAdmissionForSpec: 'DID_NOT_PAY'
          }
        };
        break;
    }

    return responseData;
  }
};
