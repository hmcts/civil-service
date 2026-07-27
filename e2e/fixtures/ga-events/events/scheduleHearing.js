const {date, listElement} = require('../../../api/dataHelper');
const config = require('../../../config');
module.exports = {
  scheduleHearing: (allocatedTrack) => {
    return {
      valid: {
        HearingNoticeSelect: {
          hearingNoticeList: allocatedTrack,
          hearingNoticeListOther: allocatedTrack == 'OTHER' ? 'Text' : ' '
        },
        ListingOrRelisting: {
          listingOrRelisting: 'LISTING'
        },
        HearingDetails: {
          hearingLocation: {
            list_items: [
              listElement(config.claimantSelectedCourt)
            ],
            value: listElement(config.claimantSelectedCourt)
          },
          channel: 'IN_PERSON',
          hearingDate: date(60),
          hearingTimeHourMinute: '1015',
          hearingDuration: 'MINUTES_55'
        },
        HearingInformation: {
          information: 'string'
        }
      }
    };
  }
};
