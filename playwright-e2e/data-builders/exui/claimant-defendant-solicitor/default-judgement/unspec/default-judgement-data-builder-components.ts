import CaseDataHelper from '../../../../../helpers/case-data-helper.ts';
import DateHelper from '../../../../../helpers/date-helper.ts';
import { ClaimantDefendantPartyType } from '../../../../../models/users/claimant-defendant-party-types.ts';
import partys from '../../../../../constants/users/partys.ts';
import DJHearingType from '../../../../../constants/ccd-events/default-judgement/dj-hearing-type.ts';
import preferredCourts from '../../../../../config/preferred-courts.ts';
import ClaimType from '../../../../../constants/cases/claim-type.ts';
import ClaimTypeHelper from '../../../../../helpers/claim-type-helper.ts';

const formatDate = (date: Date) =>
  DateHelper.formatDateToString(date, { outputFormat: 'YYYY-MM-DD' });

const defendantDetails = (claimType: ClaimType, defendant1Party: ClaimantDefendantPartyType) => {
  if (ClaimTypeHelper.isDefendant2(claimType)) {
    return {
      defendantDetails: {
        defendantDetails: {
          list_items: [
            CaseDataHelper.setCodeToData(
              'Both Defendants'
            ),
          ],
          value: CaseDataHelper.setCodeToData(
            'Both Defendants'
          ),
        },
      },
    };
  }

  return {
    defendantDetails: {
      defendantDetails: {
        list_items: [
          CaseDataHelper.setCodeToData(
            CaseDataHelper.buildClaimantAndDefendantData(partys.DEFENDANT_1, defendant1Party)
              .partyName,
          ),
        ],
        value: CaseDataHelper.setCodeToData(
          CaseDataHelper.buildClaimantAndDefendantData(partys.DEFENDANT_1, defendant1Party).partyName,
        ),
      },
    },
  };
};

const showCertifyStatement = () => ({
  showCertifyStatement: {
    CPRAcceptance: {
      acceptance: ['CERTIFIED'],
    },
  },
});

const hearingType = (djHearingType: DJHearingType) => {
  return {
    HearingType: {
      hearingSelection: djHearingType,
      detailsOfDirection: `Details of direction - ${partys.CLAIMANT_1.key}`,
    },
  };
};

const hearingSupportRequirementsFieldDJ = (claimant1PartyType: ClaimantDefendantPartyType) => {
  const preferredCourt = preferredCourts[partys.CLAIMANT_1.key].default;
  const claimant1Data = CaseDataHelper.buildClaimantAndDefendantData(partys.CLAIMANT_1, claimant1PartyType)
  return {
    HearingSupportRequirementsFieldDJ: {
      hearingSupportRequirementsDJ: {
        hearingType: 'IN_PERSON',
        hearingTemporaryLocation: {
          list_items: [CaseDataHelper.setCodeToData(preferredCourt)],
          value: CaseDataHelper.setCodeToData(preferredCourt),
        },
        hearingUnavailableDates: 'Yes',
        hearingDates: [
          {
            value: {
              hearingUnavailableFrom: formatDate(DateHelper.addToToday({ months: 2 })),
              hearingUnavailableUntil: formatDate(DateHelper.addToToday({ months: 2, days: 2 })),
            },
          },
        ],
        hearingSupportQuestion: 'Yes',
        hearingSupportAdditional: `Additional hearing support - ${partys.CLAIMANT_1.key}`,
        hearingPreferredEmail: claimant1Data.partyEmail,
        hearingPreferredTelephoneNumber1: claimant1Data.partyPhone
      },
    },
  };
};

const requestDefaultJudgementBuilderComponents = {
  defendantDetails,
  showCertifyStatement,
  hearingType,
  hearingSupportRequirementsFieldDJ,
};

export default requestDefaultJudgementBuilderComponents;
