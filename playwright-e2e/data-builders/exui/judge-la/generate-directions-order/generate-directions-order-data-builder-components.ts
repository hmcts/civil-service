import preferredCourts from '../../../../config/preferred-courts';
import ClaimTrack from '../../../../constants/cases/claim-track';
import OrderType from '../../../../constants/ccd-events/generate-directions-order/order-type';
import MultiIntermediateTemplateTypes from '../../../../constants/ccd-events/generate-directions-order/multi-intermediate-template-types';
import partys from '../../../../constants/users/partys';
import CaseDataHelper from '../../../../helpers/case-data-helper';
import DateHelper from '../../../../helpers/date-helper';
import { ClaimantDefendantPartyType } from '../../../../models/users/claimant-defendant-party-types';
import { UploadDocumentValue } from '../../../../models/ccd-case-data';

const formatDate = (date: Date) =>
  DateHelper.formatDateToString(date, { outputFormat: 'YYYY-MM-DD' });

const finalOrderSelect = (claimTrack: ClaimTrack, orderType: OrderType) => {
  if (claimTrack === ClaimTrack.FAST_CLAIM || claimTrack === ClaimTrack.SMALL_CLAIM) {
    return {
      FinalOrderSelect: {
        finalOrderSelection: orderType,
      },
    };
  }

  return {};
};

const finalOrderAssistedOrder = (
  claimTrack: ClaimTrack,
  orderType: OrderType,
  claimantPartyType: ClaimantDefendantPartyType,
  defendantPartyType: ClaimantDefendantPartyType,
) => {
  if (
    (claimTrack === ClaimTrack.FAST_CLAIM || claimTrack === ClaimTrack.SMALL_CLAIM) &&
    orderType === OrderType.ASSISTED_ORDER
  ) {
    const claimantName = CaseDataHelper.buildClaimantAndDefendantData(
      partys.CLAIMANT_1,
      claimantPartyType,
    ).partyName;
    const defendantName = CaseDataHelper.buildClaimantAndDefendantData(
      partys.DEFENDANT_1,
      defendantPartyType,
    ).partyName;
    const preferredCourt = preferredCourts[partys.CLAIMANT_1.key].default;
    const alternativeHearingLocation = CaseDataHelper.setCodeToData(preferredCourt);

    return {
      FinalOrderAssistedOrder: {
        assistedOrderPenalNoticeContent:
          'WARNING\n\n[DEFENDANT] IF YOU DO NOT COMPLY WITH THIS ORDER YOU MAY BE HELD IN CONTEMPT OF COURT AND PUNISHED BY A FINE, IMPRISONMENT, CONFISCATION OF ASSETS OR OTHER PUNISHMENT UNDER THE LAW.\n\nA penal notice against the [DEFENDANT] is attached to paragraph X below.',
        finalOrderMadeSelection: 'Yes',
        finalOrderDateHeardComplex: {
          finalOrderMadeRadioList: 'SINGLE_DATE',
          singleDateSelection: {
            singleDate: formatDate(DateHelper.getToday()),
          },
        },
        finalOrderJudgePapers: ['CONSIDERED'],
        finalOrderRepresentation: {
          typeRepresentationList: 'CLAIMANT_AND_DEFENDANT',
          typeRepresentationComplex: {
            typeRepresentationClaimantOneDynamic: claimantName,
            typeRepresentationClaimantTwoDynamic: null,
            typeRepresentationDefendantOneDynamic: defendantName,
            typeRepresentationDefendantTwoDynamic: null,
            typeRepresentationClaimantList: 'COUNSEL_FOR_CLAIMANT',
            typeRepresentationClaimantListTwo: null,
            typeRepresentationDefendantList: 'COUNSEL_FOR_DEFENDANT',
            typeRepresentationDefendantTwoList: null,
            detailsRepresentationText: null,
          },
        },
        finalOrderRecitalsRecorded: {
          text: 'The court records that..',
        },
        finalOrderOrderedThatText: 'The court orders that...',
        finalOrderFurtherHearingComplex: {
          listFromDate: formatDate(DateHelper.addToToday({ months: 4 })),
          dateToDate: formatDate(DateHelper.addToToday({ months: 6 })),
          lengthList: 'MINUTES_30',
          datesToAvoidYesNo: 'Yes',
          hearingLocationList: {
            value: {
              code: 'OTHER_LOCATION',
              label: 'Other location',
            },
            list_items: [
              {
                code: 'LOCATION_LIST',
                label: 'Central London County Court',
              },
              {
                code: 'OTHER_LOCATION',
                label: 'Other location',
              },
            ],
          },
          alternativeHearingList: {
            value: alternativeHearingLocation,
            list_items: [alternativeHearingLocation],
          },
          hearingMethodList: 'IN_PERSON',
          hearingNotesText: 'Hearing notes',
          datesToAvoidDateDropdown: {
            datesToAvoidDates: formatDate(DateHelper.addToToday({ days: 7 })),
          },
        },
        assistedOrderCostList: 'COSTS_IN_THE_CASE',
        publicFundingCostsProtection: 'No',
        finalOrderAppealComplex: {
          list: 'CLAIMANT',
          applicationList: 'GRANTED',
          appealGrantedDropdown: {
            circuitOrHighCourtList: 'CIRCUIT_COURT',
            appealChoiceSecondDropdownA: {
              appealGrantedRefusedDate: formatDate(DateHelper.addToToday({ days: 14 })),
            },
          },
        },
        orderMadeOnDetailsList: 'COURTS_INITIATIVE',
        orderMadeOnDetailsOrderCourt: {
          ownInitiativeText:
            "As this order was made on the court's own initiative any party affected by the order may apply to set aside, vary or stay the order. Any such application must be made by 4pm on",
          ownInitiativeDate: formatDate(DateHelper.addToToday({ days: 7 })),
        },
        finalOrderGiveReasonsYesNo: 'Yes',
        finalOrderGiveReasonsComplex: {
          reasonsText: 'Breif reasons',
        },
      },
    };
  }

  return {};
};

