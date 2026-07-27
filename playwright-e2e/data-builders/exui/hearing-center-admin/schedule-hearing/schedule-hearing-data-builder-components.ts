import preferredCourts from '../../../../config/preferred-courts';
import partys from '../../../../constants/users/partys';
import DateHelper from '../../../../helpers/date-helper';
import CaseDataHelper from '../../../../helpers/case-data-helper';
import ClaimTrack from '../../../../constants/cases/claim-track';

const selectedHearingLocation = CaseDataHelper.setCodeToData(
  preferredCourts[partys.CLAIMANT_1.key].default,
);

const hearingNoticeSelect = (claimTrack: ClaimTrack) => {
  if (claimTrack === ClaimTrack.FAST_CLAIM)
    return {
      HearingNoticeSelect: {
        hearingNoticeList: 'FAST_TRACK_TRIAL',
      },
    };
  
  else if (claimTrack === ClaimTrack.SMALL_CLAIM)
    return {
      HearingNoticeSelect: {
        hearingNoticeList: 'SMALL_CLAIMS',
      },
    };
  
  return {};
};

const listingOrRelisting = {
  ListingOrRelisting: {
    listingOrRelisting: 'LISTING',
  },
};

const hearingDetails = {
  HearingDetails: {
    hearingLocation: {
      list_items: [selectedHearingLocation],
      value: selectedHearingLocation,
    },
    channel: 'IN_PERSON',
    hearingDate: DateHelper.formatDateToString(DateHelper.addToToday({ days: 60 }), {
      outputFormat: 'YYYY-MM-DD',
    }),
    hearingTimeHourMinute: '1015',
    hearingDuration: 'MINUTES_55',
  },
};

const hearingInformation = {
  HearingInformation: {
    information: 'string',
  },
};

export default {
  hearingNoticeSelect,
  listingOrRelisting,
  hearingDetails,
  hearingInformation,
};
