import { z } from 'zod';
import SdoType from '../../../../constants/ccd-events/sdo/sdo-type';

const sdo = (sdoType: SdoType) => {
  if (
    sdoType === SdoType.TRAIL ||
    sdoType === SdoType.SMALL_TRACK_SUM ||
    sdoType === SdoType.TRAIL_NIHL
  ) {
    return {
      drawDirectionsOrderRequired: z.string(),
      drawDirectionsOrder: z.looseObject({
        judgementSum: z.string(),
      }),
    };
  }

  return {
    drawDirectionsOrderRequired: z.string(),
  };
};

const claimsTrack = (sdoType: SdoType) => {
  if (sdoType === SdoType.FAST_TRACK) {
    return {
      claimsTrack: z.string(),
      fastClaims: z.array(z.string()),
    };
  }

  if (sdoType === SdoType.FAST_TRACK_NIHL) {
    return {
      claimsTrack: z.string(),
      fastClaims: z.array(z.string()),
    };
  }

  if (sdoType === SdoType.TRAIL || sdoType === SdoType.TRAIL_NIHL) {
    return {
      drawDirectionsOrderSmallClaims: z.string(),
    };
  }

  if (sdoType === SdoType.SMALL_TRACK_SUM || sdoType === SdoType.SMALL_TRACK_SUM_DRH) {
    return {
      drawDirectionsOrderSmallClaims: z.string(),
      drawDirectionsOrderSmallClaimsAdditionalDirections: z.array(z.string()),
    };
  }

  if (sdoType === SdoType.SMALL_TRACK_NO_SUM || sdoType === SdoType.SMALL_TRACK_NO_SUM_DRH) {
    return {
      claimsTrack: z.string(),
      smallClaims: z.array(z.string()),
    };
  }

  return {};
};

const orderType = (sdoType: SdoType) => {
  if (sdoType === SdoType.TRAIL || sdoType === SdoType.TRAIL_NIHL) {
    return {
      orderType: z.string(),
      trialAdditionalDirectionsForFastTrack: z.array(z.string()),
    };
  }

  if (sdoType === SdoType.DISPOSAL_HEARING) {
    return {
      orderType: z.string(),
    };
  }

  return {};
};

const fastTrack = (sdoType: SdoType) => {
  if (sdoType === SdoType.FAST_TRACK || sdoType === SdoType.TRAIL) {
    return {
      fastTrackJudgesRecital: z.looseObject({
        input: z.string(),
      }),
      fastTrackAllocation: z.looseObject({
        assignComplexityBand: z.string(),
        band: z.string(),
        reasons: z.string(),
      }),
      fastTrackDisclosureOfDocuments: z.looseObject({
        input1: z.string(),
        date1: z.string(),
        input2: z.string(),
        date2: z.string(),
        input3: z.string(),
      }),
      sdoR2FastTrackWitnessOfFact: z.looseObject({
        sdoStatementOfWitness: z.string(),
        sdoWitnessDeadline: z.string(),
        sdoWitnessDeadlineDate: z.string(),
        sdoWitnessDeadlineText: z.string(),
        sdoR2RestrictWitness: z.looseObject({
          isRestrictWitness: z.string(),
        }),
        sdoRestrictPages: z.looseObject({
          isRestrictPages: z.string(),
          restrictNoOfPagesDetails: z.looseObject({
            noOfPages: z.number(),
            fontDetails: z.string(),
            witnessShouldNotMoreThanTxt: z.string(),
          }),
        }),
      }),
      fastTrackSchedulesOfLoss: z.looseObject({
        input1: z.string(),
        date1: z.string(),
        input2: z.string(),
        date2: z.string(),
        input3: z.string(),
      }),
      fastTrackTrial: z.looseObject({
        input1: z.string(),
        date1: z.string(),
        date2: z.string(),
        input2: z.string(),
        input3: z.string(),
        type: z.array(z.string()),
      }),
      fastTrackMethod: z.string(),
      fastTrackMethodTelephoneHearing: z.string().optional(),
      fastTrackBuildingDispute: z.looseObject({
        input1: z.string(),
        input2: z.string(),
        input3: z.string(),
        date1: z.string(),
        input4: z.string(),
        date2: z.string(),
      }),
      fastTrackClinicalNegligence: z.looseObject({
        input1: z.string(),
        input2: z.string(),
        input3: z.string(),
        input4: z.string(),
      }),
      fastTrackCreditHire: z.looseObject({
        input1: z.string(),
        input2: z.string(),
        date1: z.string(),
        input3: z.string(),
        input4: z.string(),
        date2: z.string(),
        input5: z.string(),
        input6: z.string(),
        date3: z.string(),
        input7: z.string(),
        date4: z.string(),
        input8: z.string(),
      }),
      // fastTrackHousingDisrepair: z.looseObject({
      //   input1: z.string(),
      //   input2: z.string(),
      //   input3: z.string(),
      //   date1: z.string(),
      //   input4: z.string(),
      //   date2: z.string(),
      // }),
      fastTrackPersonalInjury: z.looseObject({
        input1: z.string(),
        date1: z.string(),
        input2: z.string(),
        date2: z.string(),
        input3: z.string(),
      }),
      fastTrackRoadTrafficAccident: z.looseObject({
        input: z.string(),
        date: z.string(),
      }),
      fastTrackAddNewDirections: z.array(
        z.looseObject({
          id: z.string(),
          value: z.looseObject({
            directionComment: z.string(),
          }),
        }),
      ),
      fastTrackNotes: z.looseObject({
        input: z.string(),
        date: z.string(),
      }),
      fastTrackHearingNotes: z.looseObject({
        input: z.string(),
      }),
      sdoR2FastTrackUseOfWelshLanguage: z.strictObject({
        description: z.string(),
      }),
    };
  }

  return {};
};