const freeFormOrder = (claimTrack: ClaimTrack, orderType: OrderType) => {
  if (
    (claimTrack === ClaimTrack.FAST_CLAIM || claimTrack === ClaimTrack.SMALL_CLAIM) &&
    orderType === OrderType.FREE_FORM_ORDER
  ) {
    return {
      FreeFormOrder: {
        freeFormHearingNotes: 'string',
        freeFormOrderedTextArea: 'string',
        freeFormRecordedTextArea: 'string',
        orderOnCourtsList: 'ORDER_ON_COURT_INITIATIVE',
        orderOnCourtInitiative: {
          onInitiativeSelectionDate: formatDate(DateHelper.addToToday({ days: 7 })),
          onInitiativeSelectionTextArea: 'string',
        },
      },
    };
  }

  return {};
};

const finalOrderPreview = (claimTrack: ClaimTrack) => {
  if (claimTrack === ClaimTrack.FAST_CLAIM || claimTrack === ClaimTrack.SMALL_CLAIM) {
    return {
      FinalOrderPreview: {},
    };
  }

  return {};
};

const trackAllocation = (claimTrack: ClaimTrack) => {
  if (claimTrack === ClaimTrack.INTERMEDIATE_CLAIM || claimTrack === ClaimTrack.MULTI_CLAIM) {
    return {
      TrackAllocation: {
        finalOrderAllocateToTrack: 'Yes',
        finalOrderTrackAllocation: claimTrack,
      },
    };
  }

  return {};
};

const intermediateTrackComplexityBand = (claimTrack: ClaimTrack) => {
  if (claimTrack === ClaimTrack.INTERMEDIATE_CLAIM) {
    return {
      IntermediateTrackComplexityBand: {
        finalOrderIntermediateTrackComplexityBand: {
          assignComplexityBand: 'Yes',
          band: 'BAND_1',
          reasons: 'Reason for complexity band',
        },
      },
    };
  }
  return {};
};

const selectTemplate = (claimTrack: ClaimTrack) => {
  if (claimTrack === ClaimTrack.INTERMEDIATE_CLAIM || claimTrack === ClaimTrack.MULTI_CLAIM) {
    return {
      SelectTemplate: {
        finalOrderDownloadTemplateOptions: {
          list_items: [
            CaseDataHelper.setCodeToData(MultiIntermediateTemplateTypes.TEMPLATE_AFTER_HEARING),
          ],
          value: CaseDataHelper.setCodeToData(
            MultiIntermediateTemplateTypes.TEMPLATE_AFTER_HEARING,
          ),
        },
      },
    };
  }

  return {};
};

const orderAfterHearingDate = (claimTrack: ClaimTrack) => {
  if (claimTrack === ClaimTrack.INTERMEDIATE_CLAIM || claimTrack === ClaimTrack.MULTI_CLAIM) {
    return {
      OrderAfterHearingDate: {
        orderAfterHearingDate: {
          dateType: 'SINGLE_DATE',
          date: formatDate(DateHelper.subtractFromToday({ days: 7 })),
        },
      },
    };
  }

  return {};
};

const downloadTemplate = (claimTrack: ClaimTrack) => {
  if (claimTrack === ClaimTrack.INTERMEDIATE_CLAIM || claimTrack === ClaimTrack.MULTI_CLAIM) {
    return {};
  }

  return {};
};

const uploadOrder = (claimTrack: ClaimTrack, templateDocument: UploadDocumentValue) => {
  if (claimTrack === ClaimTrack.INTERMEDIATE_CLAIM || claimTrack === ClaimTrack.MULTI_CLAIM) {
    return {
      UploadOrder: {
        uploadOrderDocumentFromTemplate: templateDocument,
      },
    };
  }

  return {};
};

export default {
  finalOrderSelect,
  finalOrderAssistedOrder,
  freeFormOrder,
  finalOrderPreview,
  trackAllocation,
  intermediateTrackComplexityBand,
  selectTemplate,
  orderAfterHearingDate,
  downloadTemplate,
  uploadOrder,
};
