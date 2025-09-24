const {date, listElement} = require('../../api/dataHelper');
const config = require('../../config');
module.exports = {
  scheduleHearing: (allocatedTrack, isMinti) => {
    return {
      userInput: {
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
          ...(!isMinti? {
            hearingDuration: 'MINUTES_55'
          } : {}),
          ...(isMinti? {
            hearingDurationMinti: 'custom hearing duration'
          } : {}),
        },
        HearingInformation: {
          information: 'string'
        }
      }
    };
  }
};
