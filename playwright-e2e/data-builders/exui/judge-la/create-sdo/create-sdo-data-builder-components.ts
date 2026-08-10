import SdoType from '../../../../constants/ccd-events/sdo/sdo-type';
import DateHelper from '../../../../helpers/date-helper';
import CaseDataHelper from '../../../../helpers/case-data-helper';
import preferredCourts from '../../../../config/preferred-courts';
import partys from '../../../../constants/users/partys';

const formatDate = (date: Date) =>
  DateHelper.formatDateToString(date, { outputFormat: 'YYYY-MM-DD' });

const sdo = (sdoType: SdoType) => {
  if (
    sdoType === SdoType.TRAIL ||
    sdoType === SdoType.SMALL_TRACK_SUM ||
    sdoType === SdoType.TRAIL_NIHL
  )
    return {
      SDO: {
        drawDirectionsOrderRequired: 'Yes',
        drawDirectionsOrder: {
          judgementSum: '100',
        },
      },
    };

  return {
    SDO: {
      drawDirectionsOrderRequired: 'No',
    },
  };
};

const claimsTrack = (sdoType: SdoType) => {
  if (sdoType === SdoType.FAST_TRACK)
    return {
      ClaimsTrack: {
        claimsTrack: 'fastTrack',
        fastClaims: [
          'fastClaimBuildingDispute',
          'fastClaimClinicalNegligence',
          'fastClaimCreditHire',
          'fastClaimEmployersLiability',
          'fastClaimHousingDisrepair',
          'fastClaimPPI',
          'fastClaimPersonalInjury',
          'fastClaimRoadTrafficAccident',
        ],
      },
    };
  else if (sdoType === SdoType.FAST_TRACK_NIHL)
    return {
      ClaimsTrack: {
        claimsTrack: 'fastTrack',
        fastClaims: ['fastClaimNoiseInducedHearingLoss'],
      },
    };
  else if (sdoType === SdoType.TRAIL || sdoType === SdoType.TRAIL_NIHL)
    return {
      ClaimsTrack: {
        drawDirectionsOrderSmallClaims: 'No',
      },
    };
  else if (sdoType === SdoType.SMALL_TRACK_SUM)
    return {
      ClaimsTrack: {
        drawDirectionsOrderSmallClaims: 'Yes',
        drawDirectionsOrderSmallClaimsAdditionalDirections: [
          'smallClaimCreditHire',
          'smallClaimFlightDelay',
          'smallClaimHousingDisrepair',
          'smallClaimPPI',
          'smallClaimRoadTrafficAccident',
        ],
      },
    };
  else if (sdoType === SdoType.SMALL_TRACK_SUM_DRH)
    return {
      ClaimsTrack: {
        drawDirectionsOrderSmallClaims: 'Yes',
        drawDirectionsOrderSmallClaimsAdditionalDirections: ['smallClaimDisputeResolutionHearing'],
      },
    };
  else if (sdoType === SdoType.SMALL_TRACK_NO_SUM)
    return {
      ClaimsTrack: {
        claimsTrack: 'smallClaimsTrack',
        smallClaims: [
          'smallClaimCreditHire',
          'smallClaimFlightDelay',
          'smallClaimHousingDisrepair',
          'smallClaimPPI',
          'smallClaimRoadTrafficAccident',
        ],
      },
    };
  else if (sdoType === SdoType.SMALL_TRACK_NO_SUM_DRH)
    return {
      ClaimsTrack: {
        claimsTrack: 'smallClaimsTrack',
        smallClaims: ['smallClaimDisputeResolutionHearing'],
      },
    };

  return {};
};

const orderType = (sdoType: SdoType) => {
  if (sdoType === SdoType.TRAIL)
    return {
      OrderType: {
        orderType: 'DECIDE_DAMAGES',
        trialAdditionalDirectionsForFastTrack: [
          'fastClaimBuildingDispute',
          'fastClaimClinicalNegligence',
          'fastClaimCreditHire',
          'fastClaimEmployersLiability',
          'fastClaimHousingDisrepair',
          'fastClaimPPI',
          'fastClaimPersonalInjury',
          'fastClaimRoadTrafficAccident',
        ],
      },
    };
  else if (sdoType === SdoType.DISPOSAL_HEARING)
    return {
      OrderType: {
        orderType: 'DISPOSAL',
      },
    };
  else if (sdoType === SdoType.TRAIL_NIHL)
    return {
      OrderType: {
        orderType: 'DECIDE_DAMAGES',
        trialAdditionalDirectionsForFastTrack: ['fastClaimNoiseInducedHearingLoss'],
      },
    };

  return {};
};