const smallClaims = (sdoType: SdoType) => {
  if (sdoType === SdoType.SMALL_TRACK_SUM || sdoType === SdoType.SMALL_TRACK_NO_SUM) {
    return {
      smallClaimsPenalNotice: z.string(),
      smallClaimsCreditHire: z.looseObject({
        input1: z.string(),
        input2: z.string(),
        input3: z.string(),
        input4: z.string(),
        input5: z.string(),
        input6: z.string(),
        input7: z.string(),
        input11: z.string(),
        date2: z.string(),
        date3: z.string(),
        date4: z.string(),
      }),
      sdoR2SmallClaimsUseOfWelshLanguage: z.looseObject({
        description: z.string(),
      }),
      sdoR2SmallClaimsWitnessStatementOther: z.looseObject({
        sdoStatementOfWitness: z.string(),
        isRestrictWitness: z.string(),
        isRestrictPages: z.string(),
        text: z.string(),
        deadlineDate: z.string(),
      }),
      smallClaimsDocuments: z.looseObject({
        input1: z.string(),
        input2: z.string(),
        deadlineDate: z.string(),
      }),
      smallClaimsFlightDelay: z.looseObject({
        relatedClaimsInput: z.string(),
        legalDocumentsInput: z.string(),
      }),
      smallClaimsHearing: z.looseObject({
        input1: z.string(),
        time: z.string(),
        input2: z.string(),
        dateFrom: z.string(),
      }),
      smallClaimsHousingDisrepair: z.looseObject({
        clauseA: z.string(),
        clauseB: z.string(),
        firstReportDateBy: z.string(),
        clauseCBeforeDate: z.string(),
        jointStatementDateBy: z.string(),
        clauseCAfterDate: z.string(),
        clauseD: z.string(),
        clauseE: z.string(),
      }),
      sdoHearingNotes: z.strictObject({ input: z.string() }),
      hearingMethodValuesSmallClaims: z.looseObject({}),
      smallClaimsJudgementDeductionValue: z.looseObject({
        value: z.string(),
      }),
      smallClaimsJudgesRecital: z.looseObject({
        input: z.string(),
      }),
      smallClaimsMethod: z.string(),
      smallClaimsMethodInPerson: z.looseObject({
        value: z.looseObject({
          code: z.string(),
          label: z.string(),
        }),
      }),
      smallClaimsNotes: z.looseObject({
        input: z.string(),
        date: z.string(),
      }),
      smallClaimsPPI: z.looseObject({
        ppiDate: z.string(),
        text: z.string(),
      }),
      smallClaimsRoadTrafficAccident: z.looseObject({
        input: z.string(),
      }),
      smallClaimsAddNewDirections: z.array(
        z.looseObject({
          id: z.string(),
          value: z.looseObject({
            directionComment: z.string(),
          }),
        }),
      ),
    };
  }

  return {};
};

