const {date, listElement} = require('../../../api/dataHelper');
const config = require('../../../config');
module.exports = {
    scheduleHearing: () => {
        return {
            gaHearingNoticeApplication: {
                hearingNoticeApplicationDetail: 'DEFENDANT',
                hearingNoticeApplicationType: 'test',
                hearingNoticeApplicationDate:  date(-1),
            },
            gaHearingNoticeDetail: {
                hearingLocation: {
                    list_items: [
                        listElement(config.defendantSelectedCourt)
                    ],
                    value: listElement(config.defendantSelectedCourt)
                },
                hearingDate: date(1),
                hearingTimeHourMinute: '0805',
                channel: 'TELEPHONE',
                hearingDuration: 'MINUTES_25',
            },
            gaHearingNoticeInformation: 'info'
        };
    },
};