const fastTrack = (sdoType: SdoType) => {
  const claimantDefaultCourt = CaseDataHelper.setCodeToData(
    preferredCourts[partys.CLAIMANT_1.key].default,
  );
  const hearingMethodInPerson = CaseDataHelper.setCodeToData('In Person');

  if (sdoType === SdoType.FAST_TRACK || sdoType === SdoType.TRAIL)
    return {
      FastTrack: {
        isSdoR2NewScreen: 'No',
        setFastTrackFlag: 'Yes',
        hearingMethodValuesFastTrack: {
          list_items: [hearingMethodInPerson],
          value: hearingMethodInPerson,
        },
        fastTrackMethodInPerson: {
          list_items: [claimantDefaultCourt],
          value: claimantDefaultCourt,
        },
        fastTrackJudgesRecital: {
          input: 'string',
        },
        fastTrackAllocation: {
          assignComplexityBand: 'Yes',
          band: 'BAND_1',
          reasons: 'string',
        },
        fastTrackDisclosureOfDocuments: {
          input1: 'string',
          date1: formatDate(DateHelper.addToToday({ days: 14 })),
          input2: 'string',
          date2: formatDate(DateHelper.addToToday({ days: 21 })),
          input3: 'string',
        },
        sdoR2FastTrackWitnessOfFact: {
          sdoStatementOfWitness: 'string',
          sdoWitnessDeadline: 'string',
          sdoWitnessDeadlineDate: formatDate(DateHelper.addToToday({ days: 35 })),
          sdoWitnessDeadlineText: 'string',
          sdoR2RestrictWitness: {
            isRestrictWitness: 'No',
          },
          sdoRestrictPages: {
            isRestrictPages: 'No',
          },
        },
        fastTrackSchedulesOfLoss: {
          input1: 'string',
          date1: formatDate(DateHelper.addToToday({ days: 84 })),
          input2: 'string',
          date2: formatDate(DateHelper.addToToday({ days: 98 })),
          input3: 'string',
        },
        fastTrackHearingTime: {
          dateFrom: formatDate(DateHelper.addToToday({ days: 140 })),
          dateTo: formatDate(DateHelper.addToToday({ days: 203 })),
          hearingDuration: 'ONE_HOUR',
          helpText1: 'string',
        },
        fastTrackBuildingDispute: {
          input1: 'string',
          input2: 'string',
          input3: 'string',
          date1: formatDate(DateHelper.addToToday({ days: 70 })),
          input4: 'string',
          date2: formatDate(DateHelper.addToToday({ days: 84 })),
        },
        fastTrackClinicalNegligence: {
          input1: 'string',
          input2: 'string',
          input3: 'string',
          input4: 'string',
        },
        sdoR2FastTrackCreditHire: {
          input1: 'string',
          input5: 'string',
          input6: 'string',
          date3: formatDate(DateHelper.addToToday({ days: 56 })),
          input7: 'string',
          date4: formatDate(DateHelper.addToToday({ days: 70 })),
          input8: 'string',
          sdoR2FastTrackCreditHireDetails: {
            input2: 'string',
            input3: 'string',
            input4: 'string',
            date2: formatDate(DateHelper.addToToday({ days: 42 })),
          },
        },
        fastTrackHousingDisrepair: {
          clauseA: 'string',
          clauseB: 'string',
          firstReportDateBy: formatDate(DateHelper.addToToday({ days: 28 })),
          clauseCBeforeDate: 'string',
          jointStatementDateBy: formatDate(DateHelper.addToToday({ days: 56 })),
          clauseCAfterDate: 'string',
          clauseD: 'string',
          clauseE: 'string',
        },
        fastTrackPersonalInjury: {
          input1: 'string',
          input2: 'string',
          date2: formatDate(DateHelper.addToToday({ days: 49 })),
          input3: 'string',
          date3: formatDate(DateHelper.addToToday({ days: 63 })),
          input4: 'string',
          date4: formatDate(DateHelper.addToToday({ days: 70 })),
          date1: formatDate(DateHelper.addToToday({ days: 1 })),
        },
        fastTrackRoadTrafficAccident: {
          input: 'string',
          date: formatDate(DateHelper.addToToday({ days: 56 })),
        },
        fastTrackPPI: {
          ppiDate: formatDate(DateHelper.addToToday({ days: 28 })),
          text: 'string',
        },
        fastTrackAddNewDirections: [
          CaseDataHelper.setIdToData({
            directionComment: 'string',
          }),
        ],
        fastTrackHearingNotes: {
          input: 'string',
        },
        sdoR2FastTrackUseOfWelshLanguage: 'string',
        fastTrackOrderWithoutJudgement: {
          input: 'string',
        },
      },
    };
};