const sdoR2FastTrack = (sdoType: SdoType) => {
  if (sdoType === SdoType.FAST_TRACK_NIHL || sdoType === SdoType.TRAIL_NIHL) {
    return {
      sdoFastTrackJudgesRecital: z.looseObject({
        input: z.string(),
      }),
      sdoR2DisclosureOfDocuments: z.looseObject({
        standardDisclosureTxt: z.string(),
        standardDisclosureDate: z.string(),
        inspectionTxt: z.string(),
        inspectionDate: z.string(),
        requestsWillBeCompiledLabel: z.string(),
      }),
      sdoR2WitnessesOfFact: z.looseObject({
        sdoStatementOfWitness: z.string(),
        sdoWitnessDeadline: z.string(),
        sdoWitnessDeadlineDate: z.string(),
        sdoWitnessDeadlineText: z.string(),
        sdoR2RestrictWitness: z.looseObject({
          isRestrictWitness: z.string(),
        }),
        sdoRestrictPages: z.looseObject({
          isRestrictPages: z.string(),
        }),
      }),
      sdoR2ExpertEvidence: z.looseObject({
        sdoClaimantPermissionToRelyTxt: z.string(),
      }),
      sdoR2AddendumReport: z.looseObject({
        sdoAddendumReportTxt: z.string(),
        sdoAddendumReportDate: z.string(),
      }),
      sdoR2FurtherAudiogram: z.looseObject({
        sdoClaimantShallUndergoTxt: z.string(),
        sdoClaimantShallUndergoDate: z.string(),
        sdoServiceReportTxt: z.string(),
        sdoServiceReportDate: z.string(),
      }),
      sdoR2QuestionsClaimantExpert: z.looseObject({
        sdoDefendantMayAskTxt: z.string(),
        sdoDefendantMayAskDate: z.string(),
        sdoQuestionsShallBeAnsweredTxt: z.string(),
        sdoQuestionsShallBeAnsweredDate: z.string(),
        sdoUploadedToDigitalPortalTxt: z.string(),
        sdoApplicationToRelyOnFurther: z.looseObject({
          doRequireApplicationToRely: z.string(),
        }),
      }),
      sdoR2PermissionToRelyOnExpert: z.looseObject({
        sdoPermissionToRelyOnExpertTxt: z.string(),
        sdoPermissionToRelyOnExpertDate: z.string(),
        sdoJointMeetingOfExpertsTxt: z.string(),
        sdoJointMeetingOfExpertsDate: z.string(),
        sdoUploadedToDigitalPortalTxt: z.string(),
      }),
      sdoR2EvidenceAcousticEngineer: z.looseObject({
        sdoEvidenceAcousticEngineerTxt: z.string(),
        sdoInstructionOfTheExpertTxt: z.string(),
        sdoInstructionOfTheExpertDate: z.string(),
        sdoInstructionOfTheExpertTxtArea: z.string(),
        sdoExpertReportTxt: z.string(),
        sdoExpertReportDate: z.string(),
        sdoExpertReportDigitalPortalTxt: z.string(),
        sdoWrittenQuestionsTxt: z.string(),
        sdoWrittenQuestionsDate: z.string(),
        sdoWrittenQuestionsDigitalPortalTxt: z.string(),
        sdoRepliesTxt: z.string(),
        sdoRepliesDate: z.string(),
        sdoRepliesDigitalPortalTxt: z.string(),
        sdoServiceOfOrderTxt: z.string(),
      }),
      sdoR2QuestionsToEntExpert: z.looseObject({
        sdoWrittenQuestionsTxt: z.string(),
        sdoWrittenQuestionsDate: z.string(),
        sdoWrittenQuestionsDigPortalTxt: z.string(),
        sdoQuestionsShallBeAnsweredTxt: z.string(),
        sdoQuestionsShallBeAnsweredDate: z.string(),
        sdoShallBeUploadedTxt: z.string(),
      }),
      sdoR2ScheduleOfLoss: z.looseObject({
        sdoR2ScheduleOfLossClaimantText: z.string(),
        sdoR2ScheduleOfLossClaimantDate: z.string(),
        sdoR2ScheduleOfLossDefendantText: z.string(),
        sdoR2ScheduleOfLossDefendantDate: z.string(),
        isClaimForPecuniaryLoss: z.string(),
      }),
      sdoR2UploadOfDocuments: z.looseObject({
        sdoUploadOfDocumentsTxt: z.string(),
      }),
      sdoR2Trial: z.looseObject({
        trialOnOptions: z.string(),
        lengthList: z.string(),
        hearingCourtLocationList: z.looseObject({
          value: z.looseObject({}),
        }),
        methodOfHearing: z.looseObject({
          list_items: z.array(z.looseObject({})),
          value: z.looseObject({}),
        }),
        physicalBundleOptions: z.string(),
        physicalBundlePartyTxt: z.string(),
        sdoR2TrialFirstOpenDateAfter: z.looseObject({
          listFrom: z.string(),
        }),
        hearingNotesTxt: z.string(),
      }),
      sdoR2NihlUseOfWelshLanguage: z.looseObject({
        description: z.string(),
      }),
      sdoR2ImportantNotesTxt: z.string(),
      sdoR2ImportantNotesDate: z.string(),
    };
  }

  return {};
};

const sdoR2SmallClaims = (sdoType: SdoType) => {
  if (sdoType === SdoType.SMALL_TRACK_SUM_DRH || sdoType === SdoType.SMALL_TRACK_NO_SUM_DRH) {
    return {
      sdoR2SmallClaimsJudgesRecital: z.looseObject({
        input: z.string(),
      }),
      sdoR2SmallClaimsPPI: z.looseObject({
        ppiDate: z.string(),
        text: z.string(),
      }),
      sdoR2SmallClaimsWitnessStatements: z.looseObject({
        sdoStatementOfWitness: z.string(),
        deadlineDate: z.string(),
        isRestrictWitness: z.string(),
        isRestrictPages: z.string(),
        text: z.string(),
      }),
      sdoR2SmallClaimsUploadDoc: z.looseObject({
        sdoUploadOfDocumentsTxt: z.string(),
      }),
      sdoR2SmallClaimsHearing: z.looseObject({
        trialOnOptions: z.string(),
        lengthList: z.string(),
        hearingCourtLocationList: z.looseObject({
          list_items: z.array(z.looseObject({})),
          value: z.looseObject({}),
        }),
        methodOfHearing: z.looseObject({
          list_items: z.array(z.looseObject({})),
          value: z.looseObject({}),
        }),
        physicalBundleOptions: z.string(),
        hearingNotesTxt: z.string(),
        sdoR2SmallClaimsHearingFirstOpenDateAfter: z.looseObject({
          listFrom: z.string(),
        }),
        sdoR2SmallClaimsBundleOfDocs: z.looseObject({
          physicalBundlePartyTxt: z.string(),
        }),
      }),
      sdoR2DrhUseOfWelshLanguage: z.looseObject({
        description: z.string(),
      }),
      sdoR2SmallClaimsImpNotes: z.looseObject({
        text: z.string(),
        date: z.string(),
      }),
    };
  }

  return {};
};

const orderPreview = {};

export default {
  sdo,
  claimsTrack,
  orderType,
  fastTrack,
  sdoR2FastTrack,
  sdoR2SmallClaims,
  smallClaims,
  orderPreview,
};