const smallClaims = (sdoType: SdoType) => {
  const claimantDefaultCourt = CaseDataHelper.setCodeToData(
    preferredCourts[partys.CLAIMANT_1.key].default,
  );
  const hearingMethodInPerson = CaseDataHelper.setCodeToData('In Person');

  if (sdoType === SdoType.SMALL_TRACK_SUM || sdoType === SdoType.SMALL_TRACK_NO_SUM) {
    return {
      SmallClaims: {
        smallClaimsPenalNotice: 'string',
        smallClaimsJudgesRecital: {
          input: 'string',
        },
        smallClaimsJudgementDeductionValue: {
          value: '100.0%',
        },
        smallClaimsFlightDelay: {
          relatedClaimsInput: 'string',
          legalDocumentsInput: 'string',
        },
        smallClaimsHearing: {
          dateFrom: formatDate(DateHelper.addToToday({ days: 42 })),
          time: 'THIRTY_MINUTES',
          input2: 'string',
          input1: 'string',
        },
        hearingMethodValuesSmallClaims: {
          list_items: [hearingMethodInPerson],
          value: hearingMethodInPerson,
        },
        smallClaimsMethodInPerson: {
          list_items: [claimantDefaultCourt],
          value: claimantDefaultCourt,
        },
        sdoHearingNotes: {
          input: 'string',
        },
        sdoR2SmallClaimsUseOfWelshLanguage: {
          description: 'string',
        },
        smallClaimsNotes: {
          input: 'string',
          date: formatDate(DateHelper.addToToday({ days: 7 })),
        },
        smallClaimsDocuments: {
          input1: 'string',
          deadlineDate: formatDate(DateHelper.addToToday({ days: 28 })),
          input2: 'string',
        },
        sdoR2SmallClaimsWitnessStatementOther: {
          sdoStatementOfWitness: 'string',
          deadlineDate: formatDate(DateHelper.addToToday({ days: 28 })),
          isRestrictWitness: 'No',
          isRestrictPages: 'No',
          text: 'string',
        },
        smallClaimsCreditHire: {
          input1: 'string',
          input2: 'string',
          input3: 'string',
          input4: 'string',
          date2: formatDate(DateHelper.addToToday({ days: 42 })),
          input5: 'string',
          input6: 'string',
          date3: formatDate(DateHelper.addToToday({ days: 56 })),
          input7: 'string',
          date4: formatDate(DateHelper.addToToday({ days: 70 })),
          input11: 'string',
          date1: formatDate(DateHelper.addToToday({ days: 1 })),
        },
        smallClaimsRoadTrafficAccident: {
          input: 'string',
        },
        smallClaimsPPI: {
          ppiDate: formatDate(DateHelper.addToToday({ days: 28 })),
          text: 'string',
        },
        smallClaimsHousingDisrepair: {
          clauseA: 'string',
          clauseB: 'string',
          firstReportDateBy: formatDate(DateHelper.addToToday({ days: 28 })),
          clauseCBeforeDate: 'string',
          jointStatementDateBy: formatDate(DateHelper.addToToday({ days: 56 })),
          clauseCAfterDate: 'string',
          clauseD: 'string',
          clauseE: 'string',
        },
        smallClaimsAddNewDirections: [
          CaseDataHelper.setIdToData({
            directionComment: 'string',
          }),
        ],
      },
    };
  }

  return {};
};

const sdoR2FastTrack = (sdoType: SdoType) => {
  const claimantDefaultCourt = CaseDataHelper.setCodeToData(
    preferredCourts[partys.CLAIMANT_1.key].default,
  );
  const hearingMethodInPerson = CaseDataHelper.setCodeToData('In Person');

  if (sdoType === SdoType.FAST_TRACK_NIHL || sdoType === SdoType.TRAIL_NIHL) {
    return {
      SdoR2FastTrack: {
        sdoFastTrackJudgesRecital: {
          input: 'string',
        },
        sdoR2DisclosureOfDocuments: {
          standardDisclosureTxt: 'string',
          standardDisclosureDate: formatDate(DateHelper.addToToday({ days: 14 })),
          inspectionTxt: 'string',
          inspectionDate: formatDate(DateHelper.addToToday({ days: 21 })),
          requestsWillBeCompiledLabel: 'within 7 days of receipt.',
        },
        sdoR2WitnessesOfFact: {
          sdoStatementOfWitness: 'string',
          sdoWitnessDeadline: 'string',
          sdoWitnessDeadlineDate: formatDate(DateHelper.addToToday({ days: 70 })),
          sdoWitnessDeadlineText: 'string',
          sdoR2RestrictWitness: {
            isRestrictWitness: 'No',
          },
          sdoRestrictPages: {
            isRestrictPages: 'No',
          },
        },
        sdoR2ExpertEvidence: {
          sdoClaimantPermissionToRelyTxt: 'string',
        },
        sdoR2AddendumReport: {
          sdoAddendumReportTxt: 'string',
          sdoAddendumReportDate: formatDate(DateHelper.addToToday({ days: 56 })),
        },
        sdoR2FurtherAudiogram: {
          sdoClaimantShallUndergoTxt: 'string',
          sdoClaimantShallUndergoDate: formatDate(DateHelper.addToToday({ days: 42 })),
          sdoServiceReportTxt: 'string',
          sdoServiceReportDate: formatDate(DateHelper.addToToday({ days: 98 })),
        },
        sdoR2QuestionsClaimantExpert: {
          sdoDefendantMayAskTxt: 'string',
          sdoDefendantMayAskDate: formatDate(DateHelper.addToToday({ days: 126 })),
          sdoQuestionsShallBeAnsweredTxt: 'string',
          sdoQuestionsShallBeAnsweredDate: formatDate(DateHelper.addToToday({ days: 147 })),
          sdoUploadedToDigitalPortalTxt: 'string',
          sdoApplicationToRelyOnFurther: {
            doRequireApplicationToRely: 'No',
          },
        },
        sdoR2PermissionToRelyOnExpert: {
          sdoPermissionToRelyOnExpertTxt: 'string',
          sdoPermissionToRelyOnExpertDate: formatDate(DateHelper.addToToday({ days: 119 })),
          sdoJointMeetingOfExpertsTxt: 'string',
          sdoJointMeetingOfExpertsDate: formatDate(DateHelper.addToToday({ days: 147 })),
          sdoUploadedToDigitalPortalTxt: 'string',
        },
        sdoR2EvidenceAcousticEngineer: {
          sdoEvidenceAcousticEngineerTxt: 'string',
          sdoInstructionOfTheExpertTxt: 'string',
          sdoInstructionOfTheExpertDate: formatDate(DateHelper.addToToday({ days: 42 })),
          sdoInstructionOfTheExpertTxtArea: 'string',
          sdoExpertReportTxt: 'string',
          sdoExpertReportDate: formatDate(DateHelper.addToToday({ days: 280 })),
          sdoExpertReportDigitalPortalTxt: 'string',
          sdoWrittenQuestionsTxt: 'string',
          sdoWrittenQuestionsDate: formatDate(DateHelper.addToToday({ days: 294 })),
          sdoWrittenQuestionsDigitalPortalTxt: 'string',
          sdoRepliesTxt: 'string',
          sdoRepliesDate: formatDate(DateHelper.addToToday({ days: 315 })),
          sdoRepliesDigitalPortalTxt: 'string',
          sdoServiceOfOrderTxt: 'string',
        },
        sdoR2QuestionsToEntExpert: {
          sdoWrittenQuestionsTxt: 'string',
          sdoWrittenQuestionsDate: formatDate(DateHelper.addToToday({ days: 336 })),
          sdoWrittenQuestionsDigPortalTxt: 'string',
          sdoQuestionsShallBeAnsweredTxt: 'string',
          sdoQuestionsShallBeAnsweredDate: formatDate(DateHelper.addToToday({ days: 350 })),
          sdoShallBeUploadedTxt: 'string',
        },
        sdoR2ScheduleOfLoss: {
          sdoR2ScheduleOfLossClaimantText: 'string',
          sdoR2ScheduleOfLossClaimantDate: formatDate(DateHelper.addToToday({ days: 84 })),
          sdoR2ScheduleOfLossDefendantText: 'string',
          sdoR2ScheduleOfLossDefendantDate: formatDate(DateHelper.addToToday({ days: 98 })),
          isClaimForPecuniaryLoss: 'No',
        },
        sdoR2UploadOfDocuments: {
          sdoUploadOfDocumentsTxt: 'string',
        },
        sdoR2Trial: {
          trialOnOptions: 'OPEN_DATE',
          lengthList: 'FIVE_HOURS',
          hearingCourtLocationList: {
            list_items: [claimantDefaultCourt],
            value: claimantDefaultCourt,
          },
          methodOfHearing: {
            list_items: [hearingMethodInPerson],
            value: hearingMethodInPerson,
          },
          physicalBundleOptions: 'PARTY',
          physicalBundlePartyTxt: 'string',
          sdoR2TrialFirstOpenDateAfter: {
            listFrom: formatDate(DateHelper.addToToday({ days: 434 })),
          },
          hearingNotesTxt: 'string',
        },
        sdoR2NihlUseOfWelshLanguage: {
          description: 'string',
        },
        sdoR2ImportantNotesTxt: 'string',
        sdoR2ImportantNotesDate: formatDate(DateHelper.addToToday({ days: 7 })),
      },
    };
  }

  return {};
};

const sdoR2SmallClaims = (sdoType: SdoType) => {
  const claimantDefaultCourt = CaseDataHelper.setCodeToData(
    preferredCourts[partys.CLAIMANT_1.key].default,
  );
  const hearingMethodInPerson = CaseDataHelper.setCodeToData('In Person');

  if (sdoType === SdoType.SMALL_TRACK_SUM_DRH || sdoType === SdoType.SMALL_TRACK_NO_SUM_DRH) {
    return {
      SdoR2SmallClaims: {
        sdoR2SmallClaimsJudgesRecital: {
          input: 'string',
        },
        sdoR2SmallClaimsPPI: {
          ppiDate: formatDate(DateHelper.addToToday({ days: 21 })),
          text: 'string',
        },
        sdoR2SmallClaimsWitnessStatements: {
          sdoStatementOfWitness: 'string',
          deadlineDate: formatDate(DateHelper.addToToday({ days: 149 })),
          isRestrictWitness: 'No',
          isRestrictPages: 'No',
          text: 'string',
        },
        sdoR2SmallClaimsUploadDoc: {
          sdoUploadOfDocumentsTxt: 'string',
        },
        sdoR2SmallClaimsHearing: {
          trialOnOptions: 'OPEN_DATE',
          lengthList: 'THIRTY_MINUTES',
          hearingCourtLocationList: {
            list_items: [claimantDefaultCourt],
            value: claimantDefaultCourt,
          },
          methodOfHearing: {
            list_items: [hearingMethodInPerson],
            value: hearingMethodInPerson,
          },
          physicalBundleOptions: 'PARTY',
          hearingNotesTxt: 'string',
          sdoR2SmallClaimsHearingFirstOpenDateAfter: {
            listFrom: formatDate(DateHelper.addToToday({ days: 56 })),
          },
          sdoR2SmallClaimsBundleOfDocs: {
            physicalBundlePartyTxt: 'string',
          },
        },
        sdoR2DrhUseOfWelshLanguage: {
          description: 'string',
        },
        sdoR2SmallClaimsImpNotes: {
          text: 'string',
          date: formatDate(DateHelper.addToToday({ days: 7 })),
        },
      },
    };
  }

  return {};
};

const orderPreview = {
  OrderPreview: {},
};

const createSdoDataBuilderComponents = {
  sdo,
  claimsTrack,
  orderType,
  fastTrack,
  sdoR2FastTrack,
  sdoR2SmallClaims,
  smallClaims,
  orderPreview,
};

export default createSdoDataBuilderComponents;
